package com.campuslink.module.forum.domain;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ResultCode;
import com.campuslink.module.forum.domain.model.BoardType;
import com.campuslink.module.forum.domain.model.Post;
import com.campuslink.module.forum.domain.model.PostStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 采纳最佳答案的领域规则（F-QA-001）：仅问答帖、不能采纳自己回复、可更换（后写覆盖） */
class PostAcceptReplyTest {

    @Test
    @DisplayName("问答帖采纳他人回复 → is_accepted=true 且 accepted_reply_id 指向该回复")
    void acceptMarksPostAndReply() {
        Post post = post(BoardType.QUESTION);

        Post accepted = post.acceptReply(456L, 999L);

        assertThat(accepted.isAccepted()).isTrue();
        assertThat(accepted.getAcceptedReplyId()).isEqualTo(456L);
        // 聚合不可变：原实例不被改动
        assertThat(post.isAccepted()).isFalse();
        assertThat(post.getAcceptedReplyId()).isNull();
    }

    @Test
    @DisplayName("讨论帖不可采纳 → 3002")
    void discussionPostCannotBeAccepted() {
        Post post = post(BoardType.DISCUSSION);

        assertThatThrownBy(() -> post.acceptReply(456L, 999L))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.NOT_A_QUESTION));
    }

    @Test
    @DisplayName("提问者不能采纳自己的回复 → 3003")
    void askerCannotAcceptOwnReply() {
        Post post = post(BoardType.QUESTION, 42L);

        assertThatThrownBy(() -> post.acceptReply(456L, 42L))
                .isInstanceOfSatisfying(ApiException.class,
                        e -> assertThat(e.getCode()).isEqualTo(ResultCode.CANNOT_ACCEPT_OWN_REPLY));
    }

    private static Post post(BoardType type) {
        return post(type, 42L);
    }

    private static Post post(BoardType type, Long authorId) {
        return Post.rehydrate(123L, 1L, authorId, type, "标题", "正文", "<p>正文</p>",
                PostStatus.PUBLISHED, 2, 0, false, null, null, null);
    }
}
