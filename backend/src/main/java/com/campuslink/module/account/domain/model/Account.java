package com.campuslink.module.account.domain.model;

import lombok.Getter;

import java.time.Instant;

/**
 * 账号聚合根（DDD 充血模型）：账号生命周期规则收敛在聚合内。
 * 明文邮箱 / 学号仅在创建瞬间经过聚合，持久化形态（_enc / _hash）由仓储适配器托管——
 * 聚合重建时不解密敏感字段亦可满足全部用例（见 AccountConverter）。
 */
@Getter
public class Account {

    private Long id;
    private final EmailAddress email;
    private final StudentId studentId;
    private final String nickname;
    private String avatarUrl;
    private String school;
    private String major;
    private String grade;
    private String bio;
    private AccountRole role;
    private AccountStatus status;
    private final boolean verified;
    private boolean anonymized;
    private Instant deleteAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private Account(Long id, EmailAddress email, StudentId studentId, String nickname, String avatarUrl,
                    String school, String major, String grade, String bio, AccountRole role, AccountStatus status,
                    boolean verified, boolean anonymized, Instant deleteAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.studentId = studentId;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.school = school;
        this.major = major;
        this.grade = grade;
        this.bio = bio;
        this.role = role;
        this.status = status;
        this.verified = verified;
        this.anonymized = anonymized;
        this.deleteAt = deleteAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 工厂方法：注册即已完成学籍核验的新账号（F-ACC-004 是注册前置，聚合不存在“未核验”态） */
    public static Account registered(EmailAddress email, StudentId studentId, String nickname) {
        return new Account(null, email, studentId, nickname.trim(), null, null, null, null, null,
                AccountRole.USER, AccountStatus.ACTIVE, true, false, null, null, null);
    }

    /** 仓储重建入口（infrastructure 适配器调用） */
    public static Account rehydrate(Long id, EmailAddress email, StudentId studentId, String nickname,
                                    String avatarUrl, String school, String major, String grade, String bio,
                                    AccountRole role, AccountStatus status, boolean verified, boolean anonymized,
                                    Instant deleteAt, Instant createdAt, Instant updatedAt) {
        return new Account(id, email, studentId, nickname, avatarUrl, school, major, grade, bio,
                role, status, verified, anonymized, deleteAt, createdAt, updatedAt);
    }

    /**
     * 工厂方法：预置管理员账号（开发期种子 / 运维初始化）。
     * 管理员是运营或校方人员、不是学生，因此**无学号**、也不走学籍核验——
     * 这是 {@link #registered} 之外唯一的建号入口，role 显式给定，不依赖注册链路。
     */
    public static Account provisioned(EmailAddress email, String nickname, AccountRole role) {
        return new Account(null, email, null, nickname.trim(), null, null, null, null, null,
                role, AccountStatus.ACTIVE, true, false, null, null, null);
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
}
