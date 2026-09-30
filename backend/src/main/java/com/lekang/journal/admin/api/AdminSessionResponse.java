package com.lekang.journal.admin.api;

public record AdminSessionResponse(
    boolean authenticated,
    String username,
    String csrfToken,
    String csrfHeaderName
) {
}
