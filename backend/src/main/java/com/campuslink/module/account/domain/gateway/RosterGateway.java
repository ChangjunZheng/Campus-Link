package com.campuslink.module.account.domain.gateway;

import com.campuslink.module.account.domain.model.StudentRecord;

import java.util.Optional;

/**
 * 出站端口：学籍名册核验查询与占用（技术方案 4.3）。
 * 双策略实现（策略模式，按 campuslink.roster.bypass 条件装配）：
 * bypass=true → 内置测试名册（开发联调）；false → MySQL 名册表（生产）。
 */
public interface RosterGateway {

    /** 按学号哈希查“未被占用”的名册记录；不存在 / 已占用返回 empty */
    Optional<StudentRecord> findAvailableByHash(String studentIdHash);

    /** 原子占用学号（一号一账号）；bypass 策略下恒为 true */
    boolean occupy(String studentIdHash, Long accountId);
}
