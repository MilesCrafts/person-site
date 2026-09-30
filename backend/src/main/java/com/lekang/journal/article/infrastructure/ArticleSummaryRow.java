package com.lekang.journal.article.infrastructure;

import java.time.OffsetDateTime;

public record ArticleSummaryRow(
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
