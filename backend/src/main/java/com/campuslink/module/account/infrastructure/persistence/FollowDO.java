package com.campuslink.module.account.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/** follows 表数据对象（CR-074）：联合主键 (follower_id, followee_id)，无代理 id 列 */
@Data
@TableName("follows")
public class FollowDO {

    private Long followerId;
    private Long followeeId;
    private Instant createdAt;
}
