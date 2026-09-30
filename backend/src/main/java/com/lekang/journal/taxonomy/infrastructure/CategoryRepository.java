package com.lekang.journal.taxonomy.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    @Query("select c from CategoryEntity c where c.enabled = true order by c.sortOrder, c.id")
    List<CategoryEntity> findEnabled();

    Optional<CategoryEntity> findByCodeIgnoreCaseAndEnabledTrue(String code);
}
