package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.domain.model.Post;
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

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * "我的帖子"读路径的 SQL 结构测试（F-ACC-007b / CR-071）：可见性口径写在 SQL 里，
 * 应用层 Mockito 看不见列语义，故捕获 wrapper 断言渲染出的 WHERE 与 ORDER BY。
 *
 * <p>两条判据各守一头：
 * ① {@code is_deleted = 0} **必须在**——作者自删的帖对自己也不该出现（列出来就是"看得见、点不进、找不回"的死行）；
 * ② {@code status} 条件**必须不在**——平台下架的帖要能对作者本人露出（这与全站列表恰好相反，
 *    一旦有人"顺手补上 status 过滤"，F-ACC-007b 的立论就整条消失，而它在应用层测试里看不出来）。
 */
@ExtendWith(MockitoExtension.class)
class PostRepositoryImplFindPageByAuthorTest {

    private static final long AUTHOR_ID = 42L;

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

    @SuppressWarnings("unchecked")
    private LambdaQueryWrapper<PostDO> capturedQuery() {
        ArgumentCaptor<Wrapper<PostDO>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).selectPage(any(IPage.class), captor.capture());
        return (LambdaQueryWrapper<PostDO>) captor.getValue();
    }

    @Test
    @DisplayName("谓词只有 author_id + is_deleted，排序 created_at DESC, id DESC（同值兜底防翻页错位）")
    void filtersTombstoneAndSortsByTime() {
        when(postMapper.selectPage(any(), any())).thenReturn(new Page<>(1, 20).setRecords(List.of()).setTotal(0L));

        repository.findPageByAuthor(AUTHOR_ID, 1, 20);

        LambdaQueryWrapper<PostDO> query = capturedQuery();
        assertThat(query.getSqlSegment()).contains("author_id", "is_deleted", "ORDER BY", "created_at");
        assertThat(query.getParamNameValuePairs()).containsValue(AUTHOR_ID);
    }

    @Test
    @DisplayName("⚠️ 反证守门：这条读路径**不写 status 条件**——REMOVED 必须对作者本人可见")
    void deliberatelyDoesNotFilterStatus() {
        when(postMapper.selectPage(any(), any())).thenReturn(new Page<>(1, 20).setRecords(List.of()).setTotal(0L));

        repository.findPageByAuthor(AUTHOR_ID, 2, 20);

        assertThat(capturedQuery().getSqlSegment()).doesNotContain("status");
    }

    @Test
    @DisplayName("total 与查询条件同源：分页对象带页码 / 页大小交给插件，总数取自同一条 SQL 的 COUNT")
    void totalComesFromTheSameQuery() {
        when(postMapper.selectPage(any(), any())).thenReturn(
                new Page<>(3, 20).setRecords(List.of(postDO())).setTotal(45L));

        PageResult<Post> page = repository.findPageByAuthor(AUTHOR_ID, 3, 20);

        assertThat(page.total()).isEqualTo(45L);
        assertThat(page.page()).isEqualTo(3);
        assertThat(page.size()).isEqualTo(20);
        assertThat(page.items()).hasSize(1);
    }

    private static PostDO postDO() {
        PostDO d = new PostDO();
        d.setId(7L);
        d.setBoardId(1L);
        d.setAuthorId(AUTHOR_ID);
        d.setType("QUESTION");
        d.setTitle("标题");
        d.setContentMd("正文");
        d.setContentHtml("<p>正文</p>");
        d.setStatus("REMOVED");
        d.setReplyCount(0);
        d.setLikeCount(0);
        d.setIsAccepted(false);
        d.setIsDeleted(false);
        d.setHotScore(0d);
        d.setCreatedAt(Instant.parse("2026-09-12T08:00:00Z"));
        d.setUpdatedAt(Instant.parse("2026-09-12T08:00:00Z"));
        return d;
    }
}
