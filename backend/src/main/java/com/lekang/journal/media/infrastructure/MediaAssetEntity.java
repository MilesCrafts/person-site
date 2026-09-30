package com.lekang.journal.media.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "media_asset")
public class MediaAssetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sourceType;
    private String storageKey;
    private String externalUrl;
    private String originalName;
    private String contentType;
    private Long sizeBytes;
    private Integer width;
    private Integer height;
    private String altText;
    private String status;
    private OffsetDateTime createdAt;

    protected MediaAssetEntity() {
    }

    public MediaAssetEntity(
        String sourceType,
        String storageKey,
        String externalUrl,
        String originalName,
        String contentType,
        Long sizeBytes,
        Integer width,
        Integer height,
        String altText,
        String status,
        OffsetDateTime createdAt
    ) {
        this.sourceType = sourceType;
        this.storageKey = storageKey;
        this.externalUrl = externalUrl;
        this.originalName = originalName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.width = width;
        this.height = height;
        this.altText = altText;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getSourceType() { return sourceType; }
    public String getStorageKey() { return storageKey; }
    public String getExternalUrl() { return externalUrl; }
    public String getOriginalName() { return originalName; }
    public String getContentType() { return contentType; }
    public Long getSizeBytes() { return sizeBytes; }
    public Integer getWidth() { return width; }
    public Integer getHeight() { return height; }
    public String getAltText() { return altText; }
    public String getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void assignExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }
}
