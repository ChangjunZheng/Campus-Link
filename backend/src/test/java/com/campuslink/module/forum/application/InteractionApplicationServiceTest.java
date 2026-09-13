package com.campuslink.module.forum.application;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ResultCode;
import com.campuslink.module.forum.application.cmd.ForumResults.InteractionResult;
import com.campuslink.module.forum.domain.exception.PostNotFoundException;
import com.campuslink.module.forum.domain.gateway.FavoriteRepository;
import com.campuslink.module.forum.domain.gateway.LikeRepository;
import com.campuslink.module.forum.domain.gateway.PostRepository;
import com.campuslink.module.forum.domain.gateway.ReplyRepository;
import com.campuslink.module.forum.domain.model.BoardType;
import com.campuslink.module.forum.domain.model.LikeTargetType;
import com.campuslink.module.forum.domain.model.Post;
import com.campuslink.module.forum.domain.model.PostStatus;
import com.campuslink.module.forum.domain.model.Reply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 点赞 / 收藏 toggle 用例（F-FORUM-005）：目标存在性核验先行（不可读一律 3001），
 * toggle 结果决定计数增减方向，计数读回值作为响应回显。
 */
@ExtendWith(MockitoExtension.class)
class InteractionApplicationServiceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-09-13T08:00:00Z");

    @Mock
    private PostRepository postRepository;
    @Mock
    private ReplyRepository replyRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private FavoriteRepository favoriteRepository;

    private InteractionApplicationService service;

    @BeforeEach
    void setUp() {
        service = new InteractionApplicationService(postRepository, replyRepository,
                likeRepository, favoriteRepository);
    }

    @Test
    @DisplayName("点赞帖子：toggle 返回 true → 计数 +1，count 为仓储读回的最新值")
    void togglePostLikeInsertsAndIncrements() {
        when(postRepository.findById(9L)).thenReturn(Optional.of(post()));
        when(likeRepository.toggle(42L, LikeTargetType.POST, 9L)).thenReturn(true);
        when(postRepository.adjustLikeCount(9L, 1)).thenReturn(5);

        InteractionResult result = service.togglePostLike(42L, 9L);

        assertThat(result.active()).isTrue();
        assertThat(result.count()).isEqualTo(5);
    }

    @Test
    @DisplayName("取消点赞：toggle 返回 false → 计数 -1")
    void togglePostLikeDeletesAndDecrements() {
        when(postRepository.findById(9L)).thenReturn(Optional.of(post()));
        when(likeRepository.toggle(42L, LikeTargetType.POST, 9L)).thenReturn(false);
        when(postRepository.adjustLikeCount(9L, -1)).thenReturn(4);

        InteractionResult result = service.togglePostLike(42L, 9L);

        assertThat(result.active()).isFalse();
        assertThat(result.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("帖子不可读（不存在 / REMOVED）→ 3001，且不触达 toggle")
    void invisiblePostIsNotFound() {
        when(postRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.togglePostLike(42L, 9L))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.NOT_FOUND));
        assertThatThrownBy(() -> service.togglePostFavorite(42L, 9L))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.NOT_FOUND));

        verify(likeRepository, never()).toggle(anyLong(), any(), anyLong());
        verify(favoriteRepository, never()).toggle(anyLong(), anyLong());
    }

    @Test
    @DisplayName("点赞楼层：target_type=REPLY；楼层不存在 → 3001")
    void toggleReplyLike() {
        when(replyRepository.findById(11L)).thenReturn(Optional.of(reply()));
        when(likeRepository.toggle(42L, LikeTargetType.REPLY, 11L)).thenReturn(true);
        when(replyRepository.adjustLikeCount(11L, 1)).thenReturn(3);

        InteractionResult result = service.toggleReplyLike(42L, 11L);

        assertThat(result.active()).isTrue();
        assertThat(result.count()).isEqualTo(3);

        when(replyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.toggleReplyLike(42L, 99L))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.NOT_FOUND));
    }

    @Test
    @DisplayName("收藏帖子：toggle 结果决定 favorite_count 增减方向")
    void togglePostFavorite() {
        when(postRepository.findById(9L)).thenReturn(Optional.of(post()));
        when(favoriteRepository.toggle(42L, 9L)).thenReturn(true);
        when(postRepository.adjustFavoriteCount(9L, 1)).thenReturn(2);

        InteractionResult result = service.togglePostFavorite(42L, 9L);

        assertThat(result.active()).isTrue();
        assertThat(result.count()).isEqualTo(2);
        verify(postRepository, never()).adjustLikeCount(anyLong(), anyInt());
    }

    private static Post post() {
        return Post.rehydrate(9L, 1L, 100L, BoardType.QUESTION, "标题", "正文", "<p>正文</p>",
                PostStatus.PUBLISHED, 0, 4, false, null, CREATED_AT, CREATED_AT);
    }

    private static Reply reply() {
        return Reply.rehydrate(11L, 9L, 100L, 1, "内容", "<p>内容</p>", false, 0, CREATED_AT);
    }
}
