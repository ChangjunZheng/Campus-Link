package com.campuslink.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 时间戳口径守护（CR-051，闭环台账 L-6）。
 *
 * <p>V1 各表用 {@code DEFAULT CURRENT_TIMESTAMP} 写 DATETIME，取值由 <b>MySQL 会话时区</b>决定；
 * 而 {@code connectionTimeZone} 只影响驱动如何解释这些墙钟值。两者不一致时，全库时间戳会整体偏移
 * 本机与 UTC 的时差（本机 +8），表现为"刚刚"恒真、绝对时间错位。
 *
 * <p><b>本测试只能锁住配置声明</b>——它看不见 MySQL 的真实会话时区，后者由真机验证兜
 * （见 {@code docs/开发/实施方案/CR-051-时间戳时区口径修复.md} §6）。
 */
class DataSourceTimezoneTest {

    private static final String CONNECTION_TIME_ZONE = "connectionTimeZone=UTC";
    private static final String FORCE_SESSION_TIME_ZONE = "forceConnectionTimeZoneToSession=true";

    private static String datasourceUrl() {
        YamlPropertiesFactoryBean loader = new YamlPropertiesFactoryBean();
        loader.setResources(new ClassPathResource("application.yml"));
        Properties properties = loader.getObject();
        assertThat(properties).as("application.yml 必须在测试类路径上可见").isNotNull();
        String url = properties.getProperty("spring.datasource.url");
        assertThat(url).as("spring.datasource.url 必须存在").isNotBlank();
        return url;
    }

    @Test
    @DisplayName("数据源必须声明 connectionTimeZone=UTC 且强制把它写进 MySQL 会话")
    void declaresUtcAndForcesItIntoSession() {
        String url = datasourceUrl();
        assertThat(url).as("连接时区须与技术方案 §4.1「所有时间字段存 UTC」一致").contains(CONNECTION_TIME_ZONE);
        assertThat(url).as("缺此项则会话仍用服务器本地时区，CURRENT_TIMESTAMP 会写成本地墙钟")
                .contains(FORCE_SESSION_TIME_ZONE);
    }

    @Test
    @DisplayName("不得退回旧别名 serverTimezone——它与 connectionTimeZone 同义，只改一处会留下隐患")
    void doesNotUseLegacyAlias() {
        assertThat(datasourceUrl()).doesNotContain("serverTimezone=");
    }
}
