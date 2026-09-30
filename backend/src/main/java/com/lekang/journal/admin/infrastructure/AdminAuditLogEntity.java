package com.lekang.journal.admin.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "admin_audit_log")
public class AdminAuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "admin_user_id")
    private AdminUserEntity adminUser;

    private String action;
    private String resourceType;
    private String resourceId;
    private String requestId;
    private OffsetDateTime createdAt;

    protected AdminAuditLogEntity() {
    }

    public AdminAuditLogEntity(
        AdminUserEntity adminUser,
        String action,
        String resourceType,
        String resourceId,
        String requestId,
        OffsetDateTime createdAt
    ) {
        this.adminUser = adminUser;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.requestId = requestId;
        this.createdAt = createdAt;
    }
}
