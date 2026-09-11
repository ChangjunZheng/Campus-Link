package com.campuslink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "campuslink")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Crypto crypto = new Crypto();
    private Captcha captcha = new Captcha();
    private Verify verify = new Verify();
    private Roster roster = new Roster();
    private CodeSender codeSender = new CodeSender();
    private Cors cors = new Cors();

    @Data
    public static class Jwt {
        private String secret;
        private int ttlDays = 7;
    }

    @Data
    public static class Crypto {
        private String hashKey;
        private String cryptKey;
    }

    @Data
    public static class Captcha {
        private int ttlMinutes = 5;
        private int resendIntervalSeconds = 60;
        private int dailyLimit = 10;
    }

    @Data
    public static class Verify {
        private int ipHourlyLimit = 10;
        private int ticketTtlMinutes = 5;
    }

    @Data
    public static class Roster {
        /** 学籍核验旁路（内置测试名册）：仅限开发联调，生产必须为 false（上线检查清单项） */
        private boolean bypass = true;
    }

    @Data
    public static class CodeSender {
        private String mode = "log";
    }

    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:5173";
    }
}
