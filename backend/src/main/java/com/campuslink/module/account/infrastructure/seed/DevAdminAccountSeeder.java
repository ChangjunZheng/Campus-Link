package com.campuslink.module.account.infrastructure.seed;

import com.campuslink.module.account.domain.gateway.AccountRepository;
import com.campuslink.module.account.domain.gateway.SensitiveCodec;
import com.campuslink.module.account.domain.model.Account;
import com.campuslink.module.account.domain.model.AccountRole;
import com.campuslink.module.account.domain.model.EmailAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 开发期种子：预置管理员账号（CR-013）。
 *
 * <p>与内置测试名册同一门控（{@code campuslink.roster.bypass=true}）——bypass 为 false 时不装配，
 * 即生产环境不存在该账号。管理员不是学生，无学号、不走学籍核验，直接以 SUPERADMIN 建号，
 * 用于联调 {@code POST /api/v1/admin/roster/import}（该接口仅 SUPERADMIN 可调用）。
 *
 * <p>幂等：按邮箱哈希判存，重启不会重复插入。登录方式与普通用户一致（邮箱 + 验证码）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "campuslink.roster.bypass", havingValue = "true", matchIfMissing = true)
public class DevAdminAccountSeeder implements ApplicationRunner {

    private static final String ADMIN_EMAIL = "admin@campuslink.local";
    private static final String ADMIN_NICKNAME = "管理员";

    private final AccountRepository accountRepository;
    private final SensitiveCodec codec;

    @Override
    public void run(ApplicationArguments args) {
        String emailHash = codec.hash(EmailAddress.of(ADMIN_EMAIL).value());
        if (accountRepository.existsByEmailHash(emailHash)) {
            log.info("[DEV] 管理员账号已存在，跳过种子：{}", ADMIN_EMAIL);
            return;
        }
        Account admin = Account.provisioned(
                EmailAddress.of(ADMIN_EMAIL), ADMIN_NICKNAME, AccountRole.SUPERADMIN);
        accountRepository.save(admin);
        log.warn("[DEV] 已预置管理员账号 {}（SUPERADMIN，无学号）——仅开发期存在，bypass=false 时不会创建", ADMIN_EMAIL);
    }
}
