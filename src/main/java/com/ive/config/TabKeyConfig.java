package com.ive.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Centralised configuration of composite key fields per Excel tab.
 *
 * <p>When a new tab needs to be supported, add an entry to {@code KEY_MAP}
 * using a normalised fragment of the tab name (lower-case, trimmed).
 */
public final class TabKeyConfig {

    /**
     * Maps a normalised tab-name fragment to its composite key field names.
     * The fragment is matched with a case-insensitive "contains" check against
     * the actual sheet name.
     */
    private static final Map<String, List<String>> KEY_MAP;

    static {
        Map<String, List<String>> m = new HashMap<>();
        m.put("sb_dato_transacciones_fondos",  List.of("dt_op_id",       "dt_cob_asociado"));
        m.put("sb_dato_operacion_fondos",       List.of("do_id",          "do_ente"));
        m.put("sb_dato_cuentas_por_cobrar",     List.of("dc_ent",         "dc_op_id"));
        m.put("sb_dato_tasa_compensada",        List.of("dt_operacion_id","dt_cliente_id"));
        m.put("sb_dato_procesos_especiales",    List.of("dp_proceso_especial", "dp_secuencial_proceso"));
        KEY_MAP = Collections.unmodifiableMap(m);
    }

    private TabKeyConfig() {}

    /**
     * Returns the key fields for the given sheet name, or empty if unknown.
     *
     * @param sheetName actual tab name from the workbook
     */
    public static Optional<List<String>> getKeyFields(String sheetName) {
        String normalized = sheetName.toLowerCase().trim();
        for (Map.Entry<String, List<String>> entry : KEY_MAP.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }
}
