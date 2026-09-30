package com.lekang.journal.article.application;

import com.lekang.journal.article.api.ArticleDetailResponse;
import com.lekang.journal.article.api.ArticleNavigationResponse;
import com.lekang.journal.article.api.ArticleSummaryResponse;
import com.lekang.journal.article.infrastructure.ArticleEntity;
import com.lekang.journal.article.infrastructure.ArticleRepository;
import com.lekang.journal.article.infrastructure.ArticleSearchRow;
import com.lekang.journal.article.infrastructure.ArticleSummaryRow;
import com.lekang.journal.common.api.PageResponse;
import com.lekang.journal.common.error.ResourceNotFoundException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleQueryService {
    private final ArticleRepository repository;
    private final Clock clock;

    @Autowired
    public ArticleQueryService(ArticleRepository repository) {
        this(repository, Clock.systemUTC());
    }

    ArticleQueryService(ArticleRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<ArticleSummaryResponse> list(
        int page,
        int size,
        String category,
        String topic,
        String query,
        String sort
    ) {
        Sort requestedSort = switch (sort) {
            case "publishedAt,asc" -> Sort.by(Sort.Order.asc("publishedAt"), Sort.Order.asc("id"));
            case "publishedAt,desc" -> Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id"));
            default -> throw new IllegalArgumentException("unsupported sort: " + sort);
        };
        String normalizedQuery = normalizeQuery(query);
        if (normalizedQuery != null) {
            Page<ArticleSearchRow> result = repository.searchPublished(
                normalizedQuery,
                now(),
                PageRequest.of(page, size)
            );
            return PageResponse.from(result, result.stream().map(this::toSummary).toList());
        }

        Page<ArticleSummaryRow> result = repository.findPublished(
            normalizeCode(category),
            normalizeCode(topic),
            "",
            now(),
            PageRequest.of(page, size, requestedSort)
        );
        return PageResponse.from(result, result.stream().map(this::toSummary).toList());
    }

    @Cacheable(cacheNames = "article", key = "#slug")
    @Transactional(readOnly = true)
    public ArticleDetailResponse detail(String slug) {
        return toDetail(requirePublished(slug));
    }

    @Transactional(readOnly = true)
    public ArticleNavigationResponse navigation(String slug) {
        ArticleEntity article = requirePublished(slug);
        var one = PageRequest.of(0, 1);
        var previous = repository.findPrevious(article.getPublishedAt(), article.getId(), now(), one)
            .stream().findFirst().map(this::toNavigation).orElse(null);
        var next = repository.findNext(article.getPublishedAt(), article.getId(), now(), one)
            .stream().findFirst().map(this::toNavigation).orElse(null);
        return new ArticleNavigationResponse(previous, next);
    }

    @Transactional(readOnly = true)
    public List<ArticleSummaryResponse> latest(int size) {
        return repository.findLatest(now(), PageRequest.of(0, size))
            .stream().map(this::toSummary).toList();
    }

    public ArticleSummaryResponse toSummary(ArticleEntity article) {
        return new ArticleSummaryResponse(
            article.getSlug(),
            article.getCategory().getCode(),
            article.getTopic().getDisplayName().toUpperCase(Locale.ROOT),
            article.getTitle(),
            article.getExcerpt(),
            article.getPublishedAt(),
            article.getReadMinutes(),
            article.getCoverAsset() == null ? null : article.getCoverAsset().getExternalUrl(),
            article.getCoverAsset() == null ? null : article.getCoverAsset().getAltText()
        );
    }

    public ArticleSummaryResponse toSummary(ArticleSummaryRow article) {
        return new ArticleSummaryResponse(
            article.slug(),
            article.category(),
            article.topic().toUpperCase(Locale.ROOT),
            article.title(),
            article.excerpt(),
            article.publishedAt(),
            article.readMinutes(),
            article.imageUrl(),
            article.imageAlt()
        );
    }

    public ArticleSummaryResponse toSummary(ArticleSearchRow article) {
        return new ArticleSummaryResponse(
            article.getSlug(),
            article.getCategory(),
            article.getTopic().toUpperCase(Locale.ROOT),
            article.getTitle(),
            article.getExcerpt(),
            OffsetDateTime.ofInstant(article.getPublishedAt(), ZoneOffset.UTC),
            article.getReadMinutes(),
            article.getImageUrl(),
            article.getImageAlt()
        );
    }

    private ArticleDetailResponse toDetail(ArticleEntity article) {
        ArticleSummaryResponse summary = toSummary(article);
        return new ArticleDetailResponse(
            summary.slug(),
            summary.category(),
            summary.topic(),
            summary.title(),
            summary.excerpt(),
            article.getBodyMarkdown(),
            article.getBodyHtml(),
            summary.publishedAt(),
            summary.readMinutes(),
            summary.imageUrl(),
            summary.imageAlt(),
            article.getUpdatedAt()
        );
    }

    private ArticleNavigationResponse.NavigationItem toNavigation(ArticleEntity article) {
        return new ArticleNavigationResponse.NavigationItem(article.getSlug(), article.getTitle());
    }

    private ArticleEntity requirePublished(String slug) {
        return repository.findPublishedBySlug(slug, now())
            .orElseThrow(() -> new ResourceNotFoundException("published article not found"));
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private static String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }

    private static String normalizeQuery(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }
}
