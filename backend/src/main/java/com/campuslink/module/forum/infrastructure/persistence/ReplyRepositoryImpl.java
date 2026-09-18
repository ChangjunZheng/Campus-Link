package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.domain.gateway.ReplyRepository;
import com.campuslink.module.forum.domain.model.Reply;
import com.campuslink.module.forum.infrastructure.persistence.mapper.ReplyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 适配器：ReplyRepository 端口的 MyBatis-Plus 实现 */
@Repository
@RequiredArgsConstructor
public class ReplyRepositoryImpl implements ReplyRepository {

    /** replies.status 与 posts.status 同一词表；本 Sprint 不建 ReplyStatus 枚举，过滤条件下推至此 */
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    private final ReplyMapper replyMapper;

    @Override
    public PageResult<Reply> findPageByPostId(Long postId, int page, int size) {
        IPage<ReplyDO> result = replyMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<ReplyDO>()
                .eq(ReplyDO::getPostId, postId)
                .eq(ReplyDO::getStatus, STATUS_PUBLISHED)
                .eq(ReplyDO::getIsDeleted, false)
                // 最佳答案置顶（F-QA-001）：is_accepted 至多一行为 1，其余按楼层号升序，分页口径不变
                .orderByDesc(ReplyDO::getIsAccepted)
                .orderByAsc(ReplyDO::getFloorNo));
        return new PageResult<>(result.getRecords().stream().map(ReplyConverter::toDomain).toList(),
                result.getTotal(), page, size);
    }

    @Override
    public Optional<Reply> findVisibleById(Long id) {
        // 与 findPageByPostId 同源的双条件：已下架（status=REMOVED）与已删除都不应对写用例暴露（CR-060 / BUG-002）
        return Optional.ofNullable(replyMapper.selectOne(new LambdaQueryWrapper<ReplyDO>()
                        .eq(ReplyDO::getId, id)
                        .eq(ReplyDO::getStatus, STATUS_PUBLISHED)
                        .eq(ReplyDO::getIsDeleted, false)))
                .map(ReplyConverter::toDomain);
    }

    @Override
    public List<Reply> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return replyMapper.selectList(new LambdaQueryWrapper<ReplyDO>()
                        .in(ReplyDO::getId, ids)
                        .eq(ReplyDO::getIsDeleted, false)).stream()
                .map(ReplyConverter::toDomain)
                .toList();
    }

    @Override
    public void updateAcceptedFlags(Long postId, Long newlyAcceptedReplyId) {
        replyMapper.update(null, new LambdaUpdateWrapper<ReplyDO>()
                .set(ReplyDO::getIsAccepted, false)
                .eq(ReplyDO::getPostId, postId)
                .eq(ReplyDO::getIsAccepted, true));
        replyMapper.update(null, new LambdaUpdateWrapper<ReplyDO>()
                .set(ReplyDO::getIsAccepted, true)
                .eq(ReplyDO::getId, newlyAcceptedReplyId));
    }

    @Override
    public Reply save(Reply reply) {
        ReplyDO d = ReplyConverter.toDo(reply);
        replyMapper.insert(d);
        return ReplyConverter.toDomain(d);
    }

    @Override
    public int adjustLikeCount(Long replyId, int delta) {
        replyMapper.update(null, new LambdaUpdateWrapper<ReplyDO>()
                .setSql("like_count = like_count + " + delta)
                .eq(ReplyDO::getId, replyId));
        return replyMapper.selectById(replyId).getLikeCount();
    }
}
