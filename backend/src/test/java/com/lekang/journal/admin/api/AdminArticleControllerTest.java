package com.lekang.journal.admin.api;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lekang.journal.admin.application.AdminArticleService;
import com.lekang.journal.common.api.PageResponse;
import com.lekang.journal.common.config.SecurityConfig;
import com.lekang.journal.common.web.RequestIdFilter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminArticleController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, RequestIdFilter.class})
class AdminArticleControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminArticleService service;

    @Test
    void anonymousAdminRequestReturnsProblemDetails401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/articles"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.type").value("https://journal.lekang.site/problems/unauthorized"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void authenticatedAdminCanReadArticleList() throws Exception {
        when(service.list(0, 20, null, "", "updatedAt", "desc"))
            .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, false));

        mockMvc.perform(get("/api/v1/admin/articles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void articleListForwardsSearchAndSortOptions() throws Exception {
        when(service.list(0, 20, "DRAFT", "spring", "title", "asc"))
            .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, false));

        mockMvc.perform(get("/api/v1/admin/articles")
                .param("status", "DRAFT")
                .param("q", "spring")
                .param("sort", "title")
                .param("direction", "asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void writeRequestRequiresCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/admin/articles/preview")
                .contentType("application/json")
                .content("{\"bodyMarkdown\":\"# Preview\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void previewAcceptsValidCsrfToken() throws Exception {
        when(service.preview("# Preview"))
            .thenReturn(new MarkdownPreviewResponse("<h1>Preview</h1>", 1));

        mockMvc.perform(post("/api/v1/admin/articles/preview")
                .with(csrf())
                .contentType("application/json")
                .content("{\"bodyMarkdown\":\"# Preview\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bodyHtml").value("<h1>Preview</h1>"));
    }
}
