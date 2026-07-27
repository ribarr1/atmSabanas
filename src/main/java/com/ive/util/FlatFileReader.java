package com.ive.util;

import com.ive.exceptions.IveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a pipe-delimited flat interface file ({@code .txt}) line by line.
 *
 * <p>Each line is returned as a raw string.  Splitting by the {@code |}
 * delimiter is the caller's responsibility, since different components
 * need the raw line for different purposes (field mapping, error reporting).
 *
 * <p>Empty lines at the end of the file are ignored.
 */
public class FlatFileReader {

    private static final Logger log = LoggerFactory.getLogger(FlatFileReader.class);

    private final String filePath;

    /**
     * @param filePath absolute or relative path to the TXT interface file
     */
    public FlatFileReader(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads all non-empty lines from the file.
     *
     * @return list of raw lines in file order; never null
     * @throws IveException if the file does not exist or cannot be read
     */
    public List<String> readLines() throws IveException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IveException("Interface file not found: " + filePath);
        }

        log.info("Reading interface file: {}", filePath);

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            throw new IveException("Cannot read interface file: " + filePath, e);
        }

        log.info("Interface file loaded: {} line(s).", lines.size());
        return lines;
    }

    /**
     * Splits one raw line into tokens using {@code |} as the delimiter.
     *
     * <p>{@code split("\\|", -1)} is used so that trailing empty fields are
     * preserved (e.g. a line ending in {@code ||} produces two empty strings
     * at the end, not zero).
     *
     * @param line raw line from the interface file
     * @return array of field tokens in position order (1-based index = token[index-1])
     */
    public static String[] splitLine(String line) {
        return line.split("\\|", -1);
    }

    /**
     * Returns the name of the file without path or extension.
     *
     * <p>Example: {@code /data/sb_dato_cuentas_por_cobrar_20271123.txt}
     * → {@code sb_dato_cuentas_por_cobrar_20271123}
     */
    public String getFileName() {
        String name = new File(filePath).getName();
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(0, dot) : name;
    }
}
