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
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.orm.reactive.entities.entities_to_grid.EntityColumn;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.EntitiesToVisualResultMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.VisualEntityColumnFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.AuthorizationRoleOperation;
import one.modality.base.shared.entities.Operation;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static dev.webfx.stack.orm.dql.DqlStatement.where;
import static one.modality.crm.backoffice.activities.admin.Admin18nKeys.*;

/**
 * Operations/Routes tab view for managing operations and routes (permissions).
 *
 * @author Claude Code
 */
public class OperationsView {

    private final boolean isRoutes;
    private EntityColumn<Operation>[] columns;

    private final VBox view;
    private final VisualGrid operationsGrid;
    private TextField searchField;
    private ReactiveEntitiesMapper<Operation> operationsMapper;
    private ReactiveEntitiesMapper<AuthorizationRoleOperation> roleOperationsMapper;
    private final ObservableList<Operation> operationsFeed = FXCollections.observableArrayList();
    private final ObservableList<AuthorizationRoleOperation> roleOperationsFeed = FXCollections.observableArrayList();
    private final ObservableList<Operation> displayedOperations = FXCollections.observableArrayList();

    static {
        // Register custom renderers
        OperationsRenderers.registerRenderers();
    }

    private UpdateStore updateStore;

    /**
     * Creates an Operations view (excludes routes).
     */
    public OperationsView() {
        this(false);
    }

    /**
     * Creates an Operations or Routes view.
     * @param isRoutes true for Routes view, false for Operations view
     */
    public OperationsView(boolean isRoutes) {
        this.isRoutes = isRoutes;

        view = new VBox();
        view.setSpacing(16);
        view.setAlignment(Pos.TOP_LEFT);
        view.setFillWidth(true);
        operationsGrid = VisualGrid.createVisualGridWithTableLayoutSkin();
        operationsGrid.setMinRowHeight(40);
        operationsGrid.setPrefRowHeight(40);
        operationsGrid.setPrefHeight(600);
        operationsGrid.setMaxHeight(Double.MAX_VALUE);
        operationsGrid.setMinWidth(0);
        operationsGrid.setPrefWidth(Double.MAX_VALUE);
        operationsGrid.setMaxWidth(Double.MAX_VALUE);

        // Pass this OperationsView instance to the renderers
        OperationsRenderers.setOperationsView(this);

        // Info box - outside card
        Object infoBoxKey = isRoutes ? RoutesInfoBox : OperationsInfoBox;
        Label infoBox = Bootstrap.infoBox(I18nControls.newLabel(infoBoxKey));
        infoBox.setWrapText(true);
        infoBox.setMaxWidth(Double.MAX_VALUE);

        // Card container - wraps content with white background
        VBox card = new VBox(16);
        card.getStyleClass().add("section-card");

        // Section title
        Object sectionTitleKey = isRoutes ? RoutesSectionTitle : OperationsSectionTitle;
        Label sectionTitle = I18nControls.newLabel(sectionTitleKey);
        sectionTitle.getStyleClass().add("section-title");

        // Header with search and create button
        HBox header = createHeader();

        card.getChildren().addAll(sectionTitle, header, operationsGrid);
        VBox.setVgrow(operationsGrid, Priority.ALWAYS);

        view.getChildren().addAll(infoBox, card);
        VBox.setVgrow(card, Priority.ALWAYS);

        // Initialize ReactiveVisualMapper
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
        Object searchKey = isRoutes ? SearchRoutes : SearchOperations;
        I18n.bindI18nTextProperty(searchField.promptTextProperty(), searchKey);
        searchField.setPrefWidth(300);
        searchField.setPadding(new Insets(8, 35, 8, 12));

        Label searchIcon = new Label("🔍");
        searchIcon.setMouseTransparent(true);

        StackPane searchContainer = new StackPane();
        searchContainer.getChildren().addAll(searchField, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(searchIcon, new Insets(0, 12, 0, 0));

        Object createKey = isRoutes ? CreateRoute : CreateOperation;
        Button createButton = Bootstrap.successButton(I18nControls.newButton(createKey));
        createButton.setOnAction(e -> showCreateDialog());

        HBox.setHgrow(searchContainer, Priority.ALWAYS);
        header.getChildren().addAll(searchContainer, createButton);

        return header;
    }

    private void startLogic() {
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        updateStore = UpdateStore.createAbove(EntityStore.create(dataSourceModel));

        // Define columns based on whether this is routes or operations
        if (isRoutes) {
            columns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
                """
                [
                    {expression: 'name', label: 'Name', minWidth: 150},
                    {expression: 'operationCode', label: 'Code', minWidth: 150},
                    {expression: 'grantRoute', label: 'Route Path', minWidth: 200},
                    {expression: 'backend', label: 'Backend', renderer: 'booleanCheck', minWidth: 80, prefWidth: 90, hShrink: false, textAlign: 'center'},
                    {expression: 'frontend', label: 'Frontend', renderer: 'booleanCheck', minWidth: 80, prefWidth: 90, hShrink: false, textAlign: 'center'},
                    {expression: 'guest', label: 'Guest', renderer: 'booleanCheck', minWidth: 70, prefWidth: 80, hShrink: false, textAlign: 'center'},
                    {expression: 'public', label: 'Public', renderer: 'booleanCheck', minWidth: 70, prefWidth: 80, hShrink: false, textAlign: 'center'},
                    {expression: 'this', label: 'Used In', renderer: 'operationUsedIn', minWidth: 400},
                    {expression: 'this', label: 'Actions', renderer: 'operationActions', minWidth: 100, prefWidth: 120, hShrink: false, textAlign: 'center'}
                ]""", dataSourceModel.getDomainModel(), "Operation");
        } else {
            columns = VisualEntityColumnFactory.get().fromJsonArray( // language=JSON5
                """
                [
                    {expression: 'name', label: 'Name', minWidth: 150},
                    {expression: 'operationCode', label: 'Code', minWidth: 150},
                    {expression: 'backend', label: 'Backend', renderer: 'booleanCheck', minWidth: 80, prefWidth: 90, hShrink: false, textAlign: 'center'},
                    {expression: 'frontend', label: 'Frontend', renderer: 'booleanCheck', minWidth: 80, prefWidth: 90, hShrink: false, textAlign: 'center'},
                    {expression: 'guest', label: 'Guest', renderer: 'booleanCheck', minWidth: 70, prefWidth: 80, hShrink: false, textAlign: 'center'},
                    {expression: 'public', label: 'Public', renderer: 'booleanCheck', minWidth: 70, prefWidth: 80, hShrink: false, textAlign: 'center'},
                    {expression: 'this', label: 'Used In', renderer: 'operationUsedIn', minWidth: 400},
                    {expression: 'this', label: 'Actions', renderer: 'operationActions', minWidth: 100, prefWidth: 120, hShrink: false, textAlign: 'center'}
                ]""", dataSourceModel.getDomainModel(), "Operation");
        }

        // Build the query based on whether this is routes or operations
        String query;
        String fields = "name,operationCode,grantRoute,backend,frontend,guest,public,group.name";
        if (isRoutes) {
            query = "{class: 'Operation', alias: 'o', fields: '" + fields + "', where: 'code like `Route%`', orderBy: 'name'}";
        } else {
            query = "{class: 'Operation', alias: 'o', fields: '" + fields + "', where: '!(code like `Route%`)', orderBy: 'name'}";
        }

        // Query operations
        operationsMapper = ReactiveEntitiesMapper.<Operation>createPushReactiveChain()
            .setDataSourceModel(dataSourceModel)
            .always(query)
            .storeEntitiesInto(operationsFeed)
            .start();

        // Query all role operations to show which roles use which operations
        roleOperationsMapper = ReactiveEntitiesMapper.<AuthorizationRoleOperation>createPushReactiveChain()
            .setDataSourceModel(dataSourceModel)
            .always("{class: 'AuthorizationRoleOperation', alias: 'ro', fields: 'role.name,operation,date', orderBy: 'role,id'}")
            .storeEntitiesInto(roleOperationsFeed)
            .start();

        // Update displayed operations when operations feed or search text changes
        FXProperties.runNowAndOnPropertiesChange(
            this::updateDisplayedOperations,
            ObservableLists.versionNumber(operationsFeed),
            searchField.textProperty()
        );

        // Update grid when displayed operations change or when role operations change
        Runnable updateGrid = () -> {
            VisualResult vr = EntitiesToVisualResultMapper.mapEntitiesToVisualResult(displayedOperations, columns);
            operationsGrid.setVisualResult(vr);
        };

        ObservableLists.runNowAndOnListChange(change -> updateGrid.run(), displayedOperations);
        ObservableLists.runNowAndOnListChange(change -> updateGrid.run(), roleOperationsFeed);
    }

    private void updateDisplayedOperations() {
        String searchText = searchField != null && searchField.getText() != null
            ? searchField.getText().toLowerCase().trim()
            : "";

        // Get all operations and sort by name (handle nulls)
        List<Operation> operations = operationsFeed.stream()
            .sorted(Comparator.comparing(Operation::getName, Comparator.nullsLast(String::compareTo)))
            .collect(Collectors.toList());

        // Apply search filter
        if (!searchText.isEmpty()) {
            operations = operations.stream()
                .filter(op -> {
                    if (isRoutes) {
                        return (op.getName() != null && op.getName().toLowerCase().contains(searchText)) ||
                               (op.getGrantRoute() != null && op.getGrantRoute().toLowerCase().contains(searchText));
                    } else {
                        return (op.getOperationCode() != null && op.getOperationCode().toLowerCase().contains(searchText)) ||
                               (op.getName() != null && op.getName().toLowerCase().contains(searchText));
                    }
                })
                .collect(Collectors.toList());
        }

        // Update displayed operations
        displayedOperations.setAll(operations);
    }

    /**
     * Helper method for renderers to get role operations for a specific operation.
     */
    List<AuthorizationRoleOperation> getRoleOperationsForOperation(Operation operation) {
        return roleOperationsFeed.stream()
            .filter(ro -> ro.getOperation() != null && ro.getOperation().getPrimaryKey().equals(operation.getPrimaryKey()))
            .collect(Collectors.toList());
    }

    private void showCreateDialog() {
        OperationDialog.show(null, isRoutes, this::refresh);
    }

    void showEditDialog(Operation operation) {
        OperationDialog.show(operation, isRoutes, this::refresh);
    }

    void showDeleteDialog(Operation operation) {
        // Create dialog content
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        // Title
        Object deleteTitleKey = isRoutes ? DeleteRoute : DeleteOperation;
        Label titleLabel = I18nControls.newLabel(deleteTitleKey);
        titleLabel.getStyleClass().add("delete-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Message
        Label messageLabel = new Label(I18n.getI18nText(Delete) + " " + operation.getName() + "?");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.getStyleClass().add("delete-dialog-message");

        // Confirmation text
        Object deleteConfirmKey = isRoutes ? DeleteRouteConfirm : DeleteOperationConfirm;
        Label confirmLabel = I18nControls.newLabel(deleteConfirmKey);
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
             updateStore.deleteEntity(operation);
             updateStore.submitChanges();
            dialogCallback.closeDialog();
            refresh();
        });
    }

    public void refresh() {
        if (operationsMapper != null) {
            operationsMapper.refreshWhenActive();
        }
        if (roleOperationsMapper != null) {
            roleOperationsMapper.refreshWhenActive();
        }
    }

    public void setActive(boolean active) {
        if (operationsMapper != null) {
            operationsMapper.getReactiveDqlQuery().setActive(active);
        }
        if (roleOperationsMapper != null) {
            roleOperationsMapper.getReactiveDqlQuery().setActive(active);
        }
    }
}
