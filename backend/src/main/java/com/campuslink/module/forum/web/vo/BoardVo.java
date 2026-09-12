package com.campuslink.module.forum.web.vo;

import com.campuslink.module.forum.domain.model.Board;

/** 版块视图（设计 §3.1）：只含展示与路由所需字段 */
public record BoardVo(String code, String name, String description, String type, int sort) {

    public static BoardVo from(Board board) {
        return new BoardVo(board.getCode(), board.getName(), board.getDescription(),
                board.getType().name(), board.getSort());
    }
}
