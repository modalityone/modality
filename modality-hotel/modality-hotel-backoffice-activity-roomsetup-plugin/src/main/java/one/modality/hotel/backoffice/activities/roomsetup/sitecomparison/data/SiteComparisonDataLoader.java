package one.modality.hotel.backoffice.activities.roomsetup.sitecomparison.data;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Resource;
import one.modality.base.shared.entities.Site;
import one.modality.hotel.backoffice.activities.roomsetup.RoomSetupPresentationModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * Data loader for the Site Comparison view.
 * Loads organization, global site, and accommodation resources for comparison.
 *
 * LAZY LOADING VERSION:
 * - Loads event sites list first (minimal data)
 * - Global resources loaded upfront (shared across all comparisons)
 * - Event site resources loaded on-demand when section is expanded
 *
 * @author Claude Code
 */
public final class SiteComparisonDataLoader {

    private final RoomSetupPresentationModel pm;
    private Object mixin;

    // Loading state for initial data (sites list + global resources)
    private final BooleanProperty loadingProperty = new SimpleBooleanProperty(true);

    // Loaded data
    private final ObjectProperty<Organization> organizationProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Site> globalSiteProperty = new SimpleObjectProperty<>();
    private final ObservableList<Resource> globalSiteResources = FXCollections.observableArrayList();

    // Event sites list (minimal data - just site/event info)
    private final ObservableList<Site> eventSites = FXCollections.observableArrayList();

    // Per-site resources (lazy loaded)
    private final Map<Object, ObservableList<Resource>> siteResourcesMap = new HashMap<>();
    private final Set<Object> loadingSites = new HashSet<>();
    private final Set<Object> loadedSites = new HashSet<>();

    // Track which queries have completed
    private boolean organizationLoaded = false;
    private boolean eventSitesLoaded = false;
    private boolean globalResourcesLoaded = false;

    public SiteComparisonDataLoader(RoomSetupPresentationModel pm) {
        this.pm = pm;
    }

    public BooleanProperty loadingProperty() {
        return loadingProperty;
    }

    public boolean isLoading() {
        return loadingProperty.get();
    }

    public ObjectProperty<Organization> organizationProperty() {
        return organizationProperty;
    }

    public ObjectProperty<Site> globalSiteProperty() {
        return globalSiteProperty;
    }

    public ObservableList<Resource> getGlobalSiteResources() {
        return globalSiteResources;
    }

    /**
     * Returns the list of event sites (minimal data for display).
     */
    public ObservableList<Site> getEventSites() {
        return eventSites;
    }

    /**
     * Returns resources for a specific site (may be empty if not loaded yet).
     */
    public ObservableList<Resource> getResourcesForSite(Object siteId) {
        return siteResourcesMap.computeIfAbsent(siteId, k -> FXCollections.observableArrayList());
    }

    /**
     * Checks if resources for a specific site are currently loading.
     */
    public boolean isSiteLoading(Object siteId) {
        return loadingSites.contains(siteId);
    }

    /**
     * Checks if resources for a specific site have been loaded.
     */
    public boolean isSiteLoaded(Object siteId) {
        return loadedSites.contains(siteId);
    }

    /**
     * Loads resources for a specific site on demand.
     *
     * @param siteId The site ID to load resources for
     * @param onLoaded Callback when loading is complete
     */
    public void loadSiteResources(Object siteId, Consumer<ObservableList<Resource>> onLoaded) {
        if (loadedSites.contains(siteId)) {
            // Already loaded
            if (onLoaded != null) {
                onLoaded.accept(getResourcesForSite(siteId));
            }
            return;
        }

        if (loadingSites.contains(siteId)) {
            // Already loading - don't start another query
            return;
        }

        loadingSites.add(siteId);
        Console.log("[SiteComparisonDataLoader] Loading resources for site: " + siteId);

        ObservableList<Resource> siteResources = getResourcesForSite(siteId);

        EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
            .<Resource>executeQuery(
                "select name,site.(id,name,event.(id,name,startDate,endDate)),kbs2ToKbs3GlobalResource.id " +
                "from Resource where site=$1 order by name limit 200",
                siteId)
            .onFailure(error -> {
                Console.log("[SiteComparisonDataLoader] Failed to load site resources: " + error.getMessage());
                loadingSites.remove(siteId);
                loadedSites.add(siteId);
                if (onLoaded != null) {
                    onLoaded.accept(siteResources);
                }
            })
            .onSuccess(resources -> {
                Console.log("[SiteComparisonDataLoader] Loaded " + resources.size() + " resources for site: " + siteId);
                siteResources.setAll(resources);
                loadingSites.remove(siteId);
                loadedSites.add(siteId);
                if (onLoaded != null) {
                    onLoaded.accept(siteResources);
                }
            });
    }

    /**
     * Starts the reactive data loading logic.
     *
     * @param mixin The mixin object for reactive chain lifecycle
     */
    public void startLogic(Object mixin) {
        this.mixin = mixin;
        loadingProperty.set(true);

        // Listen for organization ID changes
        FXProperties.runOnPropertiesChange(this::onOrganizationIdChanged, pm.organizationIdProperty());

        // Load immediately if organization ID is already set
        onOrganizationIdChanged();
    }

    private void onOrganizationIdChanged() {
        Object orgId = pm.organizationIdProperty().get();
        Console.log("[SiteComparisonDataLoader] Organization ID changed: " + orgId);

        if (orgId != null) {
            // Reset loading state
            loadingProperty.set(true);
            organizationLoaded = false;
            eventSitesLoaded = false;
            globalResourcesLoaded = false;

            // Clear per-site data
            siteResourcesMap.clear();
            loadingSites.clear();
            loadedSites.clear();

            // Load organization with global site
            loadOrganization(orgId);

            // Start event sites query (just site list, not resources)
            startEventSitesQuery();
        } else {
            // No organization - mark as loaded with no data
            Console.log("[SiteComparisonDataLoader] No organization ID, marking as loaded");
            organizationLoaded = true;
            eventSitesLoaded = true;
            globalResourcesLoaded = true;
            loadingProperty.set(false);
        }
    }

    private void checkLoadingComplete() {
        // Initial load is done when: organization loaded + event sites loaded + global resources loaded
        boolean isComplete = organizationLoaded && eventSitesLoaded && globalResourcesLoaded;
        if (isComplete && loadingProperty.get()) {
            Console.log("[SiteComparisonDataLoader] Initial loading complete. " +
                    "Global site: " + globalSiteProperty.get() +
                    ", Event sites: " + eventSites.size() +
                    ", Global resources: " + globalSiteResources.size());
            loadingProperty.set(false);
        }
    }

    private void loadOrganization(Object organizationId) {
        Console.log("[SiteComparisonDataLoader] Loading organization: " + organizationId);

        EntityStore.create(DataSourceModelService.getDefaultDataSourceModel())
            .<Organization>executeQuery("select name,globalSite.(id,name) from Organization where id=$1", organizationId)
            .onFailure(error -> {
                Console.log("[SiteComparisonDataLoader] Failed to load organization: " + error.getMessage());
                organizationLoaded = true;
                globalResourcesLoaded = true; // Mark as done since we can't load
                checkLoadingComplete();
            })
            .onSuccess(organizations -> {
                Console.log("[SiteComparisonDataLoader] Organization query returned: " + organizations.size() + " results");
                if (!organizations.isEmpty()) {
                    Organization org = organizations.get(0);
                    organizationProperty.set(org);

                    Site globalSite = org.getGlobalSite();
                    Console.log("[SiteComparisonDataLoader] Global site: " + (globalSite != null ? globalSite.getName() : "null"));
                    globalSiteProperty.set(globalSite);

                    // Start loading global site resources if global site exists
                    if (globalSite != null && globalSite.getId() != null) {
                        startGlobalSiteResourcesQuery(globalSite.getId());
                    } else {
                        globalResourcesLoaded = true;
                    }
                } else {
                    globalResourcesLoaded = true;
                }
                organizationLoaded = true;
                checkLoadingComplete();
            });
    }

    /**
     * Query for accommodation resources in the global site.
     * Limited to 500 resources for performance.
     */
    private void startGlobalSiteResourcesQuery(EntityId globalSiteId) {
        Console.log("[SiteComparisonDataLoader] Starting global site resources query for site: " + globalSiteId);

        ReactiveEntitiesMapper.<Resource>createPushReactiveChain(mixin)
            .always("{class: 'Resource', fields: 'name', orderBy: 'name', limit: '500'}")
            .always(where("site=$1", globalSiteId))
            .storeEntitiesInto(globalSiteResources)
            .addEntitiesHandler(resources -> {
                Console.log("[SiteComparisonDataLoader] Global site resources loaded: " + resources.size());
                globalResourcesLoaded = true;
                checkLoadingComplete();
            })
            .start();
    }

    /**
     * Query for event sites list (minimal data - no resources).
     * This is fast and gives us the list to display collapsed sections.
     */
    private void startEventSitesQuery() {
        Console.log("[SiteComparisonDataLoader] Starting event sites query (sites list only)");

        ReactiveEntitiesMapper.<Site>createPushReactiveChain(mixin)
            .always("{class: 'Site', " +
                    "fields: 'name,event.(id,name,startDate,endDate)', " +
                    "orderBy: 'event.startDate,name', " +
                    "limit: '100'}")
            .always(where("event is not null and event.startDate >= current_date()"))
            .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), org -> where("organization=$1", org))
            .storeEntitiesInto(eventSites)
            .addEntitiesHandler(sites -> {
                Console.log("[SiteComparisonDataLoader] Event sites loaded: " + sites.size());
                eventSitesLoaded = true;
                checkLoadingComplete();
            })
            .start();
    }
}
