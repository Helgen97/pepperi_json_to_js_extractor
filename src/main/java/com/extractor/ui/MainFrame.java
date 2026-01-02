package com.extractor.ui;

import com.extractor.config.UserPreferences;
import com.extractor.parser.JsonFormulaParser;
import com.extractor.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Main application window.
 * <p>
 * Orchestrates user interaction:
 * <ul>
 *     <li>Select input JSON file</li>
 *     <li>Select output directory</li>
 *     <li>Configure generation options</li>
 *     <li>Run extraction in a background thread</li>
 * </ul>
 */
public class MainFrame extends JFrame {

    /**
     * Input JSON path field
     */
    private final JTextField inputField = new JTextField();

    /**
     * Output directory path field
     */
    private final JTextField outputField = new JTextField();

    /**
     * Main action button
     */
    private final JButton startBtn = new JButton("START");

    /**
     * Progress bar + log output panel
     */
    private final ProgressPanel progressPanel = new ProgressPanel();

    /**
     * Toggle for JS header comments
     */
    private final JCheckBox addCommentsCheckBox =
            new JCheckBox("Add comments to generated JS files", true);

    /**
     * Creates and initializes the main window.
     */
    public MainFrame() {
        initUI();
        loadPreferences();
    }

    /**
     * Initializes Swing components and window layout.
     */
    private void initUI() {
        setTitle("Pepperi Transaction/Activity JSON → JS files v1.2");
        setSize(640, 480);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        // ---- Input file row ----
        g.gridx = 0;
        g.gridy = 0;
        top.add(new JLabel("JSON File:"), g);

        g.gridx = 1;
        g.weightx = 1;
        top.add(inputField, g);

        g.gridx = 2;
        g.weightx = 0;
        JButton browseInput = new JButton("Browse...");
        browseInput.addActionListener(_ -> chooseInput());
        top.add(browseInput, g);

        // ---- Output directory row ----
        g.gridx = 0;
        g.gridy = 1;
        top.add(new JLabel("Output folder:"), g);

        g.gridx = 1;
        top.add(outputField, g);

        g.gridx = 2;
        JButton browseOutput = new JButton("Choose...");
        browseOutput.addActionListener(_ -> chooseOutput());
        top.add(browseOutput, g);

        // ---- Options ----
        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 3;
        g.anchor = GridBagConstraints.WEST;
        addCommentsCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        top.add(addCommentsCheckBox, g);

        // ---- Start button ----
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        startBtn.setPreferredSize(new Dimension(120, 52));
        startBtn.addActionListener(_ -> startExtraction());

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        center.add(startBtn);

        JPanel north = new JPanel(new BorderLayout());
        north.add(top, BorderLayout.CENTER);
        north.add(center, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(progressPanel, BorderLayout.CENTER);

        progressPanel.log("Select JSON file and output folder.");
    }

    /**
     * Opens file chooser for input JSON selection
     * and auto-suggests output directory.
     */
    private void chooseInput() {
        JFileChooser fc = new JFileChooser();

        String lastOut = UserPreferences.getLastOutput();
        if (!lastOut.isEmpty()) {
            File dir = new File(lastOut).getParentFile();
            if (dir != null && dir.isDirectory()) {
                fc.setCurrentDirectory(dir);
            }
        }

        fc.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("JSON (*.json)", "json")
        );

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            inputField.setText(f.getAbsolutePath());

            // Suggest output folder: same dir, filename + "_extracted"
            String name = f.getName().replaceFirst("\\.json$", "");
            String suggested = f.getParent() + File.separator + name + "_extracted";
            outputField.setText(suggested);

            progressPanel.log("Input: " + f.getName());
            progressPanel.log("Suggested output: " + new File(suggested).getName());

            savePreferences();
        }
    }

    /**
     * Opens directory chooser for output folder selection.
     */
    private void chooseOutput() {
        JFileChooser fc = new JFileChooser();

        String lastOut = UserPreferences.getLastOutput();

        if (!lastOut.isEmpty()) {
            fc.setCurrentDirectory(new File(lastOut));
        }

        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputField.setText(fc.getSelectedFile().getAbsolutePath());
            savePreferences();
        }
    }

    /**
     * Runs extraction in a background thread to keep UI responsive.
     */
    private void startExtraction() {
        String in = inputField.getText().trim();
        String out = outputField.getText().trim();

        if (in.isEmpty() || out.isEmpty()) {
            progressPanel.log("ERROR: Select both files!");
            return;
        }

        File inFile = new File(in);
        if (!inFile.exists()) {
            progressPanel.log("ERROR: Input file not found!");
            return;
        }

        startBtn.setEnabled(false);
        progressPanel.reset();

        // Run extraction in background to keep UI responsive
        new Thread(() -> {
            try {
                String json = FileUtils.readFile(in);
                JsonFormulaParser parser = new JsonFormulaParser();
                var fields = parser.parse(json);

                File rootDir = new File(out);
                FileUtils.ensureDir(rootDir);

                parser.writeJsFiles(fields, rootDir, addCommentsCheckBox.isSelected(), new ProgressCallback() {
                    @Override
                    public void update(String msg, int percent) {
                        progressPanel.setProgress(percent, msg);
                    }

                    @Override
                    public void log(String msg) {
                        progressPanel.log(msg);
                    }
                });

                // Success message on EDT
                SwingUtilities.invokeLater(() -> {
                    progressPanel.log("");
                    progressPanel.log("SUCCESS! Generated " + fields.size() + " files");
                    progressPanel.log("Folder: " + rootDir.getAbsolutePath());
                    startBtn.setEnabled(true);
                });

                savePreferences();

            } catch (Exception ex) {
                // Error handling on EDT
                SwingUtilities.invokeLater(() -> {
                    progressPanel.log("FATAL: " + ex.getMessage());
                    startBtn.setEnabled(true);
                });
            }
        }).start();
    }

    /**
     * Loads persisted user preferences into UI fields
     */
    private void loadPreferences() {
        inputField.setText(UserPreferences.getLastInput());
        outputField.setText(UserPreferences.getLastOutput());
        addCommentsCheckBox.setSelected(UserPreferences.isAddCommentsEnabled());
    }

    /**
     * Saves current UI state into user preferences
     */
    private void savePreferences() {
        UserPreferences.setLastInput(inputField.getText());
        UserPreferences.setLastOutput(outputField.getText());
        UserPreferences.setAddCommentsEnabled(addCommentsCheckBox.isSelected());
    }
}
```