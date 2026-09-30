package com.lekang.journal.admin.application;

import com.lekang.journal.admin.api.AdminArticleActionRequest;
import com.lekang.journal.admin.api.AdminArticleCreateRequest;
import com.lekang.journal.admin.api.AdminArticleDetailResponse;
import com.lekang.journal.admin.api.AdminArticleSummaryResponse;
import com.lekang.journal.admin.api.AdminArticleUpdateRequest;
import com.lekang.journal.admin.api.MarkdownPreviewResponse;
import com.lekang.journal.article.infrastructure.ArticleEntity;
import com.lekang.journal.article.infrastructure.ArticleRepository;
import com.lekang.journal.common.api.PageResponse;
import com.lekang.journal.common.error.ConflictException;
import com.lekang.journal.common.error.ResourceNotFoundException;
import com.lekang.journal.media.infrastructure.MediaAssetEntity;
import com.lekang.journal.media.infrastructure.MediaAssetRepository;
import com.lekang.journal.taxonomy.infrastructure.CategoryEntity;
import com.lekang.journal.taxonomy.infrastructure.CategoryRepository;
import com.lekang.journal.taxonomy.infrastructure.TopicEntity;
import com.lekang.journal.taxonomy.infrastructure.TopicRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AdminArticleService {
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;
    private final MediaAssetRepository mediaRepository;
    private final MarkdownService markdownService;
    private final AdminAuditService auditService;
    private final CacheManager cacheManager;

    public AdminArticleService(
        ArticleRepository articleRepository,
        CategoryRepository categoryRepository,
        TopicRepository topicRepository,
        MediaAssetRepository mediaRepository,
        MarkdownService markdownService,
        AdminAuditService auditService,
        CacheManager cacheManager
    ) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.topicRepository = topicRepository;
        this.mediaRepository = mediaRepository;
        this.markdownService = markdownService;
        this.auditService = auditService;
        this.cacheManager = cacheManager;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminArticleSummaryResponse> list(
        int page,
        int size,
        String status,
        String query,
        String sort,
        String direction
    ) {
        String normalizedStatus = status == null || status.isBlank() ? "" : normalizeStatus(status);
        String sortProperty = normalizeSort(sort);
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(
            page,
            size,
            Sort.by(sortDirection, sortProperty).and(Sort.by(Sort.Direction.DESC, "id"))
        );
        Page<ArticleEntity> result = articleRepository.searchAdmin(
            normalizedStatus,
            query == null ? "" : query.trim(),
            pageable
        );
        return PageResponse.from(result, result.stream().map(this::toSummary).toList());
    }

    @Transactional(readOnly = true)
    public AdminArticleDetailResponse detail(Long id) {
        return toDetail(requireArticle(id));
    }

    @Transactional
    public AdminArticleDetailResponse create(
        AdminArticleCreateRequest request,
        Authentication authentication
    ) {
        String slug = request.slug().trim();
        if (articleRepository.existsBySlugIgnoreCase(slug)) {
            throw new ConflictException("article slug already exists");
        }
        Taxonomy taxonomy = requireTaxonomy(request.categoryCode(), request.topicCode());
        MediaAssetEntity cover = requireReadyMedia(request.coverAssetId());
        MarkdownPreviewResponse rendered = markdownService.render(request.bodyMarkdown());
        OffsetDateTime now = now();
        ArticleEntity article = new ArticleEntity(
            slug,
            request.title().trim(),
            request.excerpt().trim(),
            request.bodyMarkdown(),
            rendered.bodyHtml(),
            rendered.readMinutes(),
            taxonomy.category(),
            taxonomy.topic(),
            cover,
            now
        );
        articleRepository.saveAndFlush(article);
        auditService.record(authentication.getName(), "ARTICLE_CREATED", "ARTICLE", article.getId().toString());
        evictPublicCachesAfterCommit();
        return toDetail(article);
    }

    @Transactional
    public AdminArticleDetailResponse update(
        Long id,
        AdminArticleUpdateRequest request,
        Authentication authentication
    ) {
        ArticleEntity article = requireArticle(id);
        requireVersion(article, request.version());
        String slug = request.slug().trim();
        if (article.getPublishedAt() != null && !article.getSlug().equals(slug)) {
            throw new ConflictException("a published article slug cannot be changed");
        }
        if (articleRepository.existsBySlugIgnoreCaseAndIdNot(slug, id)) {
            throw new ConflictException("article slug already exists");
        }
        Taxonomy taxonomy = requireTaxonomy(request.categoryCode(), request.topicCode());
        MediaAssetEntity cover = requireReadyMedia(request.coverAssetId());
        MarkdownPreviewResponse rendered = markdownService.render(request.bodyMarkdown());
        article.updateContent(
            slug,
            request.title().trim(),
            request.excerpt().trim(),
            request.bodyMarkdown(),
            rendered.bodyHtml(),
            rendered.readMinutes(),
            taxonomy.category(),
            taxonomy.topic(),
            cover,
            now()
        );
        articleRepository.flush();
        auditService.record(authentication.getName(), "ARTICLE_UPDATED", "ARTICLE", id.toString());
        evictPublicCachesAfterCommit();
        return toDetail(article);
    }

    @Transactional
    public AdminArticleDetailResponse publish(
        Long id,
        AdminArticleActionRequest request,
        Authentication authentication
    ) {
        ArticleEntity article = requireArticle(id);
        requireVersion(article, request.version());
        if ("ARCHIVED".equals(article.getStatus())) {
            throw new ConflictException("an archived article must be restored before publishing");
        }
        if ("PUBLISHED".equals(article.getStatus())) {
            throw new ConflictException("article is already published");
        }
        MarkdownPreviewResponse rendered = markdownService.render(article.getBodyMarkdown());
        article.updateContent(
            article.getSlug(),
            article.getTitle(),
            article.getExcerpt(),
            article.getBodyMarkdown(),
            rendered.bodyHtml(),
            rendered.readMinutes(),
            article.getCategory(),
            article.getTopic(),
            article.getCoverAsset(),
            now()
        );
        article.publish(now());
        articleRepository.flush();
        auditService.record(authentication.getName(), "ARTICLE_PUBLISHED", "ARTICLE", id.toString());
        evictPublicCachesAfterCommit();
        return toDetail(article);
    }

    @Transactional
    public AdminArticleDetailResponse unpublish(
        Long id,
        AdminArticleActionRequest request,
        Authentication authentication
    ) {
        ArticleEntity article = requireArticle(id);
        requireVersion(article, request.version());
        if (!"PUBLISHED".equals(article.getStatus())) {
            throw new ConflictException("only a published article can be unpublished");
        }
        article.unpublish(now());
        articleRepository.flush();
        auditService.record(authentication.getName(), "ARTICLE_UNPUBLISHED", "ARTICLE", id.toString());
        evictPublicCachesAfterCommit();
        return toDetail(article);
    }

    @Transactional
    public AdminArticleDetailResponse archive(
        Long id,
        AdminArticleActionRequest request,
        Authentication authentication
    ) {
        ArticleEntity article = requireArticle(id);
        requireVersion(article, request.version());
        if ("ARCHIVED".equals(article.getStatus())) {
            throw new ConflictException("article is already archived");
        }
        article.archive(now());
        articleRepository.flush();
        auditService.record(authentication.getName(), "ARTICLE_ARCHIVED", "ARTICLE", id.toString());
        evictPublicCachesAfterCommit();
        return toDetail(article);
    }

    public MarkdownPreviewResponse preview(String markdown) {
        return markdownService.render(markdown);
    }

    private ArticleEntity requireArticle(Long id) {
        return articleRepository.findAdminById(id)
            .orElseThrow(() -> new ResourceNotFoundException("article not found"));
    }

    private Taxonomy requireTaxonomy(String categoryCode, String topicCode) {
        String categoryValue = categoryCode.trim().toUpperCase(Locale.ROOT);
        String topicValue = topicCode.trim().toUpperCase(Locale.ROOT);
        CategoryEntity category = categoryRepository.findByCodeIgnoreCaseAndEnabledTrue(categoryValue)
            .orElseThrow(() -> new IllegalArgumentException("unknown or disabled category"));
        TopicEntity topic = topicRepository
            .findByCodeIgnoreCaseAndCategory_CodeIgnoreCaseAndEnabledTrue(topicValue, categoryValue)
            .orElseThrow(() -> new IllegalArgumentException("topic does not belong to category"));
        return new Taxonomy(category, topic);
    }

    private MediaAssetEntity requireReadyMedia(Long mediaId) {
        if (mediaId == null) {
            return null;
        }
        MediaAssetEntity media = mediaRepository.findById(mediaId)
            .orElseThrow(() -> new IllegalArgumentException("cover media does not exist"));
        if (!"READY".equals(media.getStatus())) {
            throw new ConflictException("cover media is not ready");
        }
        return media;
    }

    private static void requireVersion(ArticleEntity article, int requestedVersion) {
        if (article.getVersion() != requestedVersion) {
            throw new ConflictException("article version is stale");
        }
    }

    private static String normalizeStatus(String status) {
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("DRAFT")
            && !normalized.equals("PUBLISHED")
            && !normalized.equals("ARCHIVED")) {
            throw new IllegalArgumentException("unsupported article status");
        }
        return normalized;
    }

    private static String normalizeSort(String sort) {
        return switch (sort == null ? "" : sort.trim()) {
            case "createdAt" -> "createdAt";
            case "title" -> "title";
            default -> "updatedAt";
        };
    }

    private void evictPublicCachesAfterCommit() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            clearCaches();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                clearCaches();
            }
        });
    }

    private void clearCaches() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    private AdminArticleSummaryResponse toSummary(ArticleEntity article) {
        return new AdminArticleSummaryResponse(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getCategory().getCode(),
            article.getTopic().getCode(),
            article.getStatus(),
            article.getPublishedAt(),
            article.getUpdatedAt(),
            article.getVersion()
        );
    }

    private AdminArticleDetailResponse toDetail(ArticleEntity article) {
        return new AdminArticleDetailResponse(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getExcerpt(),
            article.getCategory().getCode(),
            article.getTopic().getCode(),
            article.getBodyMarkdown(),
            article.getBodyHtml(),
            article.getCoverAsset() == null ? null : article.getCoverAsset().getId(),
            article.getCoverAsset() == null ? null : article.getCoverAsset().getExternalUrl(),
            article.getStatus(),
            article.getPublishedAt(),
            article.getReadMinutes(),
            article.getVersion(),
            article.getCreatedAt(),
            article.getUpdatedAt(),
            article.getArchivedAt()
        );
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    private record Taxonomy(CategoryEntity category, TopicEntity topic) {
    }
}
