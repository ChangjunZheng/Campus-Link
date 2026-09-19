package com.campuslink.module.forum.web.vo;

import com.campuslink.module.forum.application.cmd.ForumResults.MyReplyItem;

import java.time.Instant;

/**
 * "我的回帖"条目视图（F-ACC-007c）：{@code postId} + {@code postTitle} + {@code floorNo} 是父帖定位，
 * 缺一样用户就只知道"我答过一层"而答不出"答在哪"。
 *
 * <p>{@code status} 是**楼层自身**的处置状态（父帖的状态不会到这里——父帖不可见的楼层在 SQL 侧就整条不出现）；
 * 与帖子侧同理，**不下发 {@code contentMd} 原文**：本期楼层没有编辑能力，下发原文只是扩大出口。
 */
public record MyReplyVo(Long id, Long postId, String postTitle, int floorNo, String summary,
                       String status, Instant createdAt) {

    public static MyReplyVo from(MyReplyItem item) {
        return new MyReplyVo(item.id(), item.postId(), item.postTitle(), item.floorNo(),
                item.summary(), item.status(), item.createdAt());
    }
}
