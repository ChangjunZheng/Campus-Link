package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.module.forum.domain.gateway.LikeRepository;
import com.campuslink.module.forum.domain.model.LikeTargetType;
import com.campuslink.module.forum.infrastructure.persistence.mapper.LikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

/** 适配器：LikeRepository 端口的 MyBatis-Plus 实现 */
@Repository
@RequiredArgsConstructor
public class LikeRepositoryImpl implements LikeRepository {

    private final LikeMapper likeMapper;

    @Override
    public boolean toggle(long userId, LikeTargetType targetType, long targetId) {
        // 主键唯一约束兜底并发双击：插入撞键即已赞 → 走删除分支，两次并发 toggle 中恰有一次生效
        try {
            LikeDO d = new LikeDO();
            d.setUserId(userId);
            d.setTargetType(targetType.name());
            d.setTargetId(targetId);
            likeMapper.insert(d);
            return true;
        } catch (DuplicateKeyException e) {
            likeMapper.delete(new LambdaQueryWrapper<LikeDO>()
                    .eq(LikeDO::getUserId, userId)
                    .eq(LikeDO::getTargetType, targetType.name())
                    .eq(LikeDO::getTargetId, targetId));
            return false;
        }
    }

    @Override
    public Set<Long> findLikedTargetIds(long userId, LikeTargetType targetType, Collection<Long> targetIds) {
        if (targetIds.isEmpty()) {
            return Set.of();
        }
        return likeMapper.selectList(new LambdaQueryWrapper<LikeDO>()
                        .eq(LikeDO::getUserId, userId)
                        .eq(LikeDO::getTargetType, targetType.name())
                        .in(LikeDO::getTargetId, targetIds)).stream()
                .map(LikeDO::getTargetId)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
