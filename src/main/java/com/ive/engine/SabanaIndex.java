package com.ive.engine;

import com.ive.exceptions.IveException;
import com.ive.model.FieldDefinition;
import com.ive.util.FlatFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Indexes the sábana (pipe-delimited TXT) for fast record lookup.
 *
 * <p>Each line is indexed by a composite key built from the values at the
 * positions of the designated key fields.  The key format is
 * {@code value1|value2} (lower-cased and trimmed).
 *
 * <p>If two lines share the same composite key, only the first occurrence
 * is kept (with a WARN). This mirrors the expected uniqueness constraint
 * of the sábana.
 */
public class SabanaIndex {

    private static final Logger log = LoggerFactory.getLogger(SabanaIndex.class);

    /** Composite-key → raw token array for that line. */
    private final Map<String, String[]> index = new HashMap<>();

    private final int totalLines;

    /**
     * Builds the index from a sábana TXT file.
     *
     * @param sabanaPath  path to the pipe-delimited TXT file
     * @param allFields   ordered field definitions (provide position → field name mapping)
     * @param keyFields   names of the fields that form the composite key
     * @throws IveException if the file cannot be read
     */
    public SabanaIndex(String sabanaPath, List<FieldDefinition> allFields, List<String> keyFields)
            throws IveException {

        FlatFileReader reader = new FlatFileReader(sabanaPath);
        List<String> lines = reader.readLines();

        // Build position → fieldName lookup (1-based position → fieldName lower)
        Map<Integer, String> posToName = new HashMap<>();
        for (FieldDefinition fd : allFields) {
            posToName.put(fd.getPosition(), fd.getFieldName().toLowerCase().trim());
        }

        // Build fieldName → position lookup
        Map<String, Integer> nameToPos = new HashMap<>();
        for (FieldDefinition fd : allFields) {
            nameToPos.put(fd.getFieldName().toLowerCase().trim(), fd.getPosition());
        }

        // Resolve key positions (1-based)
        int[] keyPositions = keyFields.stream()
                .mapToInt(k -> {
                    Integer pos = nameToPos.get(k.toLowerCase().trim());
                    if (pos == null) {
                        log.warn("Key field '{}' not found in field definitions — key lookup will be incomplete.", k);
                        return -1;
                    }
                    return pos;
                })
                .toArray();

        for (String line : lines) {
            String[] tokens = FlatFileReader.splitLine(line);
            String key = buildKey(tokens, keyPositions);

            if (index.containsKey(key)) {
                log.warn("Duplicate sábana key '{}' — keeping first occurrence.", key);
            } else {
                index.put(key, tokens);
            }
        }

        this.totalLines = lines.size();
        log.info("SabanaIndex built: {} record(s) indexed.", index.size());
    }

    /**
     * Finds a sábana record by composite key built from the given Excel row values.
     *
     * @param excelValues map of fieldName (lower) → value from Excel row
     * @param keyFields   key field names in order
     * @return the token array for the matching sábana line, or {@code null} if not found
     */
    public String[] find(Map<String, String> excelValues, List<String> keyFields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyFields.size(); i++) {
            if (i > 0) sb.append("|");
            String val = excelValues.getOrDefault(keyFields.get(i).toLowerCase().trim(), "").trim().toLowerCase();
            sb.append(val);
        }
        return index.get(sb.toString());
    }

    public int totalLines() {
        return totalLines;
    }

    // -------------------------------------------------------------------------

    private String buildKey(String[] tokens, int[] keyPositions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyPositions.length; i++) {
            if (i > 0) sb.append("|");
            int pos = keyPositions[i];
            if (pos < 1 || pos > tokens.length) {
                sb.append("");
            } else {
                sb.append(tokens[pos - 1].trim().toLowerCase());
            }
        }
        return sb.toString();
    }
}
