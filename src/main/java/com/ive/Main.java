package com.ive;

import com.ive.dictionary.DictionaryReader;
import com.ive.engine.ComparisonEngine;
import com.ive.excel.ResultWriter;
import com.ive.exceptions.IveException;
import com.ive.model.ComparisonSummary;
import com.ive.model.DictionaryModel;
import com.ive.model.SheetDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Application entry point for the Interface Validation Engine (IVE).
 *
 * <p>Usage: java -jar ive.jar &lt;dictionary.xlsx&gt; &lt;sabana.txt&gt;
 *
 * <p>Arguments:
 * <ul>
 *   <li>args[0] - path to the Excel Data Dictionary (contains data rows to validate)</li>
 *   <li>args[1] - path to the sábana TXT file (pipe-delimited, used as source of truth)</li>
 * </ul>
 *
 * <p>The tab matched inside the Excel is determined by the sábana file name.
 * The output is a new Excel file written to the same directory as the sábana.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Interface Validation Engine - starting");

        if (args.length < 2) {
            log.error("Usage: ive.jar <dictionary.xlsx> <sabana.txt>");
            System.exit(1);
        }

        String dictionaryPath = args[0];
        String sabanaPath     = args[1];

        log.info("Dictionary : {}", dictionaryPath);
        log.info("Sábana     : {}", sabanaPath);

        try {
            // 1. Read Excel dictionary (schema + data rows)
            DictionaryModel model = new DictionaryReader(dictionaryPath).read();
            log.info("Dictionary loaded: {} sheet(s).", model.sheets().size());

            // 2. Match the sábana file name to an Excel tab
            String sabanaFileName = new File(sabanaPath).getName();
            String sheetFragment  = extractSheetFragment(sabanaFileName);
            log.info("Looking for sheet matching fragment: '{}'", sheetFragment);

            SheetDefinition sheet = model.findSheet(sheetFragment)
                    .orElseThrow(() -> new IveException(
                            "No sheet found matching fragment '" + sheetFragment
                            + "' in dictionary: " + dictionaryPath));

            log.info("Matched sheet: '{}' ({} fields, {} Excel data row(s))",
                    sheet.sheetName(), sheet.fields().size(), sheet.dataRows().size());

            if (sheet.dataRows().isEmpty()) {
                log.warn("No data rows found in sheet '{}'. Nothing to compare.", sheet.sheetName());
                System.exit(0);
            }

            // 3. Compare Excel rows against sábana
            ComparisonSummary summary = new ComparisonEngine(sheet).compare(sabanaPath);

            // 4. Write result Excel
            String outputDir  = new File(sabanaPath).getParent();
            String outputPath = new ResultWriter(sheet, outputDir).write(summary);

            // 5. Log final result
            if (summary.passed()) {
                log.info("Result: PASS - all {} Excel row(s) match the sábana.", summary.totalExcelRows());
            } else {
                log.warn("Result: FAIL - {}/{} Excel row(s) have errors. See: {}",
                        summary.totalErrors(), summary.totalExcelRows(), outputPath);
            }

        } catch (IveException e) {
            log.error("Fatal error: {}", e.getMessage(), e);
            System.exit(2);
        }

        log.info("Interface Validation Engine - done");
    }

    /**
     * Derives the sheet-name fragment from the sábana file name.
     *
     * <p>Examples:
     * <pre>
     *   sb_dato_operacion_fondos_20271124.txt  →  sb_dato_operacion_fondos_20271124
     *   sb_dato_cuentas_por_cobrar_20271123.txt →  sb_dato_cuentas_por_cobrar_20271123
     * </pre>
     * The match in {@link DictionaryModel#findSheet} uses "contains", so a longer
     * file-name fragment will still find a shorter tab name.
     */
    private static String extractSheetFragment(String fileName) {
        int dotIdx = fileName.lastIndexOf('.');
        return dotIdx > 0 ? fileName.substring(0, dotIdx) : fileName;
    }
}

