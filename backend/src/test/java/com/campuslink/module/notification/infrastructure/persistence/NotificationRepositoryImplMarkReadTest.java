package com.campuslink.module.notification.infrastructure.persistence;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.campuslink.module.notification.domain.model.Notification;
import com.campuslink.module.notification.domain.model.NotificationTargetType;
import com.campuslink.module.notification.domain.model.NotificationType;
import com.campuslink.module.notification.infrastructure.persistence.mapper.NotificationMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 单条已读适配器的 SQL 结构测试（CR-078 功能 C）。
 *
 * <p>条件写在 SQL 里，应用层 Mockito 看不见列语义（同 {@code NotificationRepositoryImplFilterTest} 记过的
 * unread / is_read 反义教训），故把 wrapper 捕获出来直接断言渲染出的 SET 与 WHERE：{@code markRead} 必须
 * {@code SET is_read=1 WHERE id=? AND user_id=? AND is_read=0}。
 *
 * <p>三个条件各有存在理由：{@code id} 是本体；{@code user_id} 是**纵深防御**——应用层已判过一次归属，
 * 但把那道校验摘掉不该让"改到别人的通知"变得可能；{@code is_read=0} 让已读行不被命中，于是"改到几行"
 * 成为可信事实，重复点击的后到者拿到 0 行（幂等的最后一道，口径同 {@code markAllRead}）。
 */
@ExtendWith(MockitoExtension.class)
class NotificationRepositoryImplMarkReadTest {

    private static final Pattern IS_READ_PARAM =
            Pattern.compile("is_read\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(\\w+)}");
    private static final Pattern USER_ID_PARAM =
            Pattern.compile("user_id\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(\\w+)}");
    /** {@code \b} 挡住 user_id / target_id 里的 "id"：下划线是词字符，二者前面没有词边界 */
    private static final Pattern ID_PARAM =
            Pattern.compile("\\bid\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(\\w+)}");

    private static final long NOTIFICATION_ID = 5L;
    private static final long USER_ID = 42L;

    @Mock
    private NotificationMapper notificationMapper;

    private NotificationRepositoryImpl repository;

    /** 纯单测没有 SqlSession，lambda 条件的列名解析依赖 MP 的 TableInfo 缓存，先手工预热 */
    @BeforeAll
    static void initTableInfoCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        assistant.setCurrentNamespace(NotificationMapper.class.getName());
        TableInfoHelper.initTableInfo(assistant, NotificationDO.class);
    }

    @BeforeEach
    void setUp() {
        repository = new NotificationRepositoryImpl(notificationMapper);
    }

    @Test
    @DisplayName("markRead：SET is_read=1，WHERE 同时带 id + user_id + is_read=0（定向单列，不整行回写）")
    void markReadSetsFlagWithGuardedWhere() {
        LambdaUpdateWrapper<NotificationDO> update = capturedUpdate(1);
        Map<String, Object> pairs = update.getParamNameValuePairs();

        assertThat(bound(update.getSqlSet(), pairs, IS_READ_PARAM)).isEqualTo(true);
        assertThat(bound(update.getSqlSegment(), pairs, IS_READ_PARAM)).isEqualTo(false);
        assertThat(bound(update.getSqlSegment(), pairs, ID_PARAM)).isEqualTo(NOTIFICATION_ID);
        assertThat(bound(update.getSqlSegment(), pairs, USER_ID_PARAM)).isEqualTo(USER_ID);
        // 只写 is_read 一列：其余列一旦进 SET，就等于把读出来的旧值整行回写（并发下会抹掉别人的改动）
        assertThat(update.getSqlSet()).doesNotContain("user_id", "type", "actor_id", "target_id", "created_at");
    }

    @Test
    @DisplayName("1 行受影响 → true（本人的未读行被标记）")
    void unreadRowYieldsTrue() {
        when(notificationMapper.update(isNull(), any())).thenReturn(1);

        assertThat(repository.markRead(NOTIFICATION_ID, USER_ID)).isTrue();
    }

    @Test
    @DisplayName("0 行受影响 → false（已读行不命中 is_read=0 条件，重复点击的后到者拿到 false）")
    void alreadyReadRowYieldsFalse() {
        when(notificationMapper.update(isNull(), any())).thenReturn(0);

        assertThat(repository.markRead(NOTIFICATION_ID, USER_ID)).isFalse();
    }

    /**
     * {@code findById} 是应用层幂等短路的唯一依据：{@code is_read} 映射错了（例如恒为 false），
     * 编排层单测看不出来，却会让"已读再调不发 UPDATE"这条承诺静默失效，故在这里连映射一起锁住。
     */
    @Test
    @DisplayName("findById：命中时按 toDomain 还原（含 is_read），未命中时是空 Optional")
    void findByIdRehydratesOrIsEmpty() {
        when(notificationMapper.selectById(NOTIFICATION_ID)).thenReturn(row(true));

        Notification found = repository.findById(NOTIFICATION_ID).orElseThrow();

        assertThat(found.getId()).isEqualTo(NOTIFICATION_ID);
        assertThat(found.getUserId()).isEqualTo(USER_ID);
        assertThat(found.getType()).isEqualTo(NotificationType.REPLY);
        assertThat(found.getTargetType()).isEqualTo(NotificationTargetType.REPLY);
        assertThat(found.getTargetId()).isEqualTo(11L);
        assertThat(found.isRead()).isTrue();

        when(notificationMapper.selectById(404L)).thenReturn(null);
        assertThat(repository.findById(404L)).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private LambdaUpdateWrapper<NotificationDO> capturedUpdate(int affectedRows) {
        when(notificationMapper.update(isNull(), any())).thenReturn(affectedRows);

        repository.markRead(NOTIFICATION_ID, USER_ID);

        ArgumentCaptor<Wrapper<NotificationDO>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(notificationMapper).update(isNull(), captor.capture());
        return (LambdaUpdateWrapper<NotificationDO>) captor.getValue();
    }

    private static Object bound(String segment, Map<String, Object> pairs, Pattern pattern) {
        Matcher matcher = pattern.matcher(segment);
        assertThat(matcher.find()).as("片段里应出现 %s，实际：%s", pattern.pattern(), segment).isTrue();
        return pairs.get(matcher.group(1));
    }

    private static NotificationDO row(boolean read) {
        NotificationDO d = new NotificationDO();
        d.setId(NOTIFICATION_ID);
        d.setUserId(USER_ID);
        d.setType("reply");
        d.setActorId(7L);
        d.setTargetType("REPLY");
        d.setTargetId(11L);
        d.setIsRead(read);
        d.setCreatedAt(Instant.parse("2026-09-21T08:00:00Z"));
        return d;
    }
}
