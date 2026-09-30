package com.lekang.journal.homepage.infrastructure;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;

@Entity
@Table(name = "homepage_copy")
public class HomepageCopyEntity {
    @Id
    private Short id;
    private String eyebrow;
    private String headlinePrimary;
    private String headlineEmphasis;
    private String headlineAccent;
    private String description;
    private String paperLabel;
    private String paperLineOne;
    private String paperLineTwo;
    private String paperLineThree;
    private String paperFooter;
    @Version
    private int version;
    private OffsetDateTime updatedAt;

    protected HomepageCopyEntity() {
    }

    public Short getId() { return id; }
    public String getEyebrow() { return eyebrow; }
    public String getHeadlinePrimary() { return headlinePrimary; }
    public String getHeadlineEmphasis() { return headlineEmphasis; }
    public String getHeadlineAccent() { return headlineAccent; }
    public String getDescription() { return description; }
    public String getPaperLabel() { return paperLabel; }
    public String getPaperLineOne() { return paperLineOne; }
    public String getPaperLineTwo() { return paperLineTwo; }
    public String getPaperLineThree() { return paperLineThree; }
    public String getPaperFooter() { return paperFooter; }
    public int getVersion() { return version; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void update(
        String eyebrow,
        String headlinePrimary,
        String headlineEmphasis,
        String headlineAccent,
        String description,
        String paperLabel,
        String paperLineOne,
        String paperLineTwo,
        String paperLineThree,
        String paperFooter,
        OffsetDateTime updatedAt
    ) {
        this.eyebrow = eyebrow;
        this.headlinePrimary = headlinePrimary;
        this.headlineEmphasis = headlineEmphasis;
        this.headlineAccent = headlineAccent;
        this.description = description;
        this.paperLabel = paperLabel;
        this.paperLineOne = paperLineOne;
        this.paperLineTwo = paperLineTwo;
        this.paperLineThree = paperLineThree;
        this.paperFooter = paperFooter;
        this.updatedAt = updatedAt;
    }
}
