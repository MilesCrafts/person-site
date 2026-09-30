package com.lekang.journal.media.api;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lekang.journal.common.api.PageResponse;
import com.lekang.journal.common.config.SecurityConfig;
import com.lekang.journal.common.web.RequestIdFilter;
import com.lekang.journal.media.application.MediaService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminMediaController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, RequestIdFilter.class})
class AdminMediaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaService service;

    @Test
    void anonymousMediaListReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/media"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void authenticatedAdminCanListMedia() throws Exception {
        when(service.list(0, 12)).thenReturn(new PageResponse<>(List.of(), 0, 12, 0, 0, false));

        mockMvc.perform(get("/api/v1/admin/media"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void uploadRequiresCsrf() throws Exception {
        var file = new MockMultipartFile("file", "cover.png", "image/png", new byte[]{1});

        mockMvc.perform(multipart("/api/v1/admin/media").file(file))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void validUploadReturnsCreatedAsset() throws Exception {
        var file = new MockMultipartFile("file", "cover.png", "image/png", new byte[]{1});
        var response = new AdminMediaAssetResponse(
            9L,
            "/api/v1/media/9/content",
            "cover.png",
            "image/png",
            100L,
            20,
            10,
            "cover",
            OffsetDateTime.parse("2026-08-11T00:00:00Z")
        );
        when(service.upload(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.eq("cover"),
            org.mockito.ArgumentMatchers.any(Authentication.class)
        )).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/admin/media")
                .file(file)
                .param("altText", "cover")
                .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(9))
            .andExpect(jsonPath("$.url").value("/api/v1/media/9/content"));
    }
}
