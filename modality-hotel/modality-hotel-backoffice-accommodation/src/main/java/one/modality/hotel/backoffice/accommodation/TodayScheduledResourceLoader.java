package one.modality.hotel.backoffice.accommodation;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.cache.client.LocalStorageCache;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.client.gantt.fx.today.FXToday;
import one.modality.base.shared.entities.ScheduledResource;

import static dev.webfx.stack.orm.dql.DqlStatement.where;

public final class TodayScheduledResourceLoader {

    // The presentation model used by the logic code to query the server.
    private final AccommodationPresentationModel pm;
    // The reactive entities mapper that will query the server.
    private ReactiveEntitiesMapper<ScheduledResource> rem;
    // The results returned by the server will be stored in an observable list of ScheduledResource entities:
    private final ObservableList<ScheduledResource> todayScheduledResources = FXCollections.observableArrayList();

    // Workaround for a WebFX push notification issue that happens when several identical reactive entities mappers (ie
    // sending the exact same query and parameters to the server) run on the same client => the issue is that the push
    // notifications are sent to only 1 instance at a time. The workaround is to keep a single instance of the loader.
    // TODO: remove this workaround when the WebFX push notification issue is fixed
    private static TodayScheduledResourceLoader INSTANCE;
    private ObservableValue<Boolean> activeProperty;
    public static TodayScheduledResourceLoader getOrCreate(AccommodationPresentationModel pm) {
        // Creating the instance on first call only (assuming the presentation model is identical on subsequent calls)
        if (INSTANCE == null)
            INSTANCE = new TodayScheduledResourceLoader(pm);
        return INSTANCE;
    }

    private TodayScheduledResourceLoader(AccommodationPresentationModel pm) {
        this.pm = pm;
    }

    public ObservableList<ScheduledResource> getTodayScheduledResources() {
        return todayScheduledResources;
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
            rem = ReactiveEntitiesMapper.<ScheduledResource>createPushReactiveChain(mixin)
                    .always( // language=JSON5
                        "{class: 'ScheduledResource', alias: 'sr', fields: 'max,configuration,(select count(1) from Attendance where present and scheduledResource=sr and !documentLine.cancelled) as booked'}")
                    // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("configuration.resource.site.organization=?", o))
                    // Restricting events to those appearing in the time window
                    .always(FXToday.todayProperty(), today -> where("sr.date = ?", today))
                    // Storing the result directly in the events layer
                    .storeEntitiesInto(todayScheduledResources)
                    .setResultCacheEntry(LocalStorageCache.get().getCacheEntry("cache-accommodationTodayScheduledResource"))
                    // We are now ready to start
                    .start();
        } else if (activeProperty != null) // subsequent calls
            rem.bindActivePropertyTo(activeProperty); // updating the reactive entities mapper active property
    }
}
