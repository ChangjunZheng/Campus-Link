package com.campuslink.module.account.domain.service;

import com.campuslink.module.account.domain.gateway.RosterGateway;
import com.campuslink.module.account.domain.gateway.SensitiveCodec;
import com.campuslink.module.account.domain.model.StudentId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 学籍核验领域服务（技术方案 4.3 / PRD F-ACC-004）：
 * 比对规则 = 名册按学号哈希定位 + 姓名归一化容错匹配 + 未占用。
 * 三种失败（学号不存在 / 姓名不匹配 / 已注册）由应用层统一收敛为同一提示，防名册枚举。
 */
@Service
@RequiredArgsConstructor
public class StudentVerificationService {

    private final RosterGateway rosterGateway;
    private final SensitiveCodec codec;

    public boolean matches(StudentId studentId, String rawName) {
        return rosterGateway.findAvailableByHash(codec.hash(studentId.value()))
                .map(record -> record.matchesName(rawName))
                .orElse(false);
    }
}
