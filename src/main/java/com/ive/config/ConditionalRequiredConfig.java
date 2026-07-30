package com.ive.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Defines conditional required-field rules per tab.
 *
 * <p>A conditional required rule states: "field X is required when the value
 * of conditionField matches one of the expected values."
 *
 * <p>Fields are identified by their 1-based TXT position (from POSICION DE CAMPOS).
 *
 * <p>To add rules for a new tab, add an entry in {@link #RULES_BY_TAB}.
 */
public final class ConditionalRequiredConfig {

    /**
     * Represents a single conditional required rule.
     *
     * @param targetPositions   TXT positions of the fields that become required when condition is met
     * @param conditionPosition TXT position of the field whose value is checked
     * @param conditionValues   accepted values for the condition field (case-insensitive)
     */
    public record ConditionalRequiredRule(
            List<Integer> targetPositions,
            int           conditionPosition,
            List<String>  conditionValues
    ) {}

    private static final Map<String, List<ConditionalRequiredRule>> RULES_BY_TAB;

    static {
        // -----------------------------------------------------------------------
        // sb_dato_operacion_fondos rules
        // Column letters refer to the Excel layout (A = label column, B = pos 1)
        // -----------------------------------------------------------------------
        List<ConditionalRequiredRule> opFondosRules = List.of(

            // Columns S,Y,Z,AA,AE,AG (pos 18,24,25,26,30,32)
            // Required if column D (do_codigo_producto, pos 3) = FVIV or FGAR
            new ConditionalRequiredRule(
                    List.of(18, 24, 25, 26, 30, 32),
                    3,
                    List.of("FVIV", "FGAR")
            ),

            // Columns AC,AF,AH (pos 28,31,33)
            // Required if column D (do_codigo_producto, pos 3) = FVIV
            new ConditionalRequiredRule(
                    List.of(28, 31, 33),
                    3,
                    List.of("FVIV")
            ),

            // Columns AD,AQ (pos 29,42)
            // Required if column D (do_codigo_producto, pos 3) = FGAR
            new ConditionalRequiredRule(
                    List.of(29, 42),
                    3,
                    List.of("FGAR")
            ),

            // Column AS (pos 44)
            // Required if column F (do_tipo, pos 5) = E
            new ConditionalRequiredRule(
                    List.of(44),
                    5,
                    List.of("E")
            )
        );

        RULES_BY_TAB = Map.of("sb_dato_operacion_fondos", opFondosRules);
    }

    private ConditionalRequiredConfig() {}

    /**
     * Returns the conditional required rules for the given sheet name.
     * Matching is case-insensitive and uses "contains".
     *
     * @param sheetName actual tab name from the workbook
     * @return list of rules; empty if none configured for this tab
     */
    public static List<ConditionalRequiredRule> getRules(String sheetName) {
        String normalized = sheetName.toLowerCase().trim();
        for (Map.Entry<String, List<ConditionalRequiredRule>> entry : RULES_BY_TAB.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return Collections.emptyList();
    }
}
