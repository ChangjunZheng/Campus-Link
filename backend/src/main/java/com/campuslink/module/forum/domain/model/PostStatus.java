package com.campuslink.module.forum.domain.model;

/** 帖子状态：本 Sprint 只产生 PUBLISHED；REMOVED 为内容处置（机审 / 管理员下架）预留 */
public enum PostStatus {
    PUBLISHED, REMOVED
}
