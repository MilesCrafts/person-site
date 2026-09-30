package com.lekang.journal.article.api;

import java.time.OffsetDateTime;

public record ArticleDetailResponse(
    String slug,
    String category,
    String topic,
    String title,
    String excerpt,
    String bodyMarkdown,
    String bodyHtml,
    OffsetDateTime publishedAt,
    int readMinutes,
    String imageUrl,
    String imageAlt,
    OffsetDateTime updatedAt
) {
}
