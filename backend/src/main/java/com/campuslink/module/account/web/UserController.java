package com.campuslink.module.account.web;

import com.campuslink.common.result.ApiResponse;
import com.campuslink.common.result.ErrorCodes;
import com.campuslink.common.result.ResultCode;
import com.campuslink.common.web.ApiDocs;
import com.campuslink.common.web.CurrentUser;
import com.campuslink.module.account.application.AccountApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Operation(summary = "我的主页（F-ACC-002 最小版；他人主页与资料编辑在后续 Sprint）")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN, ResultCode.USER_NOT_FOUND})
    @GetMapping("/me")
    public ApiResponse<UserVo> me(Authentication authentication) {
        long userId = CurrentUser.requireId(authentication);
        return ApiResponse.ok(UserVo.from(accountApplicationService.accountOf(userId)));
    }
}
