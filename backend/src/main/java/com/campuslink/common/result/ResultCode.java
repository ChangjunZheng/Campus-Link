package com.campuslink.common.result;

import lombok.Getter;

/**
 * 统一错误码（技术方案第 5 节）：0 成功；1xxx 通用；2xxx 账号（21xx 学籍核验）；3xxx 版块与帖子；4xxx 权限；5xxx 安全与机审。
 */
@Getter
public enum ResultCode {

    OK(0, 200, "成功"),

    INVALID_PARAM(1001, 400, "参数错误"),
    INTERNAL_ERROR(9999, 500, "系统繁忙，请稍后再试"),

    CAPTCHA_INVALID(2001, 400, "验证码错误或已过期"),
    CAPTCHA_TOO_FREQUENT(2002, 429, "验证码发送过于频繁，请稍后再试"),
    CAPTCHA_EXCEEDED(2003, 429, "验证码发送次数已达当日上限"),
    EMAIL_EXISTS(2004, 409, "该邮箱已注册"),
    LOGIN_FAILED(2005, 400, "邮箱或验证码错误"),
    USER_BANNED(2006, 403, "账号已被封禁"),
    USER_NOT_FOUND(2007, 404, "用户不存在"),

    STUDENT_VERIFY_FAILED(2101, 400, "学籍信息校验未通过"),
    VERIFY_TICKET_INVALID(2102, 400, "核验票据无效或已过期，请重新核验"),
    VERIFY_RATE_LIMITED(2103, 429, "尝试过于频繁，请 1 小时后再试"),

    NOT_FOUND(3001, 404, "资源不存在"),
    NOT_LOGGED_IN(4001, 401, "未登录"),
    FORBIDDEN(4002, 403, "无权限"),

    MODERATION_BLOCKED(5001, 422, "内容未通过安全审核"),
    MODERATION_UNAVAILABLE(5002, 503, "内容审核服务暂不可用，发布已临时关闭");

    private final int code;
    private final int httpStatus;
    private final String message;

    ResultCode(int code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
