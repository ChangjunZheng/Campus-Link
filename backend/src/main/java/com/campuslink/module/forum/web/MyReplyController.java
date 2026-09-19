package com.campuslink.module.forum.web;

import com.campuslink.common.result.ApiResponse;
import com.campuslink.common.result.ErrorCodes;
import com.campuslink.common.result.ResultCode;
import com.campuslink.common.web.ApiDocs;
import com.campuslink.common.web.CurrentUser;
import com.campuslink.module.forum.application.ForumQueryApplicationService;
import com.campuslink.module.forum.application.cmd.ForumResults.MyReplyItem;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.web.vo.MyReplyVo;
import com.campuslink.module.forum.web.vo.PageVo;
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
 * "我的回帖"接口（F-ACC-007c，web 层）。
 *
 * <p>为什么不挂进 {@link ReplyController}：那个类的类级前缀是 {@code /api/v1/posts/{postId}/replies}，
 * 是"某帖之下"的资源；"我的回帖"跨帖，硬挂会拼出 {@code /posts/{postId}/replies/mine} 这种自相矛盾的路径
 * （mine 语义与 {postId} 冲突）。故单开一个映射类，与 {@code /favorites/mine} 同一族做法。
 * tag 仍复用 {@code reply}——契约里同一族端点归组，tag 数不因新开映射类而增加。
 *
 * <p>鉴权约定同 {@link ReplyController}（N-4，CR-028）：声明 {@code @SecurityRequirement} 且方法体真的调
 * {@link CurrentUser}，由 {@code ArchitectureGuardTest} G7 机器校验。
 */
@Tag(name = "reply", description = "楼层回复")
@RestController
@RequestMapping("/api/v1/replies")
@RequiredArgsConstructor
public class MyReplyController {

    private final ForumQueryApplicationService forumQueryService;

    /**
     * 我的回帖（需登录）：父帖不可见（作者自删或被平台下架）的楼层整条不出现，
     * 所以列出来的每一条都能点进去；楼层自身被下架的条目仍出现并带 {@code status=REMOVED}。
     * 无身份参数，作者 id 只从令牌取（同"我的帖子"）。
     */
    @Operation(summary = "我的回帖（需登录）：时间倒序分页，条目带父帖定位；父帖不可见的楼层不出现，楼层自身被下架的带 status 标记")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN})
    @GetMapping("/mine")
    public ApiResponse<PageVo<MyReplyVo>> myReplies(Authentication authentication,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        long userId = CurrentUser.requireId(authentication);
        PageResult<MyReplyItem> result = forumQueryService.listMyReplies(userId, page, size);
        return ApiResponse.ok(new PageVo<>(result.items().stream().map(MyReplyVo::from).toList(),
                result.total(), result.page(), result.size()));
    }
}
