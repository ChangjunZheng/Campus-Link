package com.campuslink.module.forum.web;

import com.campuslink.common.result.ApiResponse;
import com.campuslink.common.result.ErrorCodes;
import com.campuslink.common.result.ResultCode;
import com.campuslink.common.web.ApiDocs;
import com.campuslink.common.web.CurrentUser;
import com.campuslink.common.web.PublicEndpoint;
import com.campuslink.module.forum.application.ForumQueryApplicationService;
import com.campuslink.module.forum.application.InteractionApplicationService;
import com.campuslink.module.forum.application.PostApplicationService;
import com.campuslink.module.forum.application.cmd.AcceptReplyCommand;
import com.campuslink.module.forum.application.cmd.ForumResults.InteractionResult;
import com.campuslink.module.forum.application.cmd.ForumResults.PostDetail;
import com.campuslink.module.forum.application.cmd.ForumResults.PostSummary;
import com.campuslink.module.forum.application.cmd.ForumResults.PublishedPost;
import com.campuslink.module.forum.application.cmd.PublishPostCommand;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.web.vo.PageVo;
import com.campuslink.module.forum.web.vo.PostDetailVo;
import com.campuslink.module.forum.web.vo.PostSummaryVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子接口（web 层）：只做协议转换，用例逻辑在 application 层。
 *
 * <p>鉴权约定（N-4 已闭环，CR-028）：每个端点**必须且只能**声明 {@link PublicEndpoint}（公开）
 * 或 {@code @SecurityRequirement}（受保护）；受保护端点必须调用 {@link CurrentUser} 的统一入口。
 * 该约定由 {@code ArchitectureGuardTest} 机器校验——漏写即测试失败，不会静默变成公开接口。
 */
@Tag(name = "post", description = "帖子")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final ForumQueryApplicationService forumQueryService;
    private final PostApplicationService postApplicationService;
    private final InteractionApplicationService interactionApplicationService;

    @Operation(summary = "帖子列表（公开）：boardCode 缺省为全站；sort=latest（缺省，created_at DESC）或 hot（hot_score DESC，热榜），非法值 400")
    @ErrorCodes({ResultCode.NOT_FOUND})
    @PublicEndpoint
    @GetMapping
    public ApiResponse<PageVo<PostSummaryVo>> list(@RequestParam(required = false) String boardCode,
                                                   @RequestParam(defaultValue = "latest") String sort,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        PageResult<PostSummary> result = forumQueryService.listPosts(boardCode, sort, page, size);
        return ApiResponse.ok(new PageVo<>(result.items().stream().map(PostSummaryVo::from).toList(),
                result.total(), result.page(), result.size()));
    }

    @Operation(summary = "站内搜索（公开）：keyword 2~50 字搜标题（ngram 全文）与标签；boardCode / days（仅 7、30、90）可筛选；按相关度 + 时间排序")
    @ErrorCodes({ResultCode.NOT_FOUND})
    @PublicEndpoint
    @GetMapping("/search")
    public ApiResponse<PageVo<PostSummaryVo>> search(@RequestParam String keyword,
                                                     @RequestParam(required = false) String boardCode,
                                                     @RequestParam(required = false) Integer days,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        PageResult<PostSummary> result = forumQueryService.searchPosts(keyword, boardCode, days, page, size);
        return ApiResponse.ok(new PageVo<>(result.items().stream().map(PostSummaryVo::from).toList(),
                result.total(), result.page(), result.size()));
    }

    @Operation(summary = "帖子详情（公开）：返回服务端渲染好的 contentHtml；登录请求附带 likedByMe / favoritedByMe，匿名时恒 false")
    @ErrorCodes({ResultCode.NOT_FOUND})
    @PublicEndpoint
    @GetMapping("/{id}")
    public ApiResponse<PostDetailVo> detail(@PathVariable("id") Long id, Authentication authentication) {
        return ApiResponse.ok(PostDetailVo.from(forumQueryService.postDetail(id, viewerIdOf(authentication))));
    }

    @Operation(summary = "发帖（需登录）：只返回 id，前端据此跳转详情")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN, ResultCode.NOT_FOUND})
    @PostMapping
    public ApiResponse<PublishedPost> publish(@Valid @RequestBody PublishPostCommand command,
                                              Authentication authentication) {
        long userId = CurrentUser.requireId(authentication);
        return ApiResponse.ok(postApplicationService.publish(userId, command));
    }

    @Operation(summary = "采纳最佳答案（需登录，仅提问者）：技术问答帖的指定回复标记为最佳，可更换；body 为 {replyId}")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN, ResultCode.FORBIDDEN, ResultCode.NOT_FOUND,
            ResultCode.NOT_A_QUESTION, ResultCode.CANNOT_ACCEPT_OWN_REPLY})
    @PostMapping("/{id}/accept")
    public ApiResponse<Void> accept(@PathVariable("id") Long id,
                                    @Valid @RequestBody AcceptReplyCommand command,
                                    Authentication authentication) {
        long userId = CurrentUser.requireId(authentication);
        postApplicationService.acceptReply(userId, id, command.replyId());
        return ApiResponse.ok();
    }

    @Operation(summary = "点赞 / 取消点赞帖子（需登录，toggle）：active 为操作后状态，count 为最新点赞数")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN, ResultCode.NOT_FOUND})
    @PostMapping("/{id}/like")
    public ApiResponse<InteractionResult> like(@PathVariable("id") Long id, Authentication authentication) {
        long userId = CurrentUser.requireId(authentication);
        return ApiResponse.ok(interactionApplicationService.togglePostLike(userId, id));
    }

    @Operation(summary = "收藏 / 取消收藏帖子（需登录，toggle）：active 为操作后状态，count 为最新收藏数")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN, ResultCode.NOT_FOUND})
    @PostMapping("/{id}/favorite")
    public ApiResponse<InteractionResult> favorite(@PathVariable("id") Long id, Authentication authentication) {
        long userId = CurrentUser.requireId(authentication);
        return ApiResponse.ok(interactionApplicationService.togglePostFavorite(userId, id));
    }

    /** 详情端点保持公开；已登录时附带登录态回显（匿名 / principal 非 id 一律视为匿名，不抛 401） */
    private static Long viewerIdOf(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof Long userId ? userId : null;
    }
}
