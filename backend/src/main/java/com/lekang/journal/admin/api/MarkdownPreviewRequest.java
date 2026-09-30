package com.lekang.journal.admin.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MarkdownPreviewRequest(
    @NotNull @Size(max = 1_000_000) String bodyMarkdown
) {
}
