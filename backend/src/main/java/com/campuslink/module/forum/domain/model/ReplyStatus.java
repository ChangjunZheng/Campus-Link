package com.campuslink.module.forum.domain.model;

/**
 * 楼层状态：与 {@link PostStatus} 同词表（{@code replies.status} / {@code posts.status} 两列存同样的取值），
 * 但**刻意不共用一个枚举**——两者是分属不同聚合的独立状态机，合并后任何一侧加取值都会污染另一侧。
 *
 * <p>{@code REMOVED} 由内容处置（CR-066 的管理员下架 / 恢复，未来的机审 F-SAFE-001）写入；
 * 作者自助删除走 {@code is_deleted} 行级墓碑，两者语义不可混用。
 */
public enum ReplyStatus {
    PUBLISHED, REMOVED
}
