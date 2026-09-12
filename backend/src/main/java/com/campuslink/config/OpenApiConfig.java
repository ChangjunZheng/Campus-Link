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
                                + "运行时的强制点是 JwtAuthenticationFilter 解析 + Controller 内 CurrentUser 统一入口校验"
                                + "（SecurityConfig 仍为 permitAll，无路径级拦截）。"
                                + "受保护端点必须声明本 security 并真的调用 CurrentUser，"
                                + "该纪律由 ArchitectureGuardTest 机器校验（N-4 已闭环，CR-028）。")));
                } 
}
