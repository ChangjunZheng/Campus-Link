package com.campuslink.module.account.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/** student_roster 表数据对象（学号仅存 HMAC 哈希，技术方案 4.3） */
@Data
@TableName("student_roster")
public class StudentRosterDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String studentIdHash;
    private String name;
    private String grade;
    private String department;
    private Long usedUserId;
    private String sourceBatch;
    private Instant createdAt;
    private Instant updatedAt;
}
