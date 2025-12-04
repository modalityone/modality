package one.modality.crm.backoffice.activities.operationsandroles;

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
import one.modality.base.shared.entities.AuthorizationRole;
import one.modality.base.shared.entities.AuthorizationRoleOperation;
import one.modality.base.shared.entities.AuthorizationRule;

import java.util.List;

/**
 * Custom renderers for the Roles view.
 *
 * @author Claude Code
 */
final class RolesRenderers {

    private static RolesView rolesView;

    static void setRolesView(RolesView view) {
        rolesView = view;
    }

    static void registerRenderers() {
        // Register common renderers
        RouteRenderers.registerRouteRenderers();

        // Register permissions list renderer
        ValueRendererRegistry.registerValueRenderer("permissionsList", (value, context) -> {
            AuthorizationRole role = (AuthorizationRole) value;

            HBox flow = new HBox(4);
            flow.setAlignment(Pos.CENTER_LEFT);

            if (rolesView != null) {
                // Get rules and role operations for this role from the view's feeds
                List<AuthorizationRule> rules = rolesView.getRulesForRole(role);
                List<AuthorizationRoleOperation> roleOperations = rolesView.getRoleOperationsForRole(role);

                boolean hasRules = rules != null && !rules.isEmpty();
                boolean hasOperations = roleOperations != null && !roleOperations.isEmpty();

                if (!hasRules && !hasOperations) {
                    Label emptyLabel = new Label("-");
                    emptyLabel.getStyleClass().add("admin-text-italic");
                    flow.getChildren().add(emptyLabel);
                } else {
                    // First add Rules (on top)
                    if (hasRules) {
                        rules.forEach(rule -> {
                            Label ruleChip = ModalityStyle.badgeLightWarning(new Label(rule.getName()));
                            ruleChip.setPadding(new Insets(3, 8, 3, 8));
                            flow.getChildren().add(ruleChip);
                        });
                    }

                    // Then add OperationGroups
                    if (hasOperations) {
                        roleOperations.stream()
                            .filter(ro -> ro.getOperationGroup() != null)
                            .forEach(ro -> {
                                Label groupChip = ModalityStyle.badgeLightDanger(new Label(ro.getOperationGroup().getName()));
                                groupChip.setPadding(new Insets(3, 8, 3, 8));
                                flow.getChildren().add(groupChip);
                            });

                        // Finally add individual Operations
                        roleOperations.stream()
                            .filter(ro -> ro.getOperation() != null)
                            .forEach(ro -> {
                                Label operationChip = ModalityStyle.badgeLightSuccess(new Label(ro.getOperation().getName()));
                                operationChip.setPadding(new Insets(3, 8, 3, 8));
                                flow.getChildren().add(operationChip);
                            });
                    }
                }
            }

            return flow;
        });

        // Register used by count renderer
        ValueRendererRegistry.registerValueRenderer("usedByCount", (value, context) -> {
            if (!(value instanceof AuthorizationRole role)) {
                return new Label("-");
            }

            if (rolesView == null) {
                return new Label("-");
            }

            int userCount = rolesView.getUserCountForRole(role);
            if (userCount == 0) {
                Label label = new Label("-");
                label.getStyleClass().add("table-cell-text-secondary");
                return label;
            }

            Label badge = ModalityStyle.badgeLightInfo(new Label(String.valueOf(userCount)));
            badge.setPadding(new Insets(3, 8, 3, 8));
            return badge;
        });

        // Register role actions renderer
        ValueRendererRegistry.registerValueRenderer("roleActions", (value, context) -> {
            AuthorizationRole role = (AuthorizationRole) value;

            HBox actionsBox = new HBox(8);
            actionsBox.setAlignment(Pos.CENTER);

            // Edit button with SVG icon
            SVGPath editIcon = SvgIcons.createEditPath();
            editIcon.setFill(Color.web("#666666"));
            editIcon.getStyleClass().add("admin-action-icon");
            MonoPane editButton = SvgIcons.createButtonPane(editIcon, () -> {
                if (rolesView != null) {
                    rolesView.showEditDialog(role);
                }
            });
            editButton.getStyleClass().add("admin-action-button");

            // Duplicate button with SVG icon
            SVGPath duplicateIcon = SvgIcons.createDuplicatePath();
            duplicateIcon.setFill(Color.web("#666666"));
            duplicateIcon.getStyleClass().add("admin-action-icon");
            MonoPane duplicateButton = SvgIcons.createButtonPane(duplicateIcon, () -> {
                if (rolesView != null) {
                    rolesView.showDuplicateDialog(role);
                }
            });
            duplicateButton.getStyleClass().add("admin-action-button");

            // Delete button with SVG icon
            SVGPath deleteIcon = SvgIcons.createTrashSVGPath();
            deleteIcon.setFill(Color.web("#dc3545"));
            deleteIcon.getStyleClass().add("admin-action-icon");
            MonoPane deleteButton = SvgIcons.createButtonPane(deleteIcon, () -> {
                if (rolesView != null) {
                    rolesView.showDeleteDialog(role);
                }
            });
            deleteButton.getStyleClass().add("admin-action-button");

            actionsBox.getChildren().addAll(editButton, duplicateButton, deleteButton);
            return actionsBox;
        });
    }
}
