package com.lekang.journal.article.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lekang.journal.article.application.ArticleQueryService;
import com.lekang.journal.common.api.PageResponse;
import com.lekang.journal.common.config.SecurityConfig;
import com.lekang.journal.common.web.RequestIdFilter;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, RequestIdFilter.class})
class ArticleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleQueryService service;

    @Test
    void returnsStablePageShapeAndRequestId() throws Exception {
        var article = new ArticleSummaryResponse(
            "messi-world-cup",
            "FOOTBALL",
            "MESSI",
            "2022，梅西终于捧起世界杯",
            "excerpt",
            OffsetDateTime.parse("2026-07-18T16:00:00Z"),
            8,
            "/images/journal/argentina-champions-wide.jpg",
            "阿根廷队捧起世界杯"
        );
        when(service.list(0, 6, null, null, null, "publishedAt,desc"))
            .thenReturn(new PageResponse<>(List.of(article), 0, 6, 1, 1, false));

        mockMvc.perform(get("/api/v1/articles").header("X-Request-Id", "test-request"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Request-Id", "test-request"))
            .andExpect(jsonPath("$.items[0].slug").value("messi-world-cup"))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void rejectsPageSizesAboveMaximum() throws Exception {
        mockMvc.perform(get("/api/v1/articles").param("size", "51"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("https://journal.lekang.site/problems/invalid-request"))
            .andExpect(jsonPath("$.requestId").isNotEmpty());
    }
}
