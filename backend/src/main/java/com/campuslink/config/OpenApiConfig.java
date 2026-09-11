package com.campuslink.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Campus-Link API")
                .description("重庆工程学院计算机专业学生交流论坛（MVP）。错误码分段见技术方案第 5 节。")
                .version("0.1.0"));
    }
}
