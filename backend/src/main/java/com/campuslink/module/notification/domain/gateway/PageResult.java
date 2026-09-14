package com.campuslink.module.notification.domain.gateway;

import java.util.List;

/**
 * 端口层分页契约。与 forum 的同名类型**各自独立**而非共用：跨上下文只允许依赖对方 {@code application} 包
 * （ADR-012 / 守护测试 G4），把分页载体放任何一方都会让另一方越界引用 domain。
 */
public record PageResult<T>(List<T> items, long total, int page, int size) {
}
