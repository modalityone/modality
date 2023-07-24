package one.modality.base.client.activity.organizationdependent;

import javafx.beans.property.ObjectProperty;

public interface OrganizationDependentPresentationModelMixin
        extends OrganizationDependentPresentationModel {

    OrganizationDependentPresentationModel getPresentationModel();

    @Override
    default ObjectProperty<Object> organizationIdProperty() {
        return getPresentationModel().organizationIdProperty();
    }

    @Override
    default void setOrganizationId(Object organizationId) {
        getPresentationModel().setOrganizationId(organizationId);
    }

    @Override
    default Object getOrganizationId() {
        return getPresentationModel().getOrganizationId();
    }
}
