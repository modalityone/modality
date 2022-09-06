package one.modality.base.client.activity.eventdependent;

import javafx.beans.property.ObjectProperty;
import one.modality.base.client.activity.organizationdependent.OrganizationDependentPresentationModelMixin;

/**
 * @author Bruno Salmon
 */
public interface EventDependentPresentationModelMixin extends EventDependentPresentationModel, OrganizationDependentPresentationModelMixin {

    EventDependentPresentationModel getPresentationModel();

    @Override
    default ObjectProperty<Object> eventIdProperty() {
        return getPresentationModel().eventIdProperty();
    }

    @Override
    default void setEventId(Object eventId) {
        getPresentationModel().setEventId(eventId);
    }

    @Override
    default Object getEventId() {
        return getPresentationModel().getEventId();
    }

}
