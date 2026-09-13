package com.campuslink.module.forum.web;

import com.campuslink.module.forum.application.ForumQueryApplicationService;
import com.campuslink.module.forum.application.PostApplicationService;
import com.campuslink.module.forum.application.ReplyApplicationService;
import com.campuslink.module.forum.application.cmd.ForumResults.PostDetail;
import com.campuslink.module.forum.application.cmd.ForumResults.PostSummary;
import com.campuslink.module.forum.application.cmd.ForumResults.ReplyItem;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.domain.model.Board;
import com.campuslink.module.forum.domain.model.BoardType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 四个公开端点必须**无需登录即可读**（sprint-2.md A3：未登录可看列表与详情）。
 *
 * <p>本类的断言价值主要在编译期：读端点的方法签名里**不允许**出现 {@code Authentication} 参数。
 * 若有人给读端点补上登录校验，这里会先改签名、再改调用，改动无法悄悄发生。
 */
class ForumPublicReadTest {

    private static final Instant CREATED_AT = Instant.parse("2026-09-12T08:00:00Z");

    private final ForumQueryApplicationService forumQueryService = mock(ForumQueryApplicationService.class);
    private final PostApplicationService postApplicationService = mock(PostApplicationService.class);
    private final ReplyApplicationService replyApplicationService = mock(ReplyApplicationService.class);

    private final BoardController boardController = new BoardController(forumQueryService);
    private final PostController postController = new PostController(forumQueryService, postApplicationService);
    private final ReplyController replyController = new ReplyController(forumQueryService, replyApplicationService);

    @Test
    @DisplayName("版块列表：匿名可读")
    void boardsAreReadableAnonymously() {
        when(forumQueryService.listBoards())
                .thenReturn(List.of(Board.rehydrate(1L, "qna", "技术问答", "提问", BoardType.QUESTION, 1)));

        var response = boardController.list();

        assertThat(response.data()).extracting("code").containsExactly("qna");
    }

    @Test
    @DisplayName("帖子列表与详情：匿名可读")
    void postsAreReadableAnonymously() {
        when(forumQueryService.listPosts(null, 1, 20)).thenReturn(new PageResult<>(
                List.of(new PostSummary(1L, "qna", "技术问答", "标题", "张三", 0, 0, "摘要", CREATED_AT, false)),
                1, 1, 20));
        when(forumQueryService.postDetail(1L)).thenReturn(new PostDetail(1L, "qna", "技术问答", "QUESTION",
                "标题", "<p>正文</p>", 42L, "张三", 0, 0, false, CREATED_AT));

        var list = postController.list(null, 1, 20);
        var detail = postController.detail(1L);

        assertThat(list.data().list()).hasSize(1);
        assertThat(list.data().total()).isEqualTo(1);
        assertThat(detail.data().contentHtml()).isEqualTo("<p>正文</p>");
    }

    @Test
    @DisplayName("楼层列表：匿名可读")
    void repliesAreReadableAnonymously() {
        when(forumQueryService.listReplies(1L, 1, 20)).thenReturn(new PageResult<>(
                List.of(new ReplyItem(11L, 1, "<p>一</p>", 42L, "张三", false, CREATED_AT)), 1, 1, 20));

        var response = replyController.list(1L, 1, 20);

        assertThat(response.data().list()).extracting("floorNo").containsExactly(1);
    }
}
