package com.campuslink.module.forum.application.cmd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 内容处置入参（F-SAFE-003 / CR-066）：把帖子或楼层在 {@code PUBLISHED ↔ REMOVED} 之间迁移。
 *
 * <p>Command 模式与既有发帖 / 采纳入参同规矩（校验注解随命令走，web 层直接以其为请求体）。
 *
 * <p>{@code status} 用字符串 + {@link Pattern} 而不是枚举字段：Jackson 绑定枚举失败会走
 * {@code HttpMessageNotReadableException}，提示是"请求体不是合法 JSON"——对"取值不在这个词表里"
 * 是误导性的。用字符串校验后，非法取值直接得到 1001 与一句说清可取值的 message。
 * 词表与 {@code PostStatus} / {@code ReplyStatus} 同步，两处枚举改动都须回头改这一条正则。
 */
public record ModerationCommand(
        @NotBlank(message = "status 不能为空")
        @Pattern(regexp = "PUBLISHED|REMOVED", message = "status 只能取 PUBLISHED 或 REMOVED") String status,
        @Size(max = 200, message = "处置理由最长 200 字") String reason) {
}
