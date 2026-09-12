package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.domain.gateway.ReplyRepository;
import com.campuslink.module.forum.domain.model.Reply;
import com.campuslink.module.forum.infrastructure.persistence.mapper.ReplyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
                .orderByAsc(ReplyDO::getFloorNo));
        return new PageResult<>(result.getRecords().stream().map(ReplyConverter::toDomain).toList(),
                result.getTotal(), page, size);
    }

    @Override
    public Reply save(Reply reply) {
        ReplyDO d = ReplyConverter.toDo(reply);
        replyMapper.insert(d);
        return ReplyConverter.toDomain(d);
    }
}
