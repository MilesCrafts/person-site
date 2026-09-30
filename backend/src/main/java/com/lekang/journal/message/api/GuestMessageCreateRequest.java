package com.lekang.journal.message.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuestMessageCreateRequest(
    @NotBlank @Size(max = 60) String name,
    @Size(max = 120) String contact,
    @NotBlank @Size(max = 2000) String message,
    @Size(max = 0) String website
) {
}
