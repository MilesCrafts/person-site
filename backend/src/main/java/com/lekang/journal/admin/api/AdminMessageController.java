package com.lekang.journal.admin.api;

import com.lekang.journal.admin.application.AdminMessageService;
import com.lekang.journal.common.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/messages")
public class AdminMessageController {
    private final AdminMessageService service;

    public AdminMessageController(AdminMessageService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<PageResponse<AdminMessageResponse>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String status
    ) {
        return ok(service.list(page, size, status));
    }

    @GetMapping("/count")
    public ResponseEntity<AdminMessageCountResponse> count() {
        return ok(service.count());
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<AdminMessageResponse> markRead(
        @PathVariable long id,
        @Valid @RequestBody AdminMessageActionRequest request,
        Authentication authentication
    ) {
        return ok(service.markRead(id, request.version(), authentication));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<AdminMessageResponse> archive(
        @PathVariable long id,
        @Valid @RequestBody AdminMessageActionRequest request,
        Authentication authentication
    ) {
        return ok(service.archive(id, request.version(), authentication));
    }

    private static <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}
