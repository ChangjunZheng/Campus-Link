package com.campuslink.module.account.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.module.account.domain.exception.AccountConflictException;
import com.campuslink.module.account.domain.gateway.AccountRepository;
import com.campuslink.module.account.domain.gateway.SensitiveCodec;
import com.campuslink.module.account.domain.model.Account;
import com.campuslink.module.account.infrastructure.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 适配器：AccountRepository 端口的 MyBatis-Plus 实现（适配器模式） */
@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    /** 唯一索引名取自 db/migration/V1__init_schema.sql，用于识别冲突字段 */
    private static final String UK_STUDENT_ID = "uk_users_student_id_hash";
    private static final String UK_EMAIL = "uk_users_email_hash";

    private final UserMapper userMapper;
    private final SensitiveCodec codec;

    @Override
    public Account save(Account account) {
        UserDO d = AccountConverter.toDo(account, codec);
        try {
            userMapper.insert(d);
        } catch (DuplicateKeyException e) {
            throw translateConflict(e);
        }
        return AccountConverter.toDomain(d, codec);
    }

    /** 端口约定：把数据库唯一键冲突翻译为领域异常，应用层据此给出业务提示（见 AccountConflictException） */
    private static RuntimeException translateConflict(DuplicateKeyException e) {
        String message = e.getMessage() == null ? "" : e.getMessage();
        if (message.contains(UK_STUDENT_ID)) {
            return new AccountConflictException(AccountConflictException.Field.STUDENT_ID, "学号已被占用", e);
        }
        if (message.contains(UK_EMAIL)) {
            return new AccountConflictException(AccountConflictException.Field.EMAIL, "邮箱已被占用", e);
        }
        // 未预期的约束冲突：不猜测业务语义，原样抛出按系统异常处理
        return e;
    }

    @Override
    public boolean existsByEmailHash(String emailHash) {
        return userMapper.selectCount(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getEmailHash, emailHash)) > 0;
    }

    @Override
    public Optional<Account> findByEmailHash(String emailHash) {
        return Optional.ofNullable(userMapper.selectOne(new LambdaQueryWrapper<UserDO>()
                        .eq(UserDO::getEmailHash, emailHash)))
                .map(d -> AccountConverter.toDomain(d, codec));
    }

    @Override
    public Optional<Account> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id))
                .map(d -> AccountConverter.toDomain(d, codec));
    }

    @Override
    public List<Account> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userMapper.selectBatchIds(ids).stream()
                .map(d -> AccountConverter.toDomain(d, codec))
                .toList();
    }
}
