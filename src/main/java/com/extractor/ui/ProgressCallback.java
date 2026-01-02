package com.extractor.ui;

/**
 * Callback interface for UI progress updates during file generation.
 */
public interface ProgressCallback {
    /**
     * Updates progress bar and status message.
     *
     * @param message Status text to display
     * @param percent Progress percentage (0–100)
     */
    void update(String message, int percent);

    /**
     * Logs a message to the console/output area.
     *
     * @param message Message to log
     */
    void log(String message);
}
