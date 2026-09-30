package com.lekang.journal.media.application;

import org.springframework.core.io.Resource;

public record MediaContent(Resource resource, String contentType, String originalName, long sizeBytes) {
}
