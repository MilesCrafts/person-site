package com.lekang.journal.album.api;

import java.time.LocalDate;

public record AlbumPhotoResponse(
    long id,
    String topic,
    String title,
    String location,
    LocalDate shotAt,
    String caption,
    int sortOrder,
    String imageUrl,
    String imageAlt
) {
}
