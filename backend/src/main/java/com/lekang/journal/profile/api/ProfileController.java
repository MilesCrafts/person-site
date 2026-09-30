package com.lekang.journal.profile.api;

import com.lekang.journal.profile.application.ProfileQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    private final ProfileQueryService service;

    public ProfileController(ProfileQueryService service) {
        this.service = service;
    }

    @GetMapping
    public ProfileResponse get() {
        return service.get();
    }
}
