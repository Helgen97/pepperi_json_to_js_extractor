package com.extractor.util;

/**
 * Platform detection utilities for UI layer.
 * <p>
 * Centralizes operating system checks used to slightly adapt
 * layout, spacing, fonts and component sizes for better
 * native look & feel on different platforms.
 * <p>
 * Currently supports:
 * <ul>
 *     <li>macOS</li>
 *     <li>Windows / Linux (fallback)</li>
 * </ul>
 */
public final class UiPlatform {

    /**
     * Indicates whether the application is running on macOS.
     * <p>
     * Used to adjust UI spacing, alignment and component sizing
     * to better match macOS design conventions.
     */
    public static final boolean IS_MAC =
            System.getProperty("os.name").toLowerCase().contains("mac");

    /**
     * Utility class — no instances allowed.
     */
    private UiPlatform() {
    }
}
