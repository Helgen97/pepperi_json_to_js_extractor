package com.extractor;

import com.extractor.ui.MainFrame;

import javax.swing.*;

/**
 * Entry point of the application.
 * Sets system look and feel and launches the main UI.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Use native OS look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to default if failed
            }
            new MainFrame().setVisible(true);
        });
    }
}