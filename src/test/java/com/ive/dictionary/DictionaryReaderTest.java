package com.ive.dictionary;

import com.ive.exceptions.IveException;
import com.ive.model.DictionaryModel;
import com.ive.model.FieldDefinition;
import com.ive.model.SheetDefinition;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DictionaryReader}.
 *
 * <p>Each test uses a dedicated small Excel file from
 * {@code src/test/resources/dictionary/}.
 * No validation data is hardcoded inline; all input comes from those files.
 */
class DictionaryReaderTest {

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private String resourcePath(String fileName) throws Exception {
        URL url = getClass().getClassLoader().getResource("dictionary/" + fileName);
        assertNotNull(url, "Test resource not found: " + fileName);
        return Paths.get(url.toURI()).toString();
    }

    // -------------------------------------------------------------------------
    // 1. Basic structure detection — labels in different rows
    // -------------------------------------------------------------------------

    @Test
    void testSimpleSheet_detectsLabelsAndBuildsFields() throws Exception {
        DictionaryReader reader = new DictionaryReader(resourcePath("test-simple.xlsx"));
        DictionaryModel model = reader.read();

        assertEquals(1, model.sheets().size());
        SheetDefinition sheet = model.sheets().get(0);
        assertEquals("test_simple", sheet.sheetName());
        assertEquals(3, sheet.fields().size());
    }

    @Test
    void testSimpleSheet_fieldPositionsAreOrdered() throws Exception {
        DictionaryModel model = new DictionaryReader(resourcePath("test-simple.xlsx")).read();
        List<FieldDefinition> fields = model.sheets().get(0).fields();

        assertEquals(1, fields.get(0).getPosition());
        assertEquals(2, fields.get(1).getPosition());
        assertEquals(3, fields.get(2).getPosition());
    }

    @Test
    void testSimpleSheet_fieldNamesAreCorrect() throws Exception {
        DictionaryModel model = new DictionaryReader(resourcePath("test-simple.xlsx")).read();
        List<FieldDefinition> fields = model.sheets().get(0).fields();

        assertEquals("field_id",     fields.get(0).getFieldName());
        assertEquals("field_name",   fields.get(1).getFieldName());
        assertEquals("field_status", fields.get(2).getFieldName());
    }

    // -------------------------------------------------------------------------
    // 2. OBLIGATORIEDAD — SI, NO, conditional
    // -------------------------------------------------------------------------

    @Test
    void testSimpleSheet_obligatoriedad_si() throws Exception {
        DictionaryModel model = new DictionaryReader(resourcePath("test-simple.xlsx")).read();
        List<FieldDefinition> fields = model.sheets().get(0).fields();

        assertTrue(fields.get(0).isRequired(),  "field_id should be REQUIRED");
        assertFalse(fields.get(1).isRequired(), "field_name should be OPTIONAL");
        assertTrue(fields.get(2).isRequired(),  "field_status should be REQUIRED");
    }

    @Test
    void testConditionalObligatoriedad_treatedAsRequired() throws Exception {
        DictionaryModel model = new DictionaryReader(
                resourcePath("test-conditional-obligatoriedad.xlsx")).read();
        List<FieldDefinition> fields = model.sheets().get(0).fields();

        // "SI cuando Tipo de Proceso (AMA)" → required = true for MVP
        assertTrue(fields.get(1).isRequired(), "Conditional SI should be treated as REQUIRED in MVP");
        assertFalse(fields.get(2).isRequired(), "NO should be OPTIONAL");
    }

    // (REGLAS/DSL tests removed — DSL flow replaced by Excel-vs-Sábana comparison)

    // -------------------------------------------------------------------------
    // 5. CAMPO fallback to next row
    // -------------------------------------------------------------------------

    @Test
    void testCampoFallback_fieldNamesReadFromNextRow() throws Exception {
        DictionaryModel model = new DictionaryReader(resourcePath("test-campo-fallback.xlsx")).read();
        List<FieldDefinition> fields = model.sheets().get(0).fields();

        assertEquals(2, fields.size());
        assertEquals("real_id",   fields.get(0).getFieldName());
        assertEquals("real_name", fields.get(1).getFieldName());
    }

    // -------------------------------------------------------------------------
    // 6. Multi-sheet: invalid sheet skipped, valid sheet loaded
    // -------------------------------------------------------------------------

    @Test
    void testMultiSheet_invalidSheetIsSkipped() throws Exception {
        DictionaryModel model = new DictionaryReader(resourcePath("test-multi-sheet.xlsx")).read();

        // Only valid_sheet should be in the model
        assertEquals(1, model.sheets().size());
        assertEquals("valid_sheet", model.sheets().get(0).sheetName());
    }

    @Test
    void testMultiSheet_validSheetFieldsAreCorrect() throws Exception {
        DictionaryModel model = new DictionaryReader(resourcePath("test-multi-sheet.xlsx")).read();
        List<FieldDefinition> fields = model.sheets().get(0).fields();

        assertEquals(2, fields.size());
        assertEquals("entity_id",   fields.get(0).getFieldName());
        assertEquals("entity_code", fields.get(1).getFieldName());
    }

    // -------------------------------------------------------------------------
    // 7. findSheet by name fragment
    // -------------------------------------------------------------------------

    @Test
    void testFindSheet_matchesByFragment() throws Exception {
        DictionaryModel model = new DictionaryReader(resourcePath("test-simple.xlsx")).read();
        Optional<SheetDefinition> result = model.findSheet("simple");

        assertTrue(result.isPresent());
        assertEquals("test_simple", result.get().sheetName());
    }

    @Test
    void testFindSheet_noMatchReturnsEmpty() throws Exception {
        DictionaryModel model = new DictionaryReader(resourcePath("test-simple.xlsx")).read();
        Optional<SheetDefinition> result = model.findSheet("nonexistent_interface");

        assertFalse(result.isPresent());
    }

    // (compiledRules test removed — DSL flow replaced)

    // -------------------------------------------------------------------------
    // 9. Fatal errors
    // -------------------------------------------------------------------------

    @Test
    void testFileNotFound_throwsIveException() {
        DictionaryReader reader = new DictionaryReader("nonexistent/path/file.xlsx");
        assertThrows(IveException.class, reader::read);
    }

    // -------------------------------------------------------------------------
    // 10. Integration — real Excel file (if present on disk)
    // -------------------------------------------------------------------------

    @Test
    void integrationTest_realDictionary_loadsSuccessfully() throws Exception {
        String realPath = "C:\\Users\\RubenDarioIbarraVirl\\Documents\\atmSabanas" +
                          "\\Sabanas Fondos Diccionario de Datos V8 QA.xlsx";

        java.io.File file = new java.io.File(realPath);
        org.junit.jupiter.api.Assumptions.assumeTrue(file.exists(),
                "Skipping integration test: real dictionary not found at " + realPath);

        DictionaryModel model = new DictionaryReader(realPath).read();

        assertTrue(model.sheets().size() > 0, "Real dictionary should contain at least one valid sheet");

        // Print full structure for visual inspection
        System.out.println(model.toDisplayString());
    }
}
