package com.campuslink.module.forum.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 封面提取规则回归（CR-074）：只取首张 https 图；http / 非图片链接 / 空正文一律 null */
class CoverImagesTest {

    @Test
    @DisplayName("取首张 https 图；后续图片忽略")
    void firstHttpsWins() {
        String md = "前文\n\n![a](https://a.example/1.png)\n\n![b](https://b.example/2.png)";
        assertThat(CoverImages.firstHttpsImage(md)).isEqualTo("https://a.example/1.png");
    }

    @Test
    @DisplayName("http 图与普通链接不采纳")
    void httpAndPlainLinksRejected() {
        assertThat(CoverImages.firstHttpsImage("![a](http://a.example/1.png)")).isNull();
        assertThat(CoverImages.firstHttpsImage("[文字](https://a.example/x)")).isNull();
    }

    @Test
    @DisplayName("http 在前不拦 https 在后：仍取首张 **https** 图")
    void skipsHttpAndTakesFirstHttps() {
        String md = "![a](http://a.example/1.png)\n\n![b](https://b.example/2.png)";
        assertThat(CoverImages.firstHttpsImage(md)).isEqualTo("https://b.example/2.png");
    }

    @Test
    @DisplayName("空正文 / 无图 → null")
    void nullSafe() {
        assertThat(CoverImages.firstHttpsImage(null)).isNull();
        assertThat(CoverImages.firstHttpsImage("纯文字正文")).isNull();
    }
}
