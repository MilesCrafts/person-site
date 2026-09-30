package com.lekang.journal.admin.api;

import com.lekang.journal.admin.application.AdminAuditService;
import com.lekang.journal.admin.application.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/session")
public class AdminSessionController {
    private final AuthenticationManager authenticationManager;
    private final LoginRateLimiter rateLimiter;
    private final AdminAuditService auditService;
    private final HttpSessionSecurityContextRepository securityContextRepository =
        new HttpSessionSecurityContextRepository();

    public AdminSessionController(
        AuthenticationManager authenticationManager,
        LoginRateLimiter rateLimiter,
        AdminAuditService auditService
    ) {
        this.authenticationManager = authenticationManager;
        this.rateLimiter = rateLimiter;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<AdminSessionResponse> login(
        @Valid @RequestBody AdminSessionRequest login,
        CsrfToken csrfToken,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String key = request.getRemoteAddr();
        rateLimiter.checkAllowed(key);
        try {
            Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                    login.username().trim().toLowerCase(Locale.ROOT),
                    login.password()
                )
            );
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
            rateLimiter.reset(key);
            auditService.record(authentication.getName(), "ADMIN_LOGIN", "SESSION", null);
            return noStore(sessionResponse(authentication, csrfToken));
        } catch (BadCredentialsException exception) {
            rateLimiter.recordFailure(key);
            auditService.record(login.username(), "ADMIN_LOGIN_FAILED", "SESSION", null);
            throw exception;
        }
    }

    @GetMapping
    public ResponseEntity<AdminSessionResponse> current(
        Authentication authentication,
        CsrfToken csrfToken
    ) {
        return noStore(sessionResponse(authentication, csrfToken));
    }

    @DeleteMapping
    public ResponseEntity<Void> logout(
        Authentication authentication,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String username = authentication.getName();
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        auditService.record(username, "ADMIN_LOGOUT", "SESSION", null);
        return ResponseEntity.noContent()
            .cacheControl(CacheControl.noStore())
            .build();
    }

    private AdminSessionResponse sessionResponse(Authentication authentication, CsrfToken csrfToken) {
        boolean authenticated = authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);
        return new AdminSessionResponse(
            authenticated,
            authenticated ? authentication.getName() : null,
            csrfToken.getToken(),
            csrfToken.getHeaderName()
        );
    }

    private static <T> ResponseEntity<T> noStore(T body) {
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(body);
    }
}
