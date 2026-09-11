package com.campuslink.module.account.domain.event;

import java.time.Instant;

/**
 * 领域事件：账号注册成功（观察者模式的发布侧）。
 * 当前消费方：注册审计（AFTER_COMMIT）；未来扩展：欢迎通知、埋点上报，主流程零改动。
 */
public record AccountRegisteredEvent(Long accountId, String nickname, Instant occurredAt) {
}
