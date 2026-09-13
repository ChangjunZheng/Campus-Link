package com.campuslink.module.forum.domain.gateway;

import com.campuslink.module.forum.domain.model.Reply;

import java.util.Optional;

/** 回复仓储端口（端口定义在 domain、实现在 infrastructure，DIP） */
public interface ReplyRepository {

    /** 某帖的楼层分页：固定 {@code status='PUBLISHED' AND is_deleted=0}，按 is_accepted DESC、floor_no ASC（F-QA-001 最佳答案置顶，设计 §3.5） */
    PageResult<Reply> findPageByPostId(Long postId, int page, int size);

    /** 按 id 查**未删除**的回复（读侧约定与 PostRepository.findById 相同）；采纳用例用它核验回复存在且属于该帖 */
    Optional<Reply> findById(Long id);

    /**
     * 采纳标志切换（F-QA-001）：先清该帖旧的已采纳楼层，再把指定回复标为已采纳——两条 UPDATE
     * 由调用方事务包裹；并发采纳以 posts 行锁串行化（见 PostRepository#updateAcceptedReply）。
     */
    void updateAcceptedFlags(Long postId, Long newlyAcceptedReplyId);

    /** 新增回复（INSERT）；返回带数据库生成 id 与时间戳的聚合 */
    Reply save(Reply reply);
}
