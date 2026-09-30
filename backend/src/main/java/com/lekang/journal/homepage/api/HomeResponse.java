package com.lekang.journal.homepage.api;

import com.lekang.journal.article.api.ArticleSummaryResponse;
import com.lekang.journal.profile.api.ProfileResponse;
import java.util.List;

public record HomeResponse(
    HomepageCopyResponse heroCopy,
    ArticleSummaryResponse feature,
    List<ArticleSummaryResponse> features,
    List<ArticleSummaryResponse> latestStories,
    ProfileResponse profile
) {
}
