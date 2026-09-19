package com.campuslink.module.forum.infrastructure.persistence;

import lombok.Data;

import java.time.Instant;

/**
 * "我的回帖"查询行（F-ACC-007c）：{@code replies} JOIN {@code posts} 的**只读结果**，不对应单张表，
 * 故不加 {@code @TableName}、也不复用 {@link ReplyDO}（父帖标题塞进楼层 DO 会让"哪列属于哪张表"含糊）。
 * 列名经 {@code map-underscore-to-camel-case} 自动映射；转领域载体见 {@code ReplyRepositoryImpl}。
 */
@Data
public class MyReplyDO {

    private Long id;
    private Long postId;
    private String postTitle;
    private Integer floorNo;
    private String contentMd;
    private String status;
    private Instant createdAt;
}
