package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.domain.gateway.PostRepository;
import com.campuslink.module.forum.domain.model.Post;
import com.campuslink.module.forum.domain.model.PostStatus;
import com.campuslink.module.forum.infrastructure.persistence.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 适配器：PostRepository 端口的 MyBatis-Plus 实现 */
@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final PostMapper postMapper;

    @Override
    public PageResult<Post> findPage(Long boardId, int page, int size) {
        LambdaQueryWrapper<PostDO> query = new LambdaQueryWrapper<>();
        if (boardId != null) {
            query.eq(PostDO::getBoardId, boardId);
        }
        query.eq(PostDO::getStatus, PostStatus.PUBLISHED.name())
                .eq(PostDO::getIsDeleted, false)
                .orderByDesc(PostDO::getCreatedAt)
                .orderByDesc(PostDO::getId);
        IPage<PostDO> result = postMapper.selectPage(new Page<>(page, size), query);
        return new PageResult<>(result.getRecords().stream().map(PostConverter::toDomain).toList(),
                result.getTotal(), page, size);
    }

    @Override
    public PageResult<Post> search(String keyword, Long boardId, Integer days, int page, int size) {
        IPage<PostDO> result = postMapper.search(new Page<>(page, size), keyword, boardId, days);
        return new PageResult<>(result.getRecords().stream().map(PostConverter::toDomain).toList(),
                result.getTotal(), page, size);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return Optional.ofNullable(postMapper.selectOne(new LambdaQueryWrapper<PostDO>()
                        .eq(PostDO::getId, id)
                        .eq(PostDO::getIsDeleted, false)))
                .map(PostConverter::toDomain);
    }

    @Override
    public List<Post> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return postMapper.selectList(new LambdaQueryWrapper<PostDO>()
                        .in(PostDO::getId, ids)
                        .eq(PostDO::getIsDeleted, false)).stream()
                .map(PostConverter::toDomain)
                .toList();
    }

    @Override
    public Post save(Post post) {
        PostDO d = PostConverter.toDo(post);
        postMapper.insert(d);
        return PostConverter.toDomain(d);
    }

    @Override
    public int incrementReplyCountAndGet(Long postId) {
        // 行锁先行的 UPDATE：同一帖子的并发回帖在此串行化；随后读回的值即本次 floor_no（设计 §4.1）
        postMapper.update(null, new LambdaUpdateWrapper<PostDO>()
                .setSql("reply_count = reply_count + 1")
                .eq(PostDO::getId, postId));
        return postMapper.selectById(postId).getReplyCount();
    }

    @Override
    public void updateAcceptedReply(Long postId, Long replyId) {
        // 行锁先行的 UPDATE：并发采纳在此串行化，后写覆盖前写即"可更换"语义（F-QA-001）
        postMapper.update(null, new LambdaUpdateWrapper<PostDO>()
                .set(PostDO::getIsAccepted, true)
                .set(PostDO::getAcceptedReplyId, replyId)
                .eq(PostDO::getId, postId));
    }

    @Override
    public int adjustLikeCount(Long postId, int delta) {
        postMapper.update(null, new LambdaUpdateWrapper<PostDO>()
                .setSql("like_count = like_count + " + delta)
                .eq(PostDO::getId, postId));
        return postMapper.selectById(postId).getLikeCount();
    }

    @Override
    public int adjustFavoriteCount(Long postId, int delta) {
        postMapper.update(null, new LambdaUpdateWrapper<PostDO>()
                .setSql("favorite_count = favorite_count + " + delta)
                .eq(PostDO::getId, postId));
        return postMapper.selectById(postId).getFavoriteCount();
    }
}
