package com.lekang.journal.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI journalOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("LEKANG JOURNAL API")
                .version("v1")
                .description("Public read API for the LEKANG JOURNAL cultural archive."))
            .servers(List.of(new Server().url("/").description("Same-origin API")));
    }
}
