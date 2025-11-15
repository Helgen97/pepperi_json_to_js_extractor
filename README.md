# Pepperi JsonToJsExtractor

A **desktop Java Swing application** that extracts `JSFormula` logic from complex JSON configuration files(Transaction JSON) and saves them as clean, well-documented `.js` files.

Perfect for project managers or developers who need to **review, debug, or migrate calculated field logic**.

---

## Features

* **Graphical User Interface** (Java Swing)
* **Two output folders**:
    * `Header Fields/` – for `Fields` entries
    * `Line Fields/` – for `LineFields` entries
* Each `.js` file includes:
    * Full header comment with `FieldID`, `Label`, `Type`, `Trigger`, `Participating Fields`
    * Clean `JSFormula` code (as extracted from the JSON)
* **Real-time progress bar & logs**
* **Auto-suggested output folder** based on the input JSON filename
* **Cross-platform**: Windows, macOS, Linux

---

## Project Structure

```
json-to-js-extractor/
├── src/
│   └── main/java/com/extractor/
│       ├── MainApp.java
│       ├── ui/
│       │   ├── MainFrame.java
│       │   └── ProgressPanel.java
│       ├── model/FieldData.java
│       ├── parser/JsonFormulaParser.java
│       └── util/FileUtils.java
├── dist/
│   ├── JsonToJsExtractor.jar          ← Run with Java
│   └── JsonToJsExtractor.exe          ← Windows native launcher
├── pom.xml
├── LICENSE
└── README.md
```

---

## How to Run

### Option 1 — Use pre-built files (recommended for non-developers)

1. Open the **`dist/`** folder.
2. **Windows**: Double-click `JsonToJsExtractor.exe`.
   **macOS / Linux**: Run the JAR:

```bash
java -jar JsonToJsExtractor.jar
```

*No installation required. Java 23+ must be installed.*

### Option 2 — Build and run from source (Maven)

```bash
git clone https://github.com/Helgen97/pepperi_json_to_js_extractor.git
cd pepperi_json_to_js_extractor
mvn clean package
java -jar dist/JsonToJsExtractor.jar
```

The generated native package will be placed in the output directory you specify.

---

## Input JSON Format (example)

```json
{
  "Fields": [
    {
      "FieldID": "TSAHeaderCalculationsInit",
      "Label": "Header Init",
      "Type": "Calculated",
      "CalculatedRuleEngine": {
        "JSFormula": "function calculate() { return 42; }",
        "ParticipatingFields": [],
        "CalculatedOn": { "Name": "OnLoad" }
      }
    }
  ],
  "LineFields": [
    {
      "FieldID": "TSALineCalculationChange",
      "Label": "Line Change",
      "Type": "Calculated",
      "CalculatedRuleEngine": {
        "JSFormula": "function calculateLine() { return item.qty * item.price; }",
        "ParticipatingFields": [
          "TSAAOQMQuantity1",
          "TSAFSUnitPrice",
          "TSALineOnMarginChange"
        ],
        "CalculatedOn": { "Name": "OnChange" }
      }
    }
  ]
}
```

---

## Example Output (directory)

```
textMyConfig_extracted/
├─ Header Fields/
│  └─ TSAHeaderCalculationsInit.js
└─ Line Fields/
   └─ TSALineCalculationChange.js
```

### Sample `.js` file content generated for a header field

```js
/**
 * SECTION: Header Fields
 * FieldID: TSAHeaderCalculationsInit
 * Label:   Header Init
 * Type:    Calculated
 * Trigger: OnLoad
 * Participating Fields: No Participating Fields
 */

function calculate() {
  return 42;
}
```

Notes:

* The original JSON is pretty-printed and included in the comment block for easier reference.
* The extracted `JSFormula` is written below the header comment unchanged (but can be post-processed for formatting).

---

## Requirements

* Java 23 or newer
* Maven (for building from source)
* Gson (managed via Maven dependency, e.g. `com.google.code.gson:gson:2.10.1`)

---

## Build & Distribution

To build a distributable JAR and optionally native packages:

```bash
mvn clean package
# JAR will be available under target/ (name depends on your pom)
```

After packaging, generated files will be placed into `dist/`. Redistribute `dist/` as desired.

---

## Troubleshooting

* If the GUI does not start, ensure your machine has a compatible Java runtime (Java 23+). Check with `java -version`.
* If fields are not detected, validate your JSON with a JSON validator and confirm `Fields` / `LineFields` arrays exist.
* For encoding issues, ensure files are UTF-8 encoded.

---

## License

MIT License — free to use, modify, and distribute.

---