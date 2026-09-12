package com.campuslink.module.forum.infrastructure.persistence;

import com.campuslink.module.forum.domain.model.Board;
import com.campuslink.module.forum.domain.model.BoardType;

/** 防腐层：boards 表 DO ↔ 版块模型互转（参考数据只读，无需反向写回） */
public final class BoardConverter {

    private BoardConverter() {
    }

    public static Board toDomain(BoardDO d) {
        return Board.rehydrate(d.getId(), d.getCode(), d.getName(), d.getDescription(),
                BoardType.valueOf(d.getType()), d.getSort());
    }
}
