package com.campuslink.module.forum.domain.gateway;

import java.util.List;

/**
 * 端口层分页契约：只暴露「本页数据 + 过滤条件下总条数 + 实际生效的页码 / 页大小」，
 * 不泄漏 MyBatis-Plus 的 IPage。
 *
 * <p>total 与当前页无关，用于前端渲染总页数；page / size 为**归一化后**的值
 * （应用层把 page 归一到 ≥1、size 归一到 1~100），使响应回显的口径与实际查询一致。
 */
public record PageResult<T>(List<T> items, long total, int page, int size) {
}
