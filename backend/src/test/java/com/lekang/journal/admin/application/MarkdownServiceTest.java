package com.lekang.journal.admin.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MarkdownServiceTest {
    private final MarkdownService service = new MarkdownService();

    @Test
    void rendersMarkdownAndRemovesDangerousHtmlAndProtocols() {
        var preview = service.render(
            "# Title\n\n[unsafe](javascript:alert(1))\n\n<script>alert('x')</script>"
        );

        assertThat(preview.bodyHtml()).contains("<h1>Title</h1>");
        assertThat(preview.bodyHtml()).doesNotContain("javascript:", "<script");
        assertThat(preview.readMinutes()).isEqualTo(1);
    }
}
