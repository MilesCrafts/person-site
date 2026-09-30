package com.lekang.journal.admin.api;

import com.lekang.journal.message.infrastructure.GuestMessageEntity;
import java.time.OffsetDateTime;

public record AdminMessageResponse(
    long id,
    String senderName,
    String contact,
    String message,
    String status,
    int version,
    OffsetDateTime createdAt,
    OffsetDateTime readAt,
    OffsetDateTime archivedAt
) {
    public static AdminMessageResponse from(GuestMessageEntity entity) {
        return new AdminMessageResponse(
            entity.getId(), entity.getSenderName(), entity.getContact(), entity.getMessageText(),
            entity.getStatus(), entity.getVersion(), entity.getCreatedAt(), entity.getReadAt(), entity.getArchivedAt()
        );
    }
}
