package one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.model;

import one.modality.base.shared.entities.Resource;

/**
 * Represents a link between an event site resource and a global site resource.
 */
public final class ResourceLink {

    private final Resource eventResource;
    private final Resource globalResource;
    private final boolean isConfirmed;
    private final double matchScore;

    public ResourceLink(
            Resource eventResource,
            Resource globalResource,
            boolean isConfirmed,
            double matchScore
    ) {
        this.eventResource = eventResource;
        this.globalResource = globalResource;
        this.isConfirmed = isConfirmed;
        this.matchScore = matchScore;
    }

    /**
     * Creates a confirmed link (user-created or persisted).
     */
    public static ResourceLink confirmed(Resource eventResource, Resource globalResource) {
        return new ResourceLink(eventResource, globalResource, true, 1.0);
    }

    /**
     * Creates a suggested link (auto-matched).
     */
    public static ResourceLink suggested(Resource eventResource, Resource globalResource, double score) {
        return new ResourceLink(eventResource, globalResource, false, score);
    }

    public Resource eventResource() {
        return eventResource;
    }

    public Resource globalResource() {
        return globalResource;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public double matchScore() {
        return matchScore;
    }

    /**
     * Returns the match score as a percentage (0-100).
     */
    public int scorePercent() {
        return (int) Math.round(matchScore * 100);
    }

    /**
     * Returns true if this link has a global resource assigned.
     */
    public boolean isLinked() {
        return globalResource != null;
    }

    /**
     * Returns the event resource name or empty string if null.
     */
    public String eventResourceName() {
        return eventResource != null && eventResource.getName() != null
                ? eventResource.getName()
                : "";
    }

    /**
     * Returns the global resource name or empty string if null/unlinked.
     */
    public String globalResourceName() {
        return globalResource != null && globalResource.getName() != null
                ? globalResource.getName()
                : "";
    }
}
