package com.ive.dictionary;

import com.ive.exceptions.IveException;
import com.ive.model.DictionaryModel;
import com.ive.model.ExcelDataRow;
import com.ive.model.FieldDefinition;
import com.ive.model.SheetDefinition;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads an Excel Data Dictionary and builds a {@link DictionaryModel}.
 *
 * <p>Sheet structure is detected dynamically by scanning column A for known labels.
 * After reading the schema rows, all rows below CAMPO are read as data rows.
 */
public class DictionaryReader {

    private static final Logger log = LoggerFactory.getLogger(DictionaryReader.class);

    private static final String LABEL_OBLIGATORIEDAD = "obligatoriedad";
    private static final String LABEL_POSICION       = "posicion de campos";
    private static final String LABEL_DESCRIPCION    = "descripcion";
    private static final String LABEL_CAMPO          = "campo";

    /** Formats cells according to their Excel format (preserves dates and decimals). */
    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    /** Date pattern used in sábana TXT files. */
    private static final java.time.format.DateTimeFormatter DATE_FMT =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String dictionaryPath;

    /**
     * @param dictionaryPath absolute or relative path to the Excel Data Dictionary (.xlsx)
     */
    public DictionaryReader(String dictionaryPath) {
        this.dictionaryPath = dictionaryPath;
    }

    /**
     * Reads the workbook and returns a fully populated {@link DictionaryModel}.
     *
     * <p>Rules are NOT compiled at this stage.  Compiled rules lists inside each
     * {@link FieldDefinition} will be empty until {@code RuleCompiler} is invoked.
     *
     * @return dictionary model containing all valid sheets
     * @throws IveException if the file is missing, cannot be opened, or a fatal
     *                      read error occurs
     */
    public DictionaryModel read() throws IveException {
        File file = new File(dictionaryPath);
        if (!file.exists()) {
            throw new IveException("Dictionary file not found: " + dictionaryPath);
        }

        log.info("Reading dictionary: {}", dictionaryPath);

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            int total = workbook.getNumberOfSheets();
            log.info("Workbook contains {} sheet(s).", total);

            List<SheetDefinition> sheets = new ArrayList<>();
            for (int i = 0; i < total; i++) {
                SheetDefinition definition = processSheet(workbook.getSheetAt(i));
                if (definition != null) {
                    sheets.add(definition);
                }
            }

            DictionaryModel model = new DictionaryModel(sheets);
            log.info("Dictionary loaded successfully: {} valid sheet(s).", sheets.size());
            return model;

        } catch (IOException e) {
            throw new IveException("Cannot open dictionary file: " + dictionaryPath, e);
        } catch (Exception e) {
            throw new IveException("Unexpected error reading dictionary: " + dictionaryPath, e);
        }
    }

    // -------------------------------------------------------------------------
    // Sheet processing
    // -------------------------------------------------------------------------

    private SheetDefinition processSheet(Sheet sheet) {
        String sheetName = sheet.getSheetName();
        log.debug("Processing sheet: '{}'", sheetName);

        Map<String, Integer> labelRows = detectLabelRows(sheet);

        if (!labelRows.containsKey(LABEL_POSICION)) {
            log.warn("Sheet '{}': missing '{}' row. Skipping.", sheetName, "POSICION DE CAMPOS");
            return null;
        }
        if (!labelRows.containsKey(LABEL_CAMPO)) {
            log.warn("Sheet '{}': missing '{}' row. Skipping.", sheetName, "CAMPO");
            return null;
        }

        Row posicionRow       = sheet.getRow(labelRows.get(LABEL_POSICION));
        int  campoRowIndex    = labelRows.get(LABEL_CAMPO);
        Row  campoRow         = resolveCampoRow(sheet, campoRowIndex, sheetName);
        Row obligatoriedadRow = labelRows.containsKey(LABEL_OBLIGATORIEDAD)
                ? sheet.getRow(labelRows.get(LABEL_OBLIGATORIEDAD)) : null;
        Row descripcionRow    = labelRows.containsKey(LABEL_DESCRIPCION)
                ? sheet.getRow(labelRows.get(LABEL_DESCRIPCION)) : null;

        if (posicionRow == null || campoRow == null) {
            log.warn("Sheet '{}': cannot resolve required rows. Skipping.", sheetName);
            return null;
        }

        int dataStartRowIndex = campoRow.getRowNum() + 1;

        List<FieldDefinition> fields = buildFields(
                sheetName, posicionRow, campoRow, obligatoriedadRow, descripcionRow);

        if (fields.isEmpty()) {
            log.warn("Sheet '{}': no valid fields detected. Skipping.", sheetName);
            return null;
        }

        fields.sort(Comparator.comparingInt(FieldDefinition::getPosition));

        Map<Integer, String> colToField = buildColToFieldMap(campoRow);
        List<ExcelDataRow>   dataRows   = readDataRows(sheet, dataStartRowIndex, colToField);

        log.info("Sheet '{}': {} field(s), {} data row(s) loaded.",
                sheetName, fields.size(), dataRows.size());

        return new SheetDefinition(sheetName, fields, dataRows);
    }

    // -------------------------------------------------------------------------
    // Label detection
    // -------------------------------------------------------------------------

    private Map<String, Integer> detectLabelRows(Sheet sheet) {
        Map<String, Integer> labelRows = new LinkedHashMap<>();

        for (Row row : sheet) {
            if (row == null) continue;
            String normalized = normalize(getCellString(row, 0));
            if (normalized.isEmpty()) continue;

            if (!labelRows.containsKey(LABEL_OBLIGATORIEDAD) && matchesLabel(normalized, LABEL_OBLIGATORIEDAD)) {
                labelRows.put(LABEL_OBLIGATORIEDAD, row.getRowNum());
            } else if (!labelRows.containsKey(LABEL_POSICION) && matchesLabel(normalized, LABEL_POSICION)) {
                labelRows.put(LABEL_POSICION, row.getRowNum());
            } else if (!labelRows.containsKey(LABEL_DESCRIPCION) && matchesLabel(normalized, LABEL_DESCRIPCION)) {
                labelRows.put(LABEL_DESCRIPCION, row.getRowNum());
            } else if (!labelRows.containsKey(LABEL_CAMPO) && matchesLabel(normalized, LABEL_CAMPO)) {
                labelRows.put(LABEL_CAMPO, row.getRowNum());
            }
        }

        return labelRows;
    }

    // -------------------------------------------------------------------------
    // CAMPO fallback
    // -------------------------------------------------------------------------

    /**
     * Returns the row that actually contains field names.
     *
     * <p>If the row at {@code campoRowIndex} has no data values (all blank from
     * column 1 onward), the immediately following row is used as a fallback.
     * This handles the anomaly observed in {@code sb_dato_operacion_fondos} where
     * the CAMPO label row contains no names.
     */
    private Row resolveCampoRow(Sheet sheet, int campoRowIndex, String sheetName) {
        Row campoRow = sheet.getRow(campoRowIndex);
        if (campoRow == null) return null;

        if (hasNoDataCells(campoRow)) {
            log.warn("Sheet '{}': CAMPO row (index {}) has no field names. Using next row as fallback.",
                    sheetName, campoRowIndex);
            Row fallback = sheet.getRow(campoRowIndex + 1);
            return fallback != null ? fallback : campoRow;
        }

        return campoRow;
    }

    /** Returns {@code true} when all cells from column 1 onward are blank. */
    private boolean hasNoDataCells(Row row) {
        short lastCell = row.getLastCellNum();
        if (lastCell <= 1) return true;

        for (int col = 1; col < lastCell; col++) {
            if (!getCellString(row, col).isBlank()) {
                return false;
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Field building
    // -------------------------------------------------------------------------

    private List<FieldDefinition> buildFields(
            String sheetName,
            Row posicionRow,
            Row campoRow,
            Row obligatoriedadRow,
            Row descripcionRow) {

        List<FieldDefinition> fields = new ArrayList<>();
        int maxCol = posicionRow.getLastCellNum();

        for (int col = 1; col < maxCol; col++) {
            String rawPosition  = getCellString(posicionRow, col);
            String rawFieldName = getCellString(campoRow, col);

            Integer position = parsePosition(rawPosition);
            if (position == null) {
                if (!rawPosition.isBlank()) {
                    log.warn("Sheet '{}', column {}: invalid position '{}'. Skipping.",
                            sheetName, col, rawPosition);
                }
                continue;
            }

            String fieldName = rawFieldName.trim();
            if (fieldName.isBlank()) {
                log.warn("Sheet '{}', column {}: empty field name at position {}. Skipping.",
                        sheetName, col, position);
                continue;
            }

            String  description = getCellString(descripcionRow, col).trim();
            boolean required    = parseRequired(sheetName, getCellString(obligatoriedadRow, col));

            fields.add(new FieldDefinition(position, fieldName, description, required));
        }

        return fields;
    }

    // -------------------------------------------------------------------------
    // Data rows
    // -------------------------------------------------------------------------

    private Map<Integer, String> buildColToFieldMap(Row campoRow) {
        Map<Integer, String> map = new LinkedHashMap<>();
        short lastCell = campoRow.getLastCellNum();
        for (int col = 1; col < lastCell; col++) {
            String name = getCellString(campoRow, col).trim();
            if (!name.isBlank()) {
                map.put(col, name.toLowerCase());
            }
        }
        return map;
    }

    private List<ExcelDataRow> readDataRows(Sheet sheet, int dataStartRowIndex,
                                             Map<Integer, String> colToField) {
        List<ExcelDataRow> rows = new ArrayList<>();
        int lastRow = sheet.getLastRowNum();

        for (int r = dataStartRowIndex; r <= lastRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            Map<String, String> values = new HashMap<>();
            boolean hasAnyValue = false;

            for (Map.Entry<Integer, String> entry : colToField.entrySet()) {
                int    col       = entry.getKey();
                String fieldName = entry.getValue();
                String value     = getCellString(row, col).trim();
                values.put(fieldName, value);
                if (!value.isBlank()) hasAnyValue = true;
            }

            if (hasAnyValue) {
                rows.add(new ExcelDataRow(r, values));
            }
        }

        return rows;
    }

    // -------------------------------------------------------------------------
    // OBLIGATORIEDAD parsing
    // -------------------------------------------------------------------------

    private boolean parseRequired(String sheetName, String obligatoriedad) {
        if (obligatoriedad == null || obligatoriedad.isBlank()) return false;
        String upper = obligatoriedad.trim().toUpperCase();
        if (!upper.startsWith("SI")) return false;
        if (upper.length() > 2) {
            log.warn("Sheet '{}': conditional OBLIGATORIEDAD '{}' treated as REQUIRED.",
                    sheetName, obligatoriedad.trim());
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Cell helpers
    // -------------------------------------------------------------------------

    private Integer parsePosition(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getCellString(Row row, int col) {
        if (row == null) return "";
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cellToString(cell);
    }

    private String cellToString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> {
                // Date cells: format as yyyy-MM-dd to match TXT format
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FMT);
                }
                // Numeric: preserve raw value with up to 2 decimals if needed
                yield numericToString(cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    try {
                        yield cell.getLocalDateTimeCellValue().toLocalDate().format(DATE_FMT);
                    } catch (Exception e) {
                        yield "";
                    }
                }
                try {
                    yield numericToString(cell.getNumericCellValue());
                } catch (Exception e) {
                    yield cell.getStringCellValue();
                }
            }
            default -> "";
        };
    }

    /**
     * Converts a double cell value to a plain-string representation that matches
     * how values appear in the pipe-delimited TXT files.
     *
     * <ul>
     *   <li>Whole numbers (e.g. 10529.0) → "10529"</li>
     *   <li>Decimals (e.g. 26080000.5) → "26080000.5"</li>
     * </ul>
     */
    private static String numericToString(double d) {
        java.math.BigDecimal bd = new java.math.BigDecimal(d);
        bd = bd.stripTrailingZeros();
        return bd.toPlainString();
    }

    /**
     * Normalizes a label value: lowercased, trimmed, and internal whitespace collapsed.
     */
    private String normalize(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private boolean matchesLabel(String normalized, String label) {
        return normalized.equals(label);
    }
}
