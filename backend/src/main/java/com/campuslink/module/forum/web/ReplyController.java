package com.campuslink.module.forum.web;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ApiResponse;
import com.campuslink.common.result.ErrorCodes;
import com.campuslink.common.result.ResultCode;
import com.campuslink.common.web.ApiDocs;
import com.campuslink.module.forum.application.ForumQueryApplicationService;
import com.campuslink.module.forum.application.ReplyApplicationService;
import com.campuslink.module.forum.application.cmd.ForumResults.PublishedReply;
import com.campuslink.module.forum.application.cmd.ForumResults.ReplyItem;
import com.campuslink.module.forum.application.cmd.PublishReplyCommand;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.web.vo.PageVo;
import com.campuslink.module.forum.web.vo.ReplyVo;
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
 * 楼层接口（web 层）。
 *
 * <p>⚠️ 同 {@link PostController}：{@code POST /api/v1/posts/{postId}/replies} 的登录态
 * **必须手写校验**（N-4 未闭环，设计 §4.2）。
 */
@Tag(name = "reply", description = "楼层回复")
@RestController
@RequestMapping("/api/v1/posts/{postId}/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ForumQueryApplicationService forumQueryService;
    private final ReplyApplicationService replyApplicationService;

    @Operation(summary = "楼层列表（公开）：按 floor_no 升序；楼层 = 回复序号，帖子本体不占楼层号")
    @ErrorCodes({ResultCode.NOT_FOUND})
    @GetMapping
    public ApiResponse<PageVo<ReplyVo>> list(@PathVariable("postId") Long postId,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        PageResult<ReplyItem> result = forumQueryService.listReplies(postId, page, size);
        return ApiResponse.ok(new PageVo<>(result.items().stream().map(ReplyVo::from).toList(),
                result.total(), result.page(), result.size()));
    }

    @Operation(summary = "回帖（需登录）：返回本次楼层号")
    @SecurityRequirement(name = ApiDocs.BEARER_AUTH)
    @ErrorCodes({ResultCode.NOT_LOGGED_IN, ResultCode.NOT_FOUND})
    @PostMapping
    public ApiResponse<PublishedReply> reply(@PathVariable("postId") Long postId,
                                             @Valid @RequestBody PublishReplyCommand command,
                                             Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new ApiException(ResultCode.NOT_LOGGED_IN);
        }
        return ApiResponse.ok(replyApplicationService.reply(postId, userId, command));
    }
}
