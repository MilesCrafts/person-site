package com.lekang.journal.archive.api;

import com.lekang.journal.archive.application.ArchiveQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/archives")
public class ArchiveController {
    private final ArchiveQueryService service;

    public ArchiveController(ArchiveQueryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ArchiveResponse> findAll() {
        return service.findAll();
    }
}
