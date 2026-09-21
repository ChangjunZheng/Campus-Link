package com.campuslink.module.forum.web.vo;

import com.campuslink.module.forum.application.cmd.ForumResults.SimilarPostResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Similar-post recommendation item (publish-time assist).
 * Exposes only the minimal fields needed to judge whether an equivalent question already exists.
 */
@Schema(description = "相似帖子推荐条目")
public record SimilarPostVo(
        Long id,
        String title,
        String boardCode,
        String boardName,
        int replyCount,
        boolean accepted,
        Instant createdAt
) {
    public static SimilarPostVo from(SimilarPostResult r) {
        return new SimilarPostVo(r.id(), r.title(), r.boardCode(), r.boardName(),
                r.replyCount(), r.accepted(), r.createdAt());
    }
}
