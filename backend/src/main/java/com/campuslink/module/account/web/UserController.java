package com.campuslink.module.account.web;

import com.campuslink.common.result.ApiResponse;
import com.campuslink.common.result.ErrorCodes;
import com.campuslink.common.result.ResultCode;
import com.campuslink.common.web.ApiDocs;
import com.campuslink.common.web.CurrentUser;
import com.campuslink.module.account.application.AccountApplicationService;
import com.campuslink.module.account.application.ProfileApplicationService;
import com.campuslink.module.account.application.cmd.AccountCommands.UpdateProfileCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口（web 层）：只经 application 服务取数。
 * 不注入 {@code domain.gateway} 端口——R-1 裁决（CR-028）与 {@code ArchitectureGuardTest} 的执行结果。
 */
@Tag(name = "user", description = "用户")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final AccountApplicationService accountApplicationService;
    private final ProfileApplicationService profileApplicationService;

    @Operation(summary = "我的主页（F-ACC-002 最小版；他人主页在后续 Sprint）")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN, ResultCode.USER_NOT_FOUND})
    @GetMapping("/me")
    public ApiResponse<UserVo> me(Authentication authentication) {
        long userId = CurrentUser.requireId(authentication);
        return ApiResponse.ok(UserVo.from(accountApplicationService.accountOf(userId)));
    }

    /**
     * 资料编辑（F-ACC-007a）：PUT 全量语义，只接受昵称 / 专业 / 签名三个字段。
     *
     * <p><b>URL 与请求体都不带身份参数</b>——作者 id 唯一来源是令牌。这不是省事：
     * "仅本人"这条线没有机器强制（问题清单 E-011 同类缺口），所以越权面靠**接口形状**保证为零。
     *
     * <p>{@code @ErrorCodes} 里**不写** {@code INVALID_PARAM}：{@code OpenApiErrorResponseCustomizer}
     * 自动补通用 400，显式声明会让契约里出现"参数错误（1001）；参数错误（1001）"
     * （[CR-049] 的 v1.8 教训，本轮重抓时实测复现过一次并删掉）。
     */
    @Operation(summary = "修改我的资料（需登录）：昵称 / 专业 / 签名；字段传 null 即不改、传空串即清空（昵称除外）；全量回显 UserVo")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN, ResultCode.USER_NOT_FOUND,
            ResultCode.USER_BANNED, ResultCode.PROFILE_UPDATE_TOO_FREQUENT})
    @PutMapping("/me")
    public ApiResponse<UserVo> updateProfile(@Valid @RequestBody UpdateProfileCommand command,
                                             Authentication authentication) {
        long userId = CurrentUser.requireId(authentication);
        return ApiResponse.ok(UserVo.from(profileApplicationService.updateProfile(userId, command)));
    }
}
