package com.campuslink.common.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 审计日志（技术方案 7.1）：后台全操作（名册导入、内容处置等）统一留痕。
 * 审计切面（注解式自动记录）在 Sprint 4 治理模块一并补齐，当前为显式调用。
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditMapper auditMapper;

    public void record(Long actorId, String action, String targetType, Long targetId, String detail) {
        AuditLog log = new AuditLog();
        log.setActorId(actorId);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        auditMapper.insert(log);
    }
}
