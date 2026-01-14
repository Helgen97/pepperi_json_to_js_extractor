package com.extractor.ui;

import com.extractor.config.UserPreferences;
import com.extractor.parser.JsonFormulaParser;
import com.extractor.util.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static com.extractor.util.UiPlatform.IS_MAC;

/**
 * Main application window.
 * <p>
 * Responsible for orchestrating the complete user flow:
 * <ul>
 *     <li>Selecting input JSON file</li>
 *     <li>Selecting output directory</li>
 *     <li>Configuring generation options</li>
 *     <li>Running extraction in a background thread</li>
 *     <li>Displaying progress and logs</li>
 * </ul>
 * <p>
 * UI layout and spacing are slightly adapted for macOS
 * to provide a more native look & feel.
 */
public class MainFrame extends JFrame {

    /**
     * Text field holding input JSON file path.
     */
    private final JTextField inputField = new JTextField();

    /**
     * Text field holding output directory path.
     */
    private final JTextField outputField = new JTextField();

    /**
     * Primary action button used to start extraction.
     */
    private final JButton startBtn = new JButton("START");

    /**
     * Panel displaying extraction progress and log output.
     */
    private final ProgressPanel progressPanel = new ProgressPanel();

    /**
     * Toggle for JS header comments
     */
    private final JCheckBox addCommentsCheckBox =
            new JCheckBox("Add comments to generated JS files", true);

    /**
     * Toggle for auto open folder after success parsing
     */
    private final JCheckBox openFolderCheckBox =
            new JCheckBox("Auto-open results folder", true);

    /**
     * Creates and initializes the main window.
     */
    public MainFrame() {
        initUI();
        loadPreferences();
    }

    /**
     * Initializes and lays out all Swing components.
     * <p>
     * Applies platform-specific spacing and sizing
     * to improve native look on macOS.
     */
    private void initUI() {
        setTitle("Pepperi Transaction/Activity JSON → JS files v1.2.1");
        setSize(640, 480);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            Image image = ImageIO.read(
                    Objects.requireNonNull(
                            getClass().getResourceAsStream("/app.png")
                    )
            );
            setIconImage(image);
        } catch (IOException e) {
            progressPanel.log("Frame icon error: %s".formatted(e.getMessage()));
        }

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = IS_MAC
                ? new Insets(6, 8, 6, 8)
                : new Insets(8, 8, 8, 8);
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
        JPanel optionsPanel = new JPanel(
                new FlowLayout(IS_MAC ? FlowLayout.LEFT : FlowLayout.CENTER, 20, 0)
        );
        optionsPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
        addCommentsCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        optionsPanel.add(addCommentsCheckBox);

        if (Desktop.isDesktopSupported()) {
            openFolderCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            optionsPanel.add(openFolderCheckBox);
        }

        g.gridx = 0;
        g.gridy = 2;
        g.gridwidth = 3;
        g.anchor = GridBagConstraints.CENTER;
        top.add(optionsPanel, g);

        // ---- Start button ----
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        startBtn.setPreferredSize(
                IS_MAC
                        ? new Dimension(110, 44)
                        : new Dimension(120, 52)
        );
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

        String lastIn = UserPreferences.getLastInput();
        if (!lastIn.isEmpty()) {
            File dir = new File(lastIn).getParentFile();
            if (dir != null && dir.isDirectory()) {
                fc.setCurrentDirectory(dir);
            }
        }

        fc.setFileFilter(new FileNameExtensionFilter("JSON (*.json)", "json"));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            inputField.setText(f.getAbsolutePath());

            progressPanel.log("Input: " + f.getName());

            if (outputField.getText().isEmpty()) {
                // Suggest output folder: same dir, filename + "_extracted"
                String name = f.getName().replaceFirst("\\.json$", "");
                String suggested = f.getParent() + File.separator + name + "_extracted";
                outputField.setText(suggested);

                progressPanel.log("Suggested output: " + new File(suggested).getName());
            }

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
     * Starts extraction process in a background thread.
     * <p>
     * UI updates are safely dispatched to the EDT.
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

                    if (Desktop.isDesktopSupported() && openFolderCheckBox.isSelected()) {
                        try {
                            Desktop.getDesktop().open(new File(rootDir.getAbsolutePath()));
                        } catch (IOException ex) {
                            progressPanel.log("FATAL: " + ex.getMessage());
                        }
                    }

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
     * Loads persisted user preferences
     * and applies them to UI components.
     */
    private void loadPreferences() {
        inputField.setText(UserPreferences.getLastInput());
        outputField.setText(UserPreferences.getLastOutput());
        addCommentsCheckBox.setSelected(UserPreferences.isAddCommentsEnabled());
        openFolderCheckBox.setSelected(UserPreferences.isOpenFolderEnabled());
    }

    /**
     * Persists current UI state into user preferences.
     */
    private void savePreferences() {
        UserPreferences.setLastInput(inputField.getText());
        UserPreferences.setLastOutput(outputField.getText());
        UserPreferences.setAddCommentsEnabled(addCommentsCheckBox.isSelected());
        UserPreferences.setOpenFolderEnabled(openFolderCheckBox.isSelected());
    }
}