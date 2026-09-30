package com.lekang.journal.admin.api;

import java.time.OffsetDateTime;

public record AdminHomepageCopyResponse(
    String eyebrow,
    String headlinePrimary,
    String headlineEmphasis,
    String headlineAccent,
    String description,
    String paperLabel,
    String paperLineOne,
    String paperLineTwo,
    String paperLineThree,
    String paperFooter,
    int version,
    OffsetDateTime updatedAt
) {
}
