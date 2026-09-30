package com.lekang.journal.admin.api;

public record AdminOverviewResponse(
    long drafts,
    long published,
    long archived,
    String dailyNote
) {
}
