package com.campuslink.module.account.domain.model;

/** 账号状态机（users.status）：后续注销冷静期（DEACTIVATED）与封禁（BANNED）在此收敛 */
public enum AccountStatus {
    ACTIVE, BANNED, DEACTIVATED
}
