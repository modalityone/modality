package one.modality.crm.backoffice.activities.admin;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.AuthorizationRole;
import one.modality.base.shared.entities.AuthorizationRule;

import java.util.List;

/**
 * Custom renderers for the Authorization Rules view.
 *
 * @author Claude Code
 */
final class AuthorizationRulesRenderers {

    private static AuthorizationRulesView authorizationRulesView;

    static void setAuthorizationRulesView(AuthorizationRulesView view) {
        authorizationRulesView = view;
    }

    static void registerRenderers() {
        // Register common renderers shared with other admin views
        AdminRenderers.registerCommonRenderers();

        // Register the "Used In" renderer
        ValueRendererRegistry.registerValueRenderer("ruleUsedIn", (value, context) -> {
            if (!(value instanceof AuthorizationRule)) {
                return null;
            }
            AuthorizationRule rule = (AuthorizationRule) value;

            if (authorizationRulesView == null) {
                return new Label("-");
            }

            List<AuthorizationRole> roles = authorizationRulesView.getRolesForRule(rule);

            if (roles.isEmpty()) {
                return new Label("-");
            }

            FlowPane flow = new FlowPane();
            flow.setHgap(8);
            flow.setVgap(8);
            flow.setAlignment(Pos.CENTER_LEFT);

            for (AuthorizationRole role : roles) {
                Label roleChip = ModalityStyle.badgeRole(new Label(role.getName()));
                roleChip.setPadding(new Insets(3, 8, 3, 8));
                flow.getChildren().add(roleChip);
            }

            return flow;
        });

        // Register the actions renderer
        ValueRendererRegistry.registerValueRenderer("ruleActions", (value, context) -> {
            if (!(value instanceof AuthorizationRule)) {
                return null;
            }
            AuthorizationRule rule = (AuthorizationRule) value;

            HBox actionsBox = new HBox(12);
            actionsBox.setAlignment(Pos.CENTER);

            // Edit button with SVG icon
            SVGPath editIcon = SvgIcons.createEditPath();
            editIcon.setFill(Color.web("#666666"));
            editIcon.getStyleClass().add("admin-action-icon");
            MonoPane editButton = SvgIcons.createButtonPane(editIcon, () -> {
                if (authorizationRulesView != null) {
                    authorizationRulesView.showEditDialog(rule);
                }
            });
            editButton.getStyleClass().add("admin-action-button");

            // Delete button with SVG icon
            SVGPath deleteIcon = SvgIcons.createTrashSVGPath();
            deleteIcon.setFill(Color.web("#dc3545"));
            deleteIcon.getStyleClass().add("admin-action-icon");
            MonoPane deleteButton = SvgIcons.createButtonPane(deleteIcon, () -> {
                if (authorizationRulesView != null) {
                    authorizationRulesView.showDeleteDialog(rule);
                }
            });
            deleteButton.getStyleClass().add("admin-action-button");

            actionsBox.getChildren().addAll(editButton, deleteButton);
            return actionsBox;
        });
    }
}
