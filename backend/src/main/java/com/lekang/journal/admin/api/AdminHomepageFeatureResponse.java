package com.lekang.journal.admin.api;

public record AdminHomepageFeatureResponse(
    Long articleId,
    String slug,
    String title,
    int sortOrder
) {
}
