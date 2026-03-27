package com.housekeeping.doc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI housekeepingOpenApi() {
        final String bearerSchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("家政服务预约平台 API 文档")
                        .description("基于 Spring Boot + MyBatis-Plus + Vue 的家政服务预约平台接口文档")
                        .version("v1.0.0")
                        .contact(new Contact().name("Housekeeping Project"))
                        .license(new License().name("For demo/testing only")))
                .addSecurityItem(new SecurityRequirement().addList(bearerSchemeName))
                .components(new Components()
                        .addSecuritySchemes(bearerSchemeName, new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("Token")));
    }
}
