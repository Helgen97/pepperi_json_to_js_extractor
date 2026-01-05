package com.extractor.ui;

import javax.swing.*;
import java.awt.*;

import static com.extractor.util.UiPlatform.IS_MAC;

/**
 * Progress and logging panel used during extraction process.
 * <p>
 * Displays:
 * <ul>
 *     <li>Progress bar with percentage and status text</li>
 *     <li>Scrollable log output area</li>
 * </ul>
 * <p>
 * UI appearance is slightly adapted per platform:
 * <ul>
 *     <li>macOS — native system font and colors</li>
 *     <li>Windows/Linux — developer-style dark console log</li>
 * </ul>
 * <p>
 * All UI updates are performed safely on the EDT
 * using {@link SwingUtilities#invokeLater(Runnable)}.
 */
public class ProgressPanel extends JPanel {

    /**
     * Progress bar displaying extraction progress (0–100%)
     */
    private final JProgressBar progressBar;

    /**
     * Scrollable text area used for log output
     */
    private final JTextArea logArea;

    /**
     * Creates progress panel with platform-aware styling.
     */
    public ProgressPanel() {
        setLayout(new BorderLayout(0, 8));

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        progressBar.setPreferredSize(
                new Dimension(10, IS_MAC ? 18 : 26)
        );

        // ---- Log area ----
        logArea = new JTextArea();
        logArea.setEditable(false);

        if (IS_MAC) {
            // Native macOS appearance
            logArea.setFont(UIManager.getFont("TextArea.font"));
            logArea.setBackground(UIManager.getColor("TextArea.background"));
            logArea.setForeground(UIManager.getColor("TextArea.foreground"));
        } else {
            // Developer-style console look
            logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
            logArea.setBackground(new Color(30, 30, 30));
            logArea.setForeground(new Color(0, 255, 120));
        }

        logArea.setText("Ready.\n");

        add(progressBar, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
    }

    /**
     * Appends a message to the log area.
     * <p>
     * Automatically scrolls to the latest entry.
     *
     * @param msg message to append
     */
    public void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(msg + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Updates progress bar value and status text.
     *
     * @param value  progress percentage (0–100)
     * @param status short status description
     */
    public void setProgress(int value, String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
            progressBar.setString(value + "% – " + status);
        });
    }

    /**
     * Resets progress bar and clears log output.
     */
    public void reset() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(0);
            progressBar.setString("0%");
            logArea.setText("");
        });
    }
}
