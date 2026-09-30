package com.lekang.journal.media.api;

import com.lekang.journal.media.infrastructure.MediaAssetEntity;
import java.time.OffsetDateTime;

public record AdminMediaAssetResponse(
    Long id,
    String url,
    String originalName,
    String contentType,
    Long sizeBytes,
    Integer width,
    Integer height,
    String altText,
    OffsetDateTime createdAt
) {
    public static AdminMediaAssetResponse from(MediaAssetEntity asset) {
        return new AdminMediaAssetResponse(
            asset.getId(),
            asset.getExternalUrl(),
            asset.getOriginalName(),
            asset.getContentType(),
            asset.getSizeBytes(),
            asset.getWidth(),
            asset.getHeight(),
            asset.getAltText(),
            asset.getCreatedAt()
        );
    }
}
