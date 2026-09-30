package com.lekang.journal.album.application;

import com.lekang.journal.album.api.AlbumPhotoResponse;
import com.lekang.journal.album.api.AlbumResponse;
import com.lekang.journal.album.infrastructure.AlbumPhotoRepository;
import com.lekang.journal.album.infrastructure.AlbumRepository;
import com.lekang.journal.common.api.PageResponse;
import com.lekang.journal.common.error.ResourceNotFoundException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlbumQueryService {
    private final AlbumRepository albumRepository;
    private final AlbumPhotoRepository photoRepository;

    public AlbumQueryService(AlbumRepository albumRepository, AlbumPhotoRepository photoRepository) {
        this.albumRepository = albumRepository;
        this.photoRepository = photoRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<AlbumResponse> list(int page, int size) {
        var result = albumRepository.findPublished(now(), PageRequest.of(page, size));
        return PageResponse.from(result, result.stream().map(this::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public AlbumResponse detail(String slug) {
        return toResponse(requirePublished(slug));
    }

    @Transactional(readOnly = true)
    public PageResponse<AlbumPhotoResponse> photos(String slug, String topic, int page, int size) {
        var album = requirePublished(slug);
        String normalizedTopic = topic == null || topic.isBlank()
            ? null
            : topic.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        var result = photoRepository.findByAlbum(album.getId(), normalizedTopic, PageRequest.of(page, size));
        List<AlbumPhotoResponse> photos = result.stream()
            .map(photo -> new AlbumPhotoResponse(
                photo.getId(),
                photo.getTopic() == null ? null : photo.getTopic().getDisplayName().toUpperCase(Locale.ROOT),
                photo.getTitle(),
                photo.getLocation(),
                photo.getShotAt(),
                photo.getCaption(),
                photo.getSortOrder(),
                photo.getMediaAsset().getExternalUrl(),
                photo.getMediaAsset().getAltText()
            ))
            .toList();
        return PageResponse.from(result, photos);
    }

    private com.lekang.journal.album.infrastructure.AlbumEntity requirePublished(String slug) {
        return albumRepository.findPublishedBySlug(slug, now())
            .orElseThrow(() -> new ResourceNotFoundException("published album not found"));
    }

    private AlbumResponse toResponse(com.lekang.journal.album.infrastructure.AlbumEntity album) {
        return new AlbumResponse(album.getSlug(), album.getTitle(), album.getDescription(), album.getPublishedAt());
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
