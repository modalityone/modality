package one.modality.hotel.backoffice.activities.accommodation;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.client.activity.eventdependent.EventDependentGenericTablePresentationModel;
import one.modality.base.client.presentationmodel.HasGanttSelectedObjectProperty;

public class AccommodationPresentationModel extends EventDependentGenericTablePresentationModel implements HasGanttSelectedObjectProperty {

    private final ObjectProperty<Object> ganttSelectedObjectProperty = new SimpleObjectProperty<>();
    @Override public ObjectProperty<Object> ganttSelectedObjectProperty() {
        return ganttSelectedObjectProperty;
    }
}
