package com.lekang.journal.archive.application;

import com.lekang.journal.archive.api.ArchiveResponse;
import com.lekang.journal.article.infrastructure.ArticleRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArchiveQueryService {
    private final ArticleRepository repository;

    public ArchiveQueryService(ArticleRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ArchiveResponse> findAll() {
        return repository.countPublishedByMonth(OffsetDateTime.now(ZoneOffset.UTC)).stream()
            .map(row -> new ArchiveResponse(row.getYear(), row.getMonth(), row.getArticleCount()))
            .toList();
    }
}
