package com.campuslink.module.forum.domain.model;

import java.util.Locale;
import java.util.Optional;

/**
 * 帖子列表排序口径（F-FORUM-003）：{@link #LATEST} 最新（created_at DESC）、{@link #HOT} 热门（hot_score DESC）。
 *
 * <p>用枚举而非裸 String 穿层：wire 值只在 application 侧解析一次，端口与仓储只见枚举，
 * 避免 {@code "latest"} / {@code "LATEST"} / {@code "new"} 之类的别名在各层各自解释。
 */
public enum PostSortOrder {

    /** 最新：与引入热榜之前完全一致的排序（缺省值） */
    LATEST("latest"),

    /** 热门：按 hot_score 倒序，并列时退化到最新序以保证翻页稳定 */
    HOT("hot");

    private final String code;

    PostSortOrder(String code) {
        this.code = code;
    }

    /**
     * 解析 wire 值：去首尾空白 + 大小写不敏感。null 与不识别的值一律 {@link Optional#empty()}，
     * **不静默归一到 LATEST**——报错口径由调用方决定（与同端点族 {@code days} 只受理 7/30/90 一致）。
     */
    public static Optional<PostSortOrder> fromCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        String normalized = code.trim().toLowerCase(Locale.ROOT);
        for (PostSortOrder order : values()) {
            if (order.code.equals(normalized)) {
                return Optional.of(order);
            }
        }
        return Optional.empty();
    }
}
