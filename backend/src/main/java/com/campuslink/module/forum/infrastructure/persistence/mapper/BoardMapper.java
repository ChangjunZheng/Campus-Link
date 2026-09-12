package com.campuslink.module.forum.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.module.forum.infrastructure.persistence.BoardDO;

/**
 * boards 表 Mapper。**必须在 {@code *.mapper} 包下**——{@code @MapperScan("com.campuslink.**.mapper")}
 * 依赖包名通配，放错包会扫不到（追认评审 F-3 的教训，AuditMapper 即因此被迫放宽扫描范围）。
 */
public interface BoardMapper extends BaseMapper<BoardDO> {
}
