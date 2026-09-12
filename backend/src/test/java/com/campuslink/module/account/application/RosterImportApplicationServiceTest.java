package com.campuslink.module.account.application;

import com.campuslink.common.audit.AuditService;
import com.campuslink.module.account.application.cmd.RosterImportResult;
import com.campuslink.module.account.domain.gateway.RosterRepository;
import com.campuslink.module.account.domain.gateway.SensitiveCodec;
import com.campuslink.module.account.domain.model.StudentRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 名册导入回归单测（N-6，CR-028）：表头识别不止中文「学号」，
 * 且逐行校验学号 {@code ^\d{9}$} —— 非法行必须计入 failed 且**不入库**。
 *
 * <p>回归背景：表头识别只认「学号」二字，英文表头（如 {@code studentId}）会被当成第一行学籍记录；
 * 同时任意字符串都能作为学号落库，名册存在脏数据风险。
 */
@ExtendWith(MockitoExtension.class)
class RosterImportApplicationServiceTest {

    @Mock
    private RosterRepository rosterRepository;
    @Mock
    private SensitiveCodec codec;
    @Mock
    private AuditService auditService;

    private RosterImportApplicationService service;

    @BeforeEach
    void setUp() {
        service = new RosterImportApplicationService(rosterRepository, codec, auditService);
    }

    /** 名册写入端口行为：哈希可预期 + 默认按"新学号"接收 */
    private void stubAcceptsNewIds() {
        when(codec.hash(anyString())).thenAnswer(inv -> "H(" + inv.getArgument(0) + ")");
        when(rosterRepository.saveIfAbsent(any(StudentRecord.class))).thenReturn(true);
    }

    @Test
    @DisplayName("中文表头「学号,姓名」跳过，数据行正常入库")
    void skipsChineseHeader() {
        stubAcceptsNewIds();

        RosterImportResult result = service.importCsv(
                List.of("学号,姓名,年级,专业", "249971346,张三,2024级,计算机"), "batch-1", 1L);

        assertThat(result.inserted()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
        assertThat(result.failed()).isZero();
    }

    @ParameterizedTest
    @DisplayName("英文表头别名（大小写与分隔符不敏感）同样跳过，不会被当成学籍记录")
    @ValueSource(strings = {"studentId,Name", "student_id,name", "Student ID,Name", "STUDENT-ID,Name"})
    void skipsEnglishHeaderAliases(String header) {
        stubAcceptsNewIds();

        RosterImportResult result = service.importCsv(List.of(header, "249971346,张三"), "batch-1", 1L);

        assertThat(result.inserted()).isEqualTo(1);
        assertThat(result.failed()).isZero();
        ArgumentCaptor<StudentRecord> captor = ArgumentCaptor.forClass(StudentRecord.class);
        verify(rosterRepository).saveIfAbsent(captor.capture());
        assertThat(captor.getValue().studentIdHash()).isEqualTo("H(249971346)");
    }

    @Test
    @DisplayName("学号非 9 位数字的行计入 failed 且不入库")
    void rejectsNonNineDigitStudentId() {
        stubAcceptsNewIds();

        RosterImportResult result = service.importCsv(List.of(
                "学号,姓名",
                "249971346,张三",
                "12345,李四",
                "24997134a,王五",
                "2499713460,赵六"), "batch-1", 1L);

        assertThat(result.inserted()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(3);
        ArgumentCaptor<StudentRecord> captor = ArgumentCaptor.forClass(StudentRecord.class);
        verify(rosterRepository).saveIfAbsent(captor.capture());
        assertThat(captor.getAllValues()).hasSize(1);
        assertThat(captor.getValue().studentIdHash()).isEqualTo("H(249971346)");
    }

    @Test
    @DisplayName("列数不足或学号/姓名为空的行走 failed，不触发写库")
    void countsMissingColumnsAsFailed() {
        RosterImportResult result = service.importCsv(
                List.of("249971346", "249971346,", ",张三"), "batch-1", 1L);

        assertThat(result.inserted()).isZero();
        assertThat(result.failed()).isEqualTo(3);
        verify(rosterRepository, never()).saveIfAbsent(any(StudentRecord.class));
    }

    @Test
    @DisplayName("空行（含 null）整体跳过，不计入任何统计")
    void ignoresBlankLines() {
        stubAcceptsNewIds();

        RosterImportResult result = service.importCsv(
                Arrays.asList(null, "", "   ", "学号,姓名", "249971346,张三"), "batch-1", 1L);

        assertThat(result.inserted()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
        assertThat(result.failed()).isZero();
    }

    @Test
    @DisplayName("名册已存在的学号计入 skipped（saveIfAbsent 返回 false）")
    void countsExistingStudentIdAsSkipped() {
        when(codec.hash(anyString())).thenAnswer(inv -> "H(" + inv.getArgument(0) + ")");
        when(rosterRepository.saveIfAbsent(any(StudentRecord.class))).thenReturn(false);

        RosterImportResult result = service.importCsv(List.of("249971346,张三"), "batch-1", 1L);

        assertThat(result.inserted()).isZero();
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.failed()).isZero();
    }

    @Test
    @DisplayName("全部行失败也照写审计日志（合规硬要求），detail 带批次与计数")
    void writesAuditLogEvenWhenAllRowsFail() {
        RosterImportResult result = service.importCsv(List.of("bad-row"), "batch-x", 1L);

        assertThat(result.failed()).isEqualTo(1);
        verify(auditService).record(eq(1L), eq("ROSTER_IMPORT"), eq("student_roster"),
                isNull(), eq("batch=batch-x inserted=0 skipped=0 failed=1"));
    }
}
