package one.modality.crm.backoffice.organization.fx;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.beans.property.ObjectProperty;
import one.modality.base.shared.entities.Organization;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public final class FXOrganization {

    public static final String EXPECTED_FIELDS = "name,type.(code,name),country";

    private final static ObjectProperty<Organization> organizationProperty = FXProperties.newObjectProperty(() ->
            FXOrganizationId.setOrganizationId(getOrganizationId()));

    static {
        FXOrganizationId.init();
    }

    static EntityId getOrganizationId() {
        return Entities.getId(getOrganization());
    }

    public static EntityStore getOrganizationStore() {
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
