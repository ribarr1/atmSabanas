package com.ive.exceptions;

/**
 * Single checked exception for all fatal errors in the Interface Validation Engine.
 *
 * <p>Thrown when the engine cannot continue execution, for example:
 * <ul>
 *   <li>the Excel workbook cannot be opened or is corrupted</li>
 *   <li>the TXT interface file is missing</li>
 *   <li>a required worksheet structure cannot be detected</li>
 *   <li>an unexpected runtime failure occurs</li>
 * </ul>
 *
 * <p>Business validation failures are <strong>not</strong> exceptions.
 * They are represented by {@code ValidationResult}.
 */
public class IveException extends Exception {

    public IveException(String message) {
        super(message);
    }

    public IveException(String message, Throwable cause) {
        super(message, cause);
    }
}
