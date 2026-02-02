package one.modality.event.backoffice.events.pm;

import dev.webfx.extras.time.window.TimeWindow;
import javafx.beans.property.*;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentGenericTablePresentationModel;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public final class EventsPresentationModel extends OrganizationDependentGenericTablePresentationModel
    implements TimeWindow<LocalDate> {

    // Display input

    private final BooleanProperty withBookingsProperty = new SimpleBooleanProperty(true); // Limit initially set to true
    public BooleanProperty withBookingsProperty() { return withBookingsProperty; }

    private final ObjectProperty<LocalDate> timeWindowStartProperty = new SimpleObjectProperty<>();
    public ObjectProperty<LocalDate> timeWindowStartProperty() { return timeWindowStartProperty; }

    private final ObjectProperty<LocalDate> timeWindowEndProperty = new SimpleObjectProperty<>();
    public ObjectProperty<LocalDate> timeWindowEndProperty() { return timeWindowEndProperty; }

    // Note: this part of the TimeWindow is not used in the pm logic
    private final DoubleProperty translateOriginXProperty = new SimpleDoubleProperty();
    @Override
    public DoubleProperty timeWindowTranslateXProperty() {
        return translateOriginXProperty;
    }
}
