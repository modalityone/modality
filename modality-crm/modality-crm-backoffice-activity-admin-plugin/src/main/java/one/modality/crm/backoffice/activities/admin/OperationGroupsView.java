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
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.reactive.dql.query.ReactiveDqlQuery;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.EntitiesToVisualResultMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.VisualEntityColumnFactory;
import javafx.scene.layout.*;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import one.modality.base.shared.entities.AuthorizationRoleOperation;
import one.modality.base.shared.entities.Operation;
import one.modality.base.shared.entities.OperationGroup;

import java.util.*;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.where;
import static one.modality.crm.backoffice.activities.admin.Admin18nKeys.*;

/**
 * Operation Groups tab view.
 *
 * @author Claude Code
 */
public class OperationGroupsView {

    private final VBox view;
    private final VisualGrid groupsGrid;
    private TextField searchField;
    private ReactiveEntitiesMapper<Operation> reactiveEntitiesMapper;
    private ReactiveEntitiesMapper<AuthorizationRoleOperation> roleOperationsMapper;
    private final ObservableList<Operation> operationsFeed = FXCollections.observableArrayList();
    private final ObservableList<AuthorizationRoleOperation> roleOperationsFeed = FXCollections.observableArrayList();
    private final ObservableList<OperationGroup> displayedGroups = FXCollections.observableArrayList();
    private EntityColumn<OperationGroup>[] columns;

    static {
        // Register custom renderers
        OperationGroupsRenderers.registerRenderers();
    }

    public OperationGroupsView() {
        view = new VBox();
        view.setSpacing(16);
        view.setAlignment(Pos.TOP_LEFT);
        view.setFillWidth(true);
        groupsGrid = VisualGrid.createVisualGridWithTableLayoutSkin();
        groupsGrid.setMinRowHeight(40);
        groupsGrid.setPrefRowHeight(40);
        groupsGrid.setPrefHeight(600);
        groupsGrid.setMaxHeight(Double.MAX_VALUE);
        groupsGrid.setMinWidth(0);
        groupsGrid.setPrefWidth(Double.MAX_VALUE);
        groupsGrid.setMaxWidth(Double.MAX_VALUE);

        // Pass this view to renderers
        OperationGroupsRenderers.setOperationGroupsView(this);

        // Info box - outside card
        javafx.scene.control.Label infoBox = Bootstrap.infoBox(I18nControls.newLabel(OperationGroupsInfoBox));
        infoBox.setWrapText(true);
        infoBox.setMaxWidth(Double.MAX_VALUE);

        // Legend
        HBox legend = createLegend();

        // Card container
        VBox card = new VBox(16);
        card.getStyleClass().add("section-card");

        // Section title
        javafx.scene.control.Label sectionTitle = I18nControls.newLabel(OperationGroupsSectionTitle);
        sectionTitle.getStyleClass().add("section-title");

        // Header with search and create button
        HBox header = createHeader();

        card.getChildren().addAll(sectionTitle, header, groupsGrid);
        VBox.setVgrow(groupsGrid, Priority.ALWAYS);

        view.getChildren().addAll(infoBox, legend, card);
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
        I18n.bindI18nTextProperty(searchField.promptTextProperty(), SearchGroups);
        searchField.setPrefWidth(300);
        searchField.setPadding(new Insets(8, 35, 8, 12));

        Label searchIcon = new Label("🔍");
        searchIcon.setMouseTransparent(true);

        StackPane searchContainer = new StackPane();
        searchContainer.getChildren().addAll(searchField, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(searchIcon, new Insets(0, 12, 0, 0));

        Button createButton = Bootstrap.successButton(I18nControls.newButton(CreateGroup));
        createButton.setOnAction(e -> showCreateDialog());

        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        header.getChildren().addAll(searchContainer, createButton);

        return header;
    }

    private HBox createLegend() {
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setPadding(new Insets(12, 0, 0, 0));

        Label legendLabel = I18nControls.newLabel(Legend);
        legendLabel.getStyleClass().add("admin-legend-label");

        // Operation badge sample
        Label operationSample = ModalityStyle.badgeOperation(new Label("Operation"));
        operationSample.setPadding(new Insets(3, 8, 3, 8));

        // Operation Group badge sample
        Label groupSample = ModalityStyle.badgeOperationGroup(new Label("Operation Group"));
        groupSample.setPadding(new Insets(3, 8, 3, 8));

        // Role badge sample
        Label roleSample = ModalityStyle.badgeRole(new Label("Role"));
        roleSample.setPadding(new Insets(3, 8, 3, 8));

        legend.getChildren().addAll(legendLabel, operationSample, groupSample, roleSample);
        return legend;
    }

    private void updateDisplayedGroups() {
        String searchText = searchField != null && searchField.getText() != null
            ? searchField.getText().toLowerCase().trim()
            : "";

        // Group operations by their OperationGroup
        Map<OperationGroup, List<Operation>> operationsByGroup = operationsFeed.stream()
            .filter(op -> op.getGroup() != null)
            .collect(Collectors.groupingBy(Operation::getGroup));

        // Get all groups and sort by name
        List<OperationGroup> groups = new ArrayList<>(operationsByGroup.keySet());

        // Apply search filter
        if (!searchText.isEmpty()) {
            groups = groups.stream()
                .filter(group -> group.getName().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        }

        groups.sort(Comparator.comparing(OperationGroup::getName));

        // Update displayed groups
        displayedGroups.setAll(groups);
    }


    private void startLogic() {
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();

        // Define columns for the grid
        columns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
            """
            [
                {expression: 'name', label: 'Group Name', minWidth: 150},
                {expression: 'this', label: 'Operations', renderer: 'operationsList', minWidth: 400},
                {expression: 'this', label: 'Used in Roles', renderer: 'usedInRoles', minWidth: 100, prefWidth: 120, hShrink: false, textAlign: 'center'},
                {expression: 'this', label: 'Actions', renderer: 'groupActions', minWidth: 100, prefWidth: 120, hShrink: false, textAlign: 'center'}
            ]""", dataSourceModel.getDomainModel(), "OperationGroup");

        // Query all operations that have a group, ordered by group then name
        reactiveEntitiesMapper = ReactiveEntitiesMapper.<Operation>createPushReactiveChain()
            .setDataSourceModel(dataSourceModel)
            .always("{class: 'Operation', alias: 'o', fields: 'name,group.name', orderBy: 'group.name,name'}")
            .always(where("group is not null"))
            .storeEntitiesInto(operationsFeed)
            .start();

        // Query all role operations to show which roles use which operation groups
        roleOperationsMapper = ReactiveEntitiesMapper.<AuthorizationRoleOperation>createPushReactiveChain()
            .setDataSourceModel(dataSourceModel)
            .always("{class: 'AuthorizationRoleOperation', alias: 'ro', fields: 'role.name,operationGroup', orderBy: 'role,id'}")
            .storeEntitiesInto(roleOperationsFeed)
            .start();

        // Update displayed groups when operations feed or search text changes
        FXProperties.runNowAndOnPropertiesChange(
            this::updateDisplayedGroups,
            ObservableLists.versionNumber(operationsFeed),
            searchField.textProperty()
        );

        // Update grid when displayed groups change or when role operations change
        Runnable updateGrid = () -> {
            VisualResult vr = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(displayedGroups, columns);
            groupsGrid.setVisualResult(vr);
        };

        // Map displayed groups to visual result for the grid
        ObservableLists.runNowAndOnListChange(change -> updateGrid.run(), displayedGroups);

        // Also update grid when role operations change (to refresh "Used in Roles" column)
        ObservableLists.runNowAndOnListChange(change -> updateGrid.run(), roleOperationsFeed);
    }

    /**
     * Helper method for renderers to get operations for a specific group.
     */
    List<Operation> getOperationsForGroup(OperationGroup group) {
        return operationsFeed.stream()
            .filter(op -> op.getGroup() != null && op.getGroup().getPrimaryKey().equals(group.getPrimaryKey()))
            .collect(Collectors.toList());
    }

    /**
     * Helper method for renderers to get role operations for a specific operation group.
     */
    List<AuthorizationRoleOperation> getRoleOperationsForGroup(OperationGroup group) {
        return roleOperationsFeed.stream()
            .filter(ro -> ro.getOperationGroup() != null && ro.getOperationGroup().getPrimaryKey().equals(group.getPrimaryKey()))
            .collect(Collectors.toList());
    }

    private void showCreateDialog() {
        OperationGroupDialog.show(null, getEntityStore(), this::refresh);
    }

    void showEditDialog(OperationGroup group) {
        OperationGroupDialog.show(group, getEntityStore(), this::refresh);
    }

    void showDeleteDialog(OperationGroup group) {
        // Create dialog content
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        // Title
        Label titleLabel = I18nControls.newLabel(Admin18nKeys.DeleteGroup);
        titleLabel.getStyleClass().add("delete-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Message
        Label messageLabel = new Label(I18n.getI18nText(Admin18nKeys.Delete) + I18n.getI18nText(Space) + group.getName() + I18n.getI18nText(QuestionMark));
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.getStyleClass().add("delete-dialog-message");

        // Confirmation text
        Label confirmLabel = I18nControls.newLabel(Admin18nKeys.DeleteGroupConfirm);
        confirmLabel.setWrapText(true);
        confirmLabel.setMaxWidth(Double.MAX_VALUE);
        confirmLabel.getStyleClass().add("delete-dialog-confirm");

        dialogContent.getChildren().addAll(titleLabel, messageLabel, confirmLabel);

        // Buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = Bootstrap.button(I18nControls.newButton(Admin18nKeys.Cancel));
        Button deleteButton = Bootstrap.dangerButton(I18nControls.newButton(Admin18nKeys.Delete));

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
                UpdateStore updateStore = UpdateStore.createAbove(store);

                // First, get all operations that belong to this group and unassign them
                store.<Operation>executeQuery("select id from Operation where group=?", group.getPrimaryKey())
                    .onSuccess(operations -> {
                        // Unassign all operations from this group
                        for (Operation operation : operations) {
                            Operation operationToUpdate = updateStore.updateEntity(operation);
                            operationToUpdate.setGroup(null);
                        }

                        // Delete the group
                        updateStore.deleteEntity(group);

                        // Submit all changes
                        updateStore.submitChanges()
                            .onSuccess(result -> {
                                dialogCallback.closeDialog();
                                refresh();
                            })
                            .onFailure(error -> {
                                // Show error dialog if deletion fails
                                showErrorDialog(error.getMessage());
                            });
                    });
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

        // Title
        Label titleLabel = I18nControls.newLabel(Admin18nKeys.Error);
        titleLabel.getStyleClass().add("error-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Header
        Label headerLabel = I18nControls.newLabel(Admin18nKeys.FailedToSaveGroup);
        headerLabel.setWrapText(true);
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.getStyleClass().add("error-dialog-header");

        // Content
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.getStyleClass().add("error-dialog-content");

        dialogContent.getChildren().addAll(titleLabel, headerLabel, contentLabel);

        // OK Button
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button okButton = Bootstrap.dangerButton(I18nControls.newButton(Admin18nKeys.OK));

        footer.getChildren().add(okButton);
        dialogContent.getChildren().add(footer);

        // Show dialog
        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        // Button action
        okButton.setOnAction(e -> dialogCallback.closeDialog());
    }

    public EntityStore getEntityStore() {
        return reactiveEntitiesMapper != null ? reactiveEntitiesMapper.getStore() : null;
    }

    public void refresh() {
        if (reactiveEntitiesMapper != null) {
            reactiveEntitiesMapper.refreshWhenActive();
        }
        if (roleOperationsMapper != null) {
            roleOperationsMapper.refreshWhenActive();
        }
    }

    public void setActive(boolean active) {
        if (reactiveEntitiesMapper != null) {
            ReactiveDqlQuery<Operation> reactiveDqlQuery = reactiveEntitiesMapper.getReactiveDqlQuery();
            reactiveDqlQuery.setActive(active);
        }
        if (roleOperationsMapper != null) {
            ReactiveDqlQuery<AuthorizationRoleOperation> roleOperationsQuery = roleOperationsMapper.getReactiveDqlQuery();
            roleOperationsQuery.setActive(active);
        }
    }
}
