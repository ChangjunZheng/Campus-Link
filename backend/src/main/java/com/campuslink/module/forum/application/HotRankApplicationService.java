package com.campuslink.module.forum.application;

import com.campuslink.config.AppProperties;
import com.campuslink.module.forum.application.cmd.ForumResults.HotRefreshResult;
import com.campuslink.module.forum.domain.gateway.PostRepository;
import com.campuslink.module.forum.domain.model.HotScoreInput;
import com.campuslink.module.forum.domain.service.HotScorePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 热榜刷新用例（ADR-006）：把刷新窗口内候选帖子的互动分按时间衰减写回 {@code posts.hot_score}，
 * 并把"不再是候选"的帖子（窗口外 / 已删除 / 非 PUBLISHED）置 0。
 *
 * <p>公式只在 {@link HotScorePolicy}（domain）里，本类只负责编排：算窗口起点 → 游标分批读 → 算分 → 定向写回。
 *
 * <p><b>不加 {@code @Transactional}</b>（与实施方案原计划的"每批一个事务"不同，偏差已记于方案 §7）：
 * 每条 UPDATE 各自提交，重算是幂等的，中途失败下一轮（默认 10 分钟后）自愈；把整窗或整批包进事务
 * 只会累积行锁与 undo，而对读者不可观测——跨批本来就不是原子的，批内原子换不来"热榜整体一致"。
 */
@Service
@RequiredArgsConstructor
public class HotRankApplicationService {

    private final PostRepository postRepository;
    private final AppProperties props;

    /**
     * 刷新一轮，返回统计供触发器记日志。
     *
     * <p>{@code now} 在方法开头取一次并贯穿全轮：同一轮里所有帖子必须按同一时刻算龄，
     * 否则批与批之间的衰减基准不同，分数彼此不可比、排序无意义。
     */
    public HotRefreshResult refresh() {
        long startedAt = System.nanoTime();
        AppProperties.Hot hot = props.getHot();
        Instant now = Instant.now();
        Instant windowStart = now.minus(hot.getWindowDays(), ChronoUnit.DAYS);
        HotScorePolicy policy = new HotScorePolicy(hot.getReplyWeight(), hot.getLikeWeight(),
                hot.getFavoriteWeight(), hot.getDecayPerHour());

        int batchSize = hot.getBatchSize();
        int refreshed = 0;
        Long afterId = null;
        while (true) {
            List<HotScoreInput> batch = postRepository.findHotCandidates(windowStart, batchSize, afterId);
            if (batch.isEmpty()) {
                break;
            }
            for (HotScoreInput input : batch) {
                postRepository.updateHotScore(input.id(), policy.scoreOf(input, now));
            }
            refreshed += batch.size();
            if (batch.size() < batchSize) {
                break;
            }
            afterId = batch.get(batch.size() - 1).id();
        }

        int reset = postRepository.resetHotScoresBefore(windowStart);
        return new HotRefreshResult(refreshed, reset, (System.nanoTime() - startedAt) / 1_000_000L);
    }
}
