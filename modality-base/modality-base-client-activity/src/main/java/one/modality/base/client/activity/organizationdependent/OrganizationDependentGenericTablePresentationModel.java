package one.modality.base.client.activity.organizationdependent;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import one.modality.base.client.activity.table.GenericTablePresentationModel;

/**
 * @author Bruno Salmon
 */
public class OrganizationDependentGenericTablePresentationModel
        extends GenericTablePresentationModel implements OrganizationDependentPresentationModel {

    private final ObjectProperty<Object> organizationIdProperty = new SimpleObjectProperty<>();

    public ObjectProperty<Object> organizationIdProperty() {
        return organizationIdProperty;
    }
}
