package one.modality.hotel.backoffice.activities.household;

import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.accommodation.AccommodationPresentationModel;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

/**
 * Loader for ResourceConfigurations (rooms) for the Gantt display.
 * Queries the ResourceConfiguration table directly instead of ScheduledResource to avoid huge queries.
 * This ensures all rooms are displayed in the Gantt, even if they have no attendances.
 *
 * @author Bruno Salmon
 */
final class HouseholdScheduledResourceLoader {

    /**
     * Site ID filter for resource queries.
     * Currently hardcoded to filter for a specific site within the organization.
     * TODO: Make this configurable via AccommodationPresentationModel if multi-site support is needed.
     * Set to null to disable site filtering and show all sites in the organization.
     */
    private static final Integer SITE_ID_FILTER = 1671;

    private final AccommodationPresentationModel pm;
    private final ObservableList<ResourceConfiguration> resourceConfigurations = FXCollections.observableArrayList();

    HouseholdScheduledResourceLoader(AccommodationPresentationModel pm) {
        this.pm = pm;
    }

    ObservableList<ResourceConfiguration> getResourceConfigurations() {
        return resourceConfigurations;
    }

    void startLogic(Object mixin) {
        // Query ResourceConfiguration table directly - much faster than querying ScheduledResource!
        // This is a small, static table of room configurations
        ReactiveEntitiesMapper.<ResourceConfiguration>createPushReactiveChain(mixin)
                .always("{class: 'ResourceConfiguration', alias: 'rc', fields: 'name,item.family.(name,ord)'}")
                .always(orderBy("item.family.ord,name"))
                // Filter by organization and optional site ID
                .always(pm.organizationIdProperty(), org -> {
                    if (SITE_ID_FILTER != null) {
                        return where("resource.site.organization=? and resource.site=?", org, SITE_ID_FILTER);
                    } else {
                        return where("resource.site.organization=?", org);
                    }
                })
                .storeEntitiesInto(resourceConfigurations)
                .start();
    }
}
