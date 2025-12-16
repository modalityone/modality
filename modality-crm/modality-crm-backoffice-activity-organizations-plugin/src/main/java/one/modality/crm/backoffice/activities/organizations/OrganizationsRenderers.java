package one.modality.crm.backoffice.activities.organizations;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.cell.renderer.ValueRenderingContext;
import dev.webfx.extras.panes.MonoPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Country;
import one.modality.base.shared.entities.Organization;

/**
 * Custom renderers for the Organizations view.
 *
 * @author Claude Code
 */
final class OrganizationsRenderers {

    static void registerRenderers() {
        // Register common renderers shared with other admin views
        //SuperAdminRenderers.registerCommonRenderers();

        // Register the organization ID renderer (extracts numeric ID from entity)
        ValueRendererRegistry.registerValueRenderer("organizationId", (value, context) -> {
            Organization organization = (Organization) value;
            Object primaryKey = organization.getPrimaryKey();
            Label idLabel = new Label(primaryKey != null ? primaryKey.toString() : "-");
            idLabel.getStyleClass().add("organization-id-label");
            return idLabel;
        });

        // Register the organization name renderer (shows name with full name below)
        ValueRendererRegistry.registerValueRenderer("organizationName", (value, context) -> {
            Organization organization = (Organization) value;

            VBox nameBox = new VBox(2);
            nameBox.setAlignment(Pos.CENTER_LEFT);

            // Short name (main name displayed in blue)
            Label nameLabel = new Label(organization.getName());
            nameLabel.getStyleClass().add("organization-name-primary");

            nameBox.getChildren().add(nameLabel);

            // Full name with type (displayed below in gray) if type exists
            if (organization.getType() != null) {
                String typeName = organization.getType().getName();
                if (typeName != null && !typeName.isEmpty()) {
                    Label typeLabel = new Label(typeName);
                    typeLabel.getStyleClass().add("organization-name-secondary");
                    nameBox.getChildren().add(typeLabel);
                }
            }

            return nameBox;
        });

        // Register the location renderer (city, country, street)
        ValueRendererRegistry.registerValueRenderer("organizationLocation", (value, context) -> {
            Organization organization = (Organization) value;

            VBox locationBox = new VBox(2);
            locationBox.setAlignment(Pos.CENTER_LEFT);

            String cityName = organization.getStringFieldValue("cityName");
            Country country = organization.getCountry();
            String street = organization.getStringFieldValue("street");

            // City, Country
            StringBuilder locationText = new StringBuilder();
            if (cityName != null && !cityName.isEmpty()) {
                locationText.append(cityName);
            }
            if (country != null && country.getName() != null) {
                if (locationText.length() > 0) {
                    locationText.append(", ");
                }
                locationText.append(country.getName());
            }

            if (locationText.length() > 0) {
                Label mainLocation = new Label(locationText.toString());
                mainLocation.getStyleClass().add("organization-location-main");
                locationBox.getChildren().add(mainLocation);
            }

            // Street address (secondary)
            if (street != null && !street.isEmpty()) {
                Label streetLabel = new Label(street);
                streetLabel.getStyleClass().add("organization-location-secondary");
                locationBox.getChildren().add(streetLabel);
            }

            if (locationBox.getChildren().isEmpty()) {
                Label emptyLabel = new Label("-");
                emptyLabel.getStyleClass().add("admin-text-italic");
                locationBox.getChildren().add(emptyLabel);
            }

            return locationBox;
        });

        // Register the contact renderer (email, phone)
        ValueRendererRegistry.registerValueRenderer("organizationContact", (value, context) -> {
            Organization organization = (Organization) value;

            VBox contactBox = new VBox(2);
            contactBox.setAlignment(Pos.CENTER_LEFT);

            String email = organization.getStringFieldValue("email");
            String phone = organization.getStringFieldValue("phone");

            // Email
            if (email != null && !email.isEmpty()) {
                HBox emailRow = new HBox(4);
                emailRow.setAlignment(Pos.CENTER_LEFT);
                Label emailIcon = new Label("\u2709");
                emailIcon.getStyleClass().add("organization-contact-icon");
                Label emailLabel = new Label(email);
                emailLabel.getStyleClass().add("organization-contact-email");
                emailRow.getChildren().addAll(emailIcon, emailLabel);
                contactBox.getChildren().add(emailRow);
            }

            // Phone
            if (phone != null && !phone.isEmpty()) {
                HBox phoneRow = new HBox(4);
                phoneRow.setAlignment(Pos.CENTER_LEFT);
                Label phoneIcon = new Label("\u260E");
                phoneIcon.getStyleClass().add("organization-contact-icon");
                Label phoneLabel = new Label(phone);
                phoneLabel.getStyleClass().add("organization-contact-phone");
                phoneRow.getChildren().addAll(phoneIcon, phoneLabel);
                contactBox.getChildren().add(phoneRow);
            }

            if (contactBox.getChildren().isEmpty()) {
                Label emptyLabel = new Label("-");
                emptyLabel.getStyleClass().add("admin-text-italic");
                contactBox.getChildren().add(emptyLabel);
            }

            return contactBox;
        });

        // Register the status renderer (Active/Closed badge that can be clicked)
        ValueRendererRegistry.registerValueRenderer("organizationStatus", (value, context) -> {
            Organization organization = (Organization) value;
            Boolean closed = organization.getBooleanFieldValue("closed");
            boolean isClosed = closed != null && closed;

            Label statusLabel = new Label(isClosed ? "Closed" : "Active");
            statusLabel.setPadding(new Insets(4, 8, 4, 8));
            statusLabel.getStyleClass().add(isClosed ? "organization-status-closed" : "organization-status-active");

            // Make clickable to toggle status
            statusLabel.setOnMouseClicked(e -> {
                OrganizationsDialogs.toggleClosedStatus(organization, getActivity(context));
            });
            statusLabel.getStyleClass().add("organization-status-clickable");

            return statusLabel;
        });

        // Register the actions renderer
        ValueRendererRegistry.registerValueRenderer("organizationActions", (value, context) -> {
            Organization organization = (Organization) value;

            HBox actionsBox = new HBox(12);
            actionsBox.setAlignment(Pos.CENTER);

            // Edit button with SVG icon
            SVGPath editIcon = SvgIcons.createEditPath();
            editIcon.setFill(Color.web("#666666"));
            editIcon.getStyleClass().add("admin-action-icon");
            MonoPane editButton = SvgIcons.createButtonPane(editIcon, () -> OrganizationsDialogs.showEditDialog(organization, getActivity(context)));
            editButton.getStyleClass().add("admin-action-button");

            actionsBox.getChildren().add(editButton);
            return actionsBox;
        });
    }

    private static OrganizationsActivity getActivity(ValueRenderingContext context) {
        return context.getAppContext();
    }

}
