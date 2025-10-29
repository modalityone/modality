package one.modality.crm.backoffice.activities.admin;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.visual.VisualResult;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.reactive.dql.query.ReactiveDqlQuery;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.EntitiesToVisualResultMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.VisualEntityColumnFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.AuthorizationRole;
import one.modality.base.shared.entities.AuthorizationRoleOperation;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static one.modality.crm.backoffice.activities.admin.Admin18nKeys.*;

/**
 * Roles tab view.
 *
 * @author Claude Code
 */
public class RolesView {

    private final VBox view;
    private final VisualGrid rolesGrid;
    private TextField searchField;
    private ReactiveEntitiesMapper<AuthorizationRole> rolesMapper;
    private ReactiveEntitiesMapper<AuthorizationRoleOperation> roleOperationsMapper;
    private final ObservableList<AuthorizationRole> rolesFeed = FXCollections.observableArrayList();
    private final ObservableList<AuthorizationRoleOperation> roleOperationsFeed = FXCollections.observableArrayList();
    private final ObservableList<AuthorizationRole> displayedRoles = FXCollections.observableArrayList();
    private EntityColumn<AuthorizationRole>[] columns;

    static {
        // Register custom renderers
        RolesRenderers.registerRenderers();
    }

    public RolesView() {
        view = new VBox();
        view.setSpacing(16);
        view.setAlignment(Pos.TOP_LEFT);
        view.setFillWidth(true);
        rolesGrid = VisualGrid.createVisualGridWithTableLayoutSkin();
        rolesGrid.setMinRowHeight(40);
        rolesGrid.setPrefRowHeight(40);
        rolesGrid.setPrefHeight(600);
        rolesGrid.setMaxHeight(Double.MAX_VALUE);
        rolesGrid.setMinWidth(0);
        rolesGrid.setPrefWidth(Double.MAX_VALUE);
        rolesGrid.setMaxWidth(Double.MAX_VALUE);

        // Pass this view to renderers
        RolesRenderers.setRolesView(this);

        // Info box - outside card
        Label infoBox = Bootstrap.infoBox(I18nControls.newLabel(RolesInfoBox));
        infoBox.setWrapText(true);
        infoBox.setMaxWidth(Double.MAX_VALUE);

        // Card container
        VBox card = new VBox(16);
        card.getStyleClass().add("section-card");

        // Section title
        Label sectionTitle = I18nControls.newLabel(RolesSectionTitle);
        sectionTitle.getStyleClass().add("section-title");

        // Header with search and create button
        HBox header = createHeader();

        card.getChildren().addAll(sectionTitle, header, rolesGrid);
        VBox.setVgrow(rolesGrid, Priority.ALWAYS);

        view.getChildren().addAll(infoBox, card);
        VBox.setVgrow(card, Priority.ALWAYS);

        // Initialize ReactiveEntitiesMapper and setup logic
        startLogic();
    }

    public Node getView() {
        return view;
    }

    private HBox createHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Create search field with icon
        searchField = new TextField();
        I18n.bindI18nTextProperty(searchField.promptTextProperty(), SearchRoles);
        searchField.setPrefWidth(300);
        searchField.setPadding(new Insets(8, 35, 8, 12));

        Label searchIcon = new Label("🔍");
        searchIcon.setMouseTransparent(true);

        StackPane searchContainer = new StackPane();
        searchContainer.getChildren().addAll(searchField, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(searchIcon, new Insets(0, 12, 0, 0));

        Button createButton = Bootstrap.successButton(I18nControls.newButton(CreateRole));
        createButton.setOnAction(e -> showCreateDialog());

        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        header.getChildren().addAll(searchContainer, createButton);

        return header;
    }

    private void updateDisplayedRoles() {
        String searchText = searchField != null && searchField.getText() != null
            ? searchField.getText().toLowerCase().trim()
            : "";

        // Get all roles and sort by name
        List<AuthorizationRole> roles = rolesFeed.stream()
            .sorted(Comparator.comparing(AuthorizationRole::getName))
            .collect(Collectors.toList());

        // Apply search filter
        if (!searchText.isEmpty()) {
            roles = roles.stream()
                .filter(role -> role.getName().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        }

        // Update displayed roles
        displayedRoles.setAll(roles);
    }

    private void startLogic() {
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();

        // Define columns for the grid
        columns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
            """
            [
                {expression: 'name', label: 'Role Name', minWidth: 150},
                {expression: 'this', label: 'Permissions', renderer: 'permissionsList', minWidth: 400},
                {expression: 'this', label: 'Used By', renderer: 'usedByCount', minWidth: 80, prefWidth: 100, hShrink: false, textAlign: 'center'},
                {expression: 'this', label: 'Actions', renderer: 'roleActions', minWidth: 120, prefWidth: 140, hShrink: false, textAlign: 'center'}
            ]""", dataSourceModel.getDomainModel(), "AuthorizationRole");

        // Query all roles
        rolesMapper = ReactiveEntitiesMapper.<AuthorizationRole>createPushReactiveChain()
            .setDataSourceModel(dataSourceModel)
            .always("{class: 'AuthorizationRole', alias: 'r', fields: 'id,name', orderBy: 'name'}")
            .storeEntitiesInto(rolesFeed)
            .start();

        // Query all role operations
        roleOperationsMapper = ReactiveEntitiesMapper.<AuthorizationRoleOperation>createPushReactiveChain()
            .setDataSourceModel(dataSourceModel)
            .always("{class: 'AuthorizationRoleOperation', alias: 'ro', fields: 'role,operation.name,operationGroup.name,date', orderBy: 'role,id'}")
            .storeEntitiesInto(roleOperationsFeed)
            .start();

        // Debug: Monitor roleOperationsFeed changes
        ObservableLists.runNowAndOnListChange(change -> {
            System.out.println("roleOperationsFeed changed! New size: " + roleOperationsFeed.size());
            if (!roleOperationsFeed.isEmpty()) {
                AuthorizationRoleOperation first = roleOperationsFeed.get(0);
                System.out.println("  First item: role=" + (first.getRole() != null ? first.getRole().getPrimaryKey() : "null") +
                    ", operation=" + (first.getOperation() != null ? first.getOperation().getName() : "null") +
                    ", group=" + (first.getOperationGroup() != null ? first.getOperationGroup().getName() : "null"));
            }
        }, roleOperationsFeed);

        // Update displayed roles when roles feed or search text changes
        FXProperties.runNowAndOnPropertiesChange(
            this::updateDisplayedRoles,
            ObservableLists.versionNumber(rolesFeed),
            searchField.textProperty()
        );

        // Update grid when displayed roles change or when role operations change
        Runnable updateGrid = () -> {
            VisualResult vr = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(displayedRoles, columns);
            rolesGrid.setVisualResult(vr);
        };

        // Map displayed roles to visual result for the grid
        ObservableLists.runNowAndOnListChange(change -> updateGrid.run(), displayedRoles);

        // Also update grid when role operations change (to refresh permissions column)
        ObservableLists.runNowAndOnListChange(change -> updateGrid.run(), roleOperationsFeed);
    }

    /**
     * Helper method for renderers to get role operations for a specific role.
     */
    List<AuthorizationRoleOperation> getRoleOperationsForRole(AuthorizationRole role) {
        System.out.println("getRoleOperationsForRole called for role: " + role.getName() + " (id=" + role.getPrimaryKey() + ")");
        System.out.println("Total roleOperationsFeed size: " + roleOperationsFeed.size());

        List<AuthorizationRoleOperation> result = roleOperationsFeed.stream()
            .filter(ro -> {
                boolean hasRole = ro.getRole() != null;
                if (hasRole) {
                    Object rolePk = ro.getRole().getPrimaryKey();
                    boolean matches = rolePk != null && rolePk.equals(role.getPrimaryKey());
                    System.out.println("  RoleOp: role=" + ro.getRole().getPrimaryKey() +
                        ", operation=" + (ro.getOperation() != null ? ro.getOperation().getName() : "null") +
                        ", group=" + (ro.getOperationGroup() != null ? ro.getOperationGroup().getName() : "null") +
                        ", matches=" + matches);
                    return matches;
                }
                return false;
            })
            .collect(Collectors.toList());

        System.out.println("Returning " + result.size() + " role operations");
        return result;
    }

    private void showCreateDialog() {
        RolesDialog.show(null, getEntityStore(), this::refresh);
    }

    void showEditDialog(AuthorizationRole role) {
        RolesDialog.show(role, getEntityStore(), this::refresh, false);
    }

    void showDuplicateDialog(AuthorizationRole role) {
        RolesDialog.show(role, getEntityStore(), this::refresh, true);
    }

    void showDeleteDialog(AuthorizationRole role) {
        // Create dialog content
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        // Title
        Label titleLabel = I18nControls.newLabel(DeleteRole);
        titleLabel.getStyleClass().add("delete-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Message
        Label messageLabel = new Label(I18n.getI18nText(Delete) + " " + role.getName() + "?");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.getStyleClass().add("delete-dialog-message");

        // Confirmation text
        Label confirmLabel = I18nControls.newLabel(DeleteRoleConfirm);
        confirmLabel.setWrapText(true);
        confirmLabel.setMaxWidth(Double.MAX_VALUE);
        confirmLabel.getStyleClass().add("delete-dialog-confirm");

        dialogContent.getChildren().addAll(titleLabel, messageLabel, confirmLabel);

        // Buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = Bootstrap.button(I18nControls.newButton(Cancel));
        Button deleteButton = Bootstrap.dangerButton(I18nControls.newButton(Delete));

        footer.getChildren().addAll(cancelButton, deleteButton);
        dialogContent.getChildren().add(footer);

        // Show dialog
        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        // Button actions
        cancelButton.setOnAction(e -> dialogCallback.closeDialog());
        deleteButton.setOnAction(e -> {
            EntityStore store = getEntityStore();
            if (store != null) {
                // Create first UpdateStore for deleting role operations
                UpdateStore updateStoreForRoleOps = UpdateStore.createAbove(store);

                // First, get all role operations that belong to this role and delete them
                updateStoreForRoleOps.<AuthorizationRoleOperation>executeQuery("select id from AuthorizationRoleOperation where role=?", role.getPrimaryKey())
                    .onSuccess(roleOperations -> {
                        // Delete all role operations first (foreign key constraint)
                        for (AuthorizationRoleOperation roleOp : roleOperations) {
                            updateStoreForRoleOps.deleteEntity(roleOp);
                        }

                        // Submit role operations deletion first
                        updateStoreForRoleOps.submitChanges()
                            .onSuccess(result1 -> {
                                // Now delete the role in a separate transaction
                                UpdateStore updateStoreForRole = UpdateStore.createAbove(store);
                                updateStoreForRole.deleteEntity(role);

                                updateStoreForRole.submitChanges()
                                    .onSuccess(result2 -> Platform.runLater(() -> {
                                        dialogCallback.closeDialog();
                                        refresh();
                                    }))
                                    .onFailure(error -> Platform.runLater(() -> {
                                        dialogCallback.closeDialog();
                                        showErrorDialog(error.getMessage());
                                    }));
                            })
                            .onFailure(error -> Platform.runLater(() -> {
                                dialogCallback.closeDialog();
                                showErrorDialog(error.getMessage());
                            }));
                    })
                    .onFailure(error -> Platform.runLater(() -> {
                        dialogCallback.closeDialog();
                        showErrorDialog(error.getMessage());
                    }));
            }
        });
    }

    /**
     * Shows an error dialog with the specified content.
     */
    private void showErrorDialog(String content) {
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        Label titleLabel = I18nControls.newLabel(Error);
        titleLabel.getStyleClass().add("error-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label headerLabel = I18nControls.newLabel(FailedToSaveRole);
        headerLabel.setWrapText(true);
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.getStyleClass().add("error-dialog-header");

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.getStyleClass().add("error-dialog-content");

        dialogContent.getChildren().addAll(titleLabel, headerLabel, contentLabel);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button okButton = Bootstrap.dangerButton(I18nControls.newButton(OK));

        footer.getChildren().add(okButton);
        dialogContent.getChildren().add(footer);

        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        okButton.setOnAction(e -> dialogCallback.closeDialog());
    }

    public EntityStore getEntityStore() {
        return rolesMapper != null ? rolesMapper.getStore() : null;
    }

    public void refresh() {
        if (rolesMapper != null) {
            rolesMapper.refreshWhenActive();
        }
        if (roleOperationsMapper != null) {
            roleOperationsMapper.refreshWhenActive();
        }
    }

    public void setActive(boolean active) {
        if (rolesMapper != null) {
            ReactiveDqlQuery<AuthorizationRole> reactiveDqlQuery = rolesMapper.getReactiveDqlQuery();
            reactiveDqlQuery.setActive(active);
        }
        if (roleOperationsMapper != null) {
            ReactiveDqlQuery<AuthorizationRoleOperation> reactiveDqlQuery = roleOperationsMapper.getReactiveDqlQuery();
            reactiveDqlQuery.setActive(active);
        }
    }
}
