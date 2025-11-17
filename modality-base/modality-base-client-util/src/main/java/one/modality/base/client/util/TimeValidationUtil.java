package one.modality.base.client.util;

import java.time.LocalTime;

/**
 * Utility class for validating time-related input.
 *
 * @author David Hello
 */
public final class TimeValidationUtil {

    // Private constructor to prevent instantiation
    private TimeValidationUtil() {
    }

    /**
     * Validates if the given text is a valid LocalTime format (HH:mm or HH:mm:ss).
     *
     * @param text The text to validate
     * @return true if the text can be parsed as a LocalTime, false otherwise
     */
    public static boolean isLocalTimeTextValid(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        try {
            LocalTime.parse(text.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
