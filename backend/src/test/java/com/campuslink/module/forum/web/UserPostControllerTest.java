package com.campuslink.module.forum.web;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ApiResponse;
import com.campuslink.common.result.ResultCode;
import com.campuslink.common.web.PublicEndpoint;
import com.campuslink.module.forum.application.ForumQueryApplicationService;
import com.campuslink.module.forum.application.cmd.ForumResults.PostSummary;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.web.vo.PageVo;
import com.campuslink.module.forum.web.vo.PostSummaryVo;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 他人主页时间线端点（{@code GET /api/v1/users/{id}/posts}，F-ACC-002 本体最小版）的契约面。
 *
 * <p>控制器只做三件事，测试也就只守这三件：分页参数原样透传、application 载体逐项映射成 VO、
 * 2007 不改写。可见性口径不在这里（在 {@code ForumQueryApplicationServiceUserPostsTest} 与仓储的 SQL 结构测试里）——
 * 在 web 层再断言一遍"不含已下架"只会制造一处会随实现漂移的假保证。
 *
 * <p>另有一格守匿名可读的接口形状，理由与 {@code UserControllerPublicProfileTest} 同：
 * 主页要能被没登录的人打开，这条一旦丢掉，业务测试里看不出来。
 */
class UserPostControllerTest {

    private static final long USER_ID = 42L;
    private static final Instant CREATED_AT = Instant.parse("2026-09-12T08:00:00Z");

    private final ForumQueryApplicationService forumQueryService = mock(ForumQueryApplicationService.class);
    private final UserPostController controller = new UserPostController(forumQueryService);

    @Test
    @DisplayName("匿名可读：方法不收 Authentication，只挂 @PublicEndpoint（不挂 @SecurityRequirement）")
    void userPostsIsAnonymousReadableByShape() throws NoSuchMethodException {
        Method userPosts = UserPostController.class.getMethod("userPosts", long.class, int.class, int.class);

        assertThat(Arrays.stream(userPosts.getParameterTypes())).doesNotContain(Authentication.class);
        assertThat(userPosts.isAnnotationPresent(PublicEndpoint.class)).isTrue();
        assertThat(userPosts.isAnnotationPresent(SecurityRequirement.class)).isFalse();
    }

    @Test
    @DisplayName("page / size 原样透传（归一在 application 层做，控制器不二次加工），分页壳回显生效值")
    void passesPagingThroughAndEchoesEffectiveValues() {
        when(forumQueryService.listVisiblePostsByAuthor(USER_ID, 2, 20))
                .thenReturn(new PageResult<>(List.of(summary(7L)), 45L, 2, 20));

        PageVo<PostSummaryVo> page = controller.userPosts(USER_ID, 2, 20).data();

        verify(forumQueryService).listVisiblePostsByAuthor(USER_ID, 2, 20);
        assertThat(page.total()).isEqualTo(45L);
        assertThat(page.page()).isEqualTo(2);
        assertThat(page.size()).isEqualTo(20);
    }

    @Test
    @DisplayName("映射：PostSummary → PostSummaryVo 逐字段落位，出参字段名是 list（前端按此取数组）")
    void mapsSummariesOntoVos() {
        when(forumQueryService.listVisiblePostsByAuthor(USER_ID, 1, 20))
                .thenReturn(new PageResult<>(List.of(summary(7L)), 1L, 1, 20));

        ApiResponse<PageVo<PostSummaryVo>> response = controller.userPosts(USER_ID, 1, 20);
        PostSummaryVo vo = response.data().list().getFirst();

        assertThat(vo.id()).isEqualTo(7L);
        assertThat(vo.boardCode()).isEqualTo("qna");
        assertThat(vo.boardName()).isEqualTo("技术问答");
        assertThat(vo.title()).isEqualTo("标题");
        assertThat(vo.authorNickname()).isEqualTo("张三同学");
        assertThat(vo.accepted()).isTrue();
        assertThat(Arrays.stream(PageVo.class.getRecordComponents()).map(RecordComponent::getName).toList())
                .containsExactly("list", "total", "page", "size");
    }

    @Test
    @DisplayName("用户不存在 / 已注销 → 2007 原样透传（与资料端点同码，主页整页统一 404 文案）")
    void notFoundPropagates() {
        when(forumQueryService.listVisiblePostsByAuthor(USER_ID, 1, 20))
                .thenThrow(new ApiException(ResultCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> controller.userPosts(USER_ID, 1, 20))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.USER_NOT_FOUND));
    }

    private static PostSummary summary(Long id) {
        return new PostSummary(id, "qna", "技术问答", "标题", "张三同学", 3, 2, 100, null, "摘要", CREATED_AT, true);
    }
}
