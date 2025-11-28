package com.extractor.model;

/**
 * Immutable data holder for extracted field information from JSON.
 * Represents a single calculated field with its formula and metadata.
 *
 * @param section             Section identification(Header Field or Line Field)
 * @param fieldId             Unique identifier of the field
 * @param label               Human-readable label of the field
 * @param type                Type of the field (e.g., Text, Number)
 * @param trigger             Event that triggers the formula execution
 * @param formula             JavaScript formula string
 * @param participatingFields String array of participating fields
 */
public record FieldData(
        String section,
        String fieldId,
        String label,
        String type,
        String trigger,
        String formula,
        String[] participatingFields
) {
}