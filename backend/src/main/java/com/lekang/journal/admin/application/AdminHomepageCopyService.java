package com.lekang.journal.admin.application;

import com.lekang.journal.admin.api.AdminHomepageCopyResponse;
import com.lekang.journal.admin.api.AdminHomepageCopyUpdateRequest;
import com.lekang.journal.common.error.ConflictException;
import com.lekang.journal.common.error.ResourceNotFoundException;
import com.lekang.journal.homepage.infrastructure.HomepageCopyEntity;
import com.lekang.journal.homepage.infrastructure.HomepageCopyRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class AdminHomepageCopyService {
    private final HomepageCopyRepository repository;
    private final AdminAuditService auditService;
    private final CacheManager cacheManager;

    public AdminHomepageCopyService(
        HomepageCopyRepository repository,
        AdminAuditService auditService,
        CacheManager cacheManager
    ) {
        this.repository = repository;
        this.auditService = auditService;
        this.cacheManager = cacheManager;
    }

    @Transactional(readOnly = true)
    public AdminHomepageCopyResponse get() {
        return toResponse(findCopy());
    }

    @Transactional
    public AdminHomepageCopyResponse update(
        AdminHomepageCopyUpdateRequest request,
        Authentication authentication
    ) {
        HomepageCopyEntity copy = findCopy();
        if (copy.getVersion() != request.version()) {
            throw new ConflictException("The homepage copy was changed by another request.");
        }
        copy.update(
            request.eyebrow().trim(),
            request.headlinePrimary().trim(),
            request.headlineEmphasis().trim(),
            request.headlineAccent().trim(),
            request.description().trim(),
            request.paperLabel().trim(),
            request.paperLineOne().trim(),
            request.paperLineTwo().trim(),
            request.paperLineThree().trim(),
            request.paperFooter().trim(),
            OffsetDateTime.now(ZoneOffset.UTC)
        );
        repository.flush();
        auditService.record(authentication.getName(), "HOMEPAGE_COPY_UPDATED", "HOMEPAGE_COPY", "1");
        evictHomeAfterCommit();
        return toResponse(copy);
    }

    private HomepageCopyEntity findCopy() {
        return repository.findById((short) 1)
            .orElseThrow(() -> new ResourceNotFoundException("homepage copy not found"));
    }

    private AdminHomepageCopyResponse toResponse(HomepageCopyEntity copy) {
        return new AdminHomepageCopyResponse(
            copy.getEyebrow(),
            copy.getHeadlinePrimary(),
            copy.getHeadlineEmphasis(),
            copy.getHeadlineAccent(),
            copy.getDescription(),
            copy.getPaperLabel(),
            copy.getPaperLineOne(),
            copy.getPaperLineTwo(),
            copy.getPaperLineThree(),
            copy.getPaperFooter(),
            copy.getVersion(),
            copy.getUpdatedAt()
        );
    }

    private void evictHomeAfterCommit() {
        Runnable clear = () -> {
            var cache = cacheManager.getCache("home");
            if (cache != null) cache.clear();
        };
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            clear.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { clear.run(); }
        });
    }
}
