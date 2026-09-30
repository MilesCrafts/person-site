package com.lekang.journal.admin.api;

import java.time.OffsetDateTime;

public record AdminArticleSummaryResponse(
    Long id,
    String slug,
    String title,
    String categoryCode,
    String topicCode,
    String status,
    OffsetDateTime publishedAt,
    OffsetDateTime updatedAt,
    int version
) {
}
