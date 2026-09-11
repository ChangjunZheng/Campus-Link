package com.campuslink.module.account.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.module.account.infrastructure.persistence.StudentRosterDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface StudentRosterMapper extends BaseMapper<StudentRosterDO> {

    /** 原子占用学号：仅当未被占用时绑定用户，保证“一号一账号”（并发安全） */
    @Update("UPDATE student_roster SET used_user_id = #{userId} "
            + "WHERE student_id_hash = #{hash} AND used_user_id IS NULL")
    int occupy(@Param("hash") String hash, @Param("userId") Long userId);
}
