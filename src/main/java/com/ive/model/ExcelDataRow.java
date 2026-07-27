package com.ive.model;

import java.util.Map;

/**
 * Represents one data row read from an Excel worksheet.
 *
 * @param rowIndex 0-based row index within the workbook sheet (used for output positioning)
 * @param values   field values keyed by field name (lower-case trimmed)
 */
public record ExcelDataRow(int rowIndex, Map<String, String> values) {

    public String get(String fieldName) {
        return values.getOrDefault(fieldName.toLowerCase().trim(), "");
    }
}
