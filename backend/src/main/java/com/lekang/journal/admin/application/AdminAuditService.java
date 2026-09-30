package com.lekang.journal.admin.application;

import com.lekang.journal.admin.infrastructure.AdminAuditLogEntity;
import com.lekang.journal.admin.infrastructure.AdminAuditLogRepository;
import com.lekang.journal.admin.infrastructure.AdminUserRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuditService {
    private final AdminAuditLogRepository auditRepository;
    private final AdminUserRepository userRepository;

    public AdminAuditService(
        AdminAuditLogRepository auditRepository,
        AdminUserRepository userRepository
    ) {
        this.auditRepository = auditRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void record(String username, String action, String resourceType, String resourceId) {
        var user = userRepository.findByUsernameIgnoreCase(username).orElse(null);
        auditRepository.save(new AdminAuditLogEntity(
            user,
            action,
            resourceType,
            resourceId,
            MDC.get("requestId"),
            OffsetDateTime.now(ZoneOffset.UTC)
        ));
    }
}
