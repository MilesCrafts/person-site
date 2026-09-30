package com.lekang.journal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lekang.journal.admin.infrastructure.AdminUserEntity;
import com.lekang.journal.admin.infrastructure.AdminUserRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("local")
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfSystemProperty(named = "journal.local-postgres-tests", matches = "true")
@Sql(
    statements = {
        "DELETE FROM admin_audit_log WHERE admin_user_id IN "
            + "(SELECT id FROM admin_user WHERE username = 'local-v2-integration-admin')",
        "DELETE FROM article WHERE slug = 'local-v2-integration-draft'",
        "DELETE FROM admin_user WHERE username = 'local-v2-integration-admin'"
    },
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
    statements = {
        "DELETE FROM admin_audit_log WHERE admin_user_id IN "
            + "(SELECT id FROM admin_user WHERE username = 'local-v2-integration-admin')",
        "DELETE FROM article WHERE slug = 'local-v2-integration-draft'",
        "DELETE FROM admin_user WHERE username = 'local-v2-integration-admin'"
    },
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class LocalPostgresAdminIntegrationTest {
    private static final String USERNAME = "local-v2-integration-admin";
    private String password;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void createTransactionalTestAdmin() {
        password = UUID.randomUUID() + UUID.randomUUID().toString();
        adminUserRepository.saveAndFlush(new AdminUserEntity(
            USERNAME,
            passwordEncoder.encode(password),
            OffsetDateTime.now(ZoneOffset.UTC)
        ));
    }

    @Test
    void loginCreateEditPublishConflictUnpublishAndArchive() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/admin/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(Map.of(
                    "username", USERNAME,
                    "password", password
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.authenticated").value(true))
            .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        MvcResult created = mockMvc.perform(post("/api/v1/admin/articles")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "slug":"local-v2-integration-draft",
                      "title":"Integration draft",
                      "excerpt":"Transactional integration test",
                      "categoryCode":"FOOTBALL",
                      "topicCode":"MESSI",
                      "bodyMarkdown":"# Draft"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andReturn();
        JsonNode createdBody = body(created);
        long id = createdBody.get("id").asLong();
        int createVersion = createdBody.get("version").asInt();

        MvcResult updated = mockMvc.perform(put("/api/v1/admin/articles/{id}", id)
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "version":%d,
                      "slug":"local-v2-integration-draft",
                      "title":"Integration draft updated",
                      "excerpt":"Transactional integration test",
                      "categoryCode":"FOOTBALL",
                      "topicCode":"MESSI",
                      "bodyMarkdown":"# Safe\\n\\n<script>alert('x')</script>"
                    }
                    """.formatted(createVersion)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bodyHtml").value(org.hamcrest.Matchers.not(
                org.hamcrest.Matchers.containsString("<script")
            )))
            .andReturn();
        int updateVersion = body(updated).get("version").asInt();

        MvcResult published = action(session, id, "publish", updateVersion)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PUBLISHED"))
            .andReturn();
        int publishVersion = body(published).get("version").asInt();

        mockMvc.perform(get("/api/v1/articles/local-v2-integration-draft"))
            .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/admin/articles/{id}", id)
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "version":%d,
                      "slug":"local-v2-integration-draft",
                      "title":"Stale edit",
                      "excerpt":"Transactional integration test",
                      "categoryCode":"FOOTBALL",
                      "topicCode":"MESSI",
                      "bodyMarkdown":"stale"
                    }
                    """.formatted(updateVersion)))
            .andExpect(status().isConflict());

        MvcResult unpublished = action(session, id, "unpublish", publishVersion)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DRAFT"))
            .andReturn();
        int unpublishVersion = body(unpublished).get("version").asInt();

        mockMvc.perform(get("/api/v1/articles/local-v2-integration-draft"))
            .andExpect(status().isNotFound());

        action(session, id, "archive", unpublishVersion)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    private org.springframework.test.web.servlet.ResultActions action(
        MockHttpSession session,
        long id,
        String action,
        int version
    ) throws Exception {
        return mockMvc.perform(post("/api/v1/admin/articles/{id}/{action}", id, action)
            .session(session)
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"version\":" + version + "}"));
    }

    private JsonNode body(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }
}
