package com.campuslink.module.forum.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campuslink.module.forum.infrastructure.persistence.MyReplyDO;
import com.campuslink.module.forum.infrastructure.persistence.ReplyDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** replies 表 Mapper（包名约束见 {@link BoardMapper}） */
public interface ReplyMapper extends BaseMapper<ReplyDO> {

    /**
     * "我的回帖"分页（F-ACC-007c）：楼层与**父帖的可见性在同一条 SQL 里判定**，
     * 父帖已自删（{@code p.is_deleted=1}）或被平台下架（{@code p.status<>'PUBLISHED'}）时整条不出现——
     * 与 {@code ForumQueryApplicationService#requireVisiblePost} 及 CR-066 收口的通知侧同源
     * （"标题回落了、链接还可点"是本仓抓过两次的缺陷模式，此处不能再犯第三次）。
     *
     * <p>为什么不用两次查询在内存里拼：那样 {@code total} 是楼层侧的数、条目却是过滤后的，
     * 二者必然不同源（端口 javadoc 的"total 与查询条件同源"判据）。分页交给 MyBatis-Plus 的
     * {@code IPage} + 分页插件，写法照 {@code PostMapper#search} 先例。
     *
     * <p>楼层**自身**的 {@code status} 不过滤、随列一起返回：作者要能看到"我哪一层被下架了"（可逆性判据）。
     */
    @Select("""
            SELECT r.id, r.post_id, r.floor_no, r.content_md, r.status, r.created_at,
                   p.title AS post_title
            FROM replies r
            JOIN posts p ON p.id = r.post_id
            WHERE r.author_id = #{authorId}
              AND r.is_deleted = 0
              AND p.is_deleted = 0
              AND p.status = 'PUBLISHED'
            ORDER BY r.created_at DESC, r.id DESC
            """)
    IPage<MyReplyDO> findPageByAuthor(IPage<MyReplyDO> page, @Param("authorId") Long authorId);
}
