package com.lekang.journal.media.api;

import com.lekang.journal.common.api.PageResponse;
import com.lekang.journal.media.application.MediaService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/v1/admin/media")
public class AdminMediaController {
    private final MediaService service;

    public AdminMediaController(MediaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AdminMediaAssetResponse>> list(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size
    ) {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(service.list(page, size));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminMediaAssetResponse> upload(
        @RequestPart("file") MultipartFile file,
        @RequestParam(required = false) @Size(max = 255) String altText,
        Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .cacheControl(CacheControl.noStore())
            .body(service.upload(file, altText, authentication));
    }
}
