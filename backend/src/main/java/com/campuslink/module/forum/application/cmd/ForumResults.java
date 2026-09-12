package com.campuslink.module.forum.application.cmd;

import java.time.Instant;

/**
 * 论坛用例出参（Command 模式，与 account 的 {@code AccountCommands} 同规矩：入参 / 出参都是应用层的稳定接口）。
 *
 * <p>查询类出参不直接复用 web 的 VO：web 依赖 application，反向依赖会成环（ADR-012 四层单向）。
 * 写操作的两个出参只有 id，沿用 account 的 {@code RosterImportResult} 先例直接作为响应体。
 */
public final class ForumResults {

    private ForumResults() {
    }

    /** 列表项（设计 §3.2）：summary 为服务端去 Markdown 后的纯文本摘要 */
    public record PostSummary(Long id, String boardCode, String boardName, String title, String authorNickname,
                              int replyCount, int likeCount, String summary, Instant createdAt) {
    }

    /** 详情（设计 §3.4）：contentHtml 是发布时渲染好的 HTML，前端直接渲染 */
    public record PostDetail(Long id, String boardCode, String boardName, String title, String contentHtml,
                             String authorNickname, int replyCount, int likeCount, boolean accepted,
                             Instant createdAt) {
    }

    /** 楼层项（设计 §3.5） */
    public record ReplyItem(Long id, int floorNo, String contentHtml, String authorNickname, Instant createdAt) {
    }

    /** 发帖结果：只回 id，前端据此跳详情（设计 §3.3） */
    public record PublishedPost(Long id) {
    }

    /** 回帖结果：floorNo 为本次分配的楼层号（设计 §3.6，口径见 sprint-2-design §4.1） */
    public record PublishedReply(Long id, int floorNo) {
    }
}
