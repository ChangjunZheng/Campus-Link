package com.campuslink.module.forum.domain.model;

/**
 * 帖子状态：{@code PUBLISHED} 由发帖产生；{@code REMOVED} 由**平台侧内容处置**写入
 * （CR-066 的 {@code AdminModerationController}，未来的机审 F-SAFE-001 同样写这一列）。
 *
 * <p>与 {@code posts.is_deleted} 是两列两义：后者是**作者自助删除**的行级墓碑（不可自助恢复），
 * 前者可恢复、且审计要答得出"是谁删的"。合并成一列就再也分不开了。
 */
public enum PostStatus {
    PUBLISHED, REMOVED
}
