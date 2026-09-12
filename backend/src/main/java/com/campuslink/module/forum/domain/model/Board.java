package com.campuslink.module.forum.domain.model;

import lombok.Getter;

/**
 * 版块：随 Flyway V2 种入的参考数据，本 Sprint 只读，故建模为不可变对象。
 * 仅承载展示与路由所需字段——enabled / createdAt / updatedAt 等列由适配器过滤，不进领域模型。
 */
@Getter
public class Board {

    private final Long id;
    private final String code;
    private final String name;
    private final String description;
    private final BoardType type;
    private final int sort;

    private Board(Long id, String code, String name, String description, BoardType type, int sort) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.type = type;
        this.sort = sort;
    }

    /** 仓储重建入口（infrastructure 适配器调用） */
    public static Board rehydrate(Long id, String code, String name, String description, BoardType type, int sort) {
        return new Board(id, code, name, description, type, sort);
    }
}
