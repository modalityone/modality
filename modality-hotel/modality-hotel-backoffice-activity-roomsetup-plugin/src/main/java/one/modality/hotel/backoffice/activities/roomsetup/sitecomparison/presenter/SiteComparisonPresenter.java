package one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.presenter;

import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Resource;
import one.modality.base.shared.entities.Site;
import one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.model.EventSiteComparison;
import one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.model.ResourceLink;
import one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.model.SiteComparisonData;

import java.time.LocalDate;
import java.util.*;

/**
 * Presenter for the Site Comparison view.
 * Processes loaded data and computes comparison results between
 * the global site and event sites.
 *
 * OPTIMIZED VERSION:
 * - Uses HashMap for O(1) global resource lookup by ID
 * - Removed futureEvents parameter (event data comes from resources via site.event)
 * - Removed expensive O(N×G×L²) fuzzy matching (users can link manually)
 * - Uses HashSet for O(1) name lookups instead of stream operations
 *
 * @author Claude Code
 */
public final class SiteComparisonPresenter {

    private final ObjectProperty<SiteComparisonData> comparisonDataProperty = new SimpleObjectProperty<>();

    // UpdateStore per event site (keyed by site ID) for tracking changes
    private final Map<Object, UpdateStore> updateStoresBySite = new HashMap<>();

    // Track updated resources per site (for change detection)
    private final Map<Object, Set<Resource>> updatedResourcesBySite = new HashMap<>();

    // O(1) lookup map for global resources by ID
    private Map<Object, Resource> globalResourcesById = new HashMap<>();
    private List<Resource> cachedGlobalResources = new ArrayList<>();
    private Set<String> globalResourceNames = new HashSet<>();
    private Site cachedGlobalSite = null;

    // Keep reference to event resources for UpdateStore operations
    private List<Resource> cachedEventResources = new ArrayList<>();

    // Per-site comparison cache (for lazy loading)
    private final Map<Object, EventSiteComparison> siteComparisonCache = new HashMap<>();

    public ObjectProperty<SiteComparisonData> comparisonDataProperty() {
        return comparisonDataProperty;
    }

    public SiteComparisonData getComparisonData() {
        return comparisonDataProperty.get();
    }

    public List<Resource> getGlobalResources() {
        return cachedGlobalResources;
    }

    /**
     * Gets or creates an UpdateStore for the given site.
     */
    public UpdateStore getOrCreateUpdateStore(Site site) {
        if (site == null) return null;
        Object siteId = site.getId().getPrimaryKey();
        return updateStoresBySite.computeIfAbsent(siteId, id -> {
            // Find a resource from this site to get the underlying store
            for (Resource r : cachedEventResources) {
                if (r.getSite() != null && siteId.equals(r.getSite().getId().getPrimaryKey())) {
                    return UpdateStore.createAbove(r.getStore());
                }
            }
            return null;
        });
    }

    /**
     * Checks if the UpdateStore for the given site has unsaved changes.
     */
    public boolean hasChangesForSite(Site site) {
        if (site == null) return false;
        UpdateStore updateStore = updateStoresBySite.get(site.getId().getPrimaryKey());
        return updateStore != null && updateStore.hasChanges();
    }

    /**
     * Saves changes for a specific event site.
     * @param site The site to save changes for
     * @param onSuccess Callback on successful save
     */
    public void saveChangesForSite(Site site, Runnable onSuccess) {
        if (site == null) return;
        Object siteId = site.getId().getPrimaryKey();
        UpdateStore updateStore = updateStoresBySite.get(siteId);
        if (updateStore != null && updateStore.hasChanges()) {
            updateStore.submitChanges()
                    .onFailure(error -> Console.log("Failed to save resource links: " + error.getMessage()))
                    .onSuccess(result -> {
                        Console.log("Successfully saved resource links for site: " + site.getName());
                        updatedResourcesBySite.remove(siteId);
                        updateStoresBySite.remove(siteId);
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                    });
        }
    }

    /**
     * Create a link between an event resource and a global resource.
     */
    public void linkResources(Resource eventResource, Resource globalResource) {
        if (eventResource == null || globalResource == null) return;

        Site site = eventResource.getSite();
        if (site == null) return;

        UpdateStore updateStore = getOrCreateUpdateStore(site);
        if (updateStore == null) return;

        Resource updatedResource = updateStore.updateEntity(eventResource);
        updatedResource.setKbs2ToKbs3GlobalResource(globalResource);

        Object siteId = site.getId().getPrimaryKey();
        updatedResourcesBySite.computeIfAbsent(siteId, k -> new HashSet<>()).add(eventResource);
    }

    /**
     * Remove a link for an event resource.
     */
    public void unlinkResource(Resource eventResource) {
        if (eventResource == null) return;

        Site site = eventResource.getSite();
        if (site == null) return;

        UpdateStore updateStore = getOrCreateUpdateStore(site);
        if (updateStore == null) return;

        Resource updatedResource = updateStore.updateEntity(eventResource);
        updatedResource.setKbs2ToKbs3GlobalResource(null);

        Object siteId = site.getId().getPrimaryKey();
        updatedResourcesBySite.computeIfAbsent(siteId, k -> new HashSet<>()).add(eventResource);
    }

    /**
     * Check if an event resource is linked (either from DB or pending in UpdateStore).
     */
    public boolean isLinked(Resource eventResource) {
        if (eventResource == null) return false;

        Site site = eventResource.getSite();
        if (site != null) {
            Object siteId = site.getId().getPrimaryKey();
            Set<Resource> updatedResources = updatedResourcesBySite.get(siteId);
            if (updatedResources != null && updatedResources.contains(eventResource)) {
                UpdateStore updateStore = updateStoresBySite.get(siteId);
                if (updateStore != null) {
                    Resource updated = updateStore.updateEntity(eventResource);
                    return updated.getKbs2ToKbs3GlobalResourceId() != null;
                }
            }
        }

        return eventResource.getKbs2ToKbs3GlobalResourceId() != null;
    }

    /**
     * Get the linked global resource for an event resource.
     * OPTIMIZED: Uses HashMap for O(1) lookup instead of O(n) linear search.
     */
    public Resource getLinkedGlobalResource(Resource eventResource) {
        if (eventResource == null) return null;

        Site site = eventResource.getSite();
        Object globalResourceId = null;

        if (site != null) {
            Object siteId = site.getId().getPrimaryKey();
            Set<Resource> updatedResources = updatedResourcesBySite.get(siteId);
            if (updatedResources != null && updatedResources.contains(eventResource)) {
                UpdateStore updateStore = updateStoresBySite.get(siteId);
                if (updateStore != null) {
                    Resource updated = updateStore.updateEntity(eventResource);
                    globalResourceId = updated.getKbs2ToKbs3GlobalResourceId() != null
                            ? updated.getKbs2ToKbs3GlobalResourceId().getPrimaryKey()
                            : null;
                }
            }
        }

        if (globalResourceId == null && eventResource.getKbs2ToKbs3GlobalResourceId() != null) {
            globalResourceId = eventResource.getKbs2ToKbs3GlobalResourceId().getPrimaryKey();
        }

        if (globalResourceId == null) return null;

        // O(1) HashMap lookup instead of O(n) stream filter
        return globalResourcesById.get(globalResourceId);
    }

    /**
     * Initializes global resources for lazy loading.
     * Call this once when global resources are loaded.
     */
    public void initializeGlobalResources(Site globalSite, List<Resource> globalSiteResources) {
        this.cachedGlobalSite = globalSite;
        this.cachedGlobalResources = new ArrayList<>(globalSiteResources);

        // Build O(1) lookup map for global resources by ID
        this.globalResourcesById = new HashMap<>(globalSiteResources.size());
        for (Resource r : globalSiteResources) {
            globalResourcesById.put(r.getId().getPrimaryKey(), r);
        }

        // Build HashSet for O(1) name lookups
        this.globalResourceNames = new HashSet<>(globalSiteResources.size());
        for (Resource r : globalSiteResources) {
            if (r.getName() != null) {
                globalResourceNames.add(r.getName());
            }
        }

        // Clear comparison cache when global resources change
        siteComparisonCache.clear();
    }

    /**
     * Gets the cached global site.
     */
    public Site getCachedGlobalSite() {
        return cachedGlobalSite;
    }

    /**
     * Computes comparison for a single site (lazy loading).
     * Returns cached result if already computed.
     *
     * @param eventSite The event site to compare
     * @param siteResources The resources for this site
     * @return The comparison result for this site
     */
    public EventSiteComparison computeComparisonForSite(Site eventSite, List<Resource> siteResources) {
        if (eventSite == null) return null;

        Object siteId = eventSite.getId().getPrimaryKey();

        // Check cache first
        EventSiteComparison cached = siteComparisonCache.get(siteId);
        if (cached != null) {
            return cached;
        }

        // Add resources to cached event resources for UpdateStore operations
        cachedEventResources.addAll(siteResources);

        // Get the event from the site
        Event siteEvent = eventSite.getEvent();
        List<Event> siteEvents = siteEvent != null ? List.of(siteEvent) : Collections.emptyList();

        // Build HashSet for O(1) event resource name lookups
        Set<String> eventSiteResourceNames = new HashSet<>(siteResources.size());
        for (Resource r : siteResources) {
            if (r.getName() != null) {
                eventSiteResourceNames.add(r.getName());
            }
        }

        // Compare resources using O(1) set lookups
        List<String> onlyInGlobal = new ArrayList<>();
        List<String> inBoth = new ArrayList<>();
        List<String> onlyInEventSite = new ArrayList<>();
        List<Resource> globalOnlyResources = new ArrayList<>();
        List<Resource> eventOnlyResources = new ArrayList<>();

        // Check global resources against event site
        for (Resource globalResource : cachedGlobalResources) {
            String globalName = globalResource.getName();
            if (globalName == null) continue;

            if (eventSiteResourceNames.contains(globalName)) {
                inBoth.add(globalName);
            } else {
                onlyInGlobal.add(globalName);
                globalOnlyResources.add(globalResource);
            }
        }

        // Check event site resources not in global
        for (Resource eventResource : siteResources) {
            String eventName = eventResource.getName();
            if (eventName == null) continue;

            if (!globalResourceNames.contains(eventName)) {
                onlyInEventSite.add(eventName);
                eventOnlyResources.add(eventResource);
            }
        }

        // Sort lists
        Collections.sort(onlyInGlobal);
        Collections.sort(inBoth);
        Collections.sort(onlyInEventSite);
        globalOnlyResources.sort(Comparator.comparing(r -> r.getName() != null ? r.getName() : ""));
        eventOnlyResources.sort(Comparator.comparing(r -> r.getName() != null ? r.getName() : ""));

        // Check for existing links
        List<ResourceLink> confirmedLinks = new ArrayList<>();
        for (Resource eventResource : eventOnlyResources) {
            Resource linkedGlobal = getLinkedGlobalResource(eventResource);
            if (linkedGlobal != null) {
                confirmedLinks.add(ResourceLink.confirmed(eventResource, linkedGlobal));
            }
        }

        EventSiteComparison comparison = new EventSiteComparison(
                eventSite,
                eventSite.getName(),
                siteEvents,
                onlyInGlobal,
                inBoth,
                onlyInEventSite,
                globalOnlyResources,
                eventOnlyResources,
                confirmedLinks,
                Collections.emptyList() // No suggested links
        );

        // Cache the result
        siteComparisonCache.put(siteId, comparison);

        return comparison;
    }

    /**
     * Clears the comparison cache for a specific site (call after linking/unlinking).
     */
    public void clearComparisonCacheForSite(Object siteId) {
        siteComparisonCache.remove(siteId);
    }

    /**
     * Processes the loaded data and computes comparison results.
     *
     * OPTIMIZED: No longer needs futureEvents parameter - event data comes from
     * resources via site.event (already filtered to future events by the data loader).
     *
     * Time Complexity: O(G + E + S × (G + N + G log G + N log N))
     * where G = global resources, E = event resources, S = sites, N = resources per site
     *
     * @param globalSite          The global site of the organization (may be null)
     * @param globalSiteResources Resources in the global site
     * @param eventSiteResources  Resources in event-specific sites (pre-filtered to future events)
     */
    public void processData(
            Site globalSite,
            List<Resource> globalSiteResources,
            List<Resource> eventSiteResources
    ) {
        if (globalSite == null) {
            comparisonDataProperty.set(SiteComparisonData.withWarning(
                    "No global site configured for this organization"));
            return;
        }

        String globalSiteName = globalSite.getName();

        if (globalSiteResources.isEmpty()) {
            comparisonDataProperty.set(SiteComparisonData.withNoResources(
                    globalSite, globalSiteName,
                    "No accommodation resources found in global site"));
            return;
        }

        // Cache resources for linking operations
        this.cachedGlobalResources = new ArrayList<>(globalSiteResources);
        this.cachedEventResources = new ArrayList<>(eventSiteResources);

        // Build O(1) lookup map for global resources by ID
        this.globalResourcesById = new HashMap<>(globalSiteResources.size());
        for (Resource r : globalSiteResources) {
            globalResourcesById.put(r.getId().getPrimaryKey(), r);
        }

        // Build HashSet for O(1) name lookups
        Set<String> globalResourceNames = new HashSet<>(globalSiteResources.size());
        for (Resource r : globalSiteResources) {
            if (r.getName() != null) {
                globalResourceNames.add(r.getName());
            }
        }

        // Group event site resources by site: O(E)
        Object globalSiteId = globalSite.getId().getPrimaryKey();
        Map<Site, List<Resource>> resourcesBySite = new HashMap<>();
        for (Resource r : eventSiteResources) {
            Site site = r.getSite();
            if (site != null && !site.getId().getPrimaryKey().equals(globalSiteId)) {
                resourcesBySite.computeIfAbsent(site, k -> new ArrayList<>()).add(r);
            }
        }

        // Build comparison results for each event site
        List<EventSiteComparison> comparisons = new ArrayList<>();

        for (Map.Entry<Site, List<Resource>> entry : resourcesBySite.entrySet()) {
            Site eventSite = entry.getKey();
            List<Resource> siteResources = entry.getValue();

            // Get the event from the site (already loaded via site.event)
            Event siteEvent = eventSite.getEvent();
            if (siteEvent == null) {
                continue;
            }

            List<Event> siteEvents = List.of(siteEvent);

            // Build HashSet for O(1) event resource name lookups
            Set<String> eventSiteResourceNames = new HashSet<>(siteResources.size());
            for (Resource r : siteResources) {
                if (r.getName() != null) {
                    eventSiteResourceNames.add(r.getName());
                }
            }

            // Compare resources using O(1) set lookups
            List<String> onlyInGlobal = new ArrayList<>();
            List<String> inBoth = new ArrayList<>();
            List<String> onlyInEventSite = new ArrayList<>();
            List<Resource> globalOnlyResources = new ArrayList<>();
            List<Resource> eventOnlyResources = new ArrayList<>();

            // Check global resources against event site: O(G)
            for (Resource globalResource : globalSiteResources) {
                String globalName = globalResource.getName();
                if (globalName == null) continue;

                if (eventSiteResourceNames.contains(globalName)) {
                    inBoth.add(globalName);
                } else {
                    onlyInGlobal.add(globalName);
                    globalOnlyResources.add(globalResource);
                }
            }

            // Check event site resources not in global: O(N)
            for (Resource eventResource : siteResources) {
                String eventName = eventResource.getName();
                if (eventName == null) continue;

                if (!globalResourceNames.contains(eventName)) {
                    onlyInEventSite.add(eventName);
                    eventOnlyResources.add(eventResource);
                }
            }

            // Sort lists: O(n log n)
            Collections.sort(onlyInGlobal);
            Collections.sort(inBoth);
            Collections.sort(onlyInEventSite);
            globalOnlyResources.sort(Comparator.comparing(r -> r.getName() != null ? r.getName() : ""));
            eventOnlyResources.sort(Comparator.comparing(r -> r.getName() != null ? r.getName() : ""));

            // Check for existing links: O(N) with O(1) lookups
            List<ResourceLink> confirmedLinks = new ArrayList<>();
            for (Resource eventResource : eventOnlyResources) {
                Resource linkedGlobal = getLinkedGlobalResource(eventResource);
                if (linkedGlobal != null) {
                    confirmedLinks.add(ResourceLink.confirmed(eventResource, linkedGlobal));
                }
            }

            // REMOVED: Expensive O(N×G×L²) fuzzy matching - users can link manually
            List<ResourceLink> suggestedLinks = Collections.emptyList();

            comparisons.add(new EventSiteComparison(
                    eventSite,
                    eventSite.getName(),
                    siteEvents,
                    onlyInGlobal,
                    inBoth,
                    onlyInEventSite,
                    globalOnlyResources,
                    eventOnlyResources,
                    confirmedLinks,
                    suggestedLinks
            ));
        }

        // Sort comparisons by event start date: O(S log S)
        comparisons.sort(Comparator.comparing((EventSiteComparison c) -> {
            if (c.futureEvents().isEmpty()) return LocalDate.MAX;
            Event event = c.futureEvents().get(0);
            return event.getStartDate() != null ? event.getStartDate() : LocalDate.MAX;
        }));

        String warningMessage = null;
        if (comparisons.isEmpty() && !eventSiteResources.isEmpty()) {
            warningMessage = "No event sites found with future events";
        } else if (comparisons.isEmpty()) {
            warningMessage = "No event-specific sites with resources found";
        }

        comparisonDataProperty.set(new SiteComparisonData(
                globalSite,
                globalSiteName,
                globalResourceNames.size(),
                comparisons,
                true,
                true,
                warningMessage
        ));
    }
}
