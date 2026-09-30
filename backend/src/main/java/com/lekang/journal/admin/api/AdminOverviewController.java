package com.lekang.journal.admin.api;

import com.lekang.journal.admin.application.AdminOverviewService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/overview")
public class AdminOverviewController {
    private final AdminOverviewService service;

    public AdminOverviewController(AdminOverviewService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<AdminOverviewResponse> overview() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.overview());
    }
}
