package com.lekang.journal.admin.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminSessionRequest(
    @NotBlank @Size(max = 80) String username,
    @NotBlank @Size(max = 200) String password
) {
}
