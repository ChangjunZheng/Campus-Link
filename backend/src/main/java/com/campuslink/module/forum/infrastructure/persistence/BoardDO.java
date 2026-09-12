package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/** boards 表数据对象（MyBatis-Plus）。本类属基础设施，不进入领域层——互转见 {@link BoardConverter}。 */
@Data
@TableName("boards")
public class BoardDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;
    private String name;
    private String description;
    private String type;
    private Integer sort;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
}
