package com.lekang.journal.admin.api;

import com.lekang.journal.admin.application.AdminHomepageFeatureService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/homepage/features")
public class AdminHomepageFeatureController {
    private final AdminHomepageFeatureService service;

    public AdminHomepageFeatureController(AdminHomepageFeatureService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AdminHomepageFeatureResponse>> list() {
        return ok(service.list());
    }

    @PutMapping
    public ResponseEntity<List<AdminHomepageFeatureResponse>> replace(
        @Valid @RequestBody AdminHomepageFeaturesRequest request,
        Authentication authentication
    ) {
        return ok(service.replace(request, authentication));
    }

    private static <T> ResponseEntity<T> ok(T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}
