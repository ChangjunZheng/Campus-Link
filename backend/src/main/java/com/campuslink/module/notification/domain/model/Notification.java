package com.campuslink.module.notification.domain.model;

import lombok.Getter;

import java.time.Instant;

/**
 * 站内通知（F-SOC-001）聚合根：一条"某人对你发布的内容做了某事"。
 *
 * <p>只存**事实四元组**（接收人 / 类型 / 触发人 / 目标），不存标题、昵称、楼层号等展示字段——
 * 这些在读时跨上下文组装（见 {@code NotificationApplicationService}），代价是通知内容跟随原文变化
 * （帖子改标题后通知显示新标题），换取 V1 既有表零结构变更；本产品的帖子无编辑功能，该代价当前不可见。
 *
 * <p>跳转所需的 {@code postId} 同样不落列：目标为 POST 时即 {@code targetId}，为 REPLY 时由 forum 侧读时解析。
 */
@Getter
public class Notification {

    private final Long id;
    private final Long userId;
    private final NotificationType type;
    private final Long actorId;
    private final NotificationTargetType targetType;
    private final Long targetId;
    private final boolean read;
    private final Instant createdAt;

    private Notification(Long id, Long userId, NotificationType type, Long actorId,
                         NotificationTargetType targetType, Long targetId, boolean read, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.actorId = actorId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.read = read;
        this.createdAt = createdAt;
    }

    /** 新建通知：id 与时间戳由数据库生成，未读 */
    public static Notification of(NotificationType type, long actorId, NotificationTargetType targetType,
                                  long targetId, long userId) {
        return new Notification(null, userId, type, actorId, targetType, targetId, false, null);
    }

    /** 仓储重建入口（infrastructure 适配器调用） */
    public static Notification rehydrate(Long id, Long userId, NotificationType type, Long actorId,
                                         NotificationTargetType targetType, Long targetId, boolean read,
                                         Instant createdAt) {
        return new Notification(id, userId, type, actorId, targetType, targetId, read, createdAt);
    }
}
