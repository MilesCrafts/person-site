package com.lekang.journal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class PublicApiIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
        .withDatabaseName("lekang_journal")
        .withUsername("lekang_test")
        .withPassword("lekang_test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void flywaySeedsTwelvePublishedArticlesAndPublicEndpointsReadThem() throws Exception {
        mockMvc.perform(get("/api/v1/home"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.heroCopy.headlinePrimary").value("把想法"))
            .andExpect(jsonPath("$.heroCopy.headlineAccent").value("再做出来。"))
            .andExpect(jsonPath("$.heroCopy.paperLabel").value("正在搭建"))
            .andExpect(jsonPath("$.heroCopy.paperFooter").value("BUILDING IN PUBLIC"));

        mockMvc.perform(get("/api/v1/articles").param("size", "50"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalItems").value(12))
            .andExpect(jsonPath("$.items[0].slug").value("messi-world-cup"));

        mockMvc.perform(get("/api/v1/articles/messi-world-cup"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.category").value("FOOTBALL"))
            .andExpect(jsonPath("$.bodyHtml").isNotEmpty());

        mockMvc.perform(get("/api/v1/taxonomies"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("FOOTBALL"));

        mockMvc.perform(get("/api/v1/search").param("q", "梅西"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalItems").value(1));

        mockMvc.perform(get("/api/v1/home"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.feature.slug").value("messi-world-cup"))
            .andExpect(jsonPath("$.features[0].slug").value("messi-world-cup"));

        mockMvc.perform(get("/api/v1/articles")
                .param("category", "FOOTBALL")
                .param("topic", "MESSI")
                .param("sort", "publishedAt,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    void unknownPublishedSlugUsesProblemDetails() throws Exception {
        mockMvc.perform(get("/api/v1/articles/not-found"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.type").value("https://journal.lekang.site/problems/not-found"))
            .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void adminEndpointsAreNotPublic() throws Exception {
        mockMvc.perform(get("/api/v1/admin/articles"))
            .andExpect(status().isUnauthorized());
    }
}
