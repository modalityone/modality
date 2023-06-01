package one.modality.hotel.backoffice.accommodation;

import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.client.gantt.fx.today.FXToday;
import one.modality.base.shared.entities.Attendance;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

public final class TodayAttendanceLoader {

    // The presentation model used by the logic code to query the server (see startLogic() method)
    private final AccommodationPresentationModel pm;

    // The results returned by the server will be stored in observable lists of Attendance and ScheduledResource entities:
    private final ObservableList<Attendance> todayAttendances = FXCollections.observableArrayList();

    public TodayAttendanceLoader(AccommodationPresentationModel pm) {
        this.pm = pm;
    }

    public ObservableList<Attendance> getTodayAttendances() {
        return todayAttendances;
    }

    private boolean started;

    public void startLogic(Object mixin) {
        if (started)
            return;
        // This ReactiveEntitiesMapper will populate the children of the GanttLayout (indirectly from entities observable list)
        ReactiveEntitiesMapper.<Attendance>createPushReactiveChain(mixin)
                .always("{class: 'Attendance', alias: 'a', fields: 'date,documentLine.document.(arrived,person_firstName,person_lastName,event.id),scheduledResource.configuration.(name,item.name),documentLine.document.event.name'}")
                .always(where("scheduledResource is not null"))
                .always(orderBy("scheduledResource.configuration.item.ord,scheduledResource.configuration.name,documentLine.document.person_lastName,documentLine.document.person_firstName,date")) // Order is important for TimeBarUtil (see comment on barsLayout)
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("documentLine.document.event.organization=?", o))
                // Restricting events to those appearing in the time window
                .always(FXToday.todayProperty(), today -> where("a.date = ?", today))
                // Storing the result directly in the events layer
                .storeEntitiesInto(todayAttendances)
                // We are now ready to start
                .start();
        started = true;
    }
}
