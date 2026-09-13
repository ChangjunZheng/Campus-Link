package com.campuslink.module.forum.application;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.markdown.MarkdownRenderer;
import com.campuslink.common.result.ResultCode;
import com.campuslink.module.forum.application.cmd.ForumResults.PublishedPost;
import com.campuslink.module.forum.application.cmd.PublishPostCommand;
import com.campuslink.module.forum.domain.exception.BoardNotFoundException;
import com.campuslink.module.forum.domain.exception.PostNotFoundException;
import com.campuslink.module.forum.domain.gateway.BoardRepository;
import com.campuslink.module.forum.domain.gateway.PostRepository;
import com.campuslink.module.forum.domain.gateway.ReplyRepository;
import com.campuslink.module.forum.domain.model.Board;
import com.campuslink.module.forum.domain.model.Post;
import com.campuslink.module.forum.domain.model.Reply;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 论坛写用例：发帖、采纳最佳答案 */
@Service
@RequiredArgsConstructor
public class PostApplicationService {

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final ReplyRepository replyRepository;
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

    /**
     * 采纳最佳答案（F-QA-001，仅提问者）：领域规则（问答帖 / 不能采纳自己回复）在 {@link Post#acceptReply}，
     * 跨聚合事实（回复存在且属于该帖）在应用层核验；落库走两个定向 UPDATE（posts 行锁先行，并发采纳串行化、
     * 后写覆盖前写即"可更换"），replies 的旧采纳标志清理与新标志置位同事务。
     */
    @Transactional
    public void acceptReply(Long askerId, long postId, long replyId) {
        Post post = postRepository.findById(postId)
                .filter(Post::isVisible)
                .orElseThrow(PostNotFoundException::new);
        if (!post.getAuthorId().equals(askerId)) {
            // 资源级授权归业务代码（框架只区分登录 / 未登录，CR-031 口径）
            throw new ApiException(ResultCode.FORBIDDEN);
        }
        Reply reply = replyRepository.findById(replyId)
                .filter(r -> r.getPostId().equals(post.getId()))
                .orElseThrow(PostNotFoundException::new);
        post.acceptReply(reply.getId(), reply.getAuthorId());
        postRepository.updateAcceptedReply(postId, replyId);
        replyRepository.updateAcceptedFlags(postId, replyId);
    }
}
