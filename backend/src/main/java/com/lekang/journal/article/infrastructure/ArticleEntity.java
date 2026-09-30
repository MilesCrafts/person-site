package com.lekang.journal.article.infrastructure;

import com.lekang.journal.media.infrastructure.MediaAssetEntity;
import com.lekang.journal.taxonomy.infrastructure.CategoryEntity;
import com.lekang.journal.taxonomy.infrastructure.TopicEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;

@Entity
@Table(name = "article")
public class ArticleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String slug;
    private String title;
    private String excerpt;
    private String bodyMarkdown;
    private String bodyHtml;
    private String status;
    private OffsetDateTime publishedAt;
    private int readMinutes;
    @Version
    private int version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime archivedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id")
    private TopicEntity topic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_asset_id")
    private MediaAssetEntity coverAsset;

    protected ArticleEntity() {
    }

    public ArticleEntity(
        String slug,
        String title,
        String excerpt,
        String bodyMarkdown,
        String bodyHtml,
        int readMinutes,
        CategoryEntity category,
        TopicEntity topic,
        MediaAssetEntity coverAsset,
        OffsetDateTime now
    ) {
        this.slug = slug;
        this.title = title;
        this.excerpt = excerpt;
        this.bodyMarkdown = bodyMarkdown;
        this.bodyHtml = bodyHtml;
        this.status = "DRAFT";
        this.readMinutes = readMinutes;
        this.category = category;
        this.topic = topic;
        this.coverAsset = coverAsset;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getExcerpt() { return excerpt; }
    public String getBodyMarkdown() { return bodyMarkdown; }
    public String getBodyHtml() { return bodyHtml; }
    public String getStatus() { return status; }
    public OffsetDateTime getPublishedAt() { return publishedAt; }
    public int getReadMinutes() { return readMinutes; }
    public int getVersion() { return version; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getArchivedAt() { return archivedAt; }
    public CategoryEntity getCategory() { return category; }
    public TopicEntity getTopic() { return topic; }
    public MediaAssetEntity getCoverAsset() { return coverAsset; }

    public void updateContent(
        String slug,
        String title,
        String excerpt,
        String bodyMarkdown,
        String bodyHtml,
        int readMinutes,
        CategoryEntity category,
        TopicEntity topic,
        MediaAssetEntity coverAsset,
        OffsetDateTime now
    ) {
        this.slug = slug;
        this.title = title;
        this.excerpt = excerpt;
        this.bodyMarkdown = bodyMarkdown;
        this.bodyHtml = bodyHtml;
        this.readMinutes = readMinutes;
        this.category = category;
        this.topic = topic;
        this.coverAsset = coverAsset;
        this.updatedAt = now;
    }

    public void publish(OffsetDateTime now) {
        this.status = "PUBLISHED";
        if (this.publishedAt == null) {
            this.publishedAt = now;
        }
        this.archivedAt = null;
        this.updatedAt = now;
    }

    public void unpublish(OffsetDateTime now) {
        this.status = "DRAFT";
        this.updatedAt = now;
    }

    public void archive(OffsetDateTime now) {
        this.status = "ARCHIVED";
        this.archivedAt = now;
        this.updatedAt = now;
    }
}
