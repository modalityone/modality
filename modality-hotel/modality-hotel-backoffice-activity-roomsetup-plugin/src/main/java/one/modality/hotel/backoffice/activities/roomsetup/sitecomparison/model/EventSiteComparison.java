package one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.model;

import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Resource;
import one.modality.base.shared.entities.Site;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data transfer object holding comparison results for a single event site.
 * Contains the site, its associated future events, and the comparison results.
 */
public final class EventSiteComparison {

    private final Site site;
    private final String siteName;
    private final List<Event> futureEvents;
    private final List<String> onlyInGlobal;
    private final List<String> onlyInEventSite;
    private final List<Resource> globalOnlyResources;
    private final List<Resource> eventOnlyResources;
    private final List<ResourceLink> confirmedLinks;
    private final List<ResourceLink> suggestedLinks;
    private final List<MatchedResourcePair> matchedPairs;

    public EventSiteComparison(
            Site site,
            String siteName,
            List<Event> futureEvents,
            List<String> onlyInGlobal,
            List<MatchedResourcePair> matchedPairs,
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
        this.matchedPairs = matchedPairs;
        this.onlyInEventSite = onlyInEventSite;
        this.globalOnlyResources = globalOnlyResources;
        this.eventOnlyResources = eventOnlyResources;
        this.confirmedLinks = confirmedLinks;
        this.suggestedLinks = suggestedLinks;
    }

    /**
     * Backwards-compatible constructor without Resource objects and links.
     * @deprecated Use constructor with MatchedResourcePair list instead
     */
    @Deprecated
    public EventSiteComparison(
            Site site,
            String siteName,
            List<Event> futureEvents,
            List<String> onlyInGlobal,
            List<String> inBoth,
            List<String> onlyInEventSite
    ) {
        this(site, siteName, futureEvents, onlyInGlobal,
                inBoth.stream().map(name -> new MatchedResourcePair(null, null, name)).collect(Collectors.toList()),
                onlyInEventSite, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
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

    /**
     * Returns the names of matched resources (derived from matchedPairs for backwards compatibility).
     */
    public List<String> inBoth() {
        return matchedPairs.stream()
                .map(MatchedResourcePair::name)
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of matched resource pairs (event + global resources with same name).
     */
    public List<MatchedResourcePair> matchedPairs() {
        return matchedPairs;
    }

    /**
     * Returns count of matched pairs not yet linked.
     */
    public int unlinkedMatchedCount() {
        return (int) matchedPairs.stream()
                .filter(pair -> !pair.isLinked())
                .count();
    }

    /**
     * Returns count of matched pairs already linked.
     */
    public int linkedMatchedCount() {
        return (int) matchedPairs.stream()
                .filter(MatchedResourcePair::isLinked)
                .count();
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
        return matchedPairs.size() + onlyInEventSite.size();
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
