package one.modality.hotel.backoffice.activities.roomsetup.util;

/**
 * Enumeration of room grouping options for views.
 * Replaces magic strings like "building", "type", "zone", "pool".
 *
 * @author Claude Code
 */
public enum GroupByOption {
    /**
     * Group rooms by their room type (Item).
     */
    TYPE(),

    /**
     * Group rooms by their building.
     */
    BUILDING(),

    /**
     * Group rooms by their zone within a building.
     */
    ZONE(),

    /**
     * Group rooms by their pool allocation.
     */
    POOL();

    GroupByOption() {
    }

}
