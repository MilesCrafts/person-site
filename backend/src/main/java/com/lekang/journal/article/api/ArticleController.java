package com.lekang.journal.article.api;

import com.lekang.journal.article.application.ArticleQueryService;
import com.lekang.journal.common.api.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {
    private final ArticleQueryService service;

    public ArticleController(ArticleQueryService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ArticleSummaryResponse> list(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "6") @Min(1) @Max(50) int size,
        @RequestParam(required = false) @Size(max = 32) String category,
        @RequestParam(required = false) @Size(max = 40) String topic,
        @RequestParam(defaultValue = "publishedAt,desc")
        @Pattern(regexp = "publishedAt,(asc|desc)") String sort
    ) {
        return service.list(page, size, category, topic, null, sort);
    }

    @GetMapping("/{slug}")
    public ArticleDetailResponse detail(
        @PathVariable @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 160) String slug
    ) {
        return service.detail(slug);
    }

    @GetMapping("/{slug}/navigation")
    public ArticleNavigationResponse navigation(
        @PathVariable @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 160) String slug
    ) {
        return service.navigation(slug);
    }
}
