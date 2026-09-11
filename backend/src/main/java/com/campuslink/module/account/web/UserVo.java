package com.campuslink.module.account.web;

import com.campuslink.module.account.domain.model.Account;

import java.time.Instant;

/**
 * 用户对外视图：只含展示字段；学号 / 邮箱等敏感字段（含加密与哈希形态）一律不出接口。
 */
public record UserVo(Long id, String nickname, String avatarUrl, String school, String major, String grade,
                     String bio, String role, boolean verified, Instant createdAt) {

    public static UserVo from(Account account) {
        return new UserVo(account.getId(), account.getNickname(), account.getAvatarUrl(), account.getSchool(),
                account.getMajor(), account.getGrade(), account.getBio(), account.getRole().name(),
                account.isVerified(), account.getCreatedAt());
    }
}
