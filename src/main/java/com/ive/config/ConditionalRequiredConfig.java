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

            // Columns AB, AC,AF,AH (pos 28,31,33)
            // Required if column D (do_codigo_producto, pos 3) = FVIV
            new ConditionalRequiredRule(
                    List.of(27, 28, 31, 33),
                    3,
                    List.of("FVIV")
            ),

            // Columns AD,AP,AQ (pos 29,41,42)
            // Required if column D (do_codigo_producto, pos 3) = FGAR
            new ConditionalRequiredRule(
                    List.of(29, 41, 42),
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

        RULES_BY_TAB = Map.of(
                "sb_dato_operacion_fondos", opFondosRules,
                "sb_dato_procesos_especiales", List.of(
                        // pos 5 (dp_aplicacion_previa): AMA
                        new ConditionalRequiredRule(List.of(5), 1, List.of("AMA")),
                        // pos 6 (dp_monto_aporte): AMA, CEA, LIQ
                        new ConditionalRequiredRule(List.of(6), 1, List.of("AMA", "CEA", "LIQ")),
                        // pos 7,8 (dp_monto_pignorado, dp_monto_aporte_disponible): AMA, CEA
                        new ConditionalRequiredRule(List.of(7, 8), 1, List.of("AMA", "CEA")),
                        // pos 9 (dp_monto_solicitado): AMA, AUX
                        new ConditionalRequiredRule(List.of(9), 1, List.of("AMA", "AUX")),
                        // pos 10 (dp_monto_aprobado): AMA, AUX, REAC
                        new ConditionalRequiredRule(List.of(10), 1, List.of("AMA", "AUX", "REAC")),
                        // pos 11 (dp_motivo): AUX, CEA, REC, LIQ
                        new ConditionalRequiredRule(List.of(11), 1, List.of("AUX", "CEA", "REC", "LIQ")),
                        // pos 13-16 (dp_id_cedente..dp_valor_transferir_cesionario): CEA
                        new ConditionalRequiredRule(List.of(13, 14, 15, 16), 1, List.of("CEA")),
                        // pos 17-19 (dp_tiempo_sugerido, dp_tiempo, dp_fecha_vencimiento): REC
                        new ConditionalRequiredRule(List.of(17, 18, 19), 1, List.of("REC")),
                        // pos 20-24 (dp_id_liquidacion..dp_estado_orden_pago_liquidacion): REI
                        new ConditionalRequiredRule(List.of(20, 21, 22, 23, 24), 1, List.of("REI")),
                        // pos 25 (dp_orden_pago_liquidacion): REI only (LIQ removed in V18)
                        new ConditionalRequiredRule(List.of(25), 1, List.of("REI")),
                        // pos 26-29 (dp_valor_transferir..dp_codigo_origen): TEF
                        new ConditionalRequiredRule(List.of(26, 27, 28, 29), 1, List.of("TEF")),
                        // pos 31 (dp_ids_orden_pago): LIQ, AUX, CEA, AMA (AMA added in V18)
                        new ConditionalRequiredRule(List.of(31), 1, List.of("LIQ", "AUX", "CEA", "AMA")),
                        // pos 30,32-42,46,55: LIQ only
                        new ConditionalRequiredRule(
                                List.of(30, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 46, 55), 1,
                                List.of("LIQ")),
                        // pos 44,47,48,50-52 (dp_observaciones pos 49 removed - now NO in V18): all types
                        new ConditionalRequiredRule(
                                List.of(44, 47, 48, 50, 51, 52), 1,
                                List.of("AMA", "CEA", "AUX", "LIQ", "REC", "REI", "TEF", "REAC")),
                        // pos 56-61 (dp_aplicativo..dp_fecha_generacion): AMA only (V18)
                        new ConditionalRequiredRule(
                                List.of(56, 57, 58, 59, 60, 61), 1,
                                List.of("AMA"))
                )
        );
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
