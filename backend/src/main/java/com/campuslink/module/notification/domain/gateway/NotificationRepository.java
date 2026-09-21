package com.campuslink.module.notification.domain.gateway;

import com.campuslink.module.notification.domain.model.Notification;

import java.util.Optional;

/** 通知仓储端口（端口定义在 domain、实现在 infrastructure，DIP） */
public interface NotificationRepository {

    /** 新增通知（INSERT）；自增 id 与 created_at 由数据库生成，无人消费故不回传聚合 */
    void save(Notification notification);

    /**
     * 按 id 取单条。<b>刻意不带接收人条件</b>：归属判定归应用层，那里要让"不存在"与"存在但属于他人"
     * 落到同一个 404（后者若给 403 就等于承认了他人通知的存在）。
     */
    Optional<Notification> findById(long notificationId);

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

    /**
     * 单条已读：定向 UPDATE {@code is_read=1}，条件同时锁 id 与接收人——应用层已判过一次归属，
     * 这里再锁一次是纵深防御（即便上层校验被摘掉也改不到别人的行，口径同 {@link #markAllRead}）。
     *
     * <p>{@code is_read=0} 前置条件让已读行不被命中，故返回值为"本次是否真的写了一行"；
     * 幂等由应用层的短路负责，本方法只保证"重复调用不会重复写"。
     */
    boolean markRead(long notificationId, long userId);
}
