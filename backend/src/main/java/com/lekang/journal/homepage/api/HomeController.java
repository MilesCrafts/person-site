package com.lekang.journal.homepage.api;

import com.lekang.journal.homepage.application.HomeQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController {
    private final HomeQueryService service;

    public HomeController(HomeQueryService service) {
        this.service = service;
    }

    @GetMapping
    public HomeResponse get() {
        return service.get();
    }
}
