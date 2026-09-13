package com.campuslink.module.forum.web.vo;

import com.campuslink.module.forum.application.cmd.ForumResults.PostDetail;

import java.time.Instant;

/** 帖子详情视图（设计 §3.4）：contentHtml 已在服务端渲染并净化，前端直接 v-html（XSS 防线在 MarkdownRenderer）；authorId / boardType 供前端判定采纳按钮可见性（F-QA-001） */
public record PostDetailVo(Long id, String boardCode, String boardName, String boardType, String title,
                           String contentHtml, long authorId, String authorNickname,
                           int replyCount, int likeCount, boolean accepted,
                           Instant createdAt) {

    public static PostDetailVo from(PostDetail detail) {
        return new PostDetailVo(detail.id(), detail.boardCode(), detail.boardName(), detail.boardType(),
                detail.title(), detail.contentHtml(), detail.authorId(), detail.authorNickname(),
                detail.replyCount(), detail.likeCount(), detail.accepted(), detail.createdAt());
    }
}
