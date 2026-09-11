package com.campuslink.module.account.infrastructure.listener;

import com.campuslink.common.audit.AuditService;
import com.campuslink.module.account.domain.event.AccountRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 观察者：注册成功领域事件的审计消费方。
 * AFTER_COMMIT 阶段执行——审计失败不影响已提交的注册事务；未来新增消费方（通知 / 埋点）零侵入。
 */
@Component
@RequiredArgsConstructor
public class RegistrationAuditListener {

    private final AuditService auditService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAccountRegistered(AccountRegisteredEvent event) {
        auditService.record(event.accountId(), "ACCOUNT_REGISTERED", "users", event.accountId(),
                "nickname=" + event.nickname());
    }
}
