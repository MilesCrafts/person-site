package com.lekang.journal.taxonomy.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TopicRepository extends JpaRepository<TopicEntity, Long> {
    @EntityGraph(attributePaths = "category")
    @Query("select t from TopicEntity t where t.enabled = true order by t.category.sortOrder, t.sortOrder, t.id")
    List<TopicEntity> findEnabled();

    @EntityGraph(attributePaths = "category")
    Optional<TopicEntity> findByCodeIgnoreCaseAndCategory_CodeIgnoreCaseAndEnabledTrue(
        String code,
        String categoryCode
    );
}
