package com.campuslink.module.account.domain;

import com.campuslink.module.account.domain.model.EmailAddress;
import com.campuslink.module.account.domain.model.StudentId;
import com.campuslink.module.account.domain.model.StudentRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 领域模型单测：值对象规范化与名册比对规则（DDD 重构后领域层可纯单测，不依赖 Spring 容器）。
 */
class AccountDomainModelTest {

    @Test
    @DisplayName("EmailAddress 值对象：trim + 小写规范化，空白被拒绝")
    void emailAddressNormalized() {
        assertThat(EmailAddress.of("  Xiao.Lin@EXAMPLE.com ").value()).isEqualTo("xiao.lin@example.com");
        assertThatThrownBy(() -> EmailAddress.of("   ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("StudentId 值对象：trim 规范化，且必须为 9 位数字（CR-013）")
    void studentIdNormalized() {
        assertThat(StudentId.of(" 249971346 ").value()).isEqualTo("249971346");
        assertThatThrownBy(() -> StudentId.of(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("StudentId 值对象：非 9 位数字一律拒绝")
    void studentIdRejectsWrongFormat() {
        // 旧测试名册的 7 位学号、常见错位长度、含字母、含符号，都应被格式约束挡下
        assertThatThrownBy(() -> StudentId.of("2023001")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StudentId.of("24997134")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StudentId.of("2499713460")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StudentId.of("24997134a")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StudentId.of("24997-1346")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("名册记录：姓名归一化容错比对")
    void studentRecordMatchesName() {
        StudentRecord record = new StudentRecord(1L, "hash", "张三", "2023级", "软件工程", "manual", null);
        assertThat(record.matchesName("张三")).isTrue();
        assertThat(record.matchesName(" 张三 ")).isTrue();
        assertThat(record.matchesName("张 三")).isTrue();
        assertThat(record.matchesName("李四")).isFalse();
    }

    @Test
    @DisplayName("名册记录：已占用的学号不可再核验通过（一号一账号）")
    void studentRecordAvailability() {
        StudentRecord available = new StudentRecord(1L, "hash", "张三", null, null, null, null);
        StudentRecord occupied = new StudentRecord(2L, "hash", "张三", null, null, null, 99L);
        assertThat(available.isAvailable()).isTrue();
        assertThat(occupied.isAvailable()).isFalse();
    }
}
