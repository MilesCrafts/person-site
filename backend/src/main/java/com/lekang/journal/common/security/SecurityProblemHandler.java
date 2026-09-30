package com.lekang.journal.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

public class SecurityProblemHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private static final String PROBLEM_BASE = "https://journal.lekang.site/problems/";
    private final ObjectMapper objectMapper;

    public SecurityProblemHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException {
        write(request, response, 401, "unauthorized", "Authentication required");
    }

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException exception
    ) throws IOException {
        write(request, response, 403, "forbidden", "Access is denied");
    }

    private void write(
        HttpServletRequest request,
        HttpServletResponse response,
        int status,
        String type,
        String detail
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setHeader("Cache-Control", "no-store");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", PROBLEM_BASE + type);
        body.put("title", status == 401 ? "Unauthorized" : "Forbidden");
        body.put("status", status);
        body.put("detail", detail);
        body.put("instance", request.getRequestURI());
        body.put("requestId", MDC.get("requestId"));
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
