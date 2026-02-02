package one.modality.crm.backoffice.activities.administrators;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.panes.MonoPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.AuthorizationOrganizationAdmin;
import one.modality.base.shared.entities.Organization;
import one.modality.base.shared.entities.Person;

import java.util.List;

/**
 * Custom renderers for the Organizations view.
 *
 * @author Claude Code
 */
final class AdministratorsRenderers {

    static void registerRenderers() {
        // Register managers list renderer
        ValueRendererRegistry.registerValueRenderer("managersList", (value, context) -> {
            Organization organization = (Organization) value;
            AdministratorsActivity activity = context.getAppContext();

            HBox flow = new HBox(4);
            flow.setAlignment(Pos.CENTER_LEFT);

            // Get admins for this organization from the view's feed
            List<AuthorizationOrganizationAdmin> admins = activity.getAdminsForOrganization(organization);

            if (admins == null || admins.isEmpty()) {
                Label emptyLabel = new Label("-");
                emptyLabel.getStyleClass().add("admin-text-italic");
                flow.getChildren().add(emptyLabel);
            } else {
                // Build the chips
                for (AuthorizationOrganizationAdmin admin : admins) {
                    Person person = admin.getAdmin();
                    if (person != null) {
                        String managerName = person.getFirstName() + " " + person.getLastName();
                        Label chip = ModalityStyle.badgeLightInfo(new Label(managerName));
                        chip.setPadding(new Insets(3, 8, 3, 8));
                        flow.getChildren().add(chip);
                    }
                }
            }

            return flow;
        });

        // Register active users renderer
        ValueRendererRegistry.registerValueRenderer("activeUsers", (value, context) -> {
            if (!(value instanceof Organization organization)) {
                return null;
            }

            AdministratorsActivity activity = context.getAppContext();
            int userCount = activity.getUserAccessCountForOrganization(organization);

            Label badge =ModalityStyle.badgeLightInfo(new Label(String.valueOf(userCount)));
            badge.setPadding(new Insets(3, 8, 3, 8));
            return badge;
        });

        // Register organization actions renderer
        ValueRendererRegistry.registerValueRenderer("organizationActions", (value, context) -> {
            Organization organization = (Organization) value;
            AdministratorsActivity activity = context.getAppContext();

            HBox actionsBox = new HBox(8);
            actionsBox.setAlignment(Pos.CENTER);

            // Manage Managers button with SVG icon (people icon)
            SVGPath managersIcon = SvgIcons.createSVGPath("M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75");
            managersIcon.setFill(Color.web("#666666"));
            managersIcon.getStyleClass().add("admin-action-icon");
            MonoPane managersButton = SvgIcons.createButtonPane(managersIcon, () -> activity.showManageManagersDialog(organization));
            managersButton.getStyleClass().add("admin-action-button");

            actionsBox.getChildren().add(managersButton);
            return actionsBox;
        });
    }
}
