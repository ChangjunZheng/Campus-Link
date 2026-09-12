package com.campuslink.module.forum.web;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ResultCode;
import com.campuslink.module.forum.application.ForumQueryApplicationService;
import com.campuslink.module.forum.application.PostApplicationService;
import com.campuslink.module.forum.application.cmd.ForumResults.PublishedPost;
import com.campuslink.module.forum.application.cmd.PublishPostCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 发帖端点的授权三态：匿名 / principal 非用户 ID（如匿名令牌的字符串主体）必须 401 且不触达用例，
 * 已登录则放行并把操作人 ID 从 principal 透传给用例。
 *
 * <p>为什么必须有这组断言：{@code SecurityConfig} 仍是 {@code anyRequest().permitAll()}
 * （**N-4 未闭环**），登录态实际由 Controller 手写校验——漏写或写错**不会让任何测试失败**，
 * 只会静默把发帖变成公开接口，而契约快照上的 {@code security} 声明反而制造"已受保护"的错觉。
 * 这是 [W-07](../../../../../../../../docs/tailoring-waivers.md) 推迟 A3-9 的直接后果（设计 §4.2）。
 */
class PostControllerAuthTest {

    private static final PublishPostCommand COMMAND = new PublishPostCommand("qna", "标题", "正文");

    private final ForumQueryApplicationService forumQueryService = mock(ForumQueryApplicationService.class);
    private final PostApplicationService postApplicationService = mock(PostApplicationService.class);
    private final PostController controller = new PostController(forumQueryService, postApplicationService);

    @Test
    @DisplayName("匿名发帖 → 4001，且不触达发帖用例")
    void anonymousIsUnauthorized() {
        assertThatThrownBy(() -> controller.publish(COMMAND, null))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.NOT_LOGGED_IN));

        verify(postApplicationService, never()).publish(any(), any());
    }

    @Test
    @DisplayName("principal 不是用户 ID → 4001（匿名令牌的字符串主体不得被当成已登录）")
    void nonUserIdPrincipalIsUnauthorized() {
        Authentication anonymousToken = new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of());

        assertThatThrownBy(() -> controller.publish(COMMAND, anonymousToken))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.NOT_LOGGED_IN));

        verify(postApplicationService, never()).publish(any(), any());
    }

    @Test
    @DisplayName("已登录 → 放行，操作人 ID 取自 principal")
    void authorIdComesFromPrincipal() {
        when(postApplicationService.publish(eq(42L), any())).thenReturn(new PublishedPost(123L));

        var response = controller.publish(COMMAND, user());

        assertThat(response.data().id()).isEqualTo(123L);
        verify(postApplicationService).publish(eq(42L), eq(COMMAND));
    }

    private static Authentication user() {
        return new UsernamePasswordAuthenticationToken(
                42L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
