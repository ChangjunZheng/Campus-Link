package com.campuslink.module.forum.domain.gateway;

import com.campuslink.module.forum.domain.model.Reply;

/** 回复仓储端口（端口定义在 domain、实现在 infrastructure，DIP） */
public interface ReplyRepository {

    /** 某帖的楼层分页：固定 {@code status='PUBLISHED' AND is_deleted=0}，按 floor_no ASC（设计 §3.5） */
    PageResult<Reply> findPageByPostId(Long postId, int page, int size);

    /** 新增回复（INSERT）；返回带数据库生成 id 与时间戳的聚合 */
    Reply save(Reply reply);
}
