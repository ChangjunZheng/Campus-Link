package com.campuslink.module.forum.application;

import com.campuslink.common.markdown.MarkdownRenderer;
import com.campuslink.module.account.application.AccountApplicationService;
import com.campuslink.module.forum.application.cmd.ForumResults.PostDetail;
import com.campuslink.module.forum.application.cmd.ForumResults.PostSummary;
import com.campuslink.module.forum.application.cmd.ForumResults.ReplyItem;
import com.campuslink.module.forum.domain.exception.BoardNotFoundException;
import com.campuslink.module.forum.domain.exception.PostNotFoundException;
import com.campuslink.module.forum.domain.gateway.BoardRepository;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.domain.gateway.PostRepository;
import com.campuslink.module.forum.domain.gateway.ReplyRepository;
import com.campuslink.module.forum.domain.model.Board;
import com.campuslink.module.forum.domain.model.Post;
import com.campuslink.module.forum.domain.model.Reply;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 论坛查询用例：版块列表 / 帖子列表 / 帖子详情 / 楼层列表。
 *
 * <p>作者昵称跨上下文取，**只调 account 的 application 服务**（ADR-012），一页一次批量查、不做 N+1（设计 §4.3）。
 * 版块是固定 6 行参考数据，一次取全后按 id 取用，不再逐帖查。
 */
@Service
@RequiredArgsConstructor
public class ForumQueryApplicationService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    /** 列表摘要上限（设计 §3.2，服务端截断） */
    private static final int SUMMARY_MAX_CHARS = 120;
    /** 作者已注销等取不到昵称时的回落文案（设计 §4.3） */
    private static final String NICKNAME_FALLBACK = "已注销用户";

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final ReplyRepository replyRepository;
    private final AccountApplicationService accountApplicationService;
    private final MarkdownRenderer markdownRenderer;

    public List<Board> listBoards() {
        return boardRepository.findAllEnabled();
    }

    /** 帖子列表：boardCode 缺省为全站最新；不存在的版块按资源不存在处理（设计 §3.2） */
    public PageResult<PostSummary> listPosts(String boardCode, int page, int size) {
        int currentPage = normalizePage(page);
        int pageSize = normalizeSize(size);
        Long boardId = null;
        if (boardCode != null && !boardCode.isBlank()) {
            boardId = boardRepository.findByCode(boardCode)
                    .orElseThrow(BoardNotFoundException::new)
                    .getId();
        }
        PageResult<Post> found = postRepository.findPage(boardId, currentPage, pageSize);
        Map<Long, Board> boardsById = boardsById();
        Map<Long, String> nicknames = accountApplicationService.nicknamesOf(
                found.items().stream().map(Post::getAuthorId).toList());
        List<PostSummary> items = found.items().stream()
                .map(post -> {
                    Board board = boardsById.get(post.getBoardId());
                    return new PostSummary(post.getId(),
                            board == null ? null : board.getCode(),
                            board == null ? null : board.getName(),
                            post.getTitle(),
                            nicknames.getOrDefault(post.getAuthorId(), NICKNAME_FALLBACK),
                            post.getReplyCount(), post.getLikeCount(),
                            markdownRenderer.toPlainSummary(post.getContentMd(), SUMMARY_MAX_CHARS),
                            post.getCreatedAt(), post.isAccepted());
                })
                .toList();
        return new PageResult<>(items, found.total(), currentPage, pageSize);
    }

    /** 帖子详情：contentHtml 为发布时渲染好的 HTML，请求时零渲染（ADR-005） */
    public PostDetail postDetail(Long postId) {
        Post post = requireVisiblePost(postId);
        Board board = boardsById().get(post.getBoardId());
        String nickname = accountApplicationService.nicknamesOf(List.of(post.getAuthorId()))
                .getOrDefault(post.getAuthorId(), NICKNAME_FALLBACK);
        return new PostDetail(post.getId(),
                board == null ? null : board.getCode(),
                board == null ? null : board.getName(),
                board == null ? null : board.getType().name(),
                post.getTitle(), post.getContentHtml(), post.getAuthorId(), nickname,
                post.getReplyCount(), post.getLikeCount(), post.isAccepted(), post.getCreatedAt());
    }

    /** 楼层列表：帖子不可读则不暴露其楼层 */
    public PageResult<ReplyItem> listReplies(Long postId, int page, int size) {
        requireVisiblePost(postId);
        int currentPage = normalizePage(page);
        int pageSize = normalizeSize(size);
        PageResult<Reply> found = replyRepository.findPageByPostId(postId, currentPage, pageSize);
        Map<Long, String> nicknames = accountApplicationService.nicknamesOf(
                found.items().stream().map(Reply::getAuthorId).toList());
        List<ReplyItem> items = found.items().stream()
                .map(reply -> new ReplyItem(reply.getId(), reply.getFloorNo(), reply.getContentHtml(),
                        reply.getAuthorId(), nicknames.getOrDefault(reply.getAuthorId(), NICKNAME_FALLBACK),
                        reply.isAccepted(), reply.getCreatedAt()))
                .toList();
        return new PageResult<>(items, found.total(), currentPage, pageSize);
    }

    /** 不可读（不存在 / 已删除 / 非 PUBLISHED）统一 404 / 3001，三种情况不对外区分——防按 id 探测 */
    private Post requireVisiblePost(Long postId) {
        return postRepository.findById(postId)
                .filter(Post::isVisible)
                .orElseThrow(PostNotFoundException::new);
    }

    private Map<Long, Board> boardsById() {
        return boardRepository.findAllEnabled().stream()
                .collect(Collectors.toMap(Board::getId, Function.identity()));
    }

    /** page 归一到 ≥1；size 归一到 1~100（设计 §3：默认 20、上限 100）——不报错，避免 size 被用来放大查询 */
    private static int normalizePage(int page) {
        return Math.max(page, 1);
    }

    private static int normalizeSize(int size) {
        return size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
    }
}
