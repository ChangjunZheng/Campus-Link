package com.campuslink.module.account.domain.model;

import java.util.Objects;

/**
 * 值对象：规范化邮箱（trim + 小写），规范化后相等即业务相等。
 * 明文仅在聚合内短暂存在，出聚合即由 SensitiveCodec 托管为 _enc / _hash。
 */
public record EmailAddress(String value) {

    public EmailAddress {
        Objects.requireNonNull(value, "email 不能为空");
        value = value.trim().toLowerCase();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("email 不能为空白");
        }
    }

    public static EmailAddress of(String raw) {
        return new EmailAddress(raw);
    }
}
