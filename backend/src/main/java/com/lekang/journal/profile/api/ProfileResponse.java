package com.lekang.journal.profile.api;

public record ProfileResponse(
    String displayName,
    String bio,
    String manifesto,
    String email,
    String nowWatchingTitle,
    String nowWatchingDetail
) {
}
