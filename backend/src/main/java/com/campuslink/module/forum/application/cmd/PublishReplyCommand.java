package com.campuslink.module.forum.application.cmd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 回帖入参；正文上限与发帖一致（同为 MEDIUMTEXT，防超长载荷滥用） */
public record PublishReplyCommand(
        @NotBlank(message = "回复内容不能为空") @Size(max = 50000, message = "回复最长 50000 字符") String contentMd) {
}
