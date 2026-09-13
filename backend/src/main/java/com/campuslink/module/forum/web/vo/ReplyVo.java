package com.campuslink.module.forum.web.vo;

import com.campuslink.module.forum.application.cmd.ForumResults.ReplyItem;

import java.time.Instant;

/** 楼层视图（设计 §3.5）：floorNo = 回复序号（第一条回复为 1 楼，帖子本体不占楼层号）；authorId / accepted 供采纳按钮与已采纳标识（F-QA-001） */
public record ReplyVo(Long id, int floorNo, String contentHtml, long authorId, String authorNickname,
                      boolean accepted, Instant createdAt) {

    public static ReplyVo from(ReplyItem item) {
        return new ReplyVo(item.id(), item.floorNo(), item.contentHtml(), item.authorId(),
                item.authorNickname(), item.accepted(), item.createdAt());
    }
}
