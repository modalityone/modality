package one.modality.hotel.backoffice.activities.roomcalendar;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentGenericTablePresentationModel;
import dev.webfx.extras.time.window.TimeWindow;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public final class RoomCalendarPresentationModel extends OrganizationDependentGenericTablePresentationModel
    implements TimeWindow<LocalDate> {

    // Display input

    private final ObjectProperty<LocalDate> timeWindowStartProperty = new SimpleObjectProperty<>(LocalDate.now().minusWeeks(1));
    public ObjectProperty<LocalDate> timeWindowStartProperty() { return timeWindowStartProperty; }

    private final ObjectProperty<LocalDate> timeWindowEndProperty = new SimpleObjectProperty<>(LocalDate.now().plusWeeks(3));
    public ObjectProperty<LocalDate> timeWindowEndProperty() { return timeWindowEndProperty; }

}
