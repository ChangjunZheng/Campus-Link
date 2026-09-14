package com.campuslink.module.forum.domain.gateway;

import com.campuslink.module.forum.domain.model.Post;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 帖子仓储端口（端口定义在 domain、实现在 infrastructure，DIP） */
public interface PostRepository {

    /**
     * 可见帖子分页：{@code boardId} 为 null 表示全站最新；固定 {@code status='PUBLISHED' AND is_deleted=0}，
     * 按 created_at DESC（id 兜底，避免同秒创建时翻页错位）。
     *
     * <p>与 {@link #findById} 不同，这里在 SQL 侧一并过滤 status——列表的 total 必须与查询条件同源，
     * 不能在内存里剔除后再算页数。
     */
    PageResult<Post> findPage(Long boardId, int page, int size);

    /**
     * 站内搜索（F-FORUM-008）：标题全文（ngram）+ tags 冗余列 LIKE 兜底，只搜 PUBLISHED 且未删除，
     * 按相关度得分 DESC、created_at DESC 排序。{@code boardId} 为 null 表示全站；{@code days} 为 null 表示不限时间窗。
     */
    PageResult<Post> search(String keyword, Long boardId, Integer days, int page, int size);

    /**
     * 按 id 查**未删除**的帖子（{@code is_deleted=1} 视为不存在，属仓储读侧约定：软删是行级墓碑，
     * 不进入聚合）；{@code status} 是否可见由应用层按 {@link Post#isVisible()} 判定。
     */
    Optional<Post> findById(Long id);

    /**
     * 批量按 id 查**未删除**的帖子（读侧约定同 {@link #findById}）；通知读时组装用（F-SOC-001），
     * 一页一次取齐标题，不做 N+1。空集合由实现短路，不会发出 {@code IN ()} 非法 SQL。
     */
    List<Post> findByIds(Collection<Long> ids);

    /** 新增帖子（INSERT）；返回带数据库生成 id 与时间戳的聚合 */
    Post save(Post post);

    /**
     * 分配楼层号（设计 §4.1，本 Sprint 唯一并发点）：在同一事务内先把 {@code posts.reply_count} 自增
     * （行锁先行，使同一帖子的并发回帖被串行化），再读回自增后的值作为本次回复的 floor_no。
     *
     * <p>为什么不加 {@code uk(post_id, floor_no)}：需新增迁移，与「本 Sprint 零迁移」冲突；
     * 该取舍登记在 sprint-2-design §6（D-2）。返回值为 floor_no。
     */
    int incrementReplyCountAndGet(Long postId);

    /**
     * 采纳最佳答案（F-QA-001）：定向 UPDATE {@code posts.is_accepted=1, accepted_reply_id=?}。
     * 与 {@link #incrementReplyCountAndGet} 同理走定向 SQL 而非整行回写——聚合无整行状态需要持久化，
     * 且 UPDATE posts 行锁先行使同一帖子的并发采纳在数据库层串行化（后写覆盖前写，即"可更换"语义）。
     */
    void updateAcceptedReply(Long postId, Long replyId);

    /** 点赞计数增减（F-FORUM-005，delta 为 ±1）；返回增减后的 like_count，供响应回显 */
    int adjustLikeCount(Long postId, int delta);

    /** 收藏计数增减（F-FORUM-005，delta 为 ±1）；返回增减后的 favorite_count */
    int adjustFavoriteCount(Long postId, int delta);
}
