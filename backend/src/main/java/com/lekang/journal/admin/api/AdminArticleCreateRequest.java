package com.lekang.journal.admin.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminArticleCreateRequest(
    @NotBlank
    @Size(max = 160)
    @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*")
    String slug,
    @NotBlank @Size(max = 240) String title,
    @NotBlank @Size(max = 4000) String excerpt,
    @NotBlank @Size(max = 32) String categoryCode,
    @NotBlank @Size(max = 40) String topicCode,
    @NotBlank @Size(max = 1_000_000) String bodyMarkdown,
    Long coverAssetId
) {
}
