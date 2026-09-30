package com.lekang.journal.taxonomy.application;

import com.lekang.journal.taxonomy.api.TaxonomyResponse;
import com.lekang.journal.taxonomy.infrastructure.CategoryRepository;
import com.lekang.journal.taxonomy.infrastructure.TopicRepository;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaxonomyQueryService {
    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;

    public TaxonomyQueryService(CategoryRepository categoryRepository, TopicRepository topicRepository) {
        this.categoryRepository = categoryRepository;
        this.topicRepository = topicRepository;
    }

    @Cacheable("taxonomies")
    @Transactional(readOnly = true)
    public List<TaxonomyResponse> findAll() {
        var topics = topicRepository.findEnabled();
        return categoryRepository.findEnabled().stream()
            .map(category -> new TaxonomyResponse(
                category.getCode(),
                category.getDisplayName(),
                category.getContentType(),
                topics.stream()
                    .filter(topic -> topic.getCategory().getId().equals(category.getId()))
                    .map(topic -> new TaxonomyResponse.TopicResponse(
                        topic.getCode(),
                        topic.getDisplayName(),
                        topic.getSlug()
                    ))
                    .toList()
            ))
            .toList();
    }
}
