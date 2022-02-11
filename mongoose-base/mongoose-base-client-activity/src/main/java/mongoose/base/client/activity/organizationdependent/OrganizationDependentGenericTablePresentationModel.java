package mongoose.base.client.activity.organizationdependent;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import mongoose.base.client.activity.table.GenericTablePresentationModel;
import mongoose.base.client.presentationmodel.HasOrganizationIdProperty;

/**
 * @author Bruno Salmon
 */
public class OrganizationDependentGenericTablePresentationModel
        extends GenericTablePresentationModel
        implements HasOrganizationIdProperty {

    private final ObjectProperty<Object> organizationIdProperty = new SimpleObjectProperty<>();

    public ObjectProperty<Object> organizationIdProperty() {
        return organizationIdProperty;
    }

}