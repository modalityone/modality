package one.modality.crm.backoffice.activities.superadmin;

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
import one.modality.base.shared.entities.Operation;
import one.modality.base.shared.entities.OperationGroup;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom renderers for the Operation Groups view.
 *
 * @author Claude Code
 */
final class OperationGroupsRenderers {

    private static OperationGroupsView operationGroupsView;

    static void setOperationGroupsView(OperationGroupsView view) {
        operationGroupsView = view;
    }

    static void registerRenderers() {
        // Register common renderers
        SuperAdminRenderers.registerCommonRenderers();

        // Register operations list renderer
        ValueRendererRegistry.registerValueRenderer("operationsList", (value, context) -> {
            OperationGroup group = (OperationGroup) value;

            HBox flow = new HBox(4);
            flow.setAlignment(Pos.CENTER_LEFT);

            // Get operations for this group from the view's operation feed
            if (operationGroupsView != null) {
                List<Operation> operations = operationGroupsView.getOperationsForGroup(group);

                if (operations == null || operations.isEmpty()) {
                    Label emptyLabel = new Label("-");
                    emptyLabel.getStyleClass().add("admin-text-italic");
                    flow.getChildren().add(emptyLabel);
                } else {
                    // Build the chips
                    for (Operation operation : operations) {
                        Label chip = ModalityStyle.badgeOperation(new Label(operation.getName()));
                        chip.setPadding(new Insets(3, 8, 3, 8));
                        flow.getChildren().add(chip);
                    }
                }
            }

            return flow;
        });

        // Register used in roles renderer
        ValueRendererRegistry.registerValueRenderer("usedInRoles", (value, context) -> {
            OperationGroup group = (OperationGroup) value;

            HBox flow = new HBox(4);
            flow.setAlignment(Pos.CENTER_LEFT);

            // Get role operations for this group from the view's feed
            if (operationGroupsView != null) {
                List<AuthorizationRoleOperation> roleOperations = operationGroupsView.getRoleOperationsForGroup(group);

                if (roleOperations == null || roleOperations.isEmpty()) {
                    Label emptyLabel = new Label("-");
                    emptyLabel.getStyleClass().add("admin-text-italic");
                    flow.getChildren().add(emptyLabel);
                } else {
                    // Get unique roles (in case multiple operations from this group are in the same role)
                    Set<AuthorizationRole> uniqueRoles = roleOperations.stream()
                        .map(AuthorizationRoleOperation::getRole)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                    // Build the chips
                    for (AuthorizationRole role : uniqueRoles) {
                        Label chip = ModalityStyle.badgeRole(new Label(role.getName()));
                        chip.setPadding(new Insets(3, 8, 3, 8));
                        flow.getChildren().add(chip);
                    }
                }
            }

            return flow;
        });

        // Register group actions renderer
        ValueRendererRegistry.registerValueRenderer("groupActions", (value, context) -> {
            OperationGroup group = (OperationGroup) value;

            HBox actionsBox = new HBox(8);
            actionsBox.setAlignment(Pos.CENTER);

            // Edit button with SVG icon
            SVGPath editIcon = SvgIcons.createEditPath();
            editIcon.setFill(Color.web("#666666"));
            editIcon.getStyleClass().add("admin-action-icon");
            MonoPane editButton = SvgIcons.createButtonPane(editIcon, () -> {
                if (operationGroupsView != null) {
                    operationGroupsView.showEditDialog(group);
                }
            });
            editButton.getStyleClass().add("admin-action-button");

            // Delete button with SVG icon
            SVGPath deleteIcon = SvgIcons.createTrashSVGPath();
            deleteIcon.setFill(Color.web("#dc3545"));
            deleteIcon.getStyleClass().add("admin-action-icon");
            MonoPane deleteButton = SvgIcons.createButtonPane(deleteIcon, () -> {
                if (operationGroupsView != null) {
                    operationGroupsView.showDeleteDialog(group);
                }
            });
            deleteButton.getStyleClass().add("admin-action-button");

            actionsBox.getChildren().addAll(editButton, deleteButton);
            return actionsBox;
        });
    }
}
