package com.ive.model;

/**
 * Represents a single field declared in one sheet of the Excel Data Dictionary.
 *
 * <p>The {@code position} is the 1-based index of the field within a pipe-delimited
 * sábana line (i.e. {@code tokens[position - 1]}).
 */
public class FieldDefinition {

    private final int position;
    private final String fieldName;
    private final String description;
    private final boolean required;

    public FieldDefinition(int position, String fieldName, String description, boolean required) {
        this.position    = position;
        this.fieldName   = fieldName;
        this.description = description;
        this.required    = required;
    }

    public int getPosition()      { return position; }
    public String getFieldName()  { return fieldName; }
    public String getDescription(){ return description; }
    public boolean isRequired()   { return required; }

    @Override
    public String toString() {
        return "FieldDefinition{position=" + position
                + ", fieldName='" + fieldName + '\''
                + ", required=" + required + '}';
    }
}
