package com.extractor.parser;

import com.extractor.model.FieldData;
import com.extractor.ui.ProgressCallback;
import com.extractor.util.FileUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parses Pepperi JSON transaction definitions and extracts JavaScript formulas
 * from calculated fields. Supports both Header and Line fields.
 */
public class JsonFormulaParser {
    /**
     * List to collect all extracted field data during parsing
     */
    private final List<FieldData> fieldDataList = new ArrayList<>();

    /**
     * Parses the input JSON string and extracts all fields with JSFormula.
     *
     * @param jsonContent Raw JSON string from the input file
     * @return List of {@link FieldData} containing formula and metadata
     */
    public List<FieldData> parse(String jsonContent) {
        JsonObject root = JsonParser.parseString(jsonContent).getAsJsonObject();

        // Process both "Fields" (header) and "LineFields" (line items)
        processSection(root, "Fields");
        processSection(root, "LineFields");

        return fieldDataList;
    }

    /**
     * Processes a specific section (Fields or LineFields) from the JSON root.
     *
     * @param root     Root JSON object
     * @param arrayKey Key name of the array to process ("Fields" or "LineFields")
     */
    private void processSection(JsonObject root, String arrayKey) {
        JsonArray array = root.getAsJsonArray(arrayKey);

        String section = arrayKey.equals("LineFields") ? "Line Fields" : "Header Fields";

        // Skip if section doesn't exist
        if (array == null) return;

        for (JsonElement el : array) {
            JsonObject field = el.getAsJsonObject();

            JsonElement creElement = field.get("CalculatedRuleEngine");

            // Skip if no calculation rule
            if (creElement.isJsonNull()) continue;

            JsonObject cre = creElement.getAsJsonObject();

            // Must have JSFormula
            if (cre == null || !cre.has("JSFormula")) continue;

            String formula = cre.get("JSFormula").getAsString().trim();
            if (formula.isEmpty()) continue;

            // Extract required field metadata
            String fieldId = field.get("FieldID").getAsString();
            String label = field.has("Label") ? field.get("Label").getAsString() : "No Label";
            String type = field.has("Type") ? field.get("Type").getAsString() : "Unknown";

            JsonElement participatingFieldsJsonElement = cre.get("ParticipatingFields");

            String[] participatingFields = participatingFieldsJsonElement == null
                    ? new String[0]
                    : participatingFieldsJsonElement.getAsJsonArray().asList().stream().map(JsonElement::getAsString).toArray(String[]::new);

            // Trigger is nested under CalculatedOn.Name
            String trigger = cre.getAsJsonObject("CalculatedOn").get("Name").getAsString();

            // Store full field JSON for context/debugging
            fieldDataList.add(new FieldData(section, fieldId, label, type, trigger, formula, participatingFields));
        }
    }

    /**
     * Writes each extracted formula to a separate .js file in organized folders.
     *
     * @param fields      List of parsed field data
     * @param rootDir     Root output directory
     * @param addComments Flag to determine if comments should be added to the file
     * @param callback    Callback to report progress and logs
     */
    public void writeJsFiles(List<FieldData> fields, File rootDir, boolean addComments, ProgressCallback callback) {
        FileUtils.ensureDir(rootDir);

        // Define output subdirectories
        Map<String, File> sections = Map.of(
                "Header Fields", new File(rootDir, "Header Fields"),
                "Line Fields", new File(rootDir, "Line Fields")
        );
        sections.values().forEach(FileUtils::ensureDir);

        int total = fields.size();
        int processed = 0;

        for (FieldData fd : fields) {
            // Heuristic to determine if field belongs to Line or Header
            String section = fd.section();
            File dir = sections.get(section);
            File jsFile = new File(dir, fd.label() + ".js");

            try (BufferedWriter w = new BufferedWriter(new FileWriter(jsFile))) {

                if (addComments) {
                    // Write header comment block with metadata
                    w.write("/**\n");
                    w.write(" * Section: " + section + "\n");
                    w.write(" * FieldID: " + fd.fieldId() + "\n");
                    w.write(" * Label:   " + fd.label() + "\n");
                    w.write(" * Type:    " + fd.type() + "\n");
                    w.write(" * Trigger: " + fd.trigger() + "\n");
                    w.write(" * Participating Fields: " + (fd.participatingFields().length == 0 ? "No Participating Fields \n" : "\n"));

                    for (String participatingField : fd.participatingFields()) {
                        w.write(" * \t\t" + participatingField + "\n");
                    }

                    w.write(" */\n");
                    w.write(" \n");
                }

                // Write the actual JS formula
                w.write(fd.formula());
                if (!fd.formula().endsWith("\n")) w.write("\n");

                processed++;
                int progress = (int) (processed * 100.0 / total);
                callback.log("Generated: " + section + "/" + jsFile.getName());
                callback.update("Generated: " + section + "/" + jsFile.getName(), progress);

            } catch (IOException e) {
                callback.log("Failed: " + jsFile.getName() + " → " + e.getMessage());
            }
        }
    }
}