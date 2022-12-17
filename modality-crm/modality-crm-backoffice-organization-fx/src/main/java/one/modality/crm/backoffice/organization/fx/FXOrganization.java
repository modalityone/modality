package one.modality.crm.backoffice.organization.fx;

import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Organization;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public final class FXOrganization {

    private final static ObjectProperty<Organization> organizationProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            FXOrganizationId.setOrganizationId(getOrganizationId());
        }
    };

    static {
        FXOrganizationId.init();
    }

    static EntityId getOrganizationId() {
        return Entities.getId(getOrganization());
    }

    static EntityStore getOrganizationStore() {
        Organization organization = getOrganization();
        return organization != null ? organization.getStore() : EntityStore.create(DataSourceModelService.getDefaultDataSourceModel());
    }

    public static ObjectProperty<Organization> organizationProperty() {
        return organizationProperty;
    }

    public static Organization getOrganization() {
        return organizationProperty.get();
    }

    public static void setOrganization(Organization organization) {
        if (!Objects.equals(organization, getOrganization()))
            organizationProperty.set(organization);
    }

}
