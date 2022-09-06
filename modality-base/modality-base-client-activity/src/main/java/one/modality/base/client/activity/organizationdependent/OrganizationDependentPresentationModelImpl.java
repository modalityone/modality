package one.modality.base.client.activity.organizationdependent;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * @author Bruno Salmon
 */
public class OrganizationDependentPresentationModelImpl implements OrganizationDependentPresentationModel {

    private final ObjectProperty<Object> organizationIdProperty = new SimpleObjectProperty<>();

    public ObjectProperty<Object> organizationIdProperty() {
        return organizationIdProperty;
    }

}
