package com.lekang.journal.homepage.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HomepageFeatureRepository extends JpaRepository<HomepageFeatureEntity, Short> {
    @EntityGraph(attributePaths = {"article", "article.category", "article.topic", "article.coverAsset"})
    @Query("select h from HomepageFeatureEntity h order by h.sortOrder asc, h.id asc")
    List<HomepageFeatureEntity> findAllOrdered();
}
