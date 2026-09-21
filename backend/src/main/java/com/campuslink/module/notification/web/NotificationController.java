package com.campuslink.module.notification.web;

import com.campuslink.common.result.ApiResponse;
import com.campuslink.common.result.ErrorCodes;
import com.campuslink.common.result.ResultCode;
import com.campuslink.common.web.ApiDocs;
import com.campuslink.common.web.CurrentUser;
import com.campuslink.module.notification.application.NotificationApplicationService;
import com.campuslink.module.notification.application.cmd.NotificationResults.MarkAllReadResult;
import com.campuslink.module.notification.application.cmd.NotificationResults.NotificationItem;
import com.campuslink.module.notification.application.cmd.NotificationResults.UnreadCountResult;
import com.campuslink.module.notification.domain.gateway.PageResult;
import com.campuslink.module.notification.web.vo.NotificationVo;
import com.campuslink.module.notification.web.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内通知接口（web 层，F-SOC-001）：四个端点全部是**本人私有数据**，一律受保护。
 *
 * <p>鉴权约定同 {@code PostController}（N-4 已闭环，CR-028）：声明 {@code @SecurityRequirement}
 * 且真的调用 {@link CurrentUser}，由 ArchitectureGuardTest G7 机器校验。
 */
@Tag(name = "notification", description = "站内通知")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;

    @Operation(summary = "我的通知（需登录）：unread 缺省不限、true 只未读、false 只已读；按时间倒序分页 page/size")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN})
    @GetMapping
    public ApiResponse<PageVo<NotificationVo>> list(Authentication authentication,
                                                    @RequestParam(required = false) Boolean unread,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        long userId = CurrentUser.requireId(authentication);
        PageResult<NotificationItem> result = notificationApplicationService.list(userId, unread, page, size);
        return ApiResponse.ok(new PageVo<>(result.items().stream().map(NotificationVo::from).toList(),
                result.total(), result.page(), result.size()));
    }

    @Operation(summary = "未读通知数（需登录）：顶栏铃铛角标")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN})
    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResult> unreadCount(Authentication authentication) {
        long userId = CurrentUser.requireId(authentication);
        return ApiResponse.ok(notificationApplicationService.unreadCount(userId));
    }

    @Operation(summary = "全部标记已读（需登录）：updated 为本次新标记的条数，无未读时为 0")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN})
    @PutMapping("/read-all")
    public ApiResponse<MarkAllReadResult> markAllRead(Authentication authentication) {
        long userId = CurrentUser.requireId(authentication);
        return ApiResponse.ok(notificationApplicationService.markAllRead(userId));
    }

    @Operation(summary = "单条标记已读（需登录，幂等）：不存在与非本人一律 404，已读再调仍 200")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN, ResultCode.NOT_FOUND})
    @PostMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable("id") Long id, Authentication authentication) {
        long userId = CurrentUser.requireId(authentication);
        notificationApplicationService.markRead(id, userId);
        return ApiResponse.ok();
    }
}
