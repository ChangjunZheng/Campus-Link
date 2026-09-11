package com.campuslink.common.markdown;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * XSS 用例回归（技术方案 7.1 / Sprint 1 出口标准：含 &lt;script&gt; 的 Markdown 不产生 XSS）。
 * 这些用例是论坛安全生命线的回归底线，任何渲染实现调整都必须保持全绿。
 */
class MarkdownRendererTest {

    private final MarkdownRenderer renderer = new MarkdownRenderer();

    @Test
    @DisplayName("script 标签被整体移除")
    void scriptTagRemoved() {
        String out = renderer.render("hello <script>alert(1)</script> world");
        assertThat(out).doesNotContain("<script").doesNotContain("alert(1)");
    }

    @Test
    @DisplayName("javascript: 链接被清除")
    void javascriptHrefRemoved() {
        String out = renderer.render("[click](javascript:alert(1))");
        assertThat(out).doesNotContain("javascript:");
    }

    @Test
    @DisplayName("img 仅允许 https，http 源被清除")
    void imgHttpsOnly() {
        String out = renderer.render("![a](https://a.example/b.png) ![b](http://b.example/d.png)");
        assertThat(out).contains("https://a.example/b.png");
        assertThat(out).doesNotContain("http://b.example/d.png");
    }

    @Test
    @DisplayName("代码块保留 language-* 类（前端 highlight.js 着色输入）")
    void codeLanguageClassPreserved() {
        String out = renderer.render("```java\nSystem.out.println(1);\n```");
        assertThat(out).contains("language-java");
        assertThat(out).contains("<pre><code");
    }

    @Test
    @DisplayName("事件属性被清除")
    void eventAttributesRemoved() {
        String out = renderer.render("<img src=\"https://a.example/b.png\" onerror=\"alert(1)\">");
        assertThat(out).doesNotContain("onerror");
    }

    @Test
    @DisplayName("常规 Markdown 正常保留（标题 / 加粗 / 列表 / 表格）")
    void basicMarkdownPreserved() {
        String out = renderer.render("# 标题\n\n**加粗** 与 *斜体*\n\n- 列表项\n\n| A | B |\n|---|---|\n| 1 | 2 |");
        assertThat(out).contains("<h1>");
        assertThat(out).contains("<strong>");
        assertThat(out).contains("<li>");
        assertThat(out).contains("<table>");
    }
}
