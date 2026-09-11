package com.campuslink.module.account.web;

import com.campuslink.common.exception.ApiException;
import com.campuslink.common.result.ApiResponse;
import com.campuslink.common.result.ResultCode;
import com.campuslink.module.account.application.RosterImportApplicationService;
import com.campuslink.module.account.application.cmd.RosterImportResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "admin-roster", description = "学籍名册导入（仅 SUPERADMIN）")
@RestController
@RequestMapping("/api/v1/admin/roster")
@RequiredArgsConstructor
public class AdminRosterController {

    private final RosterImportApplicationService rosterImportService;

    @Operation(summary = "CSV 导入：body 为 CSV 文本，格式“学号,姓名[,年级[,专业]]”")
    @PostMapping("/import")
    public ApiResponse<RosterImportResult> importRoster(@RequestBody String csvBody,
                                                        @RequestParam(defaultValue = "manual") String batch,
                                                        Authentication authentication) {
        requireSuperadmin(authentication);
        List<String> lines = csvBody.lines().toList();
        Long operatorId = authentication.getPrincipal() instanceof Long uid ? uid : null;
        return ApiResponse.ok(rosterImportService.importCsv(lines, batch, operatorId));
    }

    private void requireSuperadmin(Authentication authentication) {
        boolean isSuperadmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPERADMIN".equals(a.getAuthority()));
        if (!isSuperadmin) {
            throw new ApiException(ResultCode.FORBIDDEN);
        }
    }
}
