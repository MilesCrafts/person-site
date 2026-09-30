package com.lekang.journal.album.infrastructure;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlbumRepository extends JpaRepository<AlbumEntity, Long> {
    @Query("""
        select a from AlbumEntity a
        where a.status = 'PUBLISHED' and a.publishedAt <= :now
        order by a.publishedAt desc, a.id desc
        """)
    Page<AlbumEntity> findPublished(@Param("now") OffsetDateTime now, Pageable pageable);

    @Query("""
        select a from AlbumEntity a
        where a.slug = :slug and a.status = 'PUBLISHED' and a.publishedAt <= :now
        """)
    Optional<AlbumEntity> findPublishedBySlug(@Param("slug") String slug, @Param("now") OffsetDateTime now);
}
