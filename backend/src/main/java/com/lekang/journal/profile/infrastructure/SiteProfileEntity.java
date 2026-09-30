package com.lekang.journal.profile.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "site_profile")
public class SiteProfileEntity {
    @Id
    private Short id;
    private String displayName;
    private String bio;
    private String manifesto;
    private String email;
    private String nowWatchingTitle;
    private String nowWatchingDetail;
    private OffsetDateTime updatedAt;

    protected SiteProfileEntity() {
    }

    public String getDisplayName() { return displayName; }
    public String getBio() { return bio; }
    public String getManifesto() { return manifesto; }
    public String getEmail() { return email; }
    public String getNowWatchingTitle() { return nowWatchingTitle; }
    public String getNowWatchingDetail() { return nowWatchingDetail; }
}
