package com.campuslink.module.account.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.module.account.domain.gateway.RosterRepository;
import com.campuslink.module.account.domain.model.StudentRecord;
import com.campuslink.module.account.infrastructure.persistence.mapper.StudentRosterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/** 适配器：RosterRepository 端口的 MyBatis-Plus 实现（名册导入写入，始终落库） */
@Repository
@RequiredArgsConstructor
public class RosterRepositoryImpl implements RosterRepository {

    private final StudentRosterMapper rosterMapper;

    @Override
    public boolean saveIfAbsent(StudentRecord record) {
        Long exists = rosterMapper.selectCount(new LambdaQueryWrapper<StudentRosterDO>()
                .eq(StudentRosterDO::getStudentIdHash, record.studentIdHash()));
        if (exists != null && exists > 0) {
            return false;
        }
        StudentRosterDO d = new StudentRosterDO();
        d.setStudentIdHash(record.studentIdHash());
        d.setName(record.name());
        d.setGrade(record.grade());
        d.setDepartment(record.department());
        d.setSourceBatch(record.sourceBatch());
        rosterMapper.insert(d);
        return true;
    }
}
