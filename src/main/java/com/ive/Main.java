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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * Usage (explicit): java -jar ive.jar &lt;dictionary.xlsx&gt; &lt;sabana.txt&gt;
 * Usage (auto):     java -jar ive.jar
 *   Auto mode picks the single .xlsx and single .txt found in the JAR's folder.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Interface Validation Engine - starting");

        String dictionaryPath;
        String sabanaPath;

        if (args.length >= 2) {
            dictionaryPath = args[0];
            sabanaPath     = args[1];
        } else {
            // Auto-detect files from the JAR's directory
            File jarDir = resolveJarDirectory();
            log.info("Auto mode — scanning: {}", jarDir.getAbsolutePath());

            List<File> xlsxFiles = listByExtension(jarDir, ".xlsx");
            List<File> txtFiles  = listByExtension(jarDir, ".txt");

            if (xlsxFiles.isEmpty()) {
                log.error("No .xlsx file found in {}. Place the Excel dictionary next to the JAR.", jarDir);
                System.exit(1);
            }
            if (xlsxFiles.size() > 1) {
                log.error("Multiple .xlsx files found in {}. Leave only one Excel file next to the JAR: {}",
                        jarDir, xlsxFiles);
                System.exit(1);
            }
            if (txtFiles.isEmpty()) {
                log.error("No .txt file found in {}. Place the sábana TXT next to the JAR.", jarDir);
                System.exit(1);
            }
            if (txtFiles.size() > 1) {
                log.error("Multiple .txt files found in {}. Leave only one TXT file next to the JAR: {}",
                        jarDir, txtFiles);
                System.exit(1);
            }

            dictionaryPath = xlsxFiles.get(0).getAbsolutePath();
            sabanaPath     = txtFiles.get(0).getAbsolutePath();
        }

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

    /** Returns the directory that contains the running JAR; falls back to working directory. */
    private static File resolveJarDirectory() {
        try {
            File jar = new File(Main.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            return jar.isFile() ? jar.getParentFile() : jar;
        } catch (URISyntaxException e) {
            return new File(System.getProperty("user.dir"));
        }
    }

    /** Lists files in {@code dir} whose name ends with {@code extension} (case-insensitive), excluding result files. */
    private static List<File> listByExtension(File dir, String extension) {
        File[] files = dir.listFiles(f ->
                f.isFile()
                && f.getName().toLowerCase().endsWith(extension)
                && !f.getName().startsWith("resultado_"));
        return files == null ? List.of() : Arrays.asList(files);
    }
}

