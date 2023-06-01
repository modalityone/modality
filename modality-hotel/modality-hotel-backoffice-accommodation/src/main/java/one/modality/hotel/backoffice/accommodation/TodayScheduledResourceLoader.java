package one.modality.hotel.backoffice.accommodation;

import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.client.gantt.fx.today.FXToday;
import one.modality.base.shared.entities.ScheduledResource;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

public final class TodayScheduledResourceLoader {

    // The presentation model used by the logic code to query the server (see startLogic() method)
    private final AccommodationPresentationModel pm;

    // The results returned by the server will be stored in observable lists of Attendance and ScheduledResource entities:
    private final ObservableList<ScheduledResource> todayScheduledResources = FXCollections.observableArrayList();
    private boolean started;

    public TodayScheduledResourceLoader(AccommodationPresentationModel pm) {
        this.pm = pm;
    }

    public ObservableList<ScheduledResource> getTodayScheduledResources() {
        return todayScheduledResources;
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
                .always(FXToday.todayProperty(), today -> where("sr.date = ?", today))
                // Storing the result directly in the events layer
                .storeEntitiesInto(todayScheduledResources)
                // We are now ready to start
                .start();
        started = true;
    }

    private static TodayScheduledResourceLoader INSTANCE;

    public static TodayScheduledResourceLoader getOrCreate(AccommodationPresentationModel pm) {
        if (INSTANCE == null)
            INSTANCE = new TodayScheduledResourceLoader(pm);
        return INSTANCE;
    }
}
