package com.lekang.journal.message.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestMessageRepository extends JpaRepository<GuestMessageEntity, Long> {
    Page<GuestMessageEntity> findAllByStatus(String status, Pageable pageable);
    long countByStatus(String status);
}
