package com.lekang.journal.homepage.api;

public record HomepageCopyResponse(
    String eyebrow,
    String headlinePrimary,
    String headlineEmphasis,
    String headlineAccent,
    String description,
    String paperLabel,
    String paperLineOne,
    String paperLineTwo,
    String paperLineThree,
    String paperFooter
) {
}
