package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.campuslink.module.forum.domain.model.ReplyStatus;
import com.campuslink.module.forum.infrastructure.persistence.mapper.ReplyMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 处置侧楼层适配器的 SQL 结构测试（F-SAFE-003 / CR-066）。
 *
 * <p>两条各自的存在理由：
 * ① {@code findStatusById} **必须不带 status 条件**——恢复一条已下架楼层得先读得出"它现在是 REMOVED"，
 * 若沿用 {@code findVisibleById} 的可见性口径，已下架内容将永远读不到、也就永远恢复不了；
 * ② {@code updateStatus} 的 WHERE 必须同时带<b>原状态</b>与 {@code is_deleted=0}——前者让"是否真的改了"
 * 成为可信事实（并发后到方得 0 行、不重复记审计），后者挡住"用处置恢复已删墓碑"的越界。
 * harness 同 {@code ReplyRepositoryImplVisibleTest}（CR-060 立下的"断言同源须两侧都比"）。
 */
@ExtendWith(MockitoExtension.class)
class ReplyRepositoryImplModerationTest {

    private static final Pattern STATUS_PARAM =
            Pattern.compile("status\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(\\w+)}");
    private static final Pattern IS_DELETED_PARAM =
            Pattern.compile("is_deleted\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(\\w+)}");

    private static final long REPLY_ID = 11L;

    @Mock
    private ReplyMapper replyMapper;

    private ReplyRepositoryImpl repository;

    @BeforeAll
    static void initTableInfoCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        assistant.setCurrentNamespace(ReplyMapper.class.getName());
        TableInfoHelper.initTableInfo(assistant, ReplyDO.class);
    }

    @BeforeEach
    void setUp() {
        repository = new ReplyRepositoryImpl(replyMapper);
    }

    private Object boundValue(String segment, Map<String, Object> pairs, Pattern pattern) {
        Matcher matcher = pattern.matcher(segment);
        assertThat(matcher.find()).as("条件里应出现 %s，实际：%s", pattern, segment).isTrue();
        return pairs.get(matcher.group(1));
    }

    @Test
    @DisplayName("findStatusById：只按 id + is_deleted=0 窄读，条件里不得出现 status（否则已下架的恢复不了）")
    @SuppressWarnings("unchecked")
    void findStatusReadsRegardlessOfStatus() {
        ReplyDO row = new ReplyDO();
        row.setStatus(ReplyStatus.REMOVED.name());
        when(replyMapper.selectOne(any())).thenReturn(row);

        assertThat(repository.findStatusById(REPLY_ID)).contains(ReplyStatus.REMOVED);

        ArgumentCaptor<Wrapper<ReplyDO>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(replyMapper).selectOne(captor.capture());
        LambdaQueryWrapper<ReplyDO> query = (LambdaQueryWrapper<ReplyDO>) captor.getValue();
        assertThat(query.getSqlSegment()).doesNotContain("status");
        assertThat(boundValue(query.getSqlSegment(), query.getParamNameValuePairs(), IS_DELETED_PARAM)).isEqualTo(false);
    }

    @Test
    @DisplayName("findStatusById：行不存在或已删（读侧墓碑约定）→ 空，用例据此回 3001")
    void missingOrTombstonedRowYieldsEmpty() {
        when(replyMapper.selectOne(any())).thenReturn(null);

        assertThat(repository.findStatusById(REPLY_ID)).isEmpty();
    }

    @Test
    @DisplayName("updateStatus：SET status=目标值，WHERE 带 id + 原状态 + is_deleted=0（定向单列）")
    @SuppressWarnings("unchecked")
    void updateStatusGuardsOnCurrentStatusAndTombstone() {
        when(replyMapper.update(isNull(), any())).thenReturn(1);

        repository.updateStatus(REPLY_ID, ReplyStatus.REMOVED, ReplyStatus.PUBLISHED);

        ArgumentCaptor<Wrapper<ReplyDO>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(replyMapper).update(isNull(), captor.capture());
        LambdaUpdateWrapper<ReplyDO> update = (LambdaUpdateWrapper<ReplyDO>) captor.getValue();
        Map<String, Object> pairs = update.getParamNameValuePairs();

        assertThat(boundValue(update.getSqlSet(), pairs, STATUS_PARAM)).isEqualTo("REMOVED");
        assertThat(boundValue(update.getSqlSegment(), pairs, STATUS_PARAM)).isEqualTo("PUBLISHED");
        assertThat(boundValue(update.getSqlSegment(), pairs, IS_DELETED_PARAM)).isEqualTo(false);
        assertThat(update.getSqlSet()).doesNotContain("is_deleted", "is_accepted", "like_count");
    }

    @Test
    @DisplayName("0 行受影响 → 返回 false（并发下状态已被改走，用例据此不重复记审计）")
    void alreadyInTargetStateYieldsFalse() {
        when(replyMapper.update(isNull(), any())).thenReturn(0);

        assertThat(repository.updateStatus(REPLY_ID, ReplyStatus.REMOVED, ReplyStatus.PUBLISHED)).isFalse();
    }
}
