package com.lekang.journal.taxonomy.api;

import com.lekang.journal.taxonomy.application.TaxonomyQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/taxonomies")
public class TaxonomyController {
    private final TaxonomyQueryService service;

    public TaxonomyController(TaxonomyQueryService service) {
        this.service = service;
    }

    @GetMapping
    public List<TaxonomyResponse> findAll() {
        return service.findAll();
    }
}
