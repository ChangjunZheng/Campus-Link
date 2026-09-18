package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 软删适配器的 SQL 结构测试（F-FORUM-006）。
 *
 * <p>删除条件写在 SQL 里，应用层 Mockito 看不见列语义（同 {@code ReplyRepositoryImplVisibleTest} 记过的教训），
 * 故把 wrapper 捕获出来直接断言渲染出的 SET 与 WHERE：{@code markDeleted} 必须
 * {@code SET is_deleted=1 WHERE id=? AND is_deleted=0}。
 *
 * <p>WHERE 里那个 {@code is_deleted=0} 是本用例的存在理由——它让"改到几行"成为可信事实：并发二次删除的
 * 后到者得到 0 行，应用层据此返回 3001 而不是再写一条审计。少了这个条件，两次删除都"成功"。
 * 与读侧 {@code findById} 的 {@code is_deleted=0} 逐列比对，是为了锁死"删掉的即读不到的、读不到的也删不到第二次"
 * 这一条同源口径（CR-060 的教训：断言"同源"必须两侧都比，只断言一侧等于没断言）。
 */
@ExtendWith(MockitoExtension.class)
class PostRepositoryImplMarkDeletedTest {

    private static final Pattern IS_DELETED_PARAM =
            Pattern.compile("is_deleted\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(\\w+)}");

    private static final long POST_ID = 123L;

    @Mock
    private PostMapper postMapper;

    private PostRepositoryImpl repository;

    /** 纯单测没有 SqlSession，lambda 条件的列名解析依赖 MP 的 TableInfo 缓存，先手工预热 */
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

    private Object bound(String segment, java.util.Map<String, Object> pairs, Pattern pattern) {
        Matcher matcher = pattern.matcher(segment);
        assertThat(matcher.find()).as("条件里应出现 is_deleted，实际：%s", segment).isTrue();
        return pairs.get(matcher.group(1));
    }

    @SuppressWarnings("unchecked")
    private LambdaUpdateWrapper<PostDO> capturedUpdate(int affectedRows) {
        when(postMapper.update(isNull(), any())).thenReturn(affectedRows);

        repository.markDeleted(POST_ID);

        ArgumentCaptor<Wrapper<PostDO>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).update(isNull(), captor.capture());
        return (LambdaUpdateWrapper<PostDO>) captor.getValue();
    }

    @Test
    @DisplayName("markDeleted：SET is_deleted=1，WHERE 同时带 id 与 is_deleted=0（定向单列，不整行回写）")
    void markDeletedSetsTombstoneWithGuardedWhere() {
        LambdaUpdateWrapper<PostDO> update = capturedUpdate(1);

        assertThat(bound(update.getSqlSet(), update.getParamNameValuePairs(), IS_DELETED_PARAM)).isEqualTo(true);
        assertThat(bound(update.getSqlSegment(), update.getParamNameValuePairs(), IS_DELETED_PARAM)).isEqualTo(false);
        assertThat(update.getSqlSegment()).contains("id =");
        // SET 与 WHERE 各表达一次，不出现"把 is_deleted 当条件又当赋值"的重复
        assertThat(count(update.getSqlSet(), "is_deleted")).isEqualTo(1);
        assertThat(count(update.getSqlSegment(), "is_deleted")).isEqualTo(1);
        // 三个互动计数与 status 都不在写回之列（整行回写会抹掉删除期间发生的互动）
        assertThat(update.getSqlSet()).doesNotContain("reply_count", "like_count", "favorite_count", "status");
    }

    @Test
    @DisplayName("与读侧 findById 同源：两侧 is_deleted 绑定值逐列相等（删掉的即读不到的）")
    void tombstoneConditionMatchesReadSide() {
        LambdaUpdateWrapper<PostDO> update = capturedUpdate(1);

        when(postMapper.selectOne(any())).thenReturn(null);
        repository.findById(POST_ID);
        ArgumentCaptor<Wrapper<PostDO>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).selectOne(captor.capture());
        LambdaQueryWrapper<PostDO> query = (LambdaQueryWrapper<PostDO>) captor.getValue();

        assertThat(bound(update.getSqlSegment(), update.getParamNameValuePairs(), IS_DELETED_PARAM))
                .isEqualTo(bound(query.getSqlSegment(), query.getParamNameValuePairs(), IS_DELETED_PARAM));
    }

    @Test
    @DisplayName("0 行受影响 → 返回 false（并发下已被他人删除，用例据此回 3001、不重复记审计）")
    void alreadyDeletedRowYieldsFalse() {
        when(postMapper.update(isNull(), any())).thenReturn(0);

        assertThat(repository.markDeleted(POST_ID)).isFalse();
    }

    private int count(String segment, String column) {
        int n = 0;
        for (int i = segment.indexOf(column); i >= 0; i = segment.indexOf(column, i + column.length())) {
            n++;
        }
        return n;
    }
}
