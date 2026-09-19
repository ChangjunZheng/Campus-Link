package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.campuslink.module.forum.domain.model.PostStatus;
import com.campuslink.module.forum.infrastructure.persistence.mapper.PostMapper;
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
 * 处置适配器的 SQL 结构测试（F-SAFE-003 / CR-066）：状态迁移条件写在 SQL 里，应用层 Mockito 看不见列语义，
 * 故捕获 wrapper 直接断言渲染出的 SET 与 WHERE（harness 同 {@code PostRepositoryImplMarkDeletedTest}）。
 *
 * <p>本用例的存在理由是 WHERE 里那个<b>原状态条件</b>：它让"是否真的改了"成为可信事实——并发下后到的
 * 一方得到 0 行，应用层据此不写第二条审计。摘掉它，两次同向处置都会"成功"、审计变成噪音。
 */
@ExtendWith(MockitoExtension.class)
class PostRepositoryImplUpdateStatusTest {

    private static final Pattern STATUS_PARAM =
            Pattern.compile("status\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(\\w+)}");

    private static final long POST_ID = 123L;

    @Mock
    private PostMapper postMapper;

    private PostRepositoryImpl repository;

    @BeforeAll
    static void initTableInfoCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        assistant.setCurrentNamespace(PostMapper.class.getName());
        TableInfoHelper.initTableInfo(assistant, PostDO.class);
    }

    @BeforeEach
    void setUp() {
        repository = new PostRepositoryImpl(postMapper);
    }

    private Object firstBoundValue(String segment, Map<String, Object> pairs) {
        Matcher matcher = STATUS_PARAM.matcher(segment);
        assertThat(matcher.find()).as("条件里应出现 status，实际：%s", segment).isTrue();
        return pairs.get(matcher.group(1));
    }

    @SuppressWarnings("unchecked")
    private LambdaUpdateWrapper<PostDO> capturedUpdate(int affectedRows) {
        when(postMapper.update(isNull(), any())).thenReturn(affectedRows);

        repository.updateStatus(POST_ID, PostStatus.REMOVED, PostStatus.PUBLISHED);

        ArgumentCaptor<Wrapper<PostDO>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).update(isNull(), captor.capture());
        return (LambdaUpdateWrapper<PostDO>) captor.getValue();
    }

    @Test
    @DisplayName("updateStatus：SET status=目标值，WHERE 同时带 id 与原状态（定向单列，不整行回写）")
    void updateStatusSetsTargetWithGuardedWhere() {
        LambdaUpdateWrapper<PostDO> update = capturedUpdate(1);

        assertThat(firstBoundValue(update.getSqlSet(), update.getParamNameValuePairs())).isEqualTo("REMOVED");
        assertThat(firstBoundValue(update.getSqlSegment(), update.getParamNameValuePairs())).isEqualTo("PUBLISHED");
        assertThat(update.getSqlSegment()).contains("id =");
        // 墓碑列不在写回之列：处置不改 is_deleted（两列两义，CR-065 立的判据）
        assertThat(update.getSqlSet()).doesNotContain("is_deleted", "reply_count", "like_count", "favorite_count");
    }

    @Test
    @DisplayName("0 行受影响 → 返回 false（并发下状态已被改走，用例据此不重复记审计）")
    void alreadyInTargetStateYieldsFalse() {
        when(postMapper.update(isNull(), any())).thenReturn(0);

        assertThat(repository.updateStatus(POST_ID, PostStatus.REMOVED, PostStatus.PUBLISHED)).isFalse();
    }
}
