package com.campuslink.module.forum.application.cmd;

import jakarta.validation.constraints.NotNull;

/**
 * 采纳最佳答案入参（F-QA-001）。Command 模式与既有发帖 / 回帖入参同规矩。
 *
 * <p>校验不校验"replyId > 0"以外的语义：回复不存在 / 不属于该帖统一 404（防按 id 探测，见 application）。
 */
public record AcceptReplyCommand(@NotNull Long replyId) {
}
