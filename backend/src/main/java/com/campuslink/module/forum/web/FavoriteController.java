package com.campuslink.module.forum.web;

import com.campuslink.common.result.ApiResponse;
import com.campuslink.common.result.ErrorCodes;
import com.campuslink.common.result.ResultCode;
import com.campuslink.common.web.ApiDocs;
import com.campuslink.common.web.CurrentUser;
import com.campuslink.module.forum.application.ForumQueryApplicationService;
import com.campuslink.module.forum.application.cmd.ForumResults.PostSummary;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.web.vo.PageVo;
import com.campuslink.module.forum.web.vo.PostSummaryVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收藏接口（web 层）。鉴权约定同 {@link PostController}（N-4 已闭环，CR-028）：
 * 收藏列表是本人私有数据，端点受保护、经 {@link CurrentUser} 取当前用户（F-FORUM-005）。
 */
@Tag(name = "favorite", description = "收藏")
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final ForumQueryApplicationService forumQueryService;

    @Operation(summary = "我的收藏（需登录）：本人收藏的帖子，按收藏时间倒序，分页 page/size")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN})
    @GetMapping("/mine")
    public ApiResponse<PageVo<PostSummaryVo>> mine(Authentication authentication,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        long userId = CurrentUser.requireId(authentication);
        PageResult<PostSummary> result = forumQueryService.listMyFavorites(userId, page, size);
        return ApiResponse.ok(new PageVo<>(result.items().stream().map(PostSummaryVo::from).toList(),
                result.total(), result.page(), result.size()));
    }
}
