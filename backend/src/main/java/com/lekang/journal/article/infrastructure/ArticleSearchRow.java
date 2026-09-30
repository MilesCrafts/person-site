package com.lekang.journal.article.infrastructure;

import java.time.Instant;

/** Native PostgreSQL projection for ranked trigram search results. */
public interface ArticleSearchRow {
    String getSlug();
    String getCategory();
    String getTopic();
    String getTitle();
    String getExcerpt();
    Instant getPublishedAt();
    Integer getReadMinutes();
    String getImageUrl();
    String getImageAlt();
}
