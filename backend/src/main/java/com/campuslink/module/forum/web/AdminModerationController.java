package com.campuslink.module.forum.web;

import com.campuslink.common.result.ApiResponse;
import com.campuslink.common.result.ErrorCodes;
import com.campuslink.common.result.ResultCode;
import com.campuslink.common.web.ApiDocs;
import com.campuslink.common.web.CurrentUser;
import com.campuslink.module.forum.application.ContentModerationApplicationService;
import com.campuslink.module.forum.application.cmd.ModerationCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内容处置接口（F-SAFE-003，web 层）：管理员下架 / 恢复帖子与楼层。
 *
 * <p>角色口径来自 PRD F-ADMIN-001 的原文分工——"超级管理员（权限管理 + <b>处置</b>）、
 * 运营（审核 + 工单处理）"，故这里只认 {@code ROLE_SUPERADMIN}；将来放开给 OPS 时须连同 PRD 一起改，
 * 不是在这里加一个 authority 就完事。框架层（{@code EndpointAuthorizationManager}）只判"登录 / 未登录"，
 * **角色判定必须由本类的 {@link CurrentUser#requireRole} 承担**（漏写即 403 无人拦、G7 守护测试会红）。
 *
 * <p>⚠️ 只有 API：页面上没有处置台入口（{@code frontend/} 由并发会话持有），故 PRD 该条的
 * 用户可见验收与"作者收到通知"一句均不在本批范围，见实施方案 §2「不做」。
 */
@Tag(name = "admin-moderation", description = "内容处置：下架 / 恢复（仅 SUPERADMIN）")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminModerationController {

    /** Spring authority 全名；与 {@code CurrentUser.requireRole} 配套（未登录 401、角色不足 403，技术方案 §5） */
    private static final String SUPERADMIN = "ROLE_SUPERADMIN";

    private final ContentModerationApplicationService moderationService;

    @Operation(summary = "处置帖子（需登录，仅 SUPERADMIN）：body 为 {status: PUBLISHED|REMOVED, reason?}，"
            + "同状态重复调用幂等返回 200 且不记审计；下架后前台所有位置不可见，不碰作者墓碑")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN, ResultCode.FORBIDDEN, ResultCode.NOT_FOUND})
    @PutMapping("/posts/{id}/status")
    public ApiResponse<Void> moderatePost(@PathVariable("id") Long id,
                                          @Valid @RequestBody ModerationCommand command,
                                          Authentication authentication) {
        long operatorId = CurrentUser.requireRole(authentication, SUPERADMIN);
        moderationService.moderatePost(operatorId, id, command);
        return ApiResponse.ok();
    }

    @Operation(summary = "处置楼层（需登录，仅 SUPERADMIN）：body 同帖子侧；楼层下架后从楼层列表消失且不可点赞 / 采纳"
            + "（CR-059 / CR-060 的可见性守卫自此才有真实触发方），已删楼层不可被处置（404）")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN, ResultCode.FORBIDDEN, ResultCode.NOT_FOUND})
    @PutMapping("/replies/{id}/status")
    public ApiResponse<Void> moderateReply(@PathVariable("id") Long id,
                                           @Valid @RequestBody ModerationCommand command,
                                           Authentication authentication) {
        long operatorId = CurrentUser.requireRole(authentication, SUPERADMIN);
        moderationService.moderateReply(operatorId, id, command);
        return ApiResponse.ok();
    }
}
