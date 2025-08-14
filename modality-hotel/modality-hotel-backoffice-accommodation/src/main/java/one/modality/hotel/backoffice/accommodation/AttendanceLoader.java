package one.modality.hotel.backoffice.accommodation;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.routing.activity.impl.elementals.activeproperty.HasActiveProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.Attendance;

import static dev.webfx.stack.orm.dql.DqlStatement.orderBy;
import static dev.webfx.stack.orm.dql.DqlStatement.where;

public final class AttendanceLoader {

    // The presentation model used by the logic code to query the server.
    private final AccommodationPresentationModel pm;
    // The reactive entities mapper that will query the server.
    private ReactiveEntitiesMapper<Attendance> rem;
    // The results returned by the server will be stored in an observable list of Attendance entities:
    private final ObservableList<Attendance> attendances = FXCollections.observableArrayList();

    // Workaround for a WebFX push notification issue that happens when several identical reactive entities mappers (ie
    // sending the exact same query and parameters to the server) run on the same client => the issue is that the push
    // notifications are sent to only 1 instance at a time. The workaround is to keep a single instance of the loader.
    // TODO: remove this workaround when the WebFX push notification issue is fixed
    private static AttendanceLoader INSTANCE;
    private ObservableValue<Boolean> activeProperty;
    public static AttendanceLoader getOrCreate(AccommodationPresentationModel pm) {
        // Creating the instance on first call only (assuming the presentation model is identical on subsequent calls)
        if (INSTANCE == null)
            INSTANCE = new AttendanceLoader(pm);
        return INSTANCE;
    }

    private AttendanceLoader(AccommodationPresentationModel pm) {
        this.pm = pm;
    }

    public ObservableList<Attendance> getAttendances() {
        return attendances;
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
            rem = ReactiveEntitiesMapper.<Attendance>createPushReactiveChain(mixin)
                    .always( // language=JSON5
                        "{class: 'Attendance', alias: 'a', fields: 'date,documentLine.document.(arrived,person_firstName,person_lastName,event.id),scheduledResource.configuration.(name,item.name),documentLine.document.event.name'}")
                    .always(where("scheduledResource is not null"))
                    // Order is important for TimeBarUtil
                    .always(orderBy("scheduledResource.configuration.item.ord,scheduledResource.configuration.name,documentLine.document.person_lastName,documentLine.document.person_firstName,date"))
                    // Returning events for the selected organization only (or returning an empty set if no organization is selected)
                    .ifNotNullOtherwiseEmpty(pm.organizationIdProperty(), o -> where("documentLine.document.event.organization=?", o))
                    // Restricting events to those appearing in the time window
                    .always(pm.timeWindowStartProperty(), startDate -> where("a.date +1 >= ?", startDate)) // +1 is to avoid the round corners on left  for bookings exceeding the time window
                    .always(pm.timeWindowEndProperty(),   endDate   -> where("a.date -1 <= ?", endDate))   // -1 is to avoid the round corners on right for bookings exceeding the time window
                    // Storing the result directly in the events layer
                    .storeEntitiesInto(attendances)
                    // We are now ready to start
                    .start();
        } else if (activeProperty != null) // subsequent calls
            rem.bindActivePropertyTo(activeProperty); // updating the reactive entities mapper active property
    }

}
