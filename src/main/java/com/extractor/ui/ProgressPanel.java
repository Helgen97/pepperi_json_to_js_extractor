package com.extractor.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Custom panel displaying progress bar and scrollable log area.
 * Thread-safe updates via {@link SwingUtilities#invokeLater}.
 */
public class ProgressPanel extends JPanel {
    private final JProgressBar progressBar;
    private final JTextArea logArea;

    /** Constructs the panel with progress bar and log area */
    public ProgressPanel() {
        setLayout(new BorderLayout(0, 8));
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(0, 255, 120));
        logArea.setText("Ready.\n");

        add(progressBar, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
    }

    /**
     * Appends a message to the log area.
     * Thread-safe using SwingUtilities.
     *
     * @param msg Message to log
     */
    public void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Updates progress bar value and status string.
     *
     * @param value  Progress percentage (0–100)
     * @param status Status message
     */
    public void setProgress(int value, String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            progressBar.setString(value + "% – " + status);
        });
    }

    /** Resets progress and clears log */
    public void reset() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(0);
            progressBar.setString("0%");
            logArea.setText("");
        });
    }
}