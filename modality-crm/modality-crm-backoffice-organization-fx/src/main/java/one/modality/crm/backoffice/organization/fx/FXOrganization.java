package one.modality.crm.backoffice.organization.fx;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import one.modality.base.shared.entities.Organization;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public class FXOrganization {

    private final static ObjectProperty<Organization> organizationProperty = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            System.out.println("Organization = " + getOrganization());
        }
    };

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
