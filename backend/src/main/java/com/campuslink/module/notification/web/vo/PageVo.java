package com.campuslink.module.notification.web.vo;

import java.util.List;

/**
 * 通知分页出参：字段与 forum 的 {@code PageVo} 同名同口径（前端分页组件共用）。
 * 之所以各上下文各留一份而非共用：跨上下文只允许依赖对方 application 包（守护测试 G4），
 * 复用 forum 的 web 层类型即越界。
 */
public record PageVo<T>(List<T> list, long total, int page, int size) {
}
