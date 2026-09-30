package com.lekang.journal.admin.application;

import com.lekang.journal.admin.api.AdminMessageCountResponse;
import com.lekang.journal.admin.api.AdminMessageResponse;
import com.lekang.journal.common.api.PageResponse;
import com.lekang.journal.common.error.ConflictException;
import com.lekang.journal.common.error.ResourceNotFoundException;
import com.lekang.journal.message.infrastructure.GuestMessageEntity;
import com.lekang.journal.message.infrastructure.GuestMessageRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMessageService {
    private static final Set<String> STATUSES = Set.of("UNREAD", "READ", "ARCHIVED");
    private final GuestMessageRepository repository;
    private final AdminAuditService auditService;

    public AdminMessageService(GuestMessageRepository repository, AdminAuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminMessageResponse> list(int page, int size, String status) {
        if (page < 0) throw new IllegalArgumentException("page must be at least 0");
        if (size < 1 || size > 50) throw new IllegalArgumentException("size must be between 1 and 50");
        if (status != null && !STATUSES.contains(status)) throw new IllegalArgumentException("invalid message status");
        PageRequest pageable = PageRequest.of(page, size, Sort.by(
            Sort.Order.desc("createdAt"), Sort.Order.desc("id")
        ));
        Page<GuestMessageEntity> messages = status == null
            ? repository.findAll(pageable)
            : repository.findAllByStatus(status, pageable);
        return PageResponse.from(messages, messages.stream().map(AdminMessageResponse::from).toList());
    }

    @Transactional(readOnly = true)
    public AdminMessageCountResponse count() {
        return new AdminMessageCountResponse(repository.countByStatus("UNREAD"));
    }

    @Transactional
    public AdminMessageResponse markRead(long id, int version, Authentication authentication) {
        GuestMessageEntity message = find(id);
        checkVersion(message, version);
        message.markRead(OffsetDateTime.now(ZoneOffset.UTC));
        repository.flush();
        auditService.record(authentication.getName(), "MESSAGE_READ", "GUEST_MESSAGE", String.valueOf(id));
        return AdminMessageResponse.from(message);
    }

    @Transactional
    public AdminMessageResponse archive(long id, int version, Authentication authentication) {
        GuestMessageEntity message = find(id);
        checkVersion(message, version);
        message.archive(OffsetDateTime.now(ZoneOffset.UTC));
        repository.flush();
        auditService.record(authentication.getName(), "MESSAGE_ARCHIVED", "GUEST_MESSAGE", String.valueOf(id));
        return AdminMessageResponse.from(message);
    }

    private GuestMessageEntity find(long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("message not found"));
    }

    private void checkVersion(GuestMessageEntity message, int version) {
        if (message.getVersion() != version) {
            throw new ConflictException("The message was changed by another request.");
        }
    }
}
