package com.lekang.journal.admin.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AdminHomepageCopyUpdateRequest(
    @NotBlank @Size(max = 120) String eyebrow,
    @NotBlank @Size(max = 80) String headlinePrimary,
    @NotBlank @Size(max = 80) String headlineEmphasis,
    @NotBlank @Size(max = 80) String headlineAccent,
    @NotBlank @Size(max = 500) String description,
    @NotBlank @Size(max = 40) String paperLabel,
    @NotBlank @Size(max = 120) String paperLineOne,
    @NotBlank @Size(max = 120) String paperLineTwo,
    @NotBlank @Size(max = 120) String paperLineThree,
    @NotBlank @Size(max = 80) String paperFooter,
    @NotNull @PositiveOrZero Integer version
) {
}
