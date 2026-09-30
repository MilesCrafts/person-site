package com.lekang.journal.search.api;

import com.lekang.journal.article.api.ArticleSummaryResponse;
import com.lekang.journal.article.application.ArticleQueryService;
import com.lekang.journal.common.api.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private final ArticleQueryService articleService;

    public SearchController(ArticleQueryService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public PageResponse<ArticleSummaryResponse> search(
        @RequestParam @Size(min = 2, max = 100) String q,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size
    ) {
        return articleService.list(page, size, null, null, q, "publishedAt,desc");
    }
}
