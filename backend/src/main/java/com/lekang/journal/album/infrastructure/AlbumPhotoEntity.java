package com.lekang.journal.album.infrastructure;

import com.lekang.journal.media.infrastructure.MediaAssetEntity;
import com.lekang.journal.taxonomy.infrastructure.TopicEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "album_photo")
public class AlbumPhotoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String location;
    private LocalDate shotAt;
    private String caption;
    private int sortOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id")
    private AlbumEntity album;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_asset_id")
    private MediaAssetEntity mediaAsset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private TopicEntity topic;

    protected AlbumPhotoEntity() {
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public LocalDate getShotAt() { return shotAt; }
    public String getCaption() { return caption; }
    public int getSortOrder() { return sortOrder; }
    public AlbumEntity getAlbum() { return album; }
    public MediaAssetEntity getMediaAsset() { return mediaAsset; }
    public TopicEntity getTopic() { return topic; }
}
