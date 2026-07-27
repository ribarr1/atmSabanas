package com.ive.model;

import java.util.List;
import java.util.Optional;

/**
 * Represents one worksheet from the Excel Data Dictionary + its data rows.
 *
 * @param sheetName name of the worksheet tab
 * @param fields    field definitions in position order (from POSICION DE CAMPOS)
 * @param dataRows  actual data rows to compare against the sábana
 */
public record SheetDefinition(String sheetName, List<FieldDefinition> fields, List<ExcelDataRow> dataRows) {

    /** Finds a FieldDefinition by name (case-insensitive). */
    public Optional<FieldDefinition> findField(String name) {
        String key = name.toLowerCase().trim();
        return fields.stream()
                .filter(f -> f.getFieldName().toLowerCase().trim().equals(key))
                .findFirst();
    }

    /**
     * Returns a human-readable summary for logging and debugging.
     *
     * <p>Example output:
     * <pre>
     * Sheet: sb_dato_cuentas_por_cobrar (27 fields)
     *   [ 1] dc_id             REQUIRED
     *   [ 2] dc_ent            REQUIRED
     *   ...
     * </pre>
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sheet: ").append(sheetName)
          .append(" (").append(fields.size()).append(" fields)\n");

        for (FieldDefinition f : fields) {
            sb.append(String.format("  [%2d] %-30s %s%n",
                    f.getPosition(),
                    f.getFieldName(),
                    f.isRequired() ? "REQUIRED" : "OPTIONAL"));
        }

        return sb.toString();
    }
}
