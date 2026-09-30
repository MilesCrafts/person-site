package com.lekang.journal.album.api;

import java.time.OffsetDateTime;

public record AlbumResponse(
    String slug,
    String title,
    String description,
    OffsetDateTime publishedAt
) {
}
