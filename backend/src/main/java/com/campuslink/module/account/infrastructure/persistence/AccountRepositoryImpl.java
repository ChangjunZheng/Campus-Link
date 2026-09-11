package com.campuslink.module.account.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.module.account.domain.gateway.AccountRepository;
import com.campuslink.module.account.domain.gateway.SensitiveCodec;
import com.campuslink.module.account.domain.model.Account;
import com.campuslink.module.account.infrastructure.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** 适配器：AccountRepository 端口的 MyBatis-Plus 实现（适配器模式） */
@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    private final UserMapper userMapper;
    private final SensitiveCodec codec;

    @Override
    public Account save(Account account) {
        UserDO d = AccountConverter.toDo(account, codec);
        userMapper.insert(d);
        return AccountConverter.toDomain(d, codec);
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
}
