package com.lekang.journal.article.api;

import java.time.OffsetDateTime;

public record ArticleSummaryResponse(
    String slug,
    String category,
    String topic,
    String title,
    String excerpt,
    OffsetDateTime publishedAt,
    int readMinutes,
    String imageUrl,
    String imageAlt
) {
}
