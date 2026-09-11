package com.campuslink.module.account.infrastructure.memory;

import com.campuslink.module.account.domain.gateway.RosterGateway;
import com.campuslink.module.account.domain.gateway.SensitiveCodec;
import com.campuslink.module.account.domain.model.StudentRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

/**
 * 策略实现（开发联调）：内置测试名册（app.roster.bypass=true 时装配，缺省生效）。
 * 与生产名册表实现互斥（策略模式），占用不做持久化（仅联调语义，允许重复核验）；
 * 生产环境 bypass 必须 false（上线检查清单项，见 AGENTS.md 红线）。
 */
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "campuslink.roster.bypass", havingValue = "true", matchIfMissing = true)
public class InMemoryTestRosterGateway implements RosterGateway {

    private static final Map<String, String> TEST_ROSTER = Map.of(
            "2023001", "张三",
            "2023002", "李四",
            "2023003", "王五",
            "2024001", "赵六");

    private final SensitiveCodec codec;

    @Override
    public Optional<StudentRecord> findAvailableByHash(String studentIdHash) {
        return TEST_ROSTER.entrySet().stream()
                .filter(entry -> codec.hash(entry.getKey()).equals(studentIdHash))
                .findFirst()
                .map(entry -> new StudentRecord(null, studentIdHash, entry.getValue(),
                        null, null, "builtin-test", null));
    }

    @Override
    public boolean occupy(String studentIdHash, Long accountId) {
        return true;
    }
}
