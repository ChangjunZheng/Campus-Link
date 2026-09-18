package com.campuslink.module.forum.domain.gateway;

import com.campuslink.module.forum.domain.model.Reply;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 回复仓储端口（端口定义在 domain、实现在 infrastructure，DIP） */
public interface ReplyRepository {

    /** 某帖的楼层分页：固定 {@code status='PUBLISHED' AND is_deleted=0}，按 is_accepted DESC、floor_no ASC（F-QA-001 最佳答案置顶，设计 §3.5） */
    PageResult<Reply> findPageByPostId(Long postId, int page, int size);

    /**
     * 按 id 查**可见**的回复（{@code status='PUBLISHED' AND is_deleted=0}，与 {@link #findPageByPostId}
     * 的列表过滤**同源**）；不可见（已下架 / 已删除）一律按"不存在"处理。
     *
     * <p>供两条楼层写用例核验目标存在且可见：楼层点赞与采纳最佳答案（F-FORUM-005 / F-QA-001）。
     * 可见性守卫**只放在读侧这一处**，计数更新（{@link #adjustLikeCount}）不再重复表达同一条件；
     * {@code Reply} 聚合刻意不带 {@code status}（无业务规则消费它），故过滤条件下推至 SQL 适配器。
     */
    Optional<Reply> findVisibleById(Long id);

    /**
     * 批量按 id 查**未删除**的回复（只过滤 {@code is_deleted=0}，**不过滤 status**）；通知读时组装用（F-SOC-001），
     * 一页一次取齐所属帖与楼层号，不做 N+1。
     *
     * <p>与 {@link #findVisibleById} 的差别是刻意的：通知要能指出一条已下架楼层"在第几楼"，
     * 在此过滤 status 会让引用失去落点；写用例一律走 {@link #findVisibleById}。
     */
    List<Reply> findByIds(Collection<Long> ids);

    /**
     * 采纳标志切换（F-QA-001）：先清该帖旧的已采纳楼层，再把指定回复标为已采纳——两条 UPDATE
     * 由调用方事务包裹；并发采纳以 posts 行锁串行化（见 PostRepository#updateAcceptedReply）。
     */
    void updateAcceptedFlags(Long postId, Long newlyAcceptedReplyId);

    /** 新增回复（INSERT）；返回带数据库生成 id 与时间戳的聚合 */
    Reply save(Reply reply);

    /** 楼层点赞计数增减（F-FORUM-005，delta 为 ±1）；返回增减后的 like_count，供响应回显 */
    int adjustLikeCount(Long replyId, int delta);
}
