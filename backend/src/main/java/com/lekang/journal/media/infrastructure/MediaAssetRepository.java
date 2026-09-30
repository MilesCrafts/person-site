package com.lekang.journal.media.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MediaAssetRepository extends JpaRepository<MediaAssetEntity, Long> {
    Page<MediaAssetEntity> findByStatus(String status, Pageable pageable);
}
