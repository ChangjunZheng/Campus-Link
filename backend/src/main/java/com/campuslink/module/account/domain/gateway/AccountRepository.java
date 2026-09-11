package com.campuslink.module.account.domain.gateway;

import com.campuslink.module.account.domain.model.Account;

import java.util.Optional;

/**
 * 聚合仓储端口（Repository 模式）：账号聚合的持久化抽象，
 * MyBatis-Plus 实现位于 infrastructure/persistence。
 */
public interface AccountRepository {

    /** 保存新聚合，返回带持久化 id 的聚合实例 */
    Account save(Account account);

    boolean existsByEmailHash(String emailHash);

    Optional<Account> findByEmailHash(String emailHash);

    Optional<Account> findById(Long id);
}
