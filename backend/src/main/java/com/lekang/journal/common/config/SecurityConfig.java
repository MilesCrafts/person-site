package com.lekang.journal.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import com.lekang.journal.common.security.SecurityProblemHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        SecurityProblemHandler problemHandler,
        @Value("${journal.security.secure-cookies:false}") boolean secureCookies
    ) throws Exception {
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookieCustomizer(cookie -> cookie
            .path("/")
            .sameSite("Strict")
            .secure(secureCookies)
        );
        return http
            .cors(cors -> {})
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfRepository)
                .ignoringRequestMatchers(request ->
                    request.getMethod().equals("POST") && (
                        request.getRequestURI().equals("/api/v1/admin/session")
                            || request.getRequestURI().equals("/api/v1/messages")
                    )
                )
            )
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.GET, "/api/v1/admin/session").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/admin/session").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/messages").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .anyRequest().denyAll()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(problemHandler)
                .accessDeniedHandler(problemHandler)
            )
            .securityContext(context -> context.requireExplicitSave(true))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())
            .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecurityProblemHandler securityProblemHandler(ObjectMapper objectMapper) {
        return new SecurityProblemHandler(objectMapper);
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(
        @Value("${journal.cors.allowed-origins:}") List<String> allowedOrigins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins.stream().filter(value -> !value.isBlank()).toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
            "Content-Type",
            "X-Request-Id",
            "X-XSRF-TOKEN"
        ));
        configuration.setExposedHeaders(List.of("X-Request-Id", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
