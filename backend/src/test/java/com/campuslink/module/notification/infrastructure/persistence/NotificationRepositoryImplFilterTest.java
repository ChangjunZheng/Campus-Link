package com.campuslink.module.notification.infrastructure.persistence;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 适配器查询条件测试：{@code findPage} 的 unread 入参与 is_read 列**语义相反**（unread=true 要筛未读行），
 * 编排层单测看不到这层反转，真机冒烟实测到过反义缺陷（3 条全未读时 unread=true 返回 0 条）。
 * 这里把 wrapper 捕获出来，直接断言绑定到 is_read 上的值。
 */
@ExtendWith(MockitoExtension.class)
class NotificationRepositoryImplFilterTest {

    /** MP 渲染出的条件片段形如 {@code is_read = #{ew.paramNameValuePairs.MPGENVAL2}} */
    private static final Pattern IS_READ_PARAM =
            Pattern.compile("is_read\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(\\w+)}");

    private static final long USER_ID = 1000L;

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

    private Object boundValueOfIsRead(Boolean unread) {
        when(notificationMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<NotificationDO> page = invocation.getArgument(0);
            page.setRecords(List.of());
            page.setTotal(0);
            return page;
        });

        repository.findPage(USER_ID, unread, 1, 20);

        ArgumentCaptor<Wrapper<NotificationDO>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(notificationMapper).selectPage(any(IPage.class), captor.capture());
        LambdaQueryWrapper<NotificationDO> query = (LambdaQueryWrapper<NotificationDO>) captor.getValue();
        Matcher matcher = IS_READ_PARAM.matcher(query.getSqlSegment());
        assertThat(matcher.find()).as("条件里应出现 is_read=%s，实际：%s", unread, query.getSqlSegment());
        return query.getParamNameValuePairs().get(matcher.group(1));
    }

    @Test
    @DisplayName("unread=true 筛 is_read=0（未读）")
    void unreadFilterSelectsUnreadRows() {
        assertThat(boundValueOfIsRead(true)).isEqualTo(false);
    }

    @Test
    @DisplayName("unread=false 筛 is_read=1（已读）")
    void readFilterSelectsReadRows() {
        assertThat(boundValueOfIsRead(false)).isEqualTo(true);
    }

    @Test
    @DisplayName("unread 缺省时不下发 is_read 条件")
    void absentFilterAddsNoCondition() {
        when(notificationMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<NotificationDO> page = invocation.getArgument(0);
            page.setRecords(List.of());
            page.setTotal(0);
            return page;
        });

        repository.findPage(USER_ID, null, 1, 20);

        ArgumentCaptor<Wrapper<NotificationDO>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(notificationMapper).selectPage(any(IPage.class), captor.capture());
        assertThat(((LambdaQueryWrapper<NotificationDO>) captor.getValue()).getSqlSegment()).doesNotContain("is_read");
    }
}
