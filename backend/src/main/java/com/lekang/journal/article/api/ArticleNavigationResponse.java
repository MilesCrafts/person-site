package com.lekang.journal.article.api;

public record ArticleNavigationResponse(
    NavigationItem previous,
    NavigationItem next
) {
    public record NavigationItem(String slug, String title) {
    }
}
