package com.campuslink.module.forum.web;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ApiResponse;
import com.campuslink.common.result.ErrorCodes;
import com.campuslink.common.result.ResultCode;
import com.campuslink.common.web.ApiDocs;
import com.campuslink.module.forum.application.ForumQueryApplicationService;
import com.campuslink.module.forum.application.PostApplicationService;
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
 * <p>⚠️ {@code SecurityConfig} 仍是 {@code anyRequest().permitAll()}（N-4 未闭环），
 * 故 {@code POST /api/v1/posts} 的登录态**必须在本类手写校验**——漏写会静默变成公开接口，
 * 而 {@code @SecurityRequirement} 只声明契约、不构成运行时强制（技术方案 §5 / 设计 §4.2）。
 */
@Tag(name = "post", description = "帖子")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final ForumQueryApplicationService forumQueryService;
    private final PostApplicationService postApplicationService;

    @Operation(summary = "帖子列表（公开）：boardCode 缺省为全站最新，排序固定 created_at DESC")
    @ErrorCodes({ResultCode.NOT_FOUND})
    @GetMapping
    public ApiResponse<PageVo<PostSummaryVo>> list(@RequestParam(required = false) String boardCode,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        PageResult<PostSummary> result = forumQueryService.listPosts(boardCode, page, size);
        return ApiResponse.ok(new PageVo<>(result.items().stream().map(PostSummaryVo::from).toList(),
                result.total(), result.page(), result.size()));
    }

    @Operation(summary = "帖子详情（公开）：返回服务端渲染好的 contentHtml")
    @ErrorCodes({ResultCode.NOT_FOUND})
    @GetMapping("/{id}")
    public ApiResponse<PostDetailVo> detail(@PathVariable("id") Long id) {
        return ApiResponse.ok(PostDetailVo.from(forumQueryService.postDetail(id)));
    }

    @Operation(summary = "发帖（需登录）：只返回 id，前端据此跳转详情")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN, ResultCode.NOT_FOUND})
    @PostMapping
    public ApiResponse<PublishedPost> publish(@Valid @RequestBody PublishPostCommand command,
                                              Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new ApiException(ResultCode.NOT_LOGGED_IN);
        }
        return ApiResponse.ok(postApplicationService.publish(userId, command));
    }
}
