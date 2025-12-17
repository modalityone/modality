package one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.model;

import one.modality.base.shared.entities.Resource;

/**
 * Represents a matched pair of resources with the same name in both
 * the event site and global site.
 *
 * @author Claude Code
 */
public final class MatchedResourcePair {

    private final Resource eventResource;
    private final Resource globalResource;
    private final String name;

    public MatchedResourcePair(Resource eventResource, Resource globalResource, String name) {
        this.eventResource = eventResource;
        this.globalResource = globalResource;
        this.name = name;
    }

    public Resource eventResource() {
        return eventResource;
    }

    public Resource globalResource() {
        return globalResource;
    }

    public String name() {
        return name;
    }

    /**
     * Returns true if the event resource is already linked to its matched global resource.
     */
    public boolean isLinked() {
        if (eventResource == null) return false;
        var linkedId = eventResource.getKbs2ToKbs3GlobalResourceId();
        if (linkedId == null || globalResource == null) return false;
        return linkedId.getPrimaryKey().equals(globalResource.getId().getPrimaryKey());
    }

    /**
     * Returns true if the event resource is linked to a DIFFERENT global resource.
     * This indicates a mismatch that needs attention.
     */
    public boolean isLinkedToDifferent() {
        if (eventResource == null) return false;
        var linkedId = eventResource.getKbs2ToKbs3GlobalResourceId();
        if (linkedId == null) return false;
        if (globalResource == null) return true;
        return !linkedId.getPrimaryKey().equals(globalResource.getId().getPrimaryKey());
    }
}
