package com.ive.engine;

import com.ive.config.TabKeyConfig;
import com.ive.exceptions.IveException;
import com.ive.model.ComparisonSummary;
import com.ive.model.ExcelDataRow;
import com.ive.model.FieldDefinition;
import com.ive.model.FieldError;
import com.ive.model.FieldError.ErrorType;
import com.ive.model.RowComparisonResult;
import com.ive.model.SheetDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Compares every Excel data row against its counterpart in the sábana.
 *
 * <p>For each Excel row:
 * <ol>
 *   <li>Validate required fields (empty value in a REQUIRED field → {@code REQUIRED} error).</li>
 *   <li>Look up the record in the sábana by composite key.</li>
 *   <li>If not found → single {@code NOT_FOUND} error for the row.</li>
 *   <li>If found → compare every field value; differences → {@code MISMATCH} errors.</li>
 * </ol>
 */
public class ComparisonEngine {

    private static final Logger log = LoggerFactory.getLogger(ComparisonEngine.class);

    private final SheetDefinition sheet;

    public ComparisonEngine(SheetDefinition sheet) {
        this.sheet = sheet;
    }

    /**
     * Runs the full comparison and returns a {@link ComparisonSummary}.
     *
     * @param sabanaPath path to the sábana TXT file
     * @throws IveException if the sábana cannot be read or the tab has no key configuration
     */
    public ComparisonSummary compare(String sabanaPath) throws IveException {
        String sabanaFileName = new File(sabanaPath).getName();

        List<String> keyFields = TabKeyConfig.getKeyFields(sheet.sheetName())
                .orElseThrow(() -> new IveException(
                        "No key configuration found for sheet: " + sheet.sheetName()
                        + ". Add an entry to TabKeyConfig."));

        log.info("Key fields for '{}': {}", sheet.sheetName(), keyFields);

        SabanaIndex sabanaIndex = new SabanaIndex(sabanaPath, sheet.fields(), keyFields);

        List<RowComparisonResult> results = new ArrayList<>();

        for (ExcelDataRow excelRow : sheet.dataRows()) {
            List<FieldError> errors = compareRow(excelRow, sabanaIndex, keyFields);
            results.add(new RowComparisonResult(excelRow.rowIndex(), excelRow, errors));
        }

        long errorCount = results.stream().filter(RowComparisonResult::hasErrors).count();
        log.info("Comparison complete: {}/{} Excel row(s) have errors.",
                errorCount, results.size());

        return new ComparisonSummary(
                sheet.sheetName(),
                sabanaFileName,
                sheet.dataRows().size(),
                sabanaIndex.totalLines(),
                results
        );
    }

    // -------------------------------------------------------------------------

    private List<FieldError> compareRow(ExcelDataRow excelRow, SabanaIndex sabanaIndex,
                                         List<String> keyFields) {
        List<FieldError> errors = new ArrayList<>();

        // 1. Required field validation
        for (FieldDefinition fd : sheet.fields()) {
            if (fd.isRequired()) {
                String value = excelRow.get(fd.getFieldName());
                if (value == null || value.isBlank()) {
                    errors.add(new FieldError(fd.getFieldName(), ErrorType.REQUIRED, value, ""));
                }
            }
        }

        // 2. Look up in sábana
        String[] sabanaTokens = sabanaIndex.find(excelRow.values(), keyFields);

        if (sabanaTokens == null) {
            // NOT_FOUND replaces any previous errors for this row (the row is entirely missing)
            errors.clear();
            errors.add(new FieldError("", ErrorType.NOT_FOUND, "", ""));
            return errors;
        }

        // 3. Field-by-field comparison
        for (FieldDefinition fd : sheet.fields()) {
            String fieldName   = fd.getFieldName().toLowerCase().trim();
            String excelValue  = excelRow.get(fieldName).trim();

            int pos = fd.getPosition(); // 1-based
            String sabanaValue = (pos >= 1 && pos <= sabanaTokens.length)
                    ? sabanaTokens[pos - 1].trim() : "";

            if (!normalizeValue(excelValue).equals(normalizeValue(sabanaValue))) {
                // Avoid double-reporting a REQUIRED error already captured
                boolean alreadyRequired = errors.stream()
                        .anyMatch(e -> e.errorType() == ErrorType.REQUIRED
                                && e.fieldName().equalsIgnoreCase(fieldName));
                if (!alreadyRequired) {
                    errors.add(new FieldError(fd.getFieldName(), ErrorType.MISMATCH,
                            excelValue, sabanaValue));
                }
            }
        }

        return errors;
    }

    /**
     * Normalises a value for comparison:
     * <ul>
     *   <li>Case-insensitive (both lower-cased)</li>
     *   <li>Leading/trailing whitespace removed</li>
     *   <li>Numeric strings normalised to remove trailing zeros after decimal
     *       (e.g. "0.00" == "0", "80000.00" == "80000", "1.80" stays "1.80")</li>
     * </ul>
     */
    private static String normalizeValue(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase();
        // Handle datetime vs date: if sábana has "yyyy-MM-dd HH:mm:ss" and Excel has "yyyy-MM-dd",
        // compare only the date part (first 10 characters)
        if (s.length() > 10 && s.charAt(4) == '-' && s.charAt(7) == '-' && s.charAt(10) == ' ') {
            s = s.substring(0, 10);
        }
        // Try to parse as a number and normalise (removes trailing zeros: 0.00 == 0, 80000.00 == 80000)
        try {
            java.math.BigDecimal bd = new java.math.BigDecimal(s);
            return bd.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return s;
        }
    }
}
