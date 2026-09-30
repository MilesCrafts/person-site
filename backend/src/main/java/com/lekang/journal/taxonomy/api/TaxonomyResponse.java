package com.lekang.journal.taxonomy.api;

import java.util.List;

public record TaxonomyResponse(
    String code,
    String displayName,
    String contentType,
    List<TopicResponse> topics
) {
    public record TopicResponse(String code, String displayName, String slug) {
    }
}
