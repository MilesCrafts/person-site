package com.lekang.journal.album.api;

import com.lekang.journal.album.application.AlbumQueryService;
import com.lekang.journal.common.api.PageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/albums")
public class AlbumController {
    private final AlbumQueryService service;

    public AlbumController(AlbumQueryService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<AlbumResponse> list(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return service.list(page, size);
    }

    @GetMapping("/{slug}")
    public AlbumResponse detail(
        @PathVariable @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 160) String slug
    ) {
        return service.detail(slug);
    }

    @GetMapping("/{slug}/photos")
    public PageResponse<AlbumPhotoResponse> photos(
        @PathVariable @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 160) String slug,
        @RequestParam(required = false) @Size(max = 40) String topic,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return service.photos(slug, topic, page, size);
    }
}
