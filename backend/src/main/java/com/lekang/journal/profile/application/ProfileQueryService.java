package com.lekang.journal.profile.application;

import com.lekang.journal.common.error.ResourceNotFoundException;
import com.lekang.journal.profile.api.ProfileResponse;
import com.lekang.journal.profile.infrastructure.SiteProfileRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileQueryService {
    private final SiteProfileRepository repository;

    public ProfileQueryService(SiteProfileRepository repository) {
        this.repository = repository;
    }

    @Cacheable("profile")
    @Transactional(readOnly = true)
    public ProfileResponse get() {
        var profile = repository.findById((short) 1)
            .orElseThrow(() -> new ResourceNotFoundException("site profile not found"));
        return new ProfileResponse(
            profile.getDisplayName(),
            profile.getBio(),
            profile.getManifesto(),
            profile.getEmail(),
            profile.getNowWatchingTitle(),
            profile.getNowWatchingDetail()
        );
    }
}
