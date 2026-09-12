package com.campuslink.module.forum.web.vo;

import com.campuslink.module.forum.application.cmd.ForumResults.PostDetail;

import java.time.Instant;

/** 帖子详情视图（设计 §3.4）：contentHtml 已在服务端渲染并净化，前端直接 v-html（XSS 防线在 MarkdownRenderer） */
public record PostDetailVo(Long id, String boardCode, String boardName, String title, String contentHtml,
                           String authorNickname, int replyCount, int likeCount, boolean accepted,
                           Instant createdAt) {

    public static PostDetailVo from(PostDetail detail) {
        return new PostDetailVo(detail.id(), detail.boardCode(), detail.boardName(), detail.title(),
                detail.contentHtml(), detail.authorNickname(), detail.replyCount(), detail.likeCount(),
                detail.accepted(), detail.createdAt());
    }
}
