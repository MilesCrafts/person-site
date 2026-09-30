package com.lekang.journal.admin.application;

import com.lekang.journal.admin.api.MarkdownPreviewResponse;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
public class MarkdownService {
    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
    private final Safelist safelist = Safelist.relaxed()
        .addAttributes("a", "rel")
        .preserveRelativeLinks(true);

    public MarkdownPreviewResponse render(String markdown) {
        String rendered = renderer.render(parser.parse(markdown));
        String safeHtml = Jsoup.clean(rendered, "", safelist);
        return new MarkdownPreviewResponse(safeHtml, calculateReadMinutes(markdown));
    }

    private int calculateReadMinutes(String markdown) {
        String plainText = Jsoup.parse(markdown).text().trim();
        long cjkCharacters = plainText.codePoints()
            .filter(codePoint -> codePoint >= 0x3400 && codePoint <= 0x9FFF)
            .count();
        long otherWords = plainText.replaceAll("[\\p{IsHan}]", " ")
            .trim()
            .split("\\s+").length;
        if (plainText.isBlank()) {
            otherWords = 0;
        }
        double minutes = cjkCharacters / 400.0 + otherWords / 200.0;
        return Math.max(1, (int) Math.ceil(minutes));
    }
}
