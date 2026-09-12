package com.campuslink.module.forum.domain.gateway;

import com.campuslink.module.forum.domain.model.Board;

import java.util.List;
import java.util.Optional;

/** 版块仓储端口（端口定义在 domain、实现在 infrastructure，DIP） */
public interface BoardRepository {

    /** 启用中的版块，按 sort 升序；固定 6 条，不分页（设计 §3.1） */
    List<Board> findAllEnabled();

    /** 按路由标识查版块；不存在返回 empty，由应用层按「资源不存在」处理 */
    Optional<Board> findByCode(String code);
}
