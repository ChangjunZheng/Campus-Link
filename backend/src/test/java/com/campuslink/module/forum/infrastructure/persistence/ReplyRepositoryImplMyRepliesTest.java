package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.domain.model.MyReplyRow;
import com.campuslink.module.forum.domain.model.ReplyStatus;
import com.campuslink.module.forum.infrastructure.persistence.mapper.ReplyMapper;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * "我的回帖"的父帖可见性判定（F-ACC-007c / CR-071）。
 *
 * <p>条件写在 {@code @Select} 注解的 SQL 文本里（不是 wrapper），Mockito 看不见也捕获不到，
 * 故这里**直接读注解文本断言**——与 {@code ReplyRepositoryImplVisibleTest} 用 wrapper 断言是同一目的、
 * 两种形态：判据必须在这一层可见，否则"顺手去掉父帖条件"这种改动不会让任何测试变红，
 * 而它的后果正是 CR-065 / CR-066 刚抓过两次的"列出来了却点不进"。
 */
@ExtendWith(MockitoExtension.class)
class ReplyRepositoryImplMyRepliesTest {

    private static final long AUTHOR_ID = 42L;
    private static final Instant CREATED_AT = Instant.parse("2026-09-12T08:00:00Z");

    @Mock
    private ReplyMapper replyMapper;

    private ReplyRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ReplyRepositoryImpl(replyMapper);
    }

    private static String sql() {
        Select select = java.util.Arrays.stream(ReplyMapper.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("findPageByAuthor"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("ReplyMapper#findPageByAuthor 不存在"))
                .getAnnotation(Select.class);
        return String.join(" ", select.value());
    }

    @Test
    @DisplayName("同一条 SQL 里判三件事：楼层未自删、父帖未自删、父帖仍 PUBLISHED（父帖不可见 ⇒ 整条不出现）")
    void guardsAuthorTombstoneAndParentVisibility() {
        assertThat(sql())
                .contains("r.author_id = #{authorId}")
                .contains("r.is_deleted = 0")
                .contains("p.is_deleted = 0")
                .contains("p.status = 'PUBLISHED'")
                .contains("JOIN posts p ON p.id = r.post_id");
    }

    @Test
    @DisplayName("⚠️ 楼层**自身**的 status 不进 WHERE（作者要看到「我哪一层被下架」），且时间倒序带 id 兜底")
    void keepsRemovedFloorsVisibleToTheirAuthor() {
        String where = sql().substring(sql().indexOf("WHERE"), sql().indexOf("ORDER BY"));
        assertThat(where).doesNotContain("r.status");
        assertThat(sql()).contains("ORDER BY r.created_at DESC, r.id DESC");
    }

    @Test
    @DisplayName("分页参数与父帖标题透传到窄载体，total 取自同一条 SQL（不在内存剔除）")
    void mapsRowsAndKeepsTotalInSync() {
        when(replyMapper.findPageByAuthor(any(IPage.class), eq(AUTHOR_ID)))
                .thenReturn(new Page<MyReplyDO>(2, 20).setRecords(List.of(row())).setTotal(45L));

        PageResult<MyReplyRow> page = repository.findPageByAuthor(AUTHOR_ID, 2, 20);

        assertThat(page.total()).isEqualTo(45L);
        assertThat(page.page()).isEqualTo(2);
        assertThat(page.size()).isEqualTo(20);
        MyReplyRow row = page.items().getFirst();
        assertThat(row.id()).isEqualTo(11L);
        assertThat(row.postId()).isEqualTo(9L);
        assertThat(row.postTitle()).isEqualTo("父帖标题");
        assertThat(row.floorNo()).isEqualTo(3);
        assertThat(row.status()).isEqualTo(ReplyStatus.REMOVED);
        verify(replyMapper).findPageByAuthor(any(IPage.class), eq(AUTHOR_ID));
    }

    private static MyReplyDO row() {
        MyReplyDO d = new MyReplyDO();
        d.setId(11L);
        d.setPostId(9L);
        d.setPostTitle("父帖标题");
        d.setFloorNo(3);
        d.setContentMd("楼层内容");
        d.setStatus("REMOVED");
        d.setCreatedAt(CREATED_AT);
        return d;
    }
}
