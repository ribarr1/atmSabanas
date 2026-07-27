package com.ive.model;

import java.util.List;
import java.util.Optional;

/**
 * Top-level model representing the complete Excel Data Dictionary.
 *
 * <p>Contains one {@link SheetDefinition} per worksheet found in the workbook.
 * All sheets are loaded regardless of content; the engine selects the relevant
 * one by matching the interface file name.
 *
 * @param sheets all sheet definitions read from the workbook
 */
public record DictionaryModel(List<SheetDefinition> sheets) {

    /**
     * Finds a sheet whose name contains {@code nameFragment} (case-insensitive).
     *
     * <p>Used to match an interface file like
     * {@code sb_dato_cuentas_por_cobrar_20271123.txt} against the sheet
     * {@code sb_dato_cuentas_por_cobrar}.
     *
     * @param nameFragment partial sheet name derived from the interface file name
     * @return matching sheet, or empty if none found
     */
    public Optional<SheetDefinition> findSheet(String nameFragment) {
        String fragment = nameFragment.trim().toLowerCase();
        return sheets.stream()
                .filter(s -> {
                    String sheetNorm = s.sheetName().trim().toLowerCase();
                    // Match if either contains the other (handles date suffix in file names)
                    return sheetNorm.contains(fragment) || fragment.contains(sheetNorm);
                })
                .findFirst();
    }

    /**
     * Returns a human-readable summary of all sheets for logging and debugging.
     */
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Dictionary Model ===\n");
        sb.append("Total sheets: ").append(sheets.size()).append("\n\n");
        for (SheetDefinition sheet : sheets) {
            sb.append(sheet.toDisplayString()).append("\n");
        }
        return sb.toString();
    }
}
