package com.campuslink.module.account.domain.gateway;

/**
 * 出站端口：验证码发送通道（策略模式）。
 * log（开发，仅打日志）/ mail（生产邮件）两个实现按配置条件装配。
 */
public interface CodeSender {

    void send(String target, String code);
}
