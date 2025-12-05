package one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.model;

import one.modality.base.shared.entities.Site;

import java.util.Collections;
import java.util.List;

/**
 * Data transfer object holding all comparison data for the Site Comparison view.
 * Contains the global site info and comparison results for all event sites.
 */
public final class SiteComparisonData {

    private final Site globalSite;
    private final String globalSiteName;
    private final int globalSiteResourceCount;
    private final List<EventSiteComparison> eventSiteComparisons;
    private final boolean hasGlobalSite;
    private final boolean globalSiteHasResources;
    private final String warningMessage;

    public SiteComparisonData(
            Site globalSite,
            String globalSiteName,
            int globalSiteResourceCount,
            List<EventSiteComparison> eventSiteComparisons,
            boolean hasGlobalSite,
            boolean globalSiteHasResources,
            String warningMessage
    ) {
        this.globalSite = globalSite;
        this.globalSiteName = globalSiteName;
        this.globalSiteResourceCount = globalSiteResourceCount;
        this.eventSiteComparisons = eventSiteComparisons;
        this.hasGlobalSite = hasGlobalSite;
        this.globalSiteHasResources = globalSiteHasResources;
        this.warningMessage = warningMessage;
    }

    /**
     * Creates an empty comparison data with a warning message.
     */
    public static SiteComparisonData withWarning(String warningMessage) {
        return new SiteComparisonData(null, null, 0, Collections.emptyList(), false, false, warningMessage);
    }

    /**
     * Creates comparison data for when global site has no resources.
     */
    public static SiteComparisonData withNoResources(Site globalSite, String siteName, String warningMessage) {
        return new SiteComparisonData(globalSite, siteName, 0, Collections.emptyList(), true, false, warningMessage);
    }

    public Site globalSite() {
        return globalSite;
    }

    public String globalSiteName() {
        return globalSiteName;
    }

    public int globalSiteResourceCount() {
        return globalSiteResourceCount;
    }

    public List<EventSiteComparison> eventSiteComparisons() {
        return eventSiteComparisons;
    }

    public boolean hasGlobalSite() {
        return hasGlobalSite;
    }

    public boolean globalSiteHasResources() {
        return globalSiteHasResources;
    }

    public String warningMessage() {
        return warningMessage;
    }

    /**
     * Returns true if there are any differences across all event sites.
     */
    public boolean hasAnyDifferences() {
        return eventSiteComparisons.stream().anyMatch(EventSiteComparison::hasDifferences);
    }

    /**
     * Returns the total number of event sites found.
     */
    public int eventSiteCount() {
        return eventSiteComparisons.size();
    }
}
