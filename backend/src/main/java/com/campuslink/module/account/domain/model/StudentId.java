package com.campuslink.module.account.domain.model;

import java.util.Objects;

/**
 * 值对象：学号（trim 规范化）。对外展示一律不出学号明文（PRD F-ACC-004）。
 */
public record StudentId(String value) {

    public StudentId {
        Objects.requireNonNull(value, "studentId 不能为空");
        value = value.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("studentId 不能为空白");
        }
    }

    public static StudentId of(String raw) {
        return new StudentId(raw);
    }
}
