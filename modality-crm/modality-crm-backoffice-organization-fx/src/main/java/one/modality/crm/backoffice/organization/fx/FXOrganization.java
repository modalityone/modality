package one.modality.crm.backoffice.organization.fx;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.beans.property.ObjectProperty;
import one.modality.base.client.services.i18n.ModalityI18nProvider;
import one.modality.base.shared.entities.Organization;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public final class FXOrganization {

    public static final String EXPECTED_FIELDS = "name,type.(code,name),country,language,teachingsDayTicketItem,globalSite";

    private final static ObjectProperty<Organization> organizationProperty = FXProperties.newObjectProperty(() ->
        FXOrganizationId.setOrganizationId(getOrganizationId()));

    static {
        FXOrganizationId.init();
        // Registering "FXOrganization" as a possible entity holder for i18n key forwards in Modality (used by KBS to
        // rename "Recurring events" to "STTP" or "GP Classes" depending on the selected organization).
        ModalityI18nProvider.registerEntityHolder("FXOrganization", FXOrganization::getOrganization);
    }

    static EntityId getOrganizationId() {
        return Entities.getId(getOrganization());
    }

    public static EntityStore getOrganizationStore() {
        Organization organization = getOrganization();
        return organization != null ? organization.getStore() : EntityStore.create();
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

    public static void setOrganizationOnceExpectedFieldsAreLoaded(Organization organization) {
        organization.<Organization>onExpressionLoaded(EXPECTED_FIELDS)
            .onSuccess(FXOrganization::setOrganization);
    }

}
