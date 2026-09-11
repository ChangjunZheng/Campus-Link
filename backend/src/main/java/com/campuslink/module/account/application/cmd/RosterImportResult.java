package com.campuslink.module.account.application.cmd;

public record RosterImportResult(int inserted, int skipped, int failed) {
}
