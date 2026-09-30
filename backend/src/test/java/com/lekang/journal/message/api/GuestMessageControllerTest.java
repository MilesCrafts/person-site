package com.lekang.journal.message.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lekang.journal.common.config.SecurityConfig;
import com.lekang.journal.common.web.RequestIdFilter;
import com.lekang.journal.message.application.GuestMessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GuestMessageController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, RequestIdFilter.class})
class GuestMessageControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GuestMessageService service;

    @Test
    void anonymousVisitorCanSubmitWithoutCsrf() throws Exception {
        when(service.create(any(GuestMessageCreateRequest.class), eq("127.0.0.1")))
            .thenReturn(new GuestMessageCreatedResponse("留言已经放进收件箱。谢谢你写下来。"));

        mockMvc.perform(post("/api/v1/messages")
                .contentType("application/json")
                .content("""
                    {"name":"一位读者","contact":"reader@example.com","message":"你好，这是一条认真写下的留言。","website":""}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message").value("留言已经放进收件箱。谢谢你写下来。"));

        verify(service).create(any(GuestMessageCreateRequest.class), eq("127.0.0.1"));
    }

    @Test
    void emptyMessageIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                .contentType("application/json")
                .content("""
                    {"name":"读者","message":"","website":""}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shortMessageIsAccepted() throws Exception {
        when(service.create(any(GuestMessageCreateRequest.class), eq("127.0.0.1")))
            .thenReturn(new GuestMessageCreatedResponse("留言已经放进收件箱。谢谢你写下来。"));

        mockMvc.perform(post("/api/v1/messages")
                .contentType("application/json")
                .content("""
                    {"name":"读者","message":"你好","website":""}
                    """))
            .andExpect(status().isCreated());
    }

    @Test
    void filledHoneypotIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/messages")
                .contentType("application/json")
                .content("""
                    {"name":"机器人","message":"这是一条长度足够的垃圾留言。","website":"https://spam.invalid"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }
}
