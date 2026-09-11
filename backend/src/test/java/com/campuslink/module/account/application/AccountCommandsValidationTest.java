package com.campuslink.module.account.application;

import com.campuslink.module.account.application.cmd.AccountCommands.VerifyStudentCommand;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 入参校验单测（CR-013）：学号 9 位数字、姓名限中文名或外文名。
 * 直接跑 Bean Validation，不加载 Spring 容器——校验注解在 Command 上，可独立验证。
 */
class AccountCommandsValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private static boolean hasViolationOn(String field, VerifyStudentCommand command) {
        return validator.validate(command).stream()
                .anyMatch(v -> field.equals(v.getPropertyPath().toString()));
    }

    @Test
    @DisplayName("合法入参：9 位学号 + 中文姓名 → 无校验错误")
    void acceptsNineDigitIdWithChineseName() {
        assertThat(validator.validate(new VerifyStudentCommand("249971346", "张三"))).isEmpty();
    }

    @ParameterizedTest
    @DisplayName("合法姓名：复姓/少数民族名（·分隔）与外文名均通过")
    @ValueSource(strings = {"阿卜杜拉·买买提", "欧阳修", "John Smith", "O'Brien", "Jean-Luc Picard", "J. R. R. Tolkien"})
    void acceptsValidNames(String name) {
        assertThat(hasViolationOn("name", new VerifyStudentCommand("249971346", name))).isFalse();
    }

    @ParameterizedTest
    @DisplayName("非法学号：非 9 位、含字母或符号一律拒绝")
    @ValueSource(strings = {"2023001", "24997134", "2499713460", "24997134a", "24997-1346", "２４９９７１３４６"})
    void rejectsInvalidStudentId(String studentId) {
        assertThat(hasViolationOn("studentId", new VerifyStudentCommand(studentId, "张三"))).isTrue();
    }

    @ParameterizedTest
    @DisplayName("非法姓名：含数字/符号、单字、纯标点一律拒绝")
    @ValueSource(strings = {"张三123", "张", "1", "张 三-4", "<script>", "李@四"})
    void rejectsInvalidNames(String name) {
        assertThat(hasViolationOn("name", new VerifyStudentCommand("249971346", name))).isTrue();
    }

    @Test
    @DisplayName("空白姓名与空白学号均被拒绝")
    void rejectsBlanks() {
        assertThat(hasViolationOn("studentId", new VerifyStudentCommand(" ", "张三"))).isTrue();
        assertThat(hasViolationOn("name", new VerifyStudentCommand("249971346", " "))).isTrue();
    }
}
