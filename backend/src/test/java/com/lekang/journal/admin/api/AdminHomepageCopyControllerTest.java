package com.lekang.journal.admin.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lekang.journal.admin.application.AdminHomepageCopyService;
import com.lekang.journal.common.config.SecurityConfig;
import com.lekang.journal.common.web.RequestIdFilter;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminHomepageCopyController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, RequestIdFilter.class})
class AdminHomepageCopyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminHomepageCopyService service;

    @Test
    void anonymousRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/homepage/copy"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void authenticatedAdminCanReadCopy() throws Exception {
        when(service.get()).thenReturn(copy());

        mockMvc.perform(get("/api/v1/admin/homepage/copy"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.headlinePrimary").value("把想法"))
            .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void authenticatedAdminCanUpdateValidCopy() throws Exception {
        when(service.update(any(AdminHomepageCopyUpdateRequest.class), any(Authentication.class)))
            .thenReturn(copy());

        mockMvc.perform(put("/api/v1/admin/homepage/copy")
                .with(csrf())
                .contentType("application/json")
                .content("""
                    {
                      "eyebrow":"LEKANG · 产品、开发与记录",
                      "headlinePrimary":"把想法",
                      "headlineEmphasis":"理清楚，",
                      "headlineAccent":"再做出来。",
                      "description":"关注产品体验、前端开发和内容系统。",
                      "paperLabel":"正在搭建",
                      "paperLineOne":"让文章有地方住，",
                      "paperLineTwo":"让作品慢慢长，",
                      "paperLineThree":"也让我持续更新。",
                      "paperFooter":"BUILDING IN PUBLIC",
                      "version":0
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.headlineAccent").value("再做出来。"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blankCopyIsRejected() throws Exception {
        mockMvc.perform(put("/api/v1/admin/homepage/copy")
                .with(csrf())
                .contentType("application/json")
                .content("""
                    {
                      "eyebrow":"",
                      "headlinePrimary":"",
                      "headlineEmphasis":"",
                      "headlineAccent":"",
                      "description":"",
                      "version":0
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    private AdminHomepageCopyResponse copy() {
        return new AdminHomepageCopyResponse(
            "LEKANG · 产品、开发与记录",
            "把想法",
            "理清楚，",
            "再做出来。",
            "关注产品体验、前端开发和内容系统。",
            "正在搭建",
            "让文章有地方住，",
            "让作品慢慢长，",
            "也让我持续更新。",
            "BUILDING IN PUBLIC",
            0,
            OffsetDateTime.parse("2026-08-11T12:00:00Z")
        );
    }
}
