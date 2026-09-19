package com.campuslink.module.account.domain.model;

import java.util.Locale;

/**
 * 资料编辑涉及的字段（F-ACC-007a / CR-071）：枚举即"哪一列该写"的单一词表，
 * 差异结果、定向 UPDATE 的 SET 列表与审计 detail 三处共用它，不再各写一份字段名。
 *
 * <p>只有这三列在列内：{@code school} / {@code grade} 本期不开放（口径见增量 PRD §4.3 三分表），
 * {@code email} / {@code student_id} 是敏感列、{@code role} / {@code status} 是平台侧事实。
 */
public enum ProfileField {

    NICKNAME,
    MAJOR,
    BIO;

    /** 审计 detail 里的字段名（小写，形如 {@code fields=major,bio}）：只记改了哪几个字段，不落新旧值 */
    public String auditName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
