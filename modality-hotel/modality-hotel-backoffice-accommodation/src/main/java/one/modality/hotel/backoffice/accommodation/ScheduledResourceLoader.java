package one.modality.hotel.backoffice.accommodation;

import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.ScheduledResource;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class ScheduledResourceLoader {

    // The presentation model used by the logic code to query the server (see startLogic() method)
    private final AccommodationPresentationModel pm;

    // The results returned by the server will be stored in observable lists of Attendance and ScheduledResource entities:
    private final ObservableList<ScheduledResource> scheduledResources = FXCollections.observableArrayList();
    private boolean started;

    public ScheduledResourceLoader(AccommodationPresentationModel pm) {
        this.pm = pm;
    }

    public ObservableList<ScheduledResource> getScheduledResources() {
        return scheduledResources;
    }

    public void startLogic(Object mixin) {
        if (started)
            return;
        // This ReactiveEntitiesMapper will populate the provided parents of the GanttLayout (indirectly from allScheduledResources observable list)
        ReactiveEntitiesMapper.<ScheduledResource>createPushReactiveChain(mixin)
                .always("{class: 'ScheduledResource', alias: 'sr', fields: 'date,available,online,max,configuration.(name,item.name),(select count(1) from Attendance where scheduledResource=sr) as booked'}")
                .always(orderBy("configuration.item.ord,configuration.name,configuration,date")) // Order is important for TimeBarUtil (see comment on barsLayout)
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("configuration.resource.site.organization=?", o))
                // Restricting events to those appearing in the time window
                .always(pm.timeWindowStartProperty(), startDate -> where("sr.date >= ?", startDate))
                .always(pm.timeWindowEndProperty(), endDate -> where("sr.date <= ?", endDate))
                // Storing the result directly in the events layer
                .storeEntitiesInto(scheduledResources)
                // We are now ready to start
                .start();
        started = true;
    }

    private static ScheduledResourceLoader INSTANCE;

    public static ScheduledResourceLoader getOrCreate(AccommodationPresentationModel pm) {
        if (INSTANCE == null)
            INSTANCE = new ScheduledResourceLoader(pm);
        return INSTANCE;
    }
}
