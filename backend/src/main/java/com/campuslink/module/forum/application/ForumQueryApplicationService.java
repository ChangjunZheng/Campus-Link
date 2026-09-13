package com.campuslink.module.forum.application;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.markdown.MarkdownRenderer;
import com.campuslink.common.result.ResultCode;
import com.campuslink.module.account.application.AccountApplicationService;
import com.campuslink.module.forum.application.cmd.ForumResults.PostDetail;
import com.campuslink.module.forum.application.cmd.ForumResults.PostSummary;
import com.campuslink.module.forum.application.cmd.ForumResults.ReplyItem;
import com.campuslink.module.forum.domain.exception.BoardNotFoundException;
import com.campuslink.module.forum.domain.exception.PostNotFoundException;
import com.campuslink.module.forum.domain.gateway.BoardRepository;
import com.campuslink.module.forum.domain.gateway.FavoriteRepository;
import com.campuslink.module.forum.domain.gateway.LikeRepository;
import com.campuslink.module.forum.domain.gateway.PageResult;
import com.campuslink.module.forum.domain.gateway.PostRepository;
import com.campuslink.module.forum.domain.gateway.ReplyRepository;
import com.campuslink.module.forum.domain.model.Board;
import com.campuslink.module.forum.domain.model.LikeTargetType;
import com.campuslink.module.forum.domain.model.Post;
import com.campuslink.module.forum.domain.model.Reply;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
    /** 搜索关键词长度：下限对齐 ngram 分词（默认 ngram_token_size=2，单字分不出词），上限防超长串放大查询 */
    private static final int KEYWORD_MIN_CHARS = 2;
    private static final int KEYWORD_MAX_CHARS = 50;
    /** 搜索时间窗白名单（天）；不在白名单即 1001，不做静默归一 */
    private static final Set<Integer> SEARCH_TIME_WINDOWS = Set.of(7, 30, 90);

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final ReplyRepository replyRepository;
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final AccountApplicationService accountApplicationService;
    private final MarkdownRenderer markdownRenderer;

    public List<Board> listBoards() {
        return boardRepository.findAllEnabled();
    }

    /** 帖子列表：boardCode 缺省为全站最新；不存在的版块按资源不存在处理（设计 §3.2） */
    public PageResult<PostSummary> listPosts(String boardCode, int page, int size) {
        int currentPage = normalizePage(page);
        int pageSize = normalizeSize(size);
        Long boardId = boardIdOf(boardCode);
        PageResult<Post> found = postRepository.findPage(boardId, currentPage, pageSize);
        return new PageResult<>(toSummaries(found.items()), found.total(), currentPage, pageSize);
    }

    /**
     * 站内搜索（F-FORUM-008）：标题全文（ngram，中文 2 字起可命中）+ tags 冗余列 LIKE 兜底，
     * 按相关度 + 时间排序，可按版块 / 时间窗筛选；出参与帖子列表同口径（{@link PostSummary}）。
     * keyword 去首尾空白后长度须 2~50，days 只接受 7 / 30 / 90——不满足即 1001，不静默归一（与分页口径不同：
     * 分页越界只是翻页放大，关键词过短会退化成全表扫描且 ngram 分不出词）。
     */
    public PageResult<PostSummary> searchPosts(String keyword, String boardCode, Integer days, int page, int size) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.length() < KEYWORD_MIN_CHARS || kw.length() > KEYWORD_MAX_CHARS) {
            throw new ApiException(ResultCode.INVALID_PARAM);
        }
        if (days != null && !SEARCH_TIME_WINDOWS.contains(days)) {
            throw new ApiException(ResultCode.INVALID_PARAM);
        }
        int currentPage = normalizePage(page);
        int pageSize = normalizeSize(size);
        Long boardId = boardIdOf(boardCode);
        PageResult<Post> found = postRepository.search(kw, boardId, days, currentPage, pageSize);
        return new PageResult<>(toSummaries(found.items()), found.total(), currentPage, pageSize);
    }

    /**
     * 帖子详情：contentHtml 为发布时渲染好的 HTML，请求时零渲染（ADR-005）。
     * viewerId 为 null（匿名）时 likedByMe / favoritedByMe 恒 false——详情端点保持公开，登录态回显是**附加**能力（F-FORUM-005）。
     */
    public PostDetail postDetail(Long postId, Long viewerId) {
        Post post = requireVisiblePost(postId);
        Board board = boardsById().get(post.getBoardId());
        String nickname = accountApplicationService.nicknamesOf(List.of(post.getAuthorId()))
                .getOrDefault(post.getAuthorId(), NICKNAME_FALLBACK);
        boolean likedByMe = viewerId != null
                && likeRepository.findLikedTargetIds(viewerId, LikeTargetType.POST, List.of(postId)).contains(postId);
        boolean favoritedByMe = viewerId != null
                && favoriteRepository.findFavoritedPostIds(viewerId, List.of(postId)).contains(postId);
        return new PostDetail(post.getId(),
                board == null ? null : board.getCode(),
                board == null ? null : board.getName(),
                board == null ? null : board.getType().name(),
                post.getTitle(), post.getContentHtml(), post.getAuthorId(), nickname,
                post.getReplyCount(), post.getLikeCount(), post.isAccepted(),
                likedByMe, favoritedByMe, post.getCreatedAt());
    }

    /** 楼层列表：帖子不可读则不暴露其楼层；viewerId 为 null（匿名）时 likedByMe 恒 false（F-FORUM-005） */
    public PageResult<ReplyItem> listReplies(Long postId, int page, int size, Long viewerId) {
        requireVisiblePost(postId);
        int currentPage = normalizePage(page);
        int pageSize = normalizeSize(size);
        PageResult<Reply> found = replyRepository.findPageByPostId(postId, currentPage, pageSize);
        Map<Long, String> nicknames = accountApplicationService.nicknamesOf(
                found.items().stream().map(Reply::getAuthorId).toList());
        Set<Long> likedReplyIds = viewerId == null ? Set.of()
                : likeRepository.findLikedTargetIds(viewerId, LikeTargetType.REPLY,
                        found.items().stream().map(Reply::getId).toList());
        List<ReplyItem> items = found.items().stream()
                .map(reply -> new ReplyItem(reply.getId(), reply.getFloorNo(), reply.getContentHtml(),
                        reply.getAuthorId(), nicknames.getOrDefault(reply.getAuthorId(), NICKNAME_FALLBACK),
                        reply.isAccepted(), reply.getLikeCount(),
                        likedReplyIds.contains(reply.getId()), reply.getCreatedAt()))
                .toList();
        return new PageResult<>(items, found.total(), currentPage, pageSize);
    }

    /**
     * 我的收藏（F-FORUM-005）：按收藏时间倒序分页，出参复用 {@link PostSummary}（列表口径一致）。
     * 收藏行为本人可见的私有列表——调用方（Controller）已要求登录。
     */
    public PageResult<PostSummary> listMyFavorites(long userId, int page, int size) {
        int currentPage = normalizePage(page);
        int pageSize = normalizeSize(size);
        PageResult<Post> found = favoriteRepository.findFavoritePosts(userId, currentPage, pageSize);
        return new PageResult<>(toSummaries(found.items()), found.total(), currentPage, pageSize);
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

    /** boardCode 空 / 空白 → 全站（null）；查不到该版块 → 3001，与列表口径一致 */
    private Long boardIdOf(String boardCode) {
        if (boardCode == null || boardCode.isBlank()) {
            return null;
        }
        return boardRepository.findByCode(boardCode)
                .orElseThrow(BoardNotFoundException::new)
                .getId();
    }

    /** 列表 / 收藏 / 搜索三处共用的摘要映射：版块名与作者昵称一页一次批量解析（设计 §4.3，不做 N+1） */
    private List<PostSummary> toSummaries(List<Post> posts) {
        Map<Long, Board> boardsById = boardsById();
        Map<Long, String> nicknames = accountApplicationService.nicknamesOf(
                posts.stream().map(Post::getAuthorId).toList());
        return posts.stream()
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
    }

    /** page 归一到 ≥1；size 归一到 1~100（设计 §3：默认 20、上限 100）——不报错，避免 size 被用来放大查询 */
    private static int normalizePage(int page) {
        return Math.max(page, 1);
    }

    private static int normalizeSize(int size) {
        return size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
    }
}
