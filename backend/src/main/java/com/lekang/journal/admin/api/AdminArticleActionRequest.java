package com.lekang.journal.admin.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AdminArticleActionRequest(@NotNull @Min(0) Integer version) {
}
