package com.campuslink.module.forum.domain.service;

/**
 * 封面图提取（CR-074，外链方案）：从 Markdown 正文提取首张 **https** 图片的 URL 作为封面。
 * 只认 Markdown 图片语法 {@code ![alt](https://…)}——普通链接、http 源、data:/javascript: 一律不采纳；
 * 纯静态规则、无 IO，提取发生在发布时一次（实施方案 §2），列表读时零成本。
 */
public final class CoverImages {

    private static final int MAX_URL_CHARS = 512;

    private CoverImages() {
    }

    public static String firstHttpsImage(String contentMd) {
        if (contentMd == null || contentMd.isBlank()) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("!\\[[^\\]]*]\\(\\s*(https://[^)\\s]+)\\s*\\)")
                .matcher(contentMd);
        if (!m.find()) {
            return null;
        }
        String url = m.group(1);
        return url.length() > MAX_URL_CHARS ? url.substring(0, MAX_URL_CHARS) : url;
    }
}
