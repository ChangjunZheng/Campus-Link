package com.campuslink.module.forum.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campuslink.module.forum.infrastructure.persistence.PostDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** posts 表 Mapper（包名约束见 {@link BoardMapper}） */
public interface PostMapper extends BaseMapper<PostDO> {

    /**
     * 站内搜索（F-FORUM-008）：标题走 V1 已就位的 {@code FULLTEXT ft_posts_title}（ngram 分词，中文 2 字起可命中），
     * 标签走 {@code posts.tags} 冗余列 LIKE 兜底（该列当前无数据，有值即生效）；相关度得分随 MATCH 产生，
     * 排序 {@code 得分 DESC, created_at DESC, id DESC}。{@code keyword} 由应用层校验长度，此处只参数化传入。
     */
    @Select("""
            <script>
            SELECT *, MATCH(title) AGAINST(#{keyword}) AS relevance
            FROM posts
            WHERE status = 'PUBLISHED' AND is_deleted = 0
              AND (MATCH(title) AGAINST(#{keyword}) OR tags LIKE CONCAT('%', #{keyword}, '%'))
              <if test="boardId != null">AND board_id = #{boardId}</if>
              <if test="days != null">AND created_at &gt;= DATE_SUB(NOW(), INTERVAL #{days} DAY)</if>
            ORDER BY relevance DESC, created_at DESC, id DESC
            </script>
            """)
    IPage<PostDO> search(IPage<PostDO> page,
                         @Param("keyword") String keyword,
                         @Param("boardId") Long boardId,
                         @Param("days") Integer days);

    /**
     * Similar-post recommendation (publish-time assist): full-text search on title (ngram),
     * restricted to PUBLISHED + not-deleted posts in the given board, excluding the caller's own posts.
     * Results ordered by relevance DESC, created_at DESC, id DESC, capped at {@code limit}.
     *
     * <p>显式列清单（CR-077 Warning #7）：只取推荐条目需要的 6 列，避免 SELECT * 拉出 content_md / content_html 等大字段。
     */
    @Select("""
            SELECT id, board_id, title, reply_count, is_accepted, created_at,
                   MATCH(title) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) AS relevance
            FROM posts
            WHERE status = 'PUBLISHED' AND is_deleted = 0
              AND board_id = #{boardId}
              AND author_id != #{excludeAuthorId}
              AND MATCH(title) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE)
            ORDER BY relevance DESC, created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<PostDO> findSimilar(@Param("keyword") String keyword,
                             @Param("boardId") Long boardId,
                             @Param("excludeAuthorId") Long excludeAuthorId,
                             @Param("limit") int limit);
}
