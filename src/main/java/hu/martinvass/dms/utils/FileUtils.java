package hu.martinvass.dms.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtils {

    /**
     * Convert bytes to human-readable format
     *
     * @param bytes Number of bytes
     * @return Human-readable string (e.g., "1.5 MB", "3.2 GB")
     */
    public String humanReadableSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";

        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}