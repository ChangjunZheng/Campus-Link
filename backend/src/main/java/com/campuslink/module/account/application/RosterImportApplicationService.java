package com.campuslink.module.account.application;

import com.campuslink.common.audit.AuditService;
import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ResultCode;
import com.campuslink.module.account.application.cmd.RosterImportResult;
import com.campuslink.module.account.domain.gateway.RosterRepository;
import com.campuslink.module.account.domain.gateway.SensitiveCodec;
import com.campuslink.module.account.domain.model.StudentRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 学籍名册 CSV 导入用例（技术方案 4.3）：“学号,姓名[,年级[,专业]]”，首行表头自动跳过；
 * 名册哈希后入库（saveIfAbsent 跳过已存在学号），导入操作写审计日志（合规硬要求）。
 * 始终写库，不受 bypass 策略影响（bypass 只切换核验查询来源）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RosterImportApplicationService {

    private final RosterRepository rosterRepository;
    private final SensitiveCodec codec;
    private final AuditService auditService;

    @Transactional
    public RosterImportResult importCsv(List<String> lines, String batch, Long operatorId) {
        int inserted = 0;
        int skipped = 0;
        int failed = 0;
        boolean firstLine = true;

        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            String[] cols = line.trim().split(",", -1);
            if (firstLine) {
                firstLine = false;
                if (cols.length > 0 && cols[0].contains("学号")) {
                    continue;
                }
            }
            if (cols.length < 2 || cols[0].isBlank() || cols[1].isBlank()) {
                failed++;
                continue;
            }
            String studentId = cols[0].trim();
            String name = cols[1].trim();
            if (name.isEmpty()) {
                failed++;
                continue;
            }
            StudentRecord record = new StudentRecord(
                    null,
                    codec.hash(studentId),
                    name,
                    cols.length > 2 && !cols[2].isBlank() ? cols[2].trim() : null,
                    cols.length > 3 && !cols[3].isBlank() ? cols[3].trim() : null,
                    batch,
                    null);
            if (rosterRepository.saveIfAbsent(record)) {
                inserted++;
            } else {
                skipped++;
            }
        }

        auditService.record(operatorId, "ROSTER_IMPORT", "student_roster", null,
                "batch=%s inserted=%d skipped=%d failed=%d".formatted(batch, inserted, skipped, failed));
        return new RosterImportResult(inserted, skipped, failed);
    }
}
