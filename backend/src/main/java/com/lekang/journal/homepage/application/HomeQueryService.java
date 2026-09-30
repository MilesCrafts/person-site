package com.lekang.journal.homepage.application;

import com.lekang.journal.article.api.ArticleSummaryResponse;
import com.lekang.journal.article.application.ArticleQueryService;
import com.lekang.journal.homepage.api.HomeResponse;
import com.lekang.journal.homepage.api.HomepageCopyResponse;
import com.lekang.journal.homepage.infrastructure.HomepageCopyEntity;
import com.lekang.journal.homepage.infrastructure.HomepageCopyRepository;
import com.lekang.journal.homepage.infrastructure.HomepageFeatureRepository;
import com.lekang.journal.profile.application.ProfileQueryService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeQueryService {
    private final HomepageFeatureRepository featureRepository;
    private final HomepageCopyRepository copyRepository;
    private final ArticleQueryService articleService;
    private final ProfileQueryService profileService;

    public HomeQueryService(
        HomepageFeatureRepository featureRepository,
        HomepageCopyRepository copyRepository,
        ArticleQueryService articleService,
        ProfileQueryService profileService
    ) {
        this.featureRepository = featureRepository;
        this.copyRepository = copyRepository;
        this.articleService = articleService;
        this.profileService = profileService;
    }

    @Cacheable("home")
    @Transactional(readOnly = true)
    public HomeResponse get() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        List<ArticleSummaryResponse> features = featureRepository.findAllOrdered().stream()
            .map(item -> item.getArticle())
            .filter(article -> article != null
                && "PUBLISHED".equals(article.getStatus())
                && article.getPublishedAt() != null
                && !article.getPublishedAt().isAfter(now))
            .map(articleService::toSummary)
            .toList();
        ArticleSummaryResponse feature = features.isEmpty() ? null : features.get(0);
        HomepageCopyResponse heroCopy = copyRepository.findById((short) 1)
            .map(this::toCopyResponse)
            .orElse(null);
        return new HomeResponse(heroCopy, feature, features, articleService.latest(4), profileService.get());
    }

    private HomepageCopyResponse toCopyResponse(HomepageCopyEntity copy) {
        return new HomepageCopyResponse(
            copy.getEyebrow(),
            copy.getHeadlinePrimary(),
            copy.getHeadlineEmphasis(),
            copy.getHeadlineAccent(),
            copy.getDescription(),
            copy.getPaperLabel(),
            copy.getPaperLineOne(),
            copy.getPaperLineTwo(),
            copy.getPaperLineThree(),
            copy.getPaperFooter()
        );
    }
}
