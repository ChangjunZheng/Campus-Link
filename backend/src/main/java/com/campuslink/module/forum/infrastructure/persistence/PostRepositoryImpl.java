package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.domain.gateway.PostRepository;
import com.campuslink.module.forum.domain.model.HotScoreInput;
import com.campuslink.module.forum.domain.model.Post;
import com.campuslink.module.forum.domain.model.PostSortOrder;
import com.campuslink.module.forum.domain.model.PostStatus;
import com.campuslink.module.forum.infrastructure.persistence.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 适配器：PostRepository 端口的 MyBatis-Plus 实现 */
@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final PostMapper postMapper;

    @Override
    public PageResult<Post> findPage(Long boardId, PostSortOrder sort, int page, int size) {
        LambdaQueryWrapper<PostDO> query = new LambdaQueryWrapper<>();
        if (boardId != null) {
            query.eq(PostDO::getBoardId, boardId);
        }
        query.eq(PostDO::getStatus, PostStatus.PUBLISHED.name())
                .eq(PostDO::getIsDeleted, false);
        if (sort == PostSortOrder.HOT) {
            query.orderByDesc(PostDO::getHotScore);
        }
        query.orderByDesc(PostDO::getCreatedAt)
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
    public boolean markDeleted(Long postId) {
        // 条件带 is_deleted=0：受影响行数即"这次是否真的删掉了它"，并发二次删除拿到 false 而不重复记审计
        return postMapper.update(null, new LambdaUpdateWrapper<PostDO>()
                .set(PostDO::getIsDeleted, true)
                .eq(PostDO::getId, postId)
                .eq(PostDO::getIsDeleted, false)) > 0;
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

    @Override
    public List<HotScoreInput> findHotCandidates(Instant since, int limit, Long afterId) {
        LambdaQueryWrapper<PostDO> query = new LambdaQueryWrapper<PostDO>()
                .select(PostDO::getId, PostDO::getReplyCount, PostDO::getLikeCount,
                        PostDO::getFavoriteCount, PostDO::getCreatedAt)
                .eq(PostDO::getStatus, PostStatus.PUBLISHED.name())
                .eq(PostDO::getIsDeleted, false)
                .ge(PostDO::getCreatedAt, since)
                .orderByAsc(PostDO::getId);
        if (afterId != null) {
            query.gt(PostDO::getId, afterId);
        }
        // searchCount=false：游标分批只需要这一批的数据，总数由调用方按"取满即继续"推进
        return postMapper.selectPage(new Page<>(1, limit, false), query).getRecords().stream()
                .map(d -> new HotScoreInput(d.getId(), d.getReplyCount(), d.getLikeCount(),
                        d.getFavoriteCount(), d.getCreatedAt()))
                .toList();
    }

    @Override
    public void updateHotScore(Long postId, double score) {
        // 定向单列 UPDATE：绝不整行回写，否则会覆盖刷新期间并发变化的三个互动计数
        postMapper.update(null, new LambdaUpdateWrapper<PostDO>()
                .set(PostDO::getHotScore, score)
                .eq(PostDO::getId, postId));
    }

    @Override
    public int resetHotScoresBefore(Instant since) {
        // 只碰"已经不是候选"且分数非 0 的行：窗口外的老帖、已删除、非 PUBLISHED
        return postMapper.update(null, new LambdaUpdateWrapper<PostDO>()
                .set(PostDO::getHotScore, 0d)
                .ne(PostDO::getHotScore, 0d)
                .and(notCandidate -> notCandidate
                        .eq(PostDO::getIsDeleted, true)
                        .or().ne(PostDO::getStatus, PostStatus.PUBLISHED.name())
                        .or().lt(PostDO::getCreatedAt, since)));
    }
}
