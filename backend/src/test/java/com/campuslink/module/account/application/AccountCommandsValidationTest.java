package com.campuslink.module.account.application;

import com.campuslink.module.account.application.cmd.AccountCommands.RegisterCommand;
import com.campuslink.module.account.application.cmd.AccountCommands.UpdateProfileCommand;
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

import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * 资料编辑（F-ACC-007a）的边界：PUT 全量语义下 {@code null} = "这个字段不改"，
     * 因此"三个字段全 null"必须是**合法**请求（Bean Validation 对 null 一律放行，正好合用）。
     */
    @Test
    @DisplayName("UpdateProfileCommand：全 null 合法（什么都不改）；空串合法（清空由聚合归一为 NULL）")
    void profileUpdateAcceptsNullsAndEmpties() {
        assertThat(validator.validate(new UpdateProfileCommand(null, null, null))).isEmpty();
        assertThat(validator.validate(new UpdateProfileCommand("新昵称", "", ""))).isEmpty();
    }

    @Test
    @DisplayName("昵称长度 2~32 与注册侧同源：改一处常量，注册与编辑两侧同时生效")
    void rejectsNicknameOutOfRange() {
        for (String nickname : new String[]{"一", "a", "昵".repeat(33), "x".repeat(33)}) {
            assertThat(violationsOn(new UpdateProfileCommand(nickname, null, null), "nickname"))
                    .as("编辑侧应拒掉长度 %d 的昵称", nickname.length()).isNotEmpty();
            assertThat(violationsOn(new RegisterCommand("t", "a@b.com", "123456", nickname), "nickname"))
                    .as("注册侧应拒掉长度 %d 的昵称", nickname.length()).isNotEmpty();
        }
    }

    @Test
    @DisplayName("尖括号 / 控制字符在服务端就拒（CSP 实测零实现，这是唯一防线），注册与编辑同一判据")
    void rejectsMarkupInTextFields() {
        assertThat(violationsOn(new UpdateProfileCommand(null, null, "<script>alert(1)</script>"), "bio")).isNotEmpty();
        assertThat(violationsOn(new UpdateProfileCommand(null, "软件工程<script>", null), "major")).isNotEmpty();
        assertThat(violationsOn(new UpdateProfileCommand("张三<b>", null, null), "nickname")).isNotEmpty();
        assertThat(violationsOn(new RegisterCommand("t", "a@b.com", "123456", "<b>nick</b>"), "nickname")).isNotEmpty();
    }

    @Test
    @DisplayName("专业 ≤64、签名 ≤200（产品上限，不是列宽），边界值本身合法")
    void enforcesPerFieldLengthCaps() {
        assertThat(violationsOn(new UpdateProfileCommand(null, "专".repeat(64), null), "major")).isEmpty();
        assertThat(violationsOn(new UpdateProfileCommand(null, "专".repeat(65), null), "major")).isNotEmpty();
        assertThat(violationsOn(new UpdateProfileCommand(null, null, "想".repeat(200)), "bio")).isEmpty();
        assertThat(violationsOn(new UpdateProfileCommand(null, null, "想".repeat(201)), "bio")).isNotEmpty();
    }

    @Test
    @DisplayName("全空白昵称在**边界**就被拒（真机取证缺口：否则落到聚合只报笼统「参数错误」，且不消耗限流配额）")
    void rejectsBlankNicknameAtBoundary() {
        assertThat(violationsOn(new UpdateProfileCommand("    ", null, null), "nickname"))
                .anyMatch(m -> m.contains("空白"));
        // 带前后空白的合法昵称仍放行（trim 由聚合负责）
        assertThat(violationsOn(new UpdateProfileCommand("  张三同学  ", null, null), "nickname")).isEmpty();
    }

    private static Set<String> violationsOn(Object command, String field) {
        return validator.validate(command).stream()
                .filter(v -> field.equals(v.getPropertyPath().toString()))
                .map(v -> v.getMessage())
                .collect(Collectors.toSet());
    }
}
