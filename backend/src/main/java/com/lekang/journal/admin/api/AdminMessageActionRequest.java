package com.lekang.journal.admin.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AdminMessageActionRequest(@NotNull @PositiveOrZero Integer version) {
}
