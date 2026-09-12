package com.campuslink.module.forum.web;

import com.campuslink.common.result.ApiResponse;
import com.campuslink.common.web.PublicEndpoint;
import com.campuslink.module.forum.application.ForumQueryApplicationService;
import com.campuslink.module.forum.web.vo.BoardVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "board", description = "版块")
@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController {

    private final ForumQueryApplicationService forumQueryService;

    @Operation(summary = "版块列表（公开）：固定 6 个启用版块，按 sort 升序，不分页")
    @PublicEndpoint
    @GetMapping
    public ApiResponse<List<BoardVo>> list() {
        return ApiResponse.ok(forumQueryService.listBoards().stream().map(BoardVo::from).toList());
    }
}
