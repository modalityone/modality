package one.modality.crm.backoffice.activities.admin;

import dev.webfx.extras.cell.renderer.ValueRendererRegistry;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.AuthorizationRole;
import one.modality.base.shared.entities.AuthorizationRoleOperation;
import one.modality.base.shared.entities.Operation;
import one.modality.base.shared.entities.OperationGroup;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom renderers for the Operations/Routes views.
 *
 * @author Claude Code
 */
final class OperationsRenderers {

    private static OperationsView operationsView;

    static void setOperationsView(OperationsView view) {
        operationsView = view;
    }

    static void registerRenderers() {
        // Register common renderers shared with other admin views
        AdminRenderers.registerCommonRenderers();

        // Register the "Used In" renderer showing groups and roles
        ValueRendererRegistry.registerValueRenderer("operationUsedIn", (value, context) -> {
            Operation operation = (Operation) value;

            HBox flow = new HBox(4);
            flow.setAlignment(Pos.CENTER_LEFT);

            // Check if operation belongs to a group
            OperationGroup group = operation.getGroup();

            // Get roles that directly use this operation
            List<AuthorizationRoleOperation> roleOperations = null;
            if (operationsView != null) {
                roleOperations = operationsView.getRoleOperationsForOperation(operation);
            }

            boolean hasGroup = group != null;
            boolean hasRoles = roleOperations != null && !roleOperations.isEmpty();

            if (!hasGroup && !hasRoles) {
                Label emptyLabel = new Label("-");
                emptyLabel.getStyleClass().add("admin-text-italic");
                flow.getChildren().add(emptyLabel);
            } else {
                // Add group chip (yellow)
                if (hasGroup) {
                    Label groupChip = Bootstrap.badgeLightWarning(new Label(group.getName()));
                    groupChip.setPadding(new Insets(3, 8, 3, 8));
                    flow.getChildren().add(groupChip);
                }

                // Add role chips (green) - get unique roles
                if (hasRoles) {
                    Set<AuthorizationRole> uniqueRoles = roleOperations.stream()
                        .map(AuthorizationRoleOperation::getRole)
                        .filter(role -> role != null)
                        .collect(Collectors.toSet());

                    for (AuthorizationRole role : uniqueRoles) {
                        Label roleChip = Bootstrap.badgeLightSuccess(new Label(role.getName()));
                        roleChip.setPadding(new Insets(3, 8, 3, 8));
                        flow.getChildren().add(roleChip);
                    }
                }
            }

            return flow;
        });

        // Register the actions renderer (works for both operations and routes)
        ValueRendererRegistry.registerValueRenderer("operationActions", (value, context) -> {
            Operation operation = (Operation) value;

            HBox actionsBox = new HBox(12);
            actionsBox.setAlignment(Pos.CENTER);

            // Edit button with SVG icon
            SVGPath editIcon = SvgIcons.createEditPath();
            editIcon.setFill(Color.web("#666666"));
            editIcon.getStyleClass().add("admin-action-icon");
            MonoPane editButton = SvgIcons.createButtonPane(editIcon, () -> {
                if (operationsView != null) {
                    operationsView.showEditDialog(operation);
                }
            });
            editButton.getStyleClass().add("admin-action-button");

            // Delete button with SVG icon
            SVGPath deleteIcon = SvgIcons.createTrashSVGPath();
            deleteIcon.setFill(Color.web("#dc3545"));
            deleteIcon.getStyleClass().add("admin-action-icon");
            MonoPane deleteButton = SvgIcons.createButtonPane(deleteIcon, () -> {
                if (operationsView != null) {
                    operationsView.showDeleteDialog(operation);
                }
            });
            deleteButton.getStyleClass().add("admin-action-button");

            actionsBox.getChildren().addAll(editButton, deleteButton);
            return actionsBox;
        });
    }
}
