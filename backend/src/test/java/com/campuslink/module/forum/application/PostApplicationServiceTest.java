package com.campuslink.module.forum.application;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.markdown.MarkdownRenderer;
import com.campuslink.common.result.ResultCode;
import com.campuslink.module.forum.application.cmd.ForumResults.PublishedPost;
import com.campuslink.module.forum.application.cmd.PublishPostCommand;
import com.campuslink.module.forum.domain.gateway.BoardRepository;
import com.campuslink.module.forum.domain.gateway.PostRepository;
import com.campuslink.module.forum.domain.gateway.ReplyRepository;
import com.campuslink.module.forum.domain.model.Board;
import com.campuslink.module.forum.domain.model.BoardType;
import com.campuslink.module.forum.domain.model.Post;
import com.campuslink.module.forum.domain.model.PostStatus;
import com.campuslink.module.forum.domain.model.Reply;
import com.campuslink.module.notification.application.NotificationApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** 发帖用例：渲染时机（发布时落库，ADR-005）、type 随版块、未知版块按资源不存在处理 */
@ExtendWith(MockitoExtension.class)
class PostApplicationServiceTest {

    @Mock
    private BoardRepository boardRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private NotificationApplicationService notificationService;

    private PostApplicationService service;

    @BeforeEach
    void setUp() {
        // MarkdownRenderer 是纯组件（无 I/O），直接 new，不必走 Spring 上下文
        service = new PostApplicationService(boardRepository, postRepository, replyRepository,
                new MarkdownRenderer(), notificationService);
    }

    @Test
    @DisplayName("发帖：contentHtml 在发布时渲染落库，type 随版块，标题去首尾空白")
    void publishRendersHtmlAndSavesPost() {
        when(boardRepository.findByCode("qna"))
                .thenReturn(Optional.of(board(1L, "qna", BoardType.QUESTION)));
        when(postRepository.save(any())).thenAnswer(invocation -> {
            Post saved = invocation.getArgument(0);
            return Post.rehydrate(123L, saved.getBoardId(), saved.getAuthorId(), saved.getType(),
                    saved.getTitle(), saved.getContentMd(), saved.getContentHtml(), saved.getStatus(),
                    saved.getReplyCount(), saved.getLikeCount(), saved.isAccepted(),
                    saved.getAcceptedReplyId(), null, null);
        });

        PublishedPost result = service.publish(42L, new PublishPostCommand("qna", "  标题  ", "**加粗**"));

        assertThat(result.id()).isEqualTo(123L);
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        Post published = captor.getValue();
        assertThat(published.getTitle()).isEqualTo("标题");
        assertThat(published.getContentMd()).isEqualTo("**加粗**");
        assertThat(published.getContentHtml()).contains("<strong>加粗</strong>");
        assertThat(published.getType()).isEqualTo(BoardType.QUESTION);
        assertThat(published.getAuthorId()).isEqualTo(42L);
        assertThat(published.isVisible()).isTrue();
    }

    @Test
    @DisplayName("未知 / 停用的版块 code → 3001，不落库")
    void unknownBoardIsNotFound() {
        when(boardRepository.findByCode("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publish(42L, new PublishPostCommand("nope", "标题", "正文")))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.NOT_FOUND));

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("采纳：提问者采纳他人回复 → posts 定向更新 + replies 旧标志清理/新标志置位")
    void acceptUpdatesPostAndReplyFlags() {
        when(postRepository.findById(123L)).thenReturn(Optional.of(questionPost(42L)));
        when(replyRepository.findById(456L)).thenReturn(Optional.of(reply(456L, 999L)));

        service.acceptReply(42L, 123L, 456L);

        verify(postRepository).updateAcceptedReply(123L, 456L);
        verify(replyRepository).updateAcceptedFlags(123L, 456L);
        // 采纳通知发给被采纳楼层的作者（F-SOC-001）
        verify(notificationService).replyAccepted(42L, 456L, 999L);
    }

    @Test
    @DisplayName("采纳：非提问者 → 4002，不触达任何写操作")
    void acceptByNonAskerIsForbidden() {
        when(postRepository.findById(123L)).thenReturn(Optional.of(questionPost(42L)));

        assertThatThrownBy(() -> service.acceptReply(777L, 123L, 456L))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.FORBIDDEN));

        verify(postRepository, never()).updateAcceptedReply(any(), any());
        verify(replyRepository, never()).updateAcceptedFlags(any(), any());
        verifyNoInteractions(notificationService);
    }

    @Test
    @DisplayName("采纳：回复不存在或不属于该帖 → 3001（防按 id 探测），不触达写操作")
    void acceptForeignReplyIsNotFound() {
        when(postRepository.findById(123L)).thenReturn(Optional.of(questionPost(42L)));
        when(replyRepository.findById(456L)).thenReturn(Optional.of(reply(456L, 999L, 999L)));

        assertThatThrownBy(() -> service.acceptReply(42L, 123L, 456L))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.NOT_FOUND));

        verify(postRepository, never()).updateAcceptedReply(any(), any());
        verify(replyRepository, never()).updateAcceptedFlags(any(), any());
    }

    private static Post questionPost(Long authorId) {
        return Post.rehydrate(123L, 1L, authorId, BoardType.QUESTION, "标题", "正文", "<p>正文</p>",
                PostStatus.PUBLISHED, 1, 0, false, null, null, null);
    }

    private static Reply reply(Long id, Long authorId, Long postId) {
        return Reply.rehydrate(id, postId, authorId, 1, "内容", "<p>内容</p>", false, 0, null);
    }

    private static Reply reply(Long id, Long authorId) {
        return reply(id, authorId, 123L);
    }

    private static Board board(Long id, String code, BoardType type) {
        return Board.rehydrate(id, code, code, "描述", type, 1);
    }
}
