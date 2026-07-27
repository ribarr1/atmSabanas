package com.ive.model;

/**
 * A single field-level error found during comparison.
 */
public record FieldError(
        String fieldName,
        ErrorType errorType,
        String excelValue,
        String sabanaValue
) {

    public enum ErrorType {
        /** Required field is empty in the Excel record. */
        REQUIRED,
        /** Value in Excel does not match value in the sábana. */
        MISMATCH,
        /** No matching record was found in the sábana for the given key. */
        NOT_FOUND
    }

    public String toObservation() {
        return switch (errorType) {
            case REQUIRED  -> "Campo obligatorio vacío: " + fieldName;
            case MISMATCH  -> "Campo [" + fieldName + "]: Excel='" + excelValue + "' Sábana='" + sabanaValue + "'";
            case NOT_FOUND -> "Registro no encontrado en la sábana";
        };
    }
}
