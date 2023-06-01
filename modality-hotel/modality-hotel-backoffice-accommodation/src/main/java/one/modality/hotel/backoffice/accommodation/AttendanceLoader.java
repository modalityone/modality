package one.modality.hotel.backoffice.accommodation;

import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.Attendance;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

public class AttendanceLoader {

    // The presentation model used by the logic code to query the server (see startLogic() method)
    private final AccommodationPresentationModel pm;

    // The results returned by the server will be stored in observable lists of Attendance and ScheduledResource entities:
    private final ObservableList<Attendance> attendances = FXCollections.observableArrayList();

    public AttendanceLoader(AccommodationPresentationModel pm) {
        this.pm = pm;
    }

    public ObservableList<Attendance> getAttendances() {
        return attendances;
    }

    public void startLogic(Object mixin) {
        // This ReactiveEntitiesMapper will populate the children of the GanttLayout (indirectly from entities observable list)
        ReactiveEntitiesMapper.<Attendance>createPushReactiveChain(mixin)
                .always("{class: 'Attendance', alias: 'a', fields: 'date,documentLine.document.(arrived,person_firstName,person_lastName,event.id),scheduledResource.configuration.(name,item.name),documentLine.document.event.name'}")
                .always(where("scheduledResource is not null"))
                .always(orderBy("scheduledResource.configuration.item.ord,scheduledResource.configuration.name,documentLine.document.person_lastName,documentLine.document.person_firstName,date")) // Order is important for TimeBarUtil (see comment on barsLayout)
                // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("documentLine.document.event.organization=?", o))
                // Restricting events to those appearing in the time window
                .always(pm.timeWindowStartProperty(), startDate -> where("a.date +1 >= ?", startDate)) // +1 is to avoid the round corners on left for bookings exceeding the time window
                .always(pm.timeWindowEndProperty(),   endDate   -> where("a.date -1 <= ?", endDate)) // -1 is to avoid the round corners on right for bookings exceeding the time window
                // Storing the result directly in the events layer
                .storeEntitiesInto(attendances)
                // We are now ready to start
                .start();
    }
}
