package com.lekang.journal.album.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlbumPhotoRepository extends JpaRepository<AlbumPhotoEntity, Long> {
    @EntityGraph(attributePaths = {"mediaAsset", "topic"})
    @Query("""
        select p from AlbumPhotoEntity p
        where p.album.id = :albumId and (:topic is null or p.topic.code = :topic)
        order by p.sortOrder, p.id
        """)
    Page<AlbumPhotoEntity> findByAlbum(
        @Param("albumId") Long albumId,
        @Param("topic") String topic,
        Pageable pageable
    );
}
