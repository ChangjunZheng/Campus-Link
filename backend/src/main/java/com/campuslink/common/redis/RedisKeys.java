package com.campuslink.common.redis;

/**
 * Redis 键命名规范（CR-015）：所有键统一加应用命名空间前缀。
 *
 * <p>本机 / 共享实例上可能同时跑着多个项目，不加前缀的通用键名（如 {@code verify:ip:...}、
 * {@code captcha:code:...}）极易与其它应用冲突，也难以按应用清理。此处的逻辑键由各适配器给出，
 * **前缀只在本类拼装**，避免每个上下文各写一份、最终前缀不一致。
 *
 * <p>约定：逻辑键形如 {@code captcha:code:<hash>}、{@code verify:ip:<ip>}，
 * 最终键形如 {@code campuslink:captcha:code:<hash>}。
 */
public final class RedisKeys {

    /** 应用命名空间前缀。固定常量而非配置项：避免各环境前缀不同导致键名漂移。 */
    public static final String PREFIX = "campuslink:";

    private RedisKeys() {
    }

    /** 为逻辑键加上应用命名空间前缀。 */
    public static String of(String logicalKey) {
        return PREFIX + logicalKey;
    }
}
