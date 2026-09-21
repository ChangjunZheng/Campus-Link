package com.campuslink.module.account.application;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ResultCode;
import com.campuslink.config.AppProperties;
import com.campuslink.module.account.application.cmd.AccountCommands.LoginCommand;
import com.campuslink.module.account.application.cmd.AccountCommands.LoginResult;
import com.campuslink.module.account.application.cmd.AccountCommands.RegisterCommand;
import com.campuslink.module.account.application.cmd.AccountCommands.VerifyStudentCommand;
import com.campuslink.module.account.application.cmd.AccountCommands.VerifyStudentResult;
import com.campuslink.module.account.domain.event.AccountRegisteredEvent;
import com.campuslink.module.account.domain.exception.AccountConflictException;
import com.campuslink.module.account.domain.gateway.AccountRepository;
import com.campuslink.module.account.domain.gateway.RateLimitGateway;
import com.campuslink.module.account.domain.gateway.RosterGateway;
import com.campuslink.module.account.domain.gateway.SensitiveCodec;
import com.campuslink.module.account.domain.gateway.TokenIssuer;
import com.campuslink.module.account.domain.gateway.VerificationTicketStore;
import com.campuslink.module.account.domain.model.Account;
import com.campuslink.module.account.domain.model.AccountStatus;
import com.campuslink.module.account.domain.model.EmailAddress;
import com.campuslink.module.account.domain.model.StudentId;
import com.campuslink.module.account.domain.service.StudentVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 账号用例编排（应用层）：核验 → 注册 → 登录。
 * 只依赖 domain 端口与领域服务，不接触 MyBatis-Plus / Redis 细节（端口-适配器，DIP）。
 * 对外 API 契约与重构前完全一致（路径 / 响应结构不变）。
 */
@Service
@RequiredArgsConstructor
public class AccountApplicationService {

    private final StudentVerificationService verificationService;
    private final AccountRepository accountRepository;
    private final RosterGateway rosterGateway;
    private final VerificationTicketStore ticketStore;
    private final CaptchaService captchaService;
    private final SensitiveCodec codec;
    private final TokenIssuer tokenIssuer;
    private final RateLimitGateway rateLimitGateway;
    private final AppProperties props;
    private final ApplicationEventPublisher eventPublisher;

    /** 学籍核验（F-ACC-004）：IP 限流 → 比对 → 签发一次性票据（payload 为加密学号） */
    public VerifyStudentResult verifyStudent(VerifyStudentCommand command, String ip) {
        long hits = rateLimitGateway.hitAndCount("verify:ip:" + ip, Duration.ofHours(1));
        if (hits > props.getVerify().getIpHourlyLimit()) {
            throw new ApiException(ResultCode.VERIFY_RATE_LIMITED);
        }
        StudentId studentId = StudentId.of(command.studentId());
        if (!verificationService.matches(studentId, command.name())) {
            // 统一失败提示：学号不存在 / 姓名不匹配 / 已注册不区分（防名册枚举）
            throw new ApiException(ResultCode.STUDENT_VERIFY_FAILED);
        }
        String ticket = ticketStore.issue(codec.encrypt(studentId.value()),
                Duration.ofMinutes(props.getVerify().getTicketTtlMinutes()));
        return new VerifyStudentResult(ticket);
    }

    /** 注册（F-ACC-001 + F-ACC-004）：验证码 → 消费票据 → 建号 → 占用名册学号（事务） → 发布领域事件 */
    @Transactional
    public LoginResult register(RegisterCommand command) {
        EmailAddress email = EmailAddress.of(command.email());
        if (!captchaService.verify(email.value(), command.code())) {
            throw new ApiException(ResultCode.CAPTCHA_INVALID);
        }
        // 票据载荷是加密后的学号（VerificationTicketStore 约定：不含明文学号），此处必须解密还原
        String studentId = ticketStore.consume(command.ticket())
                .map(codec::decrypt)
                .orElseThrow(() -> new ApiException(ResultCode.VERIFY_TICKET_INVALID));
        if (accountRepository.existsByEmailHash(codec.hash(email.value()))) {
            throw new ApiException(ResultCode.EMAIL_EXISTS);
        }

        Account account;
        try {
            account = accountRepository.save(Account.registered(email, StudentId.of(studentId), command.nickname()));
        } catch (AccountConflictException e) {
            // 邮箱冲突可明确提示（邮箱非名册信息）；学号冲突必须与其它核验失败**同提示**，
            // 否则可据提示差异枚举出"哪些学号在名册中且已被占用"（PRD F-ACC-004 防名册枚举）
            throw new ApiException(e.field() == AccountConflictException.Field.EMAIL
                    ? ResultCode.EMAIL_EXISTS
                    : ResultCode.STUDENT_VERIFY_FAILED);
        }
        if (!rosterGateway.occupy(codec.hash(studentId), account.getId())) {
            // 并发注册占用冲突：抛出使整个事务回滚（含刚插入的账号）
            throw new ApiException(ResultCode.STUDENT_VERIFY_FAILED);
        }

        captchaService.consume(email.value());
        eventPublisher.publishEvent(new AccountRegisteredEvent(account.getId(), account.getNickname(), Instant.now()));
        return toResult(account);
    }

    /** 登录：统一提示不区分“账号不存在”与“验证码错误”；封禁账号拒绝 */
    public LoginResult login(LoginCommand command) {
        EmailAddress email = EmailAddress.of(command.email());
        Account account = accountRepository.findByEmailHash(codec.hash(email.value())).orElse(null);
        if (account == null || !captchaService.verify(email.value(), command.code())) {
            throw new ApiException(ResultCode.LOGIN_FAILED);
        }
        if (!account.isActive()) {
            throw new ApiException(ResultCode.USER_BANNED);
        }
        captchaService.consume(email.value());
        return toResult(account);
    }

    private LoginResult toResult(Account account) {
        String token = tokenIssuer.issue(account.getId(), account.getRole().name());
        long expiresAt = Instant.now().plusSeconds(tokenIssuer.ttlSeconds()).getEpochSecond();
        return new LoginResult(token, expiresAt, account);
    }

    /**
     * 批量取昵称：其他上下文（forum）展示作者名的唯一入口（ADR-012 跨上下文只调对方 application）。
     * 一次查询取齐整页作者，不做 N+1；查不到的 id 不出现在结果中，由调用方决定回落文案。
     */
    public Map<Long, String> nicknamesOf(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return accountRepository.findByIds(ids).stream()
                .collect(Collectors.toMap(Account::getId, Account::getNickname));
    }

    /**
     * 按 id 取账号（只读）：web 层展示当前用户资料的入口。
     * 存在意义是 R-1 裁决（CR-028）：web 不得注入 domain 端口，只允许经 application。
     */
    public Account accountOf(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException(ResultCode.USER_NOT_FOUND));
    }

    /**
     * 按 id 取**可公开露出**的账号（他人主页 {@code /u/:id} 与它的公开帖子列表共用这一道门）：
     * 不存在与已注销（{@code DEACTIVATED}）一律 2007 / 404，两种情形对外不区分——与帖子侧
     * {@code requireVisiblePost} 把「不存在 / 已删 / 未发布」全归 3001 是同一条纪律：不给按 id 探测账号状态的口子。
     *
     * <p>与 {@link #accountOf} 的差别只多一道注销门，不合并成一个方法是因为两者的调用方语义不同：
     * {@code accountOf} 答「这个 id 有没有对应账号」（关注 / 令牌校验用，注销中的账号仍是关注关系的一方），
     * 本方法答「这个账号能不能被路人看见」。
     *
     * <p>{@code BANNED} **不在这里拦**：封禁限的是发言与登录，不是把已发布内容从站内抹掉
     * （全站列表 / 搜索 / 热榜从不按作者状态过滤）；主页若单独消失，列表里那个作者名就点不开了。
     */
    public Account publicAccountOf(long accountId) {
        Account account = accountOf(accountId);
        if (account.getStatus() == AccountStatus.DEACTIVATED) {
            throw new ApiException(ResultCode.USER_NOT_FOUND);
        }
        return account;
    }
}
