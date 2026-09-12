package com.campuslink.module.forum.application.cmd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 发帖入参（web 层直接以 Command 为请求体，jakarta 校验注解随命令走） */
public record PublishPostCommand(
        @NotBlank(message = "版块不能为空") String boardCode,
        @NotBlank(message = "标题不能为空") @Size(max = 100, message = "标题最长 100 字") String title,
        @NotBlank(message = "正文不能为空") @Size(max = 50000, message = "正文最长 50000 字符") String contentMd) {
}
