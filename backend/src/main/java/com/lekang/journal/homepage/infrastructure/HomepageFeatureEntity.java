package com.lekang.journal.homepage.infrastructure;

import com.lekang.journal.article.infrastructure.ArticleEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "homepage_feature")
public class HomepageFeatureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;
    private int sortOrder;
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticleEntity article;

    protected HomepageFeatureEntity() {
    }

    public HomepageFeatureEntity(ArticleEntity article, int sortOrder, OffsetDateTime updatedAt) {
        this.article = article;
        this.sortOrder = sortOrder;
        this.updatedAt = updatedAt;
    }

    public Short getId() { return id; }
    public int getSortOrder() { return sortOrder; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public ArticleEntity getArticle() { return article; }
}
