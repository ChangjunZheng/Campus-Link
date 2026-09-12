package com.campuslink.module.forum.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.module.forum.domain.gateway.BoardRepository;
import com.campuslink.module.forum.domain.model.Board;
import com.campuslink.module.forum.infrastructure.persistence.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** 适配器：BoardRepository 端口的 MyBatis-Plus 实现 */
@Repository
@RequiredArgsConstructor
public class BoardRepositoryImpl implements BoardRepository {

    private final BoardMapper boardMapper;

    @Override
    public List<Board> findAllEnabled() {
        return boardMapper.selectList(new LambdaQueryWrapper<BoardDO>()
                        .eq(BoardDO::getEnabled, true)
                        .orderByAsc(BoardDO::getSort))
                .stream()
                .map(BoardConverter::toDomain)
                .toList();
    }

    @Override
    public Optional<Board> findByCode(String code) {
        return Optional.ofNullable(boardMapper.selectOne(new LambdaQueryWrapper<BoardDO>()
                        .eq(BoardDO::getCode, code)
                        .eq(BoardDO::getEnabled, true)))
                .map(BoardConverter::toDomain);
    }
}
