package com.campuslink.module.account.domain.model;

import com.campuslink.common.crypto.NameNormalizer;

import java.util.Objects;

/**
 * 学籍名册记录（领域模型，技术方案 4.3）：名册只存学号 HMAC 哈希、不落明文；
 * 姓名明文仅用于容错比对，本模型不对外暴露任何接口。
 */
public record StudentRecord(Long id, String studentIdHash, String name, String grade,
                            String department, String sourceBatch, Long usedByAccountId) {

    public StudentRecord {
        Objects.requireNonNull(studentIdHash, "studentIdHash 不能为空");
        Objects.requireNonNull(name, "name 不能为空");
    }

    /** 姓名容错比对：去空格、全角转半角、小写（防输入差异误拒） */
    public boolean matchesName(String rawName) {
        return NameNormalizer.normalize(name).equals(NameNormalizer.normalize(rawName));
    }

    /** 学号未被注册占用才可核验通过（一号一账号） */
    public boolean isAvailable() {
        return usedByAccountId == null;
    }
}
