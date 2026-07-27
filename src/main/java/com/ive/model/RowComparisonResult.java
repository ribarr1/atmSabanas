package com.ive.model;

import java.util.List;

/**
 * Result of comparing one Excel data row against the sábana.
 *
 * @param excelRowIndex 0-based row index of the source row in the Excel sheet
 * @param excelDataRow  the original Excel row (for output reproduction)
 * @param errors        list of field-level errors; empty means the row passed
 */
public record RowComparisonResult(
        int excelRowIndex,
        ExcelDataRow excelDataRow,
        List<FieldError> errors
) {

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isNotFound() {
        return errors.stream().anyMatch(e -> e.errorType() == FieldError.ErrorType.NOT_FOUND);
    }

    /** Builds the OBSERVACIONES text summarising all errors in this row. */
    public String buildObservations() {
        if (errors.isEmpty()) return "OK";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < errors.size(); i++) {
            if (i > 0) sb.append(" | ");
            sb.append(errors.get(i).toObservation());
        }
        return sb.toString();
    }
}
