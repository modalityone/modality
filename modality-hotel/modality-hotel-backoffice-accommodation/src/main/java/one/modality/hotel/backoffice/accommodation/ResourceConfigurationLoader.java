package one.modality.hotel.backoffice.accommodation;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.ResourceConfiguration;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

public final class ResourceConfigurationLoader {

    // The presentation model used by the logic code to query the server.
    private final AccommodationPresentationModel pm;
    // The reactive entities mapper that will query the server.
    private ReactiveEntitiesMapper<ResourceConfiguration> rem;

    // The results returned by the server will be stored in an observable list of ResourceConfiguration entities:
    private final ObservableList<ResourceConfiguration> resourceConfigurations = FXCollections.observableArrayList();

    // Workaround for a WebFX push notification issue that happens when several identical reactive entities mappers (ie
    // sending the exact same query and parameters to the server) run on the same client => the issue is that the push
    // notifications are sent to only 1 instance at a time. The workaround is to keep a single instance of the loader.
    // TODO: remove this workaround when the WebFX push notification issue is fixed
    private static ResourceConfigurationLoader INSTANCE;
    private ObservableValue<Boolean> activeProperty;
    public static ResourceConfigurationLoader getOrCreate(AccommodationPresentationModel pm) {
        // Creating the instance on first call only (assuming the presentation model is identical on subsequent calls)
        if (INSTANCE == null)
            INSTANCE = new ResourceConfigurationLoader(pm);
        return INSTANCE;
    }

    private ResourceConfigurationLoader(AccommodationPresentationModel pm) {
        this.pm = pm;
    }

    public ObservableList<ResourceConfiguration> getResourceConfigurations() {
        return resourceConfigurations;
    }

    public void startLogic(Object mixin) { // may be called several times with different mixins (due to workaround)
        // Updating the active property with a OR => mixin1.active || mixin2.active || mixin3.active ...
        if (mixin instanceof HasActiveProperty) {
            ObservableValue<Boolean> ap = ((HasActiveProperty) mixin).activeProperty();
            if (activeProperty == null)
                activeProperty = ap;
            else
                activeProperty = FXProperties.combine(activeProperty, ap, (a1, a2) -> a1 || a2);
        }
        if (rem == null) { // first call
            // Loading room types (for when parents are provided ie "All rooms" ticked)
            rem = ReactiveEntitiesMapper.<ResourceConfiguration>createPushReactiveChain(mixin)
                    .always("{class: 'ResourceConfiguration', alias: 'rc', fields: 'name,item.name,max'}")
                    .always(orderBy("item.ord,name"))
                    // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("resource.site.(organization=? and event=null)", o))
                    // Restricting events to those appearing in the time window
                    .storeEntitiesInto(resourceConfigurations)
                    // We are now ready to start
                    .start();
        } else if (activeProperty != null) // subsequent calls
            rem.bindActivePropertyTo(activeProperty); // updating the reactive entities mapper active property
    }
}
