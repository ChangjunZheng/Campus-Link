package com.campuslink.module.forum.web.vo;

import java.util.List;

/** 统一分页出参（设计 §3）：page 从 1 起、size 默认 20 / 上限 100，均为**生效后**的值 */
public record PageVo<T>(List<T> list, long total, int page, int size) {
}
