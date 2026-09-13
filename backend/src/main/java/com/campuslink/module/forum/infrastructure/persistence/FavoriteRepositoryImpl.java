package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.module.forum.domain.gateway.FavoriteRepository;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.domain.model.Post;
import com.campuslink.module.forum.infrastructure.persistence.mapper.FavoriteMapper;
import com.campuslink.module.forum.infrastructure.persistence.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 适配器：FavoriteRepository 端口的 MyBatis-Plus 实现 */
@Repository
@RequiredArgsConstructor
public class FavoriteRepositoryImpl implements FavoriteRepository {

    private final FavoriteMapper favoriteMapper;
    private final PostMapper postMapper;

    @Override
    public boolean toggle(long userId, long postId) {
        // 主键唯一约束兜底并发双击，语义同 LikeRepositoryImpl#toggle
        try {
            FavoriteDO d = new FavoriteDO();
            d.setUserId(userId);
            d.setPostId(postId);
            favoriteMapper.insert(d);
            return true;
        } catch (DuplicateKeyException e) {
            favoriteMapper.delete(new LambdaQueryWrapper<FavoriteDO>()
                    .eq(FavoriteDO::getUserId, userId)
                    .eq(FavoriteDO::getPostId, postId));
            return false;
        }
    }

    @Override
    public PageResult<Post> findFavoritePosts(long userId, int page, int size) {
        IPage<FavoriteDO> favPage = favoriteMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<FavoriteDO>()
                        .eq(FavoriteDO::getUserId, userId)
                        .orderByDesc(FavoriteDO::getCreatedAt)
                        .orderByDesc(FavoriteDO::getPostId));
        List<Long> postIds = favPage.getRecords().stream().map(FavoriteDO::getPostId).toList();
        if (postIds.isEmpty()) {
            return new PageResult<>(List.of(), 0, page, size);
        }
        Map<Long, PostDO> postsById = postMapper.selectBatchIds(postIds).stream()
                .collect(Collectors.toMap(PostDO::getId, Function.identity()));
        // 按收藏时间序回填帖子；已删 / 不可见帖从页内剔除（favorites 行保留，total 为收藏行数——已删帖会使该页可能少项，属可接受取舍）
        List<Post> items = postIds.stream()
                .map(postsById::get)
                .filter(java.util.Objects::nonNull)
                .map(PostConverter::toDomain)
                .filter(Post::isVisible)
                .toList();
        return new PageResult<>(items, favPage.getTotal(), page, size);
    }

    @Override
    public Set<Long> findFavoritedPostIds(long userId, Collection<Long> postIds) {
        if (postIds.isEmpty()) {
            return Set.of();
        }
        return favoriteMapper.selectList(new LambdaQueryWrapper<FavoriteDO>()
                        .eq(FavoriteDO::getUserId, userId)
                        .in(FavoriteDO::getPostId, postIds)).stream()
                .map(FavoriteDO::getPostId)
                .collect(Collectors.toUnmodifiableSet());
    }
}
