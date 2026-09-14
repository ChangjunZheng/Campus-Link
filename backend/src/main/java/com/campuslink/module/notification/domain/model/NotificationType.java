package com.campuslink.module.notification.domain.model;

/**
 * 通知类型（F-SOC-001）。{@code code} 是 {@code notifications.type} 列的存值，取值为小写词表
 * （V1 DDL 注释 {@code reply/like/favorite/accept/quote}），与 {@code likes.target_type} 的大写枚举名
 * 分属两套词表，不要混用。
 *
 * <p>{@code QUOTE}（回复被引用）未实现——引用回复不在 MVP，故本枚举不列该值，
 * 待 F-FORUM-009 引用功能落地时再加。
 */
public enum NotificationType {

    REPLY("reply"),
    LIKE("like"),
    FAVORITE("favorite"),
    ACCEPT("accept");

    private final String code;

    NotificationType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知通知类型: " + code);
    }
}
