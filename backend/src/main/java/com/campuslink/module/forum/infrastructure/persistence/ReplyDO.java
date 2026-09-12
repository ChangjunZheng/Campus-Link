package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/** replies 表数据对象（MyBatis-Plus）。本类属基础设施，不进入领域层——互转见 {@link ReplyConverter}。 */
@Data
@TableName("replies")
public class ReplyDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private Long authorId;
    private Integer floorNo;
    private String contentMd;
    private String contentHtml;
    private Long quotedReplyId;
    private Integer likeCount;
    private Boolean isAccepted;
    private String status;
    private Boolean isDeleted;
    private Instant createdAt;
    private Instant updatedAt;
}
