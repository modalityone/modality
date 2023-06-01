package one.modality.hotel.backoffice.accommodation;

import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.ResourceConfiguration;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class ResourceConfigurationLoader {

    // The presentation model used by the logic code to query the server (see startLogic() method)
    private final AccommodationPresentationModel pm;

    // The results returned by the server will be stored in observable lists of Attendance and ScheduledResource entities:
    private final ObservableList<ResourceConfiguration> resourceConfigurations = FXCollections.observableArrayList();

    public ResourceConfigurationLoader(AccommodationPresentationModel pm) {
        this.pm = pm;
    }

    public ObservableList<ResourceConfiguration> getResourceConfigurations() {
        return resourceConfigurations;
    }

    public void startLogic(Object mixin) {
        // Loading room types (for when parents are provided ie "All rooms" ticked)
        ReactiveEntitiesMapper.<ResourceConfiguration>createPushReactiveChain(mixin)
                .always("{class: 'ResourceConfiguration', alias: 'rc', fields: 'name,item.name'}")
                .always(orderBy("item.ord,name"))
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("resource.site.(organization=? and event=null)", o))
                // Restricting events to those appearing in the time window
                .storeEntitiesInto(resourceConfigurations)
                // We are now ready to start
                .start();
    }
}
