package com.campuslink.module.account.domain.gateway;

import com.campuslink.module.account.domain.model.StudentRecord;

/**
 * 出站端口：学籍名册写入（管理员 CSV 导入用）。
 * 与 RosterGateway 分离：导入始终写库，不受 bypass 策略影响。
 */
public interface RosterRepository {

    /** 名册哈希已存在返回 false（跳过），否则入库返回 true */
    boolean saveIfAbsent(StudentRecord record);
}
