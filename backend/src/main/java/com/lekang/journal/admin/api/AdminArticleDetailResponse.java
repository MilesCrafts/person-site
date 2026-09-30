package com.lekang.journal.admin.api;

import java.time.OffsetDateTime;

public record AdminArticleDetailResponse(
    Long id,
    String slug,
    String title,
    String excerpt,
    String categoryCode,
    String topicCode,
    String bodyMarkdown,
    String bodyHtml,
    Long coverAssetId,
    String coverImageUrl,
    String status,
    OffsetDateTime publishedAt,
    int readMinutes,
    int version,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    OffsetDateTime archivedAt
) {
}
