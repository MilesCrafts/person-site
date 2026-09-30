package com.lekang.journal.admin.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lekang.journal.admin.application.AdminHomepageFeatureService;
import com.lekang.journal.common.config.SecurityConfig;
import com.lekang.journal.common.web.RequestIdFilter;
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

@WebMvcTest(AdminHomepageFeatureController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, RequestIdFilter.class})
class AdminHomepageFeatureControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminHomepageFeatureService service;

    @Test
    void anonymousRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/homepage/features"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void authenticatedAdminCanReplaceOrderedFeatures() throws Exception {
        when(service.replace(any(AdminHomepageFeaturesRequest.class), any(Authentication.class)))
            .thenReturn(List.of(new AdminHomepageFeatureResponse(12L, "a-note", "一篇文章", 0)));

        mockMvc.perform(put("/api/v1/admin/homepage/features")
                .with(csrf())
                .contentType("application/json")
                .content("{\"articleIds\":[12]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].articleId").value(12))
            .andExpect(jsonPath("$[0].sortOrder").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void moreThanFiveFeaturesIsRejected() throws Exception {
        mockMvc.perform(put("/api/v1/admin/homepage/features")
                .with(csrf())
                .contentType("application/json")
                .content("{\"articleIds\":[1,2,3,4,5,6]}"))
            .andExpect(status().isBadRequest());
    }
}
