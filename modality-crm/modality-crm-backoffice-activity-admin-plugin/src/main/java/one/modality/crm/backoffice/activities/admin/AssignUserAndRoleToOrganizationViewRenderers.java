package one.modality.crm.backoffice.activities.admin;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.AuthorizationOrganizationUserAccess;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;

import static one.modality.crm.backoffice.activities.admin.Admin18nKeys.*;

/**
 * Custom renderers for UserManagementView grid columns.
 *
 * @author Claude Code
 */
public class AssignUserAndRoleToOrganizationViewRenderers {

    private static AssignUserAndRoleToOrganizationView userManagementView;

    public static void setUserManagementView(AssignUserAndRoleToOrganizationView view) {
        userManagementView = view;
    }

    static {
        registerRenderers();
    }

    public static void registerRenderers() {
        // Register custom renderers for user management grid
        ValueRendererRegistry.registerValueRenderer("userActions", (value, context) -> {
            if (!(value instanceof AuthorizationOrganizationUserAccess userAccess)) {
                return null;
            }

            HBox actionsBox = new HBox(12);
            actionsBox.setAlignment(Pos.CENTER);

            // Delete button with SVG icon
            SVGPath deleteIcon = SvgIcons.createTrashSVGPath();
            deleteIcon.setFill(Color.web("#dc3545"));
            deleteIcon.getStyleClass().add("admin-action-icon");
            MonoPane deleteButton = SvgIcons.createButtonPane(deleteIcon, () -> {
                if (userManagementView != null) {
                    userManagementView.showRevokeAccessDialog(userAccess);
                }
            });
            deleteButton.getStyleClass().add("admin-action-button");

            actionsBox.getChildren().add(deleteButton);
            return actionsBox;
        });

        ValueRendererRegistry.registerValueRenderer("userName", (value, context) -> {
            if (!(value instanceof AuthorizationOrganizationUserAccess userAccess)) {
                return null;
            }
            Person user = userAccess.getUser();
            if (user == null) {
                return Bootstrap.strong(new Text("Unknown"));
            }
            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            String lastName = user.getLastName() != null ? user.getLastName() : "";
            String fullName = (firstName + " " + lastName).trim();
            if (fullName.isEmpty()) {
                fullName = "Unknown";
            }
            return Bootstrap.strong(new Text(fullName));
        });

        ValueRendererRegistry.registerValueRenderer("roleName", (value, context) -> {
            if (value == null) {
                return null;
            }
            Label badge = ModalityStyle.badgeLightDanger(new Label(value.toString()));
            badge.setPadding(new Insets(4, 12, 4, 12));
            return badge;
        });

        ValueRendererRegistry.registerValueRenderer("scopeName", (value, context) -> {
            if (!(value instanceof AuthorizationOrganizationUserAccess userAccess)) {
                return null;
            }
            Event event = userAccess.getEvent();
            String text = event != null ? "Event: " + event.getName() : I18n.getI18nText(EntireOrganization);
            boolean isOrg = event == null;
            Label badge = isOrg
                ? ModalityStyle.badgeLightPurple(new Label(text))
                : ModalityStyle.badgeLightWarning(new Label(text));
            badge.setPadding(new Insets(4, 12, 4, 12));
            return badge;
        });

        ValueRendererRegistry.registerValueRenderer("accessType", (value, context) -> {
            if (value == null) {
                return null;
            }
            boolean isReadOnly;
            if (value instanceof Boolean) {
                isReadOnly = (Boolean) value;
            } else if (value instanceof AuthorizationOrganizationUserAccess userAccess) {
                Boolean readOnlyValue = userAccess.isReadOnly();
                isReadOnly = readOnlyValue != null && readOnlyValue;
            } else {
                return null;
            }

            String text = isReadOnly ? I18n.getI18nText(ReadOnly) : I18n.getI18nText(ReadAndWrite);
            Label badge = isReadOnly
                ? ModalityStyle.badgeGray(new Label(text))
                : ModalityStyle.badgeLightInfo(new Label(text));
            badge.setPadding(new Insets(4, 12, 4, 12));
            return badge;
        });

    }
}
