package com.campuslink.module.forum.domain.model;

import java.time.Instant;

/**
 * "我的回帖"窄读载体（F-ACC-007c / CR-071）：一条楼层 + 它的父帖定位信息。
 *
 * <p>为什么用独立载体而不是 {@link Reply} 聚合（先例同 {@link HotScoreInput}）：
 * ① {@code Reply} 刻意不带 {@code status}——无业务规则消费它，为列表给聚合加字段会污染聚合语义；
 * ② 父帖标题不属于楼层聚合，却必须在**同一条 SQL** 里与父帖的墓碑 / status 一起判定
 * （父帖不可见 ⇒ 整条不出现），拆开两次查询则 {@code total} 与查询条件必然不同源。
 */
public record MyReplyRow(Long id,
                         Long postId,
                         String postTitle,
                         int floorNo,
                         String contentMd,
                         ReplyStatus status,
                         Instant createdAt) {
}
