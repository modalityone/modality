package one.modality.ecommerce.backoffice.activities.statistics;

import dev.webfx.extras.time.window.TimeWindow;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentGenericTablePresentationModel;

import java.time.LocalDate;

final class StatisticsPresentationModel extends OrganizationDependentGenericTablePresentationModel
        implements TimeWindow<LocalDate> {

    // Display input

    private final ObjectProperty<LocalDate> timeWindowStartProperty = new SimpleObjectProperty<>(LocalDate.now().minusWeeks(1));
    public ObjectProperty<LocalDate> timeWindowStartProperty() { return timeWindowStartProperty; }

    private final ObjectProperty<LocalDate> timeWindowEndProperty = new SimpleObjectProperty<>(LocalDate.now().plusWeeks(3));
    public ObjectProperty<LocalDate> timeWindowEndProperty() { return timeWindowEndProperty; }

    // Note: this part of the TimeWindow is not used in the pm logic
    private final DoubleProperty translateOriginXProperty = new SimpleDoubleProperty();
    @Override
    public DoubleProperty timeWindowTranslateXProperty() {
        return translateOriginXProperty;
    }
}
