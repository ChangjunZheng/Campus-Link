package com.campuslink.module.forum.application;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.markdown.MarkdownRenderer;
import com.campuslink.common.result.ResultCode;
import com.campuslink.module.forum.application.cmd.ForumResults.PublishedPost;
import com.campuslink.module.forum.application.cmd.PublishPostCommand;
import com.campuslink.module.forum.domain.gateway.BoardRepository;
import com.campuslink.module.forum.domain.gateway.PostRepository;
import com.campuslink.module.forum.domain.model.Board;
import com.campuslink.module.forum.domain.model.BoardType;
import com.campuslink.module.forum.domain.model.Post;
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
import static org.mockito.Mockito.when;

/** 发帖用例：渲染时机（发布时落库，ADR-005）、type 随版块、未知版块按资源不存在处理 */
@ExtendWith(MockitoExtension.class)
class PostApplicationServiceTest {

    @Mock
    private BoardRepository boardRepository;
    @Mock
    private PostRepository postRepository;

    private PostApplicationService service;

    @BeforeEach
    void setUp() {
        // MarkdownRenderer 是纯组件（无 I/O），直接 new，不必走 Spring 上下文
        service = new PostApplicationService(boardRepository, postRepository, new MarkdownRenderer());
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
                    saved.getReplyCount(), saved.getLikeCount(), saved.isAccepted(), null, null);
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

    private static Board board(Long id, String code, BoardType type) {
        return Board.rehydrate(id, code, code, "描述", type, 1);
    }
}
