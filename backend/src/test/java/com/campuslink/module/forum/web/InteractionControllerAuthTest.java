package com.campuslink.module.forum.web;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ResultCode;
import com.campuslink.module.forum.application.ForumQueryApplicationService;
import com.campuslink.module.forum.application.InteractionApplicationService;
import com.campuslink.module.forum.application.cmd.ForumResults.InteractionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 点赞 / 收藏 4 个新受保护端点（CR-048，F-FORUM-005）的授权三态：匿名必须 401 且不触达用例，
 * 已登录则把操作人 ID 从 principal 透传。为什么框架已按注解拦截仍需这组断言，见 PostControllerAuthTest。
 */
class InteractionControllerAuthTest {

    private final ForumQueryApplicationService forumQueryService = mock(ForumQueryApplicationService.class);
    private final InteractionApplicationService interactionApplicationService =
            mock(InteractionApplicationService.class);
    private final PostController postController = new PostController(forumQueryService, null,
            interactionApplicationService, null);
    private final ReplyController replyController = new ReplyController(forumQueryService, null,
            interactionApplicationService);
    private final FavoriteController favoriteController = new FavoriteController(forumQueryService);

    @Test
    @DisplayName("匿名点赞帖子 → 4001，且不触达用例")
    void anonymousPostLikeIsUnauthorized() {
        assertThatThrownBy(() -> postController.like(9L, null))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.NOT_LOGGED_IN));
        verify(interactionApplicationService, never()).togglePostLike(anyLong(), anyLong());
    }

    @Test
    @DisplayName("principal 不是用户 ID → 4001（匿名令牌的字符串主体不得被当成已登录）")
    void nonUserIdPrincipalIsUnauthorized() {
        Authentication anonymousToken = new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of());

        assertThatThrownBy(() -> postController.favorite(9L, anonymousToken))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.NOT_LOGGED_IN));
        verify(interactionApplicationService, never()).togglePostFavorite(anyLong(), anyLong());
    }

    @Test
    @DisplayName("已登录点赞 / 收藏帖子 → 放行，操作人与目标从参数透传")
    void authenticatedPostInteractionsPassThrough() {
        when(interactionApplicationService.togglePostLike(42L, 9L)).thenReturn(new InteractionResult(true, 5));
        when(interactionApplicationService.togglePostFavorite(42L, 9L)).thenReturn(new InteractionResult(true, 2));

        assertThat(postController.like(9L, user()).data().active()).isTrue();
        assertThat(postController.favorite(9L, user()).data().count()).isEqualTo(2);

        verify(interactionApplicationService).togglePostLike(eq(42L), eq(9L));
        verify(interactionApplicationService).togglePostFavorite(eq(42L), eq(9L));
    }

    @Test
    @DisplayName("匿名点赞楼层 → 4001，且不触达用例")
    void anonymousReplyLikeIsUnauthorized() {
        assertThatThrownBy(() -> replyController.like(1L, 11L, null))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.NOT_LOGGED_IN));
        verify(interactionApplicationService, never()).toggleReplyLike(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("已登录点赞楼层 → 放行")
    void authenticatedReplyLikePassesThrough() {
        when(interactionApplicationService.toggleReplyLike(42L, 1L, 11L))
                .thenReturn(new InteractionResult(true, 3));

        assertThat(replyController.like(1L, 11L, user()).data().count()).isEqualTo(3);
        verify(interactionApplicationService).toggleReplyLike(eq(42L), eq(1L), eq(11L));
    }

    @Test
    @DisplayName("匿名查我的收藏 → 4001，且不触达查询")
    void anonymousFavoritesIsUnauthorized() {
        assertThatThrownBy(() -> favoriteController.mine(null, 1, 20))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.NOT_LOGGED_IN));
        verify(forumQueryService, never()).listMyFavorites(anyLong(), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    @DisplayName("已登录查我的收藏 → 放行，userId 取自 principal")
    void authenticatedFavoritesPassesThrough() {
        when(forumQueryService.listMyFavorites(42L, 1, 20)).thenReturn(
                new com.campuslink.module.forum.domain.gateway.PageResult<>(List.of(), 0, 1, 20));

        var response = favoriteController.mine(user(), 1, 20);

        assertThat(response.code()).isEqualTo(0);
        verify(forumQueryService).listMyFavorites(eq(42L), eq(1), eq(20));
    }

    private static Authentication user() {
        return new UsernamePasswordAuthenticationToken(
                42L, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
