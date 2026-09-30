package com.lekang.journal.admin.application;

import com.lekang.journal.admin.api.AdminHomepageFeatureResponse;
import com.lekang.journal.admin.api.AdminHomepageFeaturesRequest;
import com.lekang.journal.article.infrastructure.ArticleEntity;
import com.lekang.journal.article.infrastructure.ArticleRepository;
import com.lekang.journal.common.error.ResourceNotFoundException;
import com.lekang.journal.homepage.infrastructure.HomepageFeatureEntity;
import com.lekang.journal.homepage.infrastructure.HomepageFeatureRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AdminHomepageFeatureService {
    private final HomepageFeatureRepository featureRepository;
    private final ArticleRepository articleRepository;
    private final AdminAuditService auditService;
    private final CacheManager cacheManager;

    public AdminHomepageFeatureService(
        HomepageFeatureRepository featureRepository,
        ArticleRepository articleRepository,
        AdminAuditService auditService,
        CacheManager cacheManager
    ) {
        this.featureRepository = featureRepository;
        this.articleRepository = articleRepository;
        this.auditService = auditService;
        this.cacheManager = cacheManager;
    }

    @Transactional(readOnly = true)
    public List<AdminHomepageFeatureResponse> list() {
        return featureRepository.findAllOrdered().stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<AdminHomepageFeatureResponse> replace(
        AdminHomepageFeaturesRequest request,
        Authentication authentication
    ) {
        List<Long> articleIds = request.articleIds();
        if (new HashSet<>(articleIds).size() != articleIds.size()) {
            throw new IllegalArgumentException("featured article ids must be unique");
        }

        Map<Long, ArticleEntity> articlesById = articleRepository.findAllById(articleIds).stream()
            .collect(Collectors.toMap(ArticleEntity::getId, Function.identity()));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<HomepageFeatureEntity> replacements = new ArrayList<>();
        for (int index = 0; index < articleIds.size(); index++) {
            ArticleEntity article = articlesById.get(articleIds.get(index));
            if (article == null) {
                throw new ResourceNotFoundException("featured article not found");
            }
            if (!"PUBLISHED".equals(article.getStatus())
                || article.getPublishedAt() == null
                || article.getPublishedAt().isAfter(now)) {
                throw new IllegalArgumentException("only currently published articles can be featured");
            }
            replacements.add(new HomepageFeatureEntity(article, index, now));
        }

        featureRepository.deleteAllInBatch();
        featureRepository.flush();
        List<HomepageFeatureEntity> saved = featureRepository.saveAll(replacements);
        featureRepository.flush();
        auditService.record(
            authentication.getName(),
            "HOMEPAGE_FEATURES_UPDATED",
            "HOMEPAGE_FEATURE",
            articleIds.stream().map(String::valueOf).collect(Collectors.joining(","))
        );
        evictHomeAfterCommit();
        return saved.stream().map(this::toResponse).toList();
    }

    private AdminHomepageFeatureResponse toResponse(HomepageFeatureEntity item) {
        ArticleEntity article = item.getArticle();
        return new AdminHomepageFeatureResponse(
            article.getId(), article.getSlug(), article.getTitle(), item.getSortOrder()
        );
    }

    private void evictHomeAfterCommit() {
        Runnable clear = () -> {
            var cache = cacheManager.getCache("home");
            if (cache != null) cache.clear();
        };
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            clear.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { clear.run(); }
        });
    }
}
