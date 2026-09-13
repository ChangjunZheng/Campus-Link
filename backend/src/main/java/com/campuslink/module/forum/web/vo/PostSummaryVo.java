package com.campuslink.module.forum.web.vo;

import com.campuslink.module.forum.application.cmd.ForumResults.PostSummary;

import java.time.Instant;

/** 帖子列表项视图（设计 §3.2）：summary 已是服务端去 Markdown 后的纯文本，前端不再兜底截断；accepted 供 [已采纳] 徽标（F-QA-001） */
public record PostSummaryVo(Long id, String boardCode, String boardName, String title, String authorNickname,
                            int replyCount, int likeCount, String summary, Instant createdAt, boolean accepted) {

    public static PostSummaryVo from(PostSummary summary) {
        return new PostSummaryVo(summary.id(), summary.boardCode(), summary.boardName(), summary.title(),
                summary.authorNickname(), summary.replyCount(), summary.likeCount(), summary.summary(),
                summary.createdAt(), summary.accepted());
    }
}
