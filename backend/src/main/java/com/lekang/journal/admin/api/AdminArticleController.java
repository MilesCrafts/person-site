package com.lekang.journal.admin.api;

import com.lekang.journal.admin.application.AdminArticleService;
import com.lekang.journal.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/articles")
public class AdminArticleController {
    private final AdminArticleService service;

    public AdminArticleController(AdminArticleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AdminArticleSummaryResponse>> list(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
        @RequestParam(required = false) @Size(max = 20) String status,
        @RequestParam(defaultValue = "") @Size(max = 100) String q,
        @RequestParam(defaultValue = "updatedAt") @Size(max = 20) String sort,
        @RequestParam(defaultValue = "desc") @Size(max = 4) String direction
    ) {
        return ok(service.list(page, size, status, q, sort, direction));
    }

    @PostMapping
    public ResponseEntity<AdminArticleDetailResponse> create(
        @Valid @RequestBody AdminArticleCreateRequest request,
        Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .cacheControl(CacheControl.noStore())
            .body(service.create(request, authentication));
    }

    @PostMapping("/preview")
    public ResponseEntity<MarkdownPreviewResponse> preview(
        @Valid @RequestBody MarkdownPreviewRequest request
    ) {
        return ok(service.preview(request.bodyMarkdown()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminArticleDetailResponse> detail(@PathVariable @Min(1) Long id) {
        return ok(service.detail(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminArticleDetailResponse> update(
        @PathVariable @Min(1) Long id,
        @Valid @RequestBody AdminArticleUpdateRequest request,
        Authentication authentication
    ) {
        return ok(service.update(id, request, authentication));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<AdminArticleDetailResponse> publish(
        @PathVariable @Min(1) Long id,
        @Valid @RequestBody AdminArticleActionRequest request,
        Authentication authentication
    ) {
        return ok(service.publish(id, request, authentication));
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<AdminArticleDetailResponse> unpublish(
        @PathVariable @Min(1) Long id,
        @Valid @RequestBody AdminArticleActionRequest request,
        Authentication authentication
    ) {
        return ok(service.unpublish(id, request, authentication));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<AdminArticleDetailResponse> archive(
        @PathVariable @Min(1) Long id,
        @Valid @RequestBody AdminArticleActionRequest request,
        Authentication authentication
    ) {
        return ok(service.archive(id, request, authentication));
    }

    private static <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(body);
    }
}
