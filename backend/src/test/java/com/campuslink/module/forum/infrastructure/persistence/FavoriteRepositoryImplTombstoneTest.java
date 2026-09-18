package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.domain.model.Post;
import com.campuslink.module.forum.infrastructure.persistence.mapper.FavoriteMapper;
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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 收藏页读侧的墓碑过滤测试（F-FORUM-006 真机抓到的缺陷）。
 *
 * <p>这里曾是全仓唯一一条"读帖子但没排墓碑"的读路径：它用 {@code selectBatchIds} 按主键批读，
 * 再靠 {@code Post::isVisible} 过滤——而 {@code isVisible} 只看 {@code status}，作者自助删除写的是
 * {@code is_deleted=1}（status 仍是 PUBLISHED），于是**已删帖照样出现在"我的收藏"里**。
 * Mockito 结构上看不见这个差别，故把 wrapper 捕获出来直接断言 SQL 条件，并与
 * {@code PostRepositoryImpl#findById} 的绑定值逐列比对（CR-060 的教训：断言"同源"必须两侧都比）。
 */
@ExtendWith(MockitoExtension.class)
class FavoriteRepositoryImplTombstoneTest {

    private static final Pattern IS_DELETED_PARAM =
            Pattern.compile("is_deleted\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(\\w+)}");

    private static final long USER_ID = 32L;
    private static final long POST_ID = 24L;

    @Mock
    private FavoriteMapper favoriteMapper;
    @Mock
    private PostMapper postMapper;

    private FavoriteRepositoryImpl favorites;
    private PostRepositoryImpl posts;

    /** 纯单测没有 SqlSession，lambda 条件的列名解析依赖 MP 的 TableInfo 缓存，先手工预热 */
    @BeforeAll
    static void initTableInfoCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        assistant.setCurrentNamespace(PostMapper.class.getName());
        TableInfoHelper.initTableInfo(assistant, FavoriteDO.class);
        TableInfoHelper.initTableInfo(assistant, PostDO.class);
    }

    @BeforeEach
    void setUp() {
        favorites = new FavoriteRepositoryImpl(favoriteMapper, postMapper);
        posts = new PostRepositoryImpl(postMapper);
    }

    /** 收藏表里有一行（第 24 帖），帖子侧读到什么由用例决定 */
    private void stubOneFavoriteRow() {
        FavoriteDO fav = new FavoriteDO();
        fav.setUserId(USER_ID);
        fav.setPostId(POST_ID);
        when(favoriteMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            Page<FavoriteDO> page = invocation.getArgument(0);
            page.setRecords(List.of(fav));
            page.setTotal(1);
            return page;
        });
    }

    @SuppressWarnings("unchecked")
    private LambdaQueryWrapper<PostDO> capturedFavoritePostQuery(List<PostDO> readBack) {
        stubOneFavoriteRow();
        when(postMapper.selectList(any())).thenReturn(readBack);

        favorites.findFavoritePosts(USER_ID, 1, 20);

        ArgumentCaptor<Wrapper<PostDO>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).selectList(captor.capture());
        return (LambdaQueryWrapper<PostDO>) captor.getValue();
    }

    private Object bound(LambdaQueryWrapper<PostDO> query) {
        Matcher matcher = IS_DELETED_PARAM.matcher(query.getSqlSegment());
        assertThat(matcher.find()).as("条件里应出现 is_deleted，实际：%s", query.getSqlSegment()).isTrue();
        return query.getParamNameValuePairs().get(matcher.group(1));
    }

    @Test
    @DisplayName("收藏页的帖子批量读带 is_deleted=0，且与 findById 同源")
    void favoritePostReadExcludesTombstones() {
        LambdaQueryWrapper<PostDO> favoriteSide = capturedFavoritePostQuery(List.of());

        assertThat(bound(favoriteSide)).isEqualTo(false);
        assertThat(favoriteSide.getSqlSegment()).contains("id IN");
        assertThat(count(favoriteSide.getSqlSegment(), "is_deleted")).isEqualTo(1);

        when(postMapper.selectOne(any())).thenReturn(null);
        posts.findById(POST_ID);
        ArgumentCaptor<Wrapper<PostDO>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(postMapper).selectOne(captor.capture());
        LambdaQueryWrapper<PostDO> readSide = (LambdaQueryWrapper<PostDO>) captor.getValue();

        // 两侧都必须排墓碑：值相等才算同源，只断言一侧等于没断言（CR-060 的反证教训）
        assertThat(bound(readSide)).isEqualTo(bound(favoriteSide));
    }

    @Test
    @DisplayName("墓碑帖读不到时页内为空、total 仍按收藏行数计；且不再走 selectBatchIds")
    void tombstonedPostYieldsNoItemButKeepsFavoriteTotal() {
        stubOneFavoriteRow();
        when(postMapper.selectList(any())).thenReturn(List.of());

        PageResult<Post> result = favorites.findFavoritePosts(USER_ID, 1, 20);

        assertThat(result.items()).isEmpty();
        // 收藏行保留、total 按收藏行计（CR-048 已登记的取舍：该页可能少项）
        assertThat(result.total()).isEqualTo(1);
        verify(postMapper, never()).selectBatchIds(any());
    }

    private int count(String segment, String column) {
        int n = 0;
        for (int i = segment.indexOf(column); i >= 0; i = segment.indexOf(column, i + column.length())) {
            n++;
        }
        return n;
    }
}
