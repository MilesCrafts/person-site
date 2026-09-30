package com.lekang.journal.admin.api;

import com.lekang.journal.admin.application.AdminHomepageCopyService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/homepage/copy")
public class AdminHomepageCopyController {
    private final AdminHomepageCopyService service;

    public AdminHomepageCopyController(AdminHomepageCopyService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<AdminHomepageCopyResponse> get() {
        return ok(service.get());
    }

    @PutMapping
    public ResponseEntity<AdminHomepageCopyResponse> update(
        @Valid @RequestBody AdminHomepageCopyUpdateRequest request,
        Authentication authentication
    ) {
        return ok(service.update(request, authentication));
    }

    private static <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}
