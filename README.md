# Pepperi JsonToJsExtractor

A **desktop Java Swing application** that extracts `JSFormula` logic from complex Transaction JSON files and saves them as clean, well-documented `.js` files.

Perfect for project managers or developers who need to **review, debug, or migrate calculated field logic**.

---

## Features

* **Graphical User Interface** (Java Swing)
* **Two output folders**:
    * `Header Fields/` – for `Fields` entries
    * `Line Fields/` – for `LineFields` entries
* Each `.js` file includes:
    * Header comment with `FieldID`, `Label`, `Type`, `Trigger`, `Participating Fields`
    * Extracted `JSFormula` code
* **Auto-generated output folder name** based on the input file
* **Progress bar + log panel**
* **Cross-platform**: Windows, macOS, Linux
* **Native packaging**:
    * Windows `.exe` with bundled mini-JRE (via Launch4j)
    * macOS `.app` image (via jpackage)
    * Universal standalone JAR

---

## Project Structure

```
json-to-js-extractor/
├── src/main/java/com/extractor/
│   ├── Main.java
│   ├── ui/
│   │   ├── MainFrame.java
│   │   └── ProgressPanel.java
│   ├── parser/JsonFormulaParser.java
│   ├── model/FieldData.java
│   └── util/FileUtils.java
├── dist/
│   ├── JsonToJsExtractor.jar
│   ├── win/
│   │   ├── JsonToJsExtractor.exe
│   │   └── jre-mini/
│   └── mac/
│       └── JsonToJsExtractor.app
├── pom.xml
└── README.md
```

---

## How to Run

### Option 1 — Pre-built binaries

1. Open **dist/**
2. Choose your platform:

#### Windows
```
dist/win/JsonToJsExtractor.exe
```
Runs with bundled **mini JRE**, no Java required.

#### macOS
```
dist/mac/JsonToJsExtractor.app
```

#### Linux or all systems (JAR)
```
java -jar dist/JsonToJsExtractor.jar
```

> Requires **Java 23+** if using the standalone JAR.

---

## Build From Source (Maven)

```
git clone https://github.com/Helgen97/pepperi_json_to_js_extractor.git
cd pepperi_json_to_js_extractor
mvn clean package
```

After build:

```
dist/
├─ JsonToJsExtractor.jar
├─ win/
│  ├─ JsonToJsExtractor.exe
│  └─ jre-mini/
└─ mac/
   └─ JsonToJsExtractor.app
```

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

## Example Output

Directory structure:

```
MyConfig_extracted/
├─ Header Fields/
│  └─ TSAHeaderCalculationsInit.js
└─ Line Fields/
   └─ TSALineCalculationChange.js
```

Example `.js` file:

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

---

## Requirements

* Java **23+**
* Maven 3.8+
* Gson (included automatically)

---

## Build & Packaging (based on pom.xml)

POM includes:

### ✔ Uber JAR via `maven-shade-plugin`
Creates:

```
target/JsonToJsExtractor.jar
```

### ✔ Mini JRE creation via `jlink`
Generated in:

```
target/jre-mini
```

### ✔ Windows EXE with bundled JRE via Launch4j
Generated in:

```
dist/win/JsonToJsExtractor.exe
```

### ✔ macOS `.app` via jpackage
Generated in:

```
dist/mac/JsonToJsExtractor.app
```

---

## Troubleshooting

* **GUI doesn’t start**  
  Ensure Java 23+ is installed (`java -version`).

* **JSON not parsed**  
  Check if `Fields` and `LineFields` arrays exist.

* **macOS app blocked**  
  Run:
  ```
  xattr -cr JsonToJsExtractor.app
  ```

---

## License

MIT License — free for individual and commercial use.
