package com.campuslink.module.forum.domain.model;

import com.campuslink.module.forum.domain.exception.CannotAcceptOwnReplyException;
import com.campuslink.module.forum.domain.exception.NotAQuestionException;
import lombok.Getter;

import java.time.Instant;

/**
 * 帖子聚合根。contentHtml 在**发布时**由 application 调用 MarkdownRenderer 渲染后传入并落库，
 * 请求时零渲染（ADR-005）——聚合自身不感知 Markdown 实现。
 *
 * <p>本 Sprint 未建模 tags / hotScore / favoriteCount 等列：无用例读写它们，
 * 且除采纳用例外不存在整行 UPDATE（回复计数走 {@code PostRepository#incrementReplyCountAndGet} 的定向 SQL），
 * 因此不会因字段缺失而丢数据。采纳用例同样走定向 UPDATE（见 {@code PostRepository#updateAcceptedReply}），
 * {@link #acceptReply} 返回的新聚合只用于规则表达，不回写整行。
 */
@Getter
public class Post {

    private final Long id;
    private final Long boardId;
    private final Long authorId;
    private final BoardType type;
    private final String title;
    private final String contentMd;
    private final String contentHtml;
    private final PostStatus status;
    private final int replyCount;
    private final int likeCount;
    private final boolean accepted;
    private final Long acceptedReplyId;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Post(Long id, Long boardId, Long authorId, BoardType type, String title,
                 String contentMd, String contentHtml, PostStatus status, int replyCount, int likeCount,
                 boolean accepted, Long acceptedReplyId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.boardId = boardId;
        this.authorId = authorId;
        this.type = type;
        this.title = title;
        this.contentMd = contentMd;
        this.contentHtml = contentHtml;
        this.status = status;
        this.replyCount = replyCount;
        this.likeCount = likeCount;
        this.accepted = accepted;
        this.acceptedReplyId = acceptedReplyId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 发布新帖：type 随版块，status 恒为 PUBLISHED，计数归零（id / 时间戳由数据库生成）；标题去除首尾空白 */
    public static Post publish(Long boardId, Long authorId, BoardType type, String title,
                               String contentMd, String contentHtml) {
        return new Post(null, boardId, authorId, type, title.trim(), contentMd, contentHtml,
                PostStatus.PUBLISHED, 0, 0, false, null, null, null);
    }

    /** 仓储重建入口（infrastructure 适配器调用）；已删除的行由仓储读侧过滤，不会到达这里 */
    public static Post rehydrate(Long id, Long boardId, Long authorId, BoardType type, String title,
                                 String contentMd, String contentHtml, PostStatus status, int replyCount,
                                 int likeCount, boolean accepted, Long acceptedReplyId,
                                 Instant createdAt, Instant updatedAt) {
        return new Post(id, boardId, authorId, type, title, contentMd, contentHtml, status,
                replyCount, likeCount, accepted, acceptedReplyId, createdAt, updatedAt);
    }

    /** 对外可见性：仅 PUBLISHED 可读 / 可回帖（已删除属仓储读侧约定，见 PostRepository） */
    public boolean isVisible() {
        return status == PostStatus.PUBLISHED;
    }

    /**
     * 采纳最佳答案（F-QA-001）的领域规则：仅问答帖（QUESTION）可采纳；提问者不能采纳自己的回复。
     * 可更换（后一次覆盖前一次）与回复归属校验归 application（后者是仓储读得到的跨聚合事实）。
     */
    public Post acceptReply(long replyId, long replyAuthorId) {
        if (type != BoardType.QUESTION) {
            throw new NotAQuestionException();
        }
        if (authorId != null && authorId == replyAuthorId) {
            throw new CannotAcceptOwnReplyException();
        }
        return new Post(id, boardId, authorId, type, title, contentMd, contentHtml, status,
                replyCount, likeCount, true, replyId, createdAt, updatedAt);
    }
}
