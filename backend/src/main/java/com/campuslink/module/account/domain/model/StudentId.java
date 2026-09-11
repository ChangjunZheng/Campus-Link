package com.campuslink.module.account.domain.model;

import java.util.Objects;

/**
 * 值对象：学号（trim 规范化 + 9 位数字格式约束）。对外展示一律不出学号明文（PRD F-ACC-004）。
 */
public record StudentId(String value) {

    private static final String FORMAT = "\\d{9}";

    public StudentId {
        Objects.requireNonNull(value, "studentId 不能为空");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("studentId 不能为空白");
        }
        if (!value.matches(FORMAT)) {
            throw new IllegalArgumentException("studentId 必须为 9 位数字");
        }
    }

    public static StudentId of(String raw) {
        return new StudentId(raw);
    }
}
