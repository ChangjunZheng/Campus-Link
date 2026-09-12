package com.campuslink.module.forum.domain.model;

import lombok.Getter;

import java.time.Instant;

/**
 * 楼层回复（独立聚合，经 post_id 关联帖子）。
 *
 * <p>楼层口径（设计 §4.1，D-1）：**floor_no = 回复序号**，第一条回复为 1 楼，帖子本体不占楼层号。
 * 楼层号不与帖子挂钩，由 application 通过 {@code PostRepository#incrementReplyCountAndGet} 分配后传入。
 */
@Getter
public class Reply {

    private final Long id;
    private final Long postId;
    private final Long authorId;
    private final int floorNo;
    private final String contentMd;
    private final String contentHtml;
    private final Instant createdAt;

    private Reply(Long id, Long postId, Long authorId, int floorNo, String contentMd,
                  String contentHtml, Instant createdAt) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.floorNo = floorNo;
        this.contentMd = contentMd;
        this.contentHtml = contentHtml;
        this.createdAt = createdAt;
    }

    /** 发布回复：floorNo 已由调用方分配（见类注释），contentHtml 已渲染 */
    public static Reply post(Long postId, Long authorId, int floorNo, String contentMd, String contentHtml) {
        return new Reply(null, postId, authorId, floorNo, contentMd, contentHtml, null);
    }

    /** 仓储重建入口（infrastructure 适配器调用） */
    public static Reply rehydrate(Long id, Long postId, Long authorId, int floorNo, String contentMd,
                                  String contentHtml, Instant createdAt) {
        return new Reply(id, postId, authorId, floorNo, contentMd, contentHtml, createdAt);
    }
}
