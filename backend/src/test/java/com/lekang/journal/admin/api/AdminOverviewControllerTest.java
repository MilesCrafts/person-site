package com.lekang.journal.admin.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lekang.journal.admin.application.AdminOverviewService;
import com.lekang.journal.common.config.SecurityConfig;
import com.lekang.journal.common.web.RequestIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminOverviewController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, RequestIdFilter.class})
class AdminOverviewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminOverviewService service;

    @Test
    void anonymousOverviewRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/overview"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void authenticatedAdminCanReadOverview() throws Exception {
        when(service.overview()).thenReturn(new AdminOverviewResponse(2, 3, 4, "今天也写一点。"));

        mockMvc.perform(get("/api/v1/admin/overview"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.drafts").value(2))
            .andExpect(jsonPath("$.dailyNote").value("今天也写一点。"));
    }
}
