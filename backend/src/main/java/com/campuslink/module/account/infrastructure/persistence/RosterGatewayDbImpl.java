package com.campuslink.module.account.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.module.account.domain.gateway.RosterGateway;
import com.campuslink.module.account.domain.model.StudentRecord;
import com.campuslink.module.account.infrastructure.persistence.mapper.StudentRosterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 策略实现（生产）：MySQL 名册表核验与占用。
 * 装配条件 app.roster.bypass=false；生产环境必须为 false（上线检查清单项）。
 */
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "campuslink.roster.bypass", havingValue = "false")
public class RosterGatewayDbImpl implements RosterGateway {

    private final StudentRosterMapper rosterMapper;

    @Override
    public Optional<StudentRecord> findAvailableByHash(String studentIdHash) {
        return Optional.ofNullable(rosterMapper.selectOne(new LambdaQueryWrapper<StudentRosterDO>()
                        .eq(StudentRosterDO::getStudentIdHash, studentIdHash)))
                .filter(this::isAvailable)
                .map(this::toRecord);
    }

    @Override
    public boolean occupy(String studentIdHash, Long accountId) {
        return rosterMapper.occupy(studentIdHash, accountId) == 1;
    }

    private boolean isAvailable(StudentRosterDO d) {
        return d.getUsedUserId() == null;
    }

    private StudentRecord toRecord(StudentRosterDO d) {
        return new StudentRecord(d.getId(), d.getStudentIdHash(), d.getName(),
                d.getGrade(), d.getDepartment(), d.getSourceBatch(), d.getUsedUserId());
    }
}
