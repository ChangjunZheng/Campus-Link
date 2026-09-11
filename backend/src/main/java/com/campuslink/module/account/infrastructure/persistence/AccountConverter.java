package com.campuslink.module.account.infrastructure.persistence;

import com.campuslink.module.account.domain.gateway.SensitiveCodec;
import com.campuslink.module.account.domain.model.Account;
import com.campuslink.module.account.domain.model.AccountRole;
import com.campuslink.module.account.domain.model.AccountStatus;
import com.campuslink.module.account.domain.model.EmailAddress;
import com.campuslink.module.account.domain.model.StudentId;

/**
 * 防腐层（Anti-Corruption Layer）：MyBatis-Plus DO ↔ 账号聚合互转。
 * 明文仅在转换瞬间经过 SensitiveCodec 托管为 _enc / _hash；聚合重建时按需解密还原值对象。
 */
public final class AccountConverter {

    private AccountConverter() {
    }

    public static UserDO toDo(Account account, SensitiveCodec codec) {
        UserDO d = new UserDO();
        d.setId(account.getId());
        d.setEmailEnc(codec.encrypt(account.getEmail().value()));
        d.setEmailHash(codec.hash(account.getEmail().value()));
        d.setNickname(account.getNickname());
        d.setAvatarUrl(account.getAvatarUrl());
        d.setSchool(account.getSchool());
        d.setMajor(account.getMajor());
        d.setGrade(account.getGrade());
        d.setBio(account.getBio());
        d.setRole(account.getRole().name());
        d.setStatus(account.getStatus().name());
        d.setVerified(account.isVerified());
        d.setStudentIdEnc(codec.encrypt(account.getStudentId().value()));
        d.setStudentIdHash(codec.hash(account.getStudentId().value()));
        d.setAnonymized(account.isAnonymized());
        d.setDeleteAt(account.getDeleteAt());
        d.setCreatedAt(account.getCreatedAt());
        d.setUpdatedAt(account.getUpdatedAt());
        return d;
    }

    public static Account toDomain(UserDO d, SensitiveCodec codec) {
        return Account.rehydrate(
                d.getId(),
                EmailAddress.of(codec.decrypt(d.getEmailEnc())),
                StudentId.of(codec.decrypt(d.getStudentIdEnc())),
                d.getNickname(),
                d.getAvatarUrl(),
                d.getSchool(),
                d.getMajor(),
                d.getGrade(),
                d.getBio(),
                AccountRole.valueOf(d.getRole()),
                AccountStatus.valueOf(d.getStatus()),
                Boolean.TRUE.equals(d.getVerified()),
                Boolean.TRUE.equals(d.getAnonymized()),
                d.getDeleteAt(),
                d.getCreatedAt(),
                d.getUpdatedAt());
    }
}
