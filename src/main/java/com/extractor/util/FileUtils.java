package com.extractor.util;

import java.io.*;
import java.nio.file.*;

/**
 * Utility class for common file operations.
 */
public class FileUtils {

    /**
     * Reads entire file content into a string.
     *
     * @param path File path
     * @return File content as string
     * @throws IOException if reading fails
     */
    public static String readFile(String path) throws IOException {
        return Files.readString(Paths.get(path));
    }

    /**
     * Ensures directory exists (creates if missing).
     *
     * @param dir Directory to ensure
     */
    public static void ensureDir(File dir) {
        if (!dir.exists()) //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
    }
}