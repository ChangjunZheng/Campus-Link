package com.campuslink.module.forum.infrastructure.persistence;

import com.campuslink.module.forum.domain.model.Reply;

/** 防腐层：replies 表 DO ↔ 回复聚合互转（quoted_reply_id / like_count 不属本 Sprint 模型，留数据库默认值） */
public final class ReplyConverter {

    private ReplyConverter() {
    }

    public static ReplyDO toDo(Reply reply) {
        ReplyDO d = new ReplyDO();
        d.setId(reply.getId());
        d.setPostId(reply.getPostId());
        d.setAuthorId(reply.getAuthorId());
        d.setFloorNo(reply.getFloorNo());
        d.setContentMd(reply.getContentMd());
        d.setContentHtml(reply.getContentHtml());
        d.setIsAccepted(reply.isAccepted());
        d.setCreatedAt(reply.getCreatedAt());
        return d;
    }

    public static Reply toDomain(ReplyDO d) {
        return Reply.rehydrate(d.getId(), d.getPostId(), d.getAuthorId(), d.getFloorNo(),
                d.getContentMd(), d.getContentHtml(), Boolean.TRUE.equals(d.getIsAccepted()), d.getCreatedAt());
    }
}
