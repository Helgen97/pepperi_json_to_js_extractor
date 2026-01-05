package com.extractor.config;

import java.util.prefs.Preferences;

/**
 * Centralized wrapper over {@link Preferences} API.
 * <p>
 * This class is responsible for storing and retrieving
 * user-specific application settings such as:
 * <ul>
 *     <li>Last selected input JSON file</li>
 *     <li>Last selected output directory</li>
 *     <li>User preference for adding comments to generated JS files</li>
 * </ul>
 */
public final class UserPreferences {

    /**
     * Preferences node bound to this application.
     */
    private static final Preferences PREFS =
            Preferences.userNodeForPackage(UserPreferences.class);

    /**
     * Preference keys
     */
    private static final String KEY_LAST_INPUT = "lastInputFile";
    private static final String KEY_LAST_OUTPUT = "lastOutputDir";
    private static final String KEY_ADD_COMMENTS = "addComments";
    private static final String KEY_OPEN_FOLDER = "openFolder";

    /**
     * Prevent instantiation.
     */
    private UserPreferences() {
    }

    /**
     * @return absolute path of the last selected input JSON file
     */
    public static String getLastInput() {
        return PREFS.get(KEY_LAST_INPUT, "");
    }

    /**
     * Stores the last selected input JSON file path.
     *
     * @param value absolute file path
     */
    public static void setLastInput(String value) {
        PREFS.put(KEY_LAST_INPUT, value);
    }

    /**
     * @return absolute path of the last selected output directory
     */
    public static String getLastOutput() {
        return PREFS.get(KEY_LAST_OUTPUT, "");
    }

    /**
     * Stores the last selected output directory path.
     *
     * @param value absolute directory path
     */
    public static void setLastOutput(String value) {
        PREFS.put(KEY_LAST_OUTPUT, value);
    }

    /**
     * @return {@code true} if JS file header comments should be generated
     */
    public static boolean isAddCommentsEnabled() {
        return PREFS.getBoolean(KEY_ADD_COMMENTS, true);
    }

    /**
     * Enables or disables generation of JS file header comments.
     *
     * @param value flag value
     */
    public static void setAddCommentsEnabled(boolean value) {
        PREFS.putBoolean(KEY_ADD_COMMENTS, value);
    }

    /**
     * @return {@code true} if output folder should be opened after parsing.
     */
    public static boolean isOpenFolderEnabled() {
        return PREFS.getBoolean(KEY_OPEN_FOLDER, true);
    }

    /**
     * Enables or disables opening output folder after parsing.
     *
     * @param value flag value
     */
    public static void setOpenFolderEnabled(boolean value) {
        PREFS.putBoolean(KEY_OPEN_FOLDER, value);
    }
}
