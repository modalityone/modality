package one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.model;

/**
 * Data transfer object for a single resource name comparison result.
 * Holds the resource name and its comparison status between global site and event site.
 */
public final class ResourceNameComparison {

    private final String resourceName;
    private final ComparisonStatus status;

    public ResourceNameComparison(String resourceName, ComparisonStatus status) {
        this.resourceName = resourceName;
        this.status = status;
    }

    public String resourceName() {
        return resourceName;
    }

    public ComparisonStatus status() {
        return status;
    }
}
