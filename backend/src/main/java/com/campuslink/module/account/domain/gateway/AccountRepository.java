package com.campuslink.module.account.domain.gateway;

import com.campuslink.module.account.domain.exception.AccountConflictException;
import com.campuslink.module.account.domain.model.Account;
import com.campuslink.module.account.domain.model.ProfileField;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 聚合仓储端口（Repository 模式）：账号聚合的持久化抽象，
 * MyBatis-Plus 实现位于 infrastructure/persistence。
 */
public interface AccountRepository {

    /**
     * 保存新聚合，返回带持久化 id 的聚合实例。
     *
     * @throws AccountConflictException 邮箱或学号已被占用（唯一键冲突）。
     *         适配器负责把数据库层异常翻译为该领域异常，应用层无需感知 JDBC / MyBatis 异常类型（DIP）。
     */
    Account save(Account account);

    /**
     * 资料编辑（F-ACC-007a）：**定向单列 UPDATE**，只 SET {@code changed} 里那几列。
     *
     * <p>为什么不复用 {@link #save} 整行回写：聚合是从"读那一刻"的状态改来的，整行写会把
     * {@code role} / {@code status} / {@code email_*} / {@code verified} 一起盖回去——并发窗口内
     * 平台侧对这些列的改动（封禁、名册处置）会被静默抹掉。这条约定与帖子侧
     * {@code updateStatus} / {@code updateHotScore} 走定向 SQL 是同一个理由。
     *
     * <p>空集合由适配器短路（不发 SQL）：幂等提交不该推进 {@code updated_at}。
     */
    void updateProfile(Long accountId, Account account, Set<ProfileField> changed);

    boolean existsByEmailHash(String emailHash);

    Optional<Account> findByEmailHash(String emailHash);

    Optional<Account> findById(Long id);

    /** 批量按 id 查询（跨上下文读昵称的唯一入口，一次查询不做 N+1）；不存在的 id 不返回 */
    List<Account> findByIds(Collection<Long> ids);
}
