package com.campuslink.module.account.web;

import com.campuslink.module.account.domain.model.Account;

import java.time.Instant;

/**
 * 用户对外视图：只含展示字段；学号 / 邮箱等敏感字段（含加密与哈希形态）一律不出接口。
 *
 * <p>{@code status} 只在"本人视角"的两个端点上出现（F-ACC-007d 供设置页回显"正常 / 已封禁"）——
 * 登录与注册响应同样经本 VO，那时它是自己给自己的状态，不构成外泄面。
 * 刻意**不加** {@code deleteAt} / {@code anonymized}：那是 F-ACC-003 的语义，为不存在的状态预先扩契约
 * 等于制造一个恒为 null 的对外字段。
 */
public record UserVo(Long id, String nickname, String avatarUrl, String school, String major, String grade,
                     String bio, String role, String status, boolean verified, Instant createdAt) {

    public static UserVo from(Account account) {
        return new UserVo(account.getId(), account.getNickname(), account.getAvatarUrl(), account.getSchool(),
                account.getMajor(), account.getGrade(), account.getBio(), account.getRole().name(),
                account.getStatus().name(), account.isVerified(), account.getCreatedAt());
    }
}
