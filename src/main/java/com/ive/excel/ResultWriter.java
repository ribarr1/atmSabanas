package com.ive.excel;

import com.ive.exceptions.IveException;
import com.ive.model.ComparisonSummary;
import com.ive.model.ExcelDataRow;
import com.ive.model.FieldError;
import com.ive.model.FieldError.ErrorType;
import com.ive.model.RowComparisonResult;
import com.ive.model.SheetDefinition;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Writes the comparison result to a new Excel file.
 *
 * <p>Output format:
 * <ul>
 *   <li>Row 0: Summary banner (sheet name, totals, PASS/FAIL status)</li>
 *   <li>Row 1: blank</li>
 *   <li>Row 2: Column headers (field names + OBSERVACIONES)</li>
 *   <li>Row 3+: Only error rows from the Excel data, cells with MISMATCH in red,
 *       entire row in red when NOT_FOUND, last cell = OBSERVACIONES text.</li>
 *   <li>If no errors: single "ALL OK" row instead.</li>
 * </ul>
 */
public class ResultWriter {

    private static final Logger log = LoggerFactory.getLogger(ResultWriter.class);

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final SheetDefinition sheet;
    private final String outputDir;

    public ResultWriter(SheetDefinition sheet, String outputDir) {
        this.sheet     = sheet;
        this.outputDir = outputDir;
    }

    public String write(ComparisonSummary summary) throws IveException {
        String timestamp  = LocalDateTime.now().format(TS);
        String safeName   = sheet.sheetName().trim().replaceAll("[\\s/\\\\:*?\"<>|]", "_");
        String outputPath = Paths.get(outputDir,
                "resultado_" + safeName + "_" + timestamp + ".xlsx").toString();

        log.info("Writing result file: {}", outputPath);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet ws = wb.createSheet("Resultado");

            CellStyle headerStyle  = buildHeaderStyle(wb);
            CellStyle errorStyle   = buildErrorStyle(wb);
            CellStyle mismatchStyle= buildMismatchStyle(wb);
            CellStyle normalStyle  = wb.createCellStyle();

            // Ordered list of field names (as they appear in the dictionary)
            List<String> fieldNames = sheet.fields().stream()
                    .map(f -> f.getFieldName().toLowerCase().trim())
                    .collect(Collectors.toList());

            // Row 0: summary
            writeSummaryRow(ws, summary, headerStyle, fieldNames.size() + 1);
            ws.createRow(1); // blank

            // Row 2: headers
            writeHeaderRow(ws, fieldNames, headerStyle);

            // Rows 3+: error rows only
            int rowIdx = 3;
            List<RowComparisonResult> errorRows = summary.rowResults().stream()
                    .filter(RowComparisonResult::hasErrors)
                    .collect(Collectors.toList());

            if (errorRows.isEmpty()) {
                Row okRow = ws.createRow(rowIdx);
                Cell cell = okRow.createCell(0);
                cell.setCellValue("TODOS LOS REGISTROS COMPARADOS - SIN ERRORES");
                cell.setCellStyle(headerStyle);
            } else {
                for (RowComparisonResult result : errorRows) {
                    writeErrorRow(ws, rowIdx++, result, fieldNames, errorStyle, mismatchStyle, normalStyle);
                }
            }

            // Auto-size
            for (int col = 0; col <= fieldNames.size(); col++) {
                ws.autoSizeColumn(col);
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                wb.write(fos);
            }

        } catch (IOException e) {
            throw new IveException("Cannot write result file: " + outputPath, e);
        }

        log.info("Result file saved: {}", outputPath);
        return outputPath;
    }

    // -------------------------------------------------------------------------

    private void writeSummaryRow(Sheet ws, ComparisonSummary summary, CellStyle style, int totalCols) {
        Row row  = ws.createRow(0);
        String status = summary.passed() ? "PASS" : "FAIL";
        String text = String.format(
                "Pestaña: %s | Sábana: %s | Filas Excel: %d | Filas Sábana: %d | Errores: %d | Estado: %s",
                summary.sheetName(), summary.sabanaFileName(),
                summary.totalExcelRows(), summary.totalSabanaRows(),
                summary.totalErrors(), status);
        Cell cell = row.createCell(0);
        cell.setCellValue(text);
        cell.setCellStyle(style);
    }

    private void writeHeaderRow(Sheet ws, List<String> fieldNames, CellStyle style) {
        Row row = ws.createRow(2);
        for (int i = 0; i < fieldNames.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(fieldNames.get(i));
            cell.setCellStyle(style);
        }
        Cell obs = row.createCell(fieldNames.size());
        obs.setCellValue("OBSERVACIONES");
        obs.setCellStyle(style);
    }

    private void writeErrorRow(Sheet ws, int rowIdx, RowComparisonResult result,
                                List<String> fieldNames,
                                CellStyle errorStyle, CellStyle mismatchStyle, CellStyle normalStyle) {
        Row row = ws.createRow(rowIdx);

        ExcelDataRow excelRow = result.excelDataRow();

        // Collect which field names have MISMATCH errors in this row
        Set<String> mismatchFields = result.errors().stream()
                .filter(e -> e.errorType() == ErrorType.MISMATCH || e.errorType() == ErrorType.REQUIRED)
                .map(e -> e.fieldName().toLowerCase().trim())
                .collect(Collectors.toSet());

        boolean notFound = result.isNotFound();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String value     = excelRow.get(fieldName);
            Cell   cell      = row.createCell(i);
            cell.setCellValue(value != null ? value : "");

            if (notFound || mismatchFields.contains(fieldName)) {
                cell.setCellStyle(errorStyle);
            } else {
                cell.setCellStyle(normalStyle);
            }
        }

        // OBSERVACIONES column
        Cell obsCell = row.createCell(fieldNames.size());
        obsCell.setCellValue(result.buildObservations());
        obsCell.setCellStyle(notFound ? errorStyle : mismatchStyle);
    }

    // -------------------------------------------------------------------------
    // Styles
    // -------------------------------------------------------------------------

    private CellStyle buildHeaderStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /** Red background + white bold text — for NOT_FOUND rows and error cells. */
    private CellStyle buildErrorStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /** Orange background for OBSERVACIONES column on MISMATCH rows. */
    private CellStyle buildMismatchStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
