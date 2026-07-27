package com.ive.model;

import java.util.List;

/**
 * Top-level result of one full execution: Excel vs sábana comparison.
 *
 * @param sheetName         matched Excel tab name
 * @param sabanaFileName    name of the sábana TXT file
 * @param totalExcelRows    number of data rows read from the Excel
 * @param totalSabanaRows   number of records in the sábana
 * @param rowResults        per-row comparison results (only rows with errors, or all rows)
 */
public record ComparisonSummary(
        String sheetName,
        String sabanaFileName,
        int totalExcelRows,
        int totalSabanaRows,
        List<RowComparisonResult> rowResults
) {

    public long totalErrors() {
        return rowResults.stream().filter(RowComparisonResult::hasErrors).count();
    }

    public boolean passed() {
        return rowResults.stream().noneMatch(RowComparisonResult::hasErrors);
    }
}
