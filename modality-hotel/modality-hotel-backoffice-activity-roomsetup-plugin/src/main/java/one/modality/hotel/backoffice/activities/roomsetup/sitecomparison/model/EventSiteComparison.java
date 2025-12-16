package one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.model;

import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Resource;
import one.modality.base.shared.entities.Site;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object holding comparison results for a single event site.
 * Contains the site, its associated future events, and the comparison results.
 */
public final class EventSiteComparison {

    private final Site site;
    private final String siteName;
    private final List<Event> futureEvents;
    private final List<String> onlyInGlobal;
    private final List<String> inBoth;
    private final List<String> onlyInEventSite;
    private final List<Resource> globalOnlyResources;
    private final List<Resource> eventOnlyResources;
    private final List<ResourceLink> confirmedLinks;
    private final List<ResourceLink> suggestedLinks;

    public EventSiteComparison(
            Site site,
            String siteName,
            List<Event> futureEvents,
            List<String> onlyInGlobal,
            List<String> inBoth,
            List<String> onlyInEventSite,
            List<Resource> globalOnlyResources,
            List<Resource> eventOnlyResources,
            List<ResourceLink> confirmedLinks,
            List<ResourceLink> suggestedLinks
    ) {
        this.site = site;
        this.siteName = siteName;
        this.futureEvents = futureEvents;
        this.onlyInGlobal = onlyInGlobal;
        this.inBoth = inBoth;
        this.onlyInEventSite = onlyInEventSite;
        this.globalOnlyResources = globalOnlyResources;
        this.eventOnlyResources = eventOnlyResources;
        this.confirmedLinks = confirmedLinks;
        this.suggestedLinks = suggestedLinks;
    }

    /**
     * Backwards-compatible constructor without Resource objects and links.
     */
    public EventSiteComparison(
            Site site,
            String siteName,
            List<Event> futureEvents,
            List<String> onlyInGlobal,
            List<String> inBoth,
            List<String> onlyInEventSite
    ) {
        this(site, siteName, futureEvents, onlyInGlobal, inBoth, onlyInEventSite,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public Site site() {
        return site;
    }

    public String siteName() {
        return siteName;
    }

    public List<Event> futureEvents() {
        return futureEvents;
    }

    public List<String> onlyInGlobal() {
        return onlyInGlobal;
    }

    public List<String> inBoth() {
        return inBoth;
    }

    public List<String> onlyInEventSite() {
        return onlyInEventSite;
    }

    public List<Resource> globalOnlyResources() {
        return globalOnlyResources;
    }

    public List<Resource> eventOnlyResources() {
        return eventOnlyResources;
    }

    public List<ResourceLink> confirmedLinks() {
        return confirmedLinks;
    }

    public List<ResourceLink> suggestedLinks() {
        return suggestedLinks;
    }

    /**
     * Returns true if there are any differences between the sites.
     */
    public boolean hasDifferences() {
        return !onlyInGlobal.isEmpty() || !onlyInEventSite.isEmpty();
    }

    /**
     * Returns the total number of resources in the event site.
     */
    public int eventSiteResourceCount() {
        return inBoth.size() + onlyInEventSite.size();
    }

    /**
     * Returns all links (confirmed + suggested) for this comparison.
     */
    public List<ResourceLink> allLinks() {
        List<ResourceLink> all = new ArrayList<>(confirmedLinks);
        all.addAll(suggestedLinks);
        return all;
    }

    /**
     * Returns the count of unlinked event resources.
     */
    public int unlinkedEventResourceCount() {
        return eventOnlyResources.size() - confirmedLinks.size();
    }
}
