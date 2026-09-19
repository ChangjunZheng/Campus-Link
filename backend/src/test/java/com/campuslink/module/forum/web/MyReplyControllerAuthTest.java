package com.campuslink.module.forum.web;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ResultCode;
import com.campuslink.module.forum.application.ForumQueryApplicationService;
import com.campuslink.module.forum.application.cmd.ForumResults.MyReplyItem;
import com.campuslink.module.forum.domain.gateway.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * "我的回帖"端点的授权与身份来源（F-ACC-007c）。
 *
 * <p>守的是那条结构性约束：**作者 id 只从令牌来**。签名里没有任何 userId / authorId 形参，
 * 所以"看到别人的下架楼层"在这条接口上不是"忘了判权限"，而是**写不出来**——
 * 这是本仓在"仅本人无机器强制"（问题清单 E-011 同类缺口）下能给出的最强形态。
 */
class MyReplyControllerAuthTest {

    private final ForumQueryApplicationService forumQueryService = mock(ForumQueryApplicationService.class);
    private final MyReplyController controller = new MyReplyController(forumQueryService);

    @Test
    @DisplayName("匿名 → 4001，且不触达查询用例")
    void anonymousIsUnauthorized() {
        assertThatThrownBy(() -> controller.myReplies(null, 1, 20))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.NOT_LOGGED_IN));

        verify(forumQueryService, never()).listMyReplies(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("已登录 → userId 取自 principal 并透传，条目带父帖定位与 status")
    void principalIsTheOnlyAuthorSource() {
        when(forumQueryService.listMyReplies(eq(42L), eq(1), eq(20))).thenReturn(new PageResult<>(
                List.of(new MyReplyItem(11L, 9L, "父帖标题", 3, "摘要", "REMOVED", Instant.now())), 1, 1, 20));

        var response = controller.myReplies(user(), 1, 20);

        assertThat(response.data().list()).singleElement().satisfies(item -> {
            assertThat(item.postId()).isEqualTo(9L);
            assertThat(item.postTitle()).isEqualTo("父帖标题");
            assertThat(item.status()).isEqualTo("REMOVED");
        });
        verify(forumQueryService).listMyReplies(42L, 1, 20);
    }

    private static Authentication user() {
        return new UsernamePasswordAuthenticationToken(
                42L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
