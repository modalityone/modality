package one.modality.booking.frontoffice.bookingpage.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to parse SOLDOUT server errors.
 *
 * <p>When accommodation becomes unavailable during booking submission, the server
 * returns an error like:
 * {@code ERROR: Server error: Server error: ERROR: SOLDOUT site_id=2502, item_id=527 (no resource found) (P0001)}
 *
 * <p>This class provides methods to detect and parse such errors.
 *
 * @author Claude Code
 */
public final class SoldOutErrorParser {

    // Pattern to match SOLDOUT error with site_id and item_id
    private static final Pattern SOLDOUT_PATTERN = Pattern.compile(
        "SOLDOUT\\s+site_id=(\\d+),\\s*item_id=(\\d+)(?:\\s+\\(([^)]+)\\))?"
    );

    // Pattern to match just SOLDOUT keyword for quick detection
    private static final String SOLDOUT_KEYWORD = "SOLDOUT";

    /**
     * Information extracted from a SOLDOUT error.
     */
    public static class SoldOutInfo {
        private final int siteId;
        private final int itemId;
        private final String reason;

        public SoldOutInfo(int siteId, int itemId, String reason) {
            this.siteId = siteId;
            this.itemId = itemId;
            this.reason = reason;
        }

        /**
         * Returns the site ID where the sold-out item was requested.
         */
        public int getSiteId() {
            return siteId;
        }

        /**
         * Returns the item ID that is sold out.
         * This corresponds to the accommodation Item entity.
         */
        public int getItemId() {
            return itemId;
        }

        /**
         * Returns the reason for being sold out (e.g., "no resource found").
         * May be null if reason was not provided in error message.
         */
        public String getReason() {
            return reason;
        }

        @Override
        public String toString() {
            return "SoldOutInfo{siteId=" + siteId + ", itemId=" + itemId +
                   (reason != null ? ", reason='" + reason + "'" : "") + "}";
        }
    }

    /**
     * Checks if the given error is a SOLDOUT error.
     *
     * @param error The throwable to check
     * @return true if the error message contains SOLDOUT
     */
    public static boolean isSoldOutError(Throwable error) {
        if (error == null) {
            return false;
        }
        String message = error.getMessage();
        return message != null && message.contains(SOLDOUT_KEYWORD);
    }

    /**
     * Checks if the given error message is a SOLDOUT error.
     *
     * @param errorMessage The error message to check
     * @return true if the message contains SOLDOUT
     */
    public static boolean isSoldOutError(String errorMessage) {
        return errorMessage != null && errorMessage.contains(SOLDOUT_KEYWORD);
    }

    /**
     * Parses a SOLDOUT error message and extracts the site ID and item ID.
     *
     * @param error The throwable containing the error message
     * @return SoldOutInfo with parsed data, or null if parsing fails
     */
    public static SoldOutInfo parse(Throwable error) {
        if (error == null || error.getMessage() == null) {
            return null;
        }
        return parse(error.getMessage());
    }

    /**
     * Parses a SOLDOUT error message and extracts the site ID and item ID.
     *
     * <p>Example input:
     * {@code ERROR: Server error: Server error: ERROR: SOLDOUT site_id=2502, item_id=527 (no resource found) (P0001)}
     *
     * @param errorMessage The error message to parse
     * @return SoldOutInfo with parsed data, or null if parsing fails
     */
    public static SoldOutInfo parse(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }

        Matcher matcher = SOLDOUT_PATTERN.matcher(errorMessage);
        if (matcher.find()) {
            try {
                int siteId = Integer.parseInt(matcher.group(1));
                int itemId = Integer.parseInt(matcher.group(2));
                String reason = matcher.group(3); // May be null
                return new SoldOutInfo(siteId, itemId, reason);
            } catch (NumberFormatException e) {
                // Should not happen with \\d+ pattern, but be safe
                return null;
            }
        }
        return null;
    }

    // Private constructor to prevent instantiation
    private SoldOutErrorParser() {
    }
}
