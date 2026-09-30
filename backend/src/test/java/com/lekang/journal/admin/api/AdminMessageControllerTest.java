package com.lekang.journal.admin.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lekang.journal.admin.application.AdminMessageService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminMessageController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, RequestIdFilter.class})
class AdminMessageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminMessageService service;

    @Test
    void anonymousRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/messages"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadInboxAndUnreadCount() throws Exception {
        when(service.list(0, 20, "UNREAD"))
            .thenReturn(new PageResponse<>(List.of(message("UNREAD", 0)), 0, 20, 1, 1, false));
        when(service.count()).thenReturn(new AdminMessageCountResponse(1));

        mockMvc.perform(get("/api/v1/admin/messages").param("status", "UNREAD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items[0].senderName").value("一位读者"));
        mockMvc.perform(get("/api/v1/admin/messages/count"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.unread").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void writeWithoutCsrfIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/admin/messages/12/read")
                .contentType("application/json")
                .content("{\"version\":0}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanMarkReadAndArchiveWithCsrf() throws Exception {
        when(service.markRead(eq(12L), eq(0), any(Authentication.class))).thenReturn(message("READ", 1));
        when(service.archive(eq(12L), eq(1), any(Authentication.class))).thenReturn(message("ARCHIVED", 2));

        mockMvc.perform(post("/api/v1/admin/messages/12/read")
                .with(csrf()).contentType("application/json").content("{\"version\":0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("READ"));
        mockMvc.perform(post("/api/v1/admin/messages/12/archive")
                .with(csrf()).contentType("application/json").content("{\"version\":1}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    private AdminMessageResponse message(String status, int version) {
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-08-12T08:00:00Z");
        return new AdminMessageResponse(12, "一位读者", "reader@example.com", "你好，这是一条认真写下的留言。",
            status, version, createdAt, status.equals("UNREAD") ? null : createdAt.plusMinutes(1),
            status.equals("ARCHIVED") ? createdAt.plusMinutes(2) : null);
    }
}
