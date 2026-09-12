package com.campuslink.config;

import com.campuslink.common.web.ApiDocs;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Campus-Link API")
                        .description("重庆工程学院计算机专业学生交流论坛（MVP）。错误码分段见技术方案第 5 节。")
                        .version("0.1.0"))
                .components(new Components().addSecuritySchemes(ApiDocs.BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("登录返回的 JWT，请求头 `Authorization: Bearer <token>`。"
                                + "注意：这只是契约声明，运行时的强制点是 JwtAuthenticationFilter + Controller 内校验"
                                + "（SecurityConfig 目前为 permitAll），新增受保护端点若漏写校验不会被框架拦下。")));
    }
}
