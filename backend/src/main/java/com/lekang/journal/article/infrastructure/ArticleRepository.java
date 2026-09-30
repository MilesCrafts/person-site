package com.lekang.journal.article.infrastructure;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<ArticleEntity, Long> {

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

    @EntityGraph(attributePaths = {"category", "topic", "coverAsset"})
    @Query("select a from ArticleEntity a where a.id = :id")
    Optional<ArticleEntity> findAdminById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"category", "topic", "coverAsset"})
    Page<ArticleEntity> findAllByOrderByUpdatedAtDescIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "topic", "coverAsset"})
    Page<ArticleEntity> findByStatusOrderByUpdatedAtDescIdDesc(String status, Pageable pageable);

    long countByStatus(String status);

    @EntityGraph(attributePaths = {"category", "topic", "coverAsset"})
    @Query("""
        select a from ArticleEntity a
        where (:status = '' or a.status = :status)
          and (:query = ''
               or lower(a.title) like lower(concat('%', :query, '%'))
               or lower(a.slug) like lower(concat('%', :query, '%')))
        """)
    Page<ArticleEntity> searchAdmin(
        @Param("status") String status,
        @Param("query") String query,
        Pageable pageable
    );

    @Query("""
        select a from ArticleEntity a
        join fetch a.category c
        join fetch a.topic t
        left join fetch a.coverAsset m
        where a.slug = :slug and a.status = 'PUBLISHED' and a.publishedAt <= :now
        """)
    Optional<ArticleEntity> findPublishedBySlug(@Param("slug") String slug, @Param("now") OffsetDateTime now);

    @Query(
        value = """
            select new com.lekang.journal.article.infrastructure.ArticleSummaryRow(
                a.slug, a.category.code, a.topic.displayName, a.title, a.excerpt,
                a.publishedAt, a.readMinutes, a.coverAsset.externalUrl, a.coverAsset.altText
            )
            from ArticleEntity a
            where a.status = 'PUBLISHED' and a.publishedAt <= :now
              and (:category = '' or a.category.code = :category)
              and (:topic = '' or a.topic.code = :topic)
              and (:query = ''
                   or lower(a.title) like lower(concat('%', :query, '%'))
                   or lower(a.excerpt) like lower(concat('%', :query, '%'))
                   or lower(a.bodyMarkdown) like lower(concat('%', :query, '%'))
                   or lower(a.category.code) like lower(concat('%', :query, '%'))
                   or lower(a.topic.displayName) like lower(concat('%', :query, '%')))
            """,
        countQuery = """
            select count(a) from ArticleEntity a
            where a.status = 'PUBLISHED' and a.publishedAt <= :now
              and (:category = '' or a.category.code = :category)
              and (:topic = '' or a.topic.code = :topic)
              and (:query = ''
                   or lower(a.title) like lower(concat('%', :query, '%'))
                   or lower(a.excerpt) like lower(concat('%', :query, '%'))
                   or lower(a.bodyMarkdown) like lower(concat('%', :query, '%'))
                   or lower(a.category.code) like lower(concat('%', :query, '%'))
                   or lower(a.topic.displayName) like lower(concat('%', :query, '%')))
            """
    )
    Page<ArticleSummaryRow> findPublished(
        @Param("category") String category,
        @Param("topic") String topic,
        @Param("query") String query,
        @Param("now") OffsetDateTime now,
        Pageable pageable
    );

    @Query(value = """
        select a.slug as slug,
               c.code as category,
               t.display_name as topic,
               a.title as title,
               a.excerpt as excerpt,
               a.published_at as "publishedAt",
               a.read_minutes as "readMinutes",
               m.external_url as "imageUrl",
               m.alt_text as "imageAlt"
        from article a
        join content_category c on c.id = a.category_id
        join topic t on t.id = a.topic_id
        left join media_asset m on m.id = a.cover_asset_id
        where a.status = 'PUBLISHED'
          and a.published_at <= :now
          and (
              lower(a.title) like lower(concat('%', :query, '%'))
              or lower(a.excerpt) like lower(concat('%', :query, '%'))
              or lower(a.body_markdown) like lower(concat('%', :query, '%'))
              or lower(c.code) like lower(concat('%', :query, '%'))
              or lower(t.display_name) like lower(concat('%', :query, '%'))
              or public.similarity(lower(a.title), lower(:query)) >= 0.08
              or public.similarity(lower(a.excerpt), lower(:query)) >= 0.04
              or public.similarity(lower(a.body_markdown), lower(:query)) >= 0.02
          )
        order by (
            public.similarity(lower(a.title), lower(:query)) * 5.0
            + public.similarity(lower(a.excerpt), lower(:query)) * 2.0
            + public.similarity(lower(a.body_markdown), lower(:query))
            + case when lower(a.title) like lower(concat('%', :query, '%')) then 3.0 else 0.0 end
            + case when lower(a.excerpt) like lower(concat('%', :query, '%')) then 1.0 else 0.0 end
        ) desc,
        a.published_at desc,
        a.id desc
        """, countQuery = """
        select count(*)
        from article a
        join content_category c on c.id = a.category_id
        join topic t on t.id = a.topic_id
        where a.status = 'PUBLISHED'
          and a.published_at <= :now
          and (
              lower(a.title) like lower(concat('%', :query, '%'))
              or lower(a.excerpt) like lower(concat('%', :query, '%'))
              or lower(a.body_markdown) like lower(concat('%', :query, '%'))
              or lower(c.code) like lower(concat('%', :query, '%'))
              or lower(t.display_name) like lower(concat('%', :query, '%'))
              or public.similarity(lower(a.title), lower(:query)) >= 0.08
              or public.similarity(lower(a.excerpt), lower(:query)) >= 0.04
              or public.similarity(lower(a.body_markdown), lower(:query)) >= 0.02
          )
        """, nativeQuery = true)
    Page<ArticleSearchRow> searchPublished(
        @Param("query") String query,
        @Param("now") OffsetDateTime now,
        Pageable pageable
    );

    @Query("""
        select new com.lekang.journal.article.infrastructure.ArticleSummaryRow(
            a.slug, a.category.code, a.topic.displayName, a.title, a.excerpt,
            a.publishedAt, a.readMinutes, a.coverAsset.externalUrl, a.coverAsset.altText
        )
        from ArticleEntity a
        where a.status = 'PUBLISHED' and a.publishedAt <= :now
        order by a.publishedAt desc, a.id desc
        """)
    List<ArticleSummaryRow> findLatest(@Param("now") OffsetDateTime now, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "topic", "coverAsset"})
    @Query("""
        select a from ArticleEntity a
        where a.status = 'PUBLISHED' and a.publishedAt <= :now
          and (a.publishedAt > :publishedAt or (a.publishedAt = :publishedAt and a.id > :id))
        order by a.publishedAt asc, a.id asc
        """)
    List<ArticleEntity> findPrevious(
        @Param("publishedAt") OffsetDateTime publishedAt,
        @Param("id") Long id,
        @Param("now") OffsetDateTime now,
        Pageable pageable
    );

    @EntityGraph(attributePaths = {"category", "topic", "coverAsset"})
    @Query("""
        select a from ArticleEntity a
        where a.status = 'PUBLISHED' and a.publishedAt <= :now
          and (a.publishedAt < :publishedAt or (a.publishedAt = :publishedAt and a.id < :id))
        order by a.publishedAt desc, a.id desc
        """)
    List<ArticleEntity> findNext(
        @Param("publishedAt") OffsetDateTime publishedAt,
        @Param("id") Long id,
        @Param("now") OffsetDateTime now,
        Pageable pageable
    );

    @Query(value = """
        select extract(year from published_at)::int as year,
               extract(month from published_at)::int as month,
               count(*)::bigint as "articleCount"
        from article
        where status = 'PUBLISHED' and published_at <= :now
        group by extract(year from published_at), extract(month from published_at)
        order by year desc, month desc
        """, nativeQuery = true)
    List<ArchiveCount> countPublishedByMonth(@Param("now") OffsetDateTime now);

    interface ArchiveCount {
        Integer getYear();
        Integer getMonth();
        Long getArticleCount();
    }
}
