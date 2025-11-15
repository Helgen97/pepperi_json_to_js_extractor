package com.extractor.ui;

import com.extractor.parser.JsonFormulaParser;
import com.extractor.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * Main GUI window for the JSON to JS extractor tool.
 * Allows user to select input JSON and output folder, then runs extraction.
 */
public class MainFrame extends JFrame {
    private final JTextField inputField = new JTextField();
    private final JTextField outputField = new JTextField();
    private final JButton startBtn = new JButton("START");
    private final ProgressPanel progressPanel = new ProgressPanel();

    /** Initializes the UI components and layout */
    public MainFrame() {
        initUI();
    }

    /** Sets up the window layout, fields, buttons, and event handlers */
    private void initUI() {
        setTitle("Pepperi Transaction JSON → JS files v1.0");
        setSize(640, 320);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        // Input file row
        g.gridx = 0; g.gridy = 0; top.add(new JLabel("JSON File:"), g);
        g.gridx = 1; g.weightx = 1; top.add(inputField, g);
        g.gridx = 2; g.weightx = 0;
        JButton b1 = new JButton("Browse...");
        b1.addActionListener(_ -> chooseInput());
        top.add(b1, g);

        // Output folder row
        g.gridx = 0; g.gridy = 1; top.add(new JLabel("Output folder:"), g);
        g.gridx = 1; outputField.setEditable(false); top.add(outputField, g);
        g.gridx = 2;
        JButton b2 = new JButton("Choose...");
        b2.addActionListener(_ -> chooseOutput());
        top.add(b2, g);

        // Start button
        startBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        startBtn.setForeground(Color.BLACK);
        startBtn.setPreferredSize(new Dimension(120, 52));
        startBtn.addActionListener(_ -> startExtraction());

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        center.add(startBtn);

        // Layout assembly
        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.SOUTH);

        progressPanel.log("Select JSON file and output folder.");
    }

    /** Opens file chooser for input JSON file and suggests output folder */
    private void chooseInput() {
        JFileChooser fc = new JFileChooser(".");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON (*.json)", "json"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            inputField.setText(f.getAbsolutePath());

            // Suggest output folder: same dir, filename + "_extracted"
            String name = f.getName().replaceFirst("\\.json$", "");
            String suggested = f.getParent() + File.separator + name + "_extracted";
            outputField.setText(suggested);
            progressPanel.log("Input: " + f.getName());
            progressPanel.log("Suggested output: " + new File(suggested).getName());
        }
    }

    /** Opens directory chooser for output folder */
    private void chooseOutput() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputField.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    /** Starts the extraction process in a background thread */
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

                parser.writeJsFiles(fields, rootDir, new JsonFormulaParser.ProgressCallback() {
                    @Override public void update(String msg, int percent) {
                        progressPanel.setProgress(percent, msg);
                    }
                    @Override public void log(String msg) {
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

            } catch (Exception ex) {
                // Error handling on EDT
                SwingUtilities.invokeLater(() -> {
                    progressPanel.log("FATAL: " + ex.getMessage());
                    startBtn.setEnabled(true);
                });
            }
        }).start();
    }
}