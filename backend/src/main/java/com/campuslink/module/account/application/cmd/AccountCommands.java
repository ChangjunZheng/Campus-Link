package com.campuslink.module.account.application.cmd;

import com.campuslink.module.account.domain.model.Account;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 用例入参 / 出参（Command 模式）：应用服务的稳定接口，
 * web 层直接以 Command 为请求体（jakarta 校验注解随命令走）。
 */
public final class AccountCommands {

    private AccountCommands() {
    }

    public record VerifyStudentCommand(
            @NotBlank(message = "学号不能为空") String studentId,
            @NotBlank(message = "姓名不能为空") String name) {
    }

    public record VerifyStudentResult(String ticket) {
    }

    public record CaptchaCommand(
            @NotBlank @Email(message = "邮箱格式不正确") String target) {
    }

    public record RegisterCommand(
            @NotBlank(message = "核验票据不能为空") String ticket,
            @NotBlank @Email(message = "邮箱格式不正确") String email,
            @NotBlank @Pattern(regexp = "\\d{6}", message = "验证码为 6 位数字") String code,
            @NotBlank @Size(min = 2, max = 32, message = "昵称长度 2~32") String nickname) {
    }

    public record LoginCommand(
            @NotBlank @Email(message = "邮箱格式不正确") String email,
            @NotBlank @Pattern(regexp = "\\d{6}", message = "验证码为 6 位数字") String code) {
    }

    /** 应用层出参：聚合 + 令牌；web 层组装为对前端的响应 VO（契约不变） */
    public record LoginResult(String token, long expiresAtEpochSecond, Account account) {
    }
}
