package one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.model;

/**
 * Enum representing the status of a resource name comparison.
 */
public enum ComparisonStatus {
    /** Resource exists in both global site and event site */
    IN_BOTH,
    /** Resource exists only in global site */
    ONLY_IN_GLOBAL,
    /** Resource exists only in event site */
    ONLY_IN_EVENT
}
