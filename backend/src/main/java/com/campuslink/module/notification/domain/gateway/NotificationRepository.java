package com.campuslink.module.notification.domain.gateway;

import com.campuslink.module.notification.domain.model.Notification;

/** 通知仓储端口（端口定义在 domain、实现在 infrastructure，DIP） */
public interface NotificationRepository {

    /** 新增通知（INSERT）；自增 id 与 created_at 由数据库生成，无人消费故不回传聚合 */
    void save(Notification notification);

    /**
     * 我的通知分页：{@code unread} 为 null 不限、true 只未读、false 只已读，
     * 固定 {@code created_at DESC, id DESC}（id 兜底，避免同秒创建时翻页错位）。
     */
    PageResult<Notification> findPage(long userId, Boolean unread, int page, int size);

    /** 未读数（顶栏角标） */
    long countUnread(long userId);

    /**
     * 全部已读：定向 UPDATE {@code is_read=1}（不走整行回写，同 forum 的计数 / 采纳定向 SQL 口径），
     * 返回**实际更新行数**——已读过的行不重复计入，故"再次点击全部已读"返回 0。
     */
    int markAllRead(long userId);
}
