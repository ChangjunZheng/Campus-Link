package com.campuslink.module.forum.application;

import com.campuslink.common.markdown.MarkdownRenderer;
import com.campuslink.module.forum.application.cmd.ForumResults.PublishedPost;
import com.campuslink.module.forum.application.cmd.PublishPostCommand;
import com.campuslink.module.forum.domain.exception.BoardNotFoundException;
import com.campuslink.module.forum.domain.gateway.BoardRepository;
import com.campuslink.module.forum.domain.gateway.PostRepository;
import com.campuslink.module.forum.domain.model.Board;
import com.campuslink.module.forum.domain.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 论坛写用例：发帖 */
@Service
@RequiredArgsConstructor
public class PostApplicationService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final MarkdownRenderer markdownRenderer;

    /**
     * 发帖：{@code contentHtml} 在**发布时**渲染后随帖子一起落库，请求时零渲染（ADR-005）；
     * 帖子 type 随版块（QUESTION 版块即提问帖）。
     *
     * <p>单条 INSERT，无需事务编排；鉴权在 web 层（N-4 敞口下必须手写，见设计 §4.2）。
     */
    public PublishedPost publish(Long authorId, PublishPostCommand command) {
        Board board = boardRepository.findByCode(command.boardCode())
                .orElseThrow(BoardNotFoundException::new);
        Post post = Post.publish(board.getId(), authorId, board.getType(), command.title(),
                command.contentMd(), markdownRenderer.render(command.contentMd()));
        return new PublishedPost(postRepository.save(post).getId());
    }
}
