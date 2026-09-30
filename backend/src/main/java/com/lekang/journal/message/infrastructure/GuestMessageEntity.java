package com.lekang.journal.message.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;

@Entity
@Table(name = "guest_message")
public class GuestMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String senderName;
    private String contact;
    private String messageText;
    private String status;
    @Version
    private int version;
    private OffsetDateTime createdAt;
    private OffsetDateTime readAt;
    private OffsetDateTime archivedAt;

    protected GuestMessageEntity() {
    }

    public GuestMessageEntity(String senderName, String contact, String messageText, OffsetDateTime createdAt) {
        this.senderName = senderName;
        this.contact = contact;
        this.messageText = messageText;
        this.status = "UNREAD";
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getSenderName() { return senderName; }
    public String getContact() { return contact; }
    public String getMessageText() { return messageText; }
    public String getStatus() { return status; }
    public int getVersion() { return version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getReadAt() { return readAt; }
    public OffsetDateTime getArchivedAt() { return archivedAt; }

    public void markRead(OffsetDateTime now) {
        if ("UNREAD".equals(status)) {
            status = "READ";
            readAt = now;
        }
    }

    public void archive(OffsetDateTime now) {
        status = "ARCHIVED";
        archivedAt = now;
    }
}
