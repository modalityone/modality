package one.modality.crm.backoffice.activities.admin;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.reactive.dql.query.ReactiveDqlQuery;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import one.modality.base.shared.entities.Label;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.util.HashMap;
import java.util.Map;

import static one.modality.crm.backoffice.activities.admin.Admin18nKeys.*;

/**
 * Label Editor view for managing multilingual labels.
 *
 * @author Claude Code
 */
public class LabelEditorView {

    private final VBox view;
    private final VisualGrid labelGrid;
    private TextField searchField;
    private final StringProperty searchTextProperty = new SimpleStringProperty();
    private final Object mixin; // Activity mixin for reactive chain
    private ReactiveVisualMapper<Label> labelMapper;
    private final ObservableList<Label> labels = FXCollections.observableArrayList();
    private final Map<Object, LabelUsageDetails> usageDetailsCache = new HashMap<>();
    private final ProgressIndicator loadingSpinner;
    private final StackPane gridContainer;
    private boolean initialLoadComplete = false;

    static {
        // Register custom renderers
        LabelEditorRenderers.registerRenderers();
    }

    public LabelEditorView(Object mixin) {
        this.mixin = mixin;
        view = new VBox();
        view.setSpacing(16);
        view.setPadding(new Insets(24));
        view.setAlignment(Pos.TOP_LEFT);
        view.setFillWidth(true);

        labelGrid = VisualGrid.createVisualGridWithTableLayoutSkin();
        labelGrid.setMinRowHeight(40);
        labelGrid.setPrefRowHeight(40);
        labelGrid.setPrefHeight(600);
        labelGrid.setMaxHeight(Double.MAX_VALUE);
        labelGrid.setMinWidth(0);
        labelGrid.setPrefWidth(Double.MAX_VALUE);
        labelGrid.setMaxWidth(Double.MAX_VALUE);

        // Create loading spinner
        loadingSpinner = new ProgressIndicator();
        loadingSpinner.setMaxSize(50, 50);
        loadingSpinner.setVisible(true); // Show initially

        // Wrap grid in a StackPane with loading overlay
        gridContainer = new StackPane(labelGrid, loadingSpinner);
        StackPane.setAlignment(loadingSpinner, Pos.CENTER);
        gridContainer.setMinHeight(200);

        // Pass this view to renderers
        LabelEditorRenderers.setLabelEditorView(this);

        // Title and description
        javafx.scene.control.Label titleLabel = I18nControls.newLabel(LabelEditor);
        titleLabel.getStyleClass().add("labeleditor-title");

        javafx.scene.control.Label descriptionLabel = I18nControls.newLabel(LabelEditorDescription);
        descriptionLabel.getStyleClass().add("labeleditor-subtitle");

        // Label management card
        VBox managementCard = createLabelManagementCard();

        view.getChildren().addAll(titleLabel, descriptionLabel, managementCard);
        VBox.setVgrow(managementCard, Priority.ALWAYS);

        // Note: startLogic() is called lazily when setActive(true) is first called
        // This ensures the activity context is available for the reactive chain
    }

    // Column definitions for the label grid
    private static final String LABEL_COLUMNS = // language=JSON5
        """
        [
            {expression: 'this', label: 'ID', renderer: 'labelId', minWidth: 60, prefWidth: 80, textAlign: 'center'},
            {expression: 'organization.name', label: 'Organization', minWidth: 100, prefWidth: 150},
            {expression: 'ref', label: 'Ref', renderer: 'refLanguage', minWidth: 60, prefWidth: 80, textAlign: 'center'},
            {expression: 'this', label: 'Reference Text', renderer: 'referenceText', minWidth: 200, prefWidth: 300},
            {expression: 'this', label: 'Translations', renderer: 'translationFlags', minWidth: 180, prefWidth: 220},
            {expression: 'this', label: 'Usage', renderer: 'usageCount', minWidth: 70, prefWidth: 80, textAlign: 'center'},
            {expression: 'this', label: 'Actions', renderer: 'labelActions', minWidth: 90, prefWidth: 100, textAlign: 'center'}
        ]""";

    private void startLogic() {
        // Show spinner when search text changes (a new query will be triggered)
        searchTextProperty.addListener((obs, oldVal, newVal) -> {
            if (initialLoadComplete) {
                showLoading();
            }
        });

        // Query label records with database-side search using ReactiveVisualMapper for auto-updates
        labelMapper = ReactiveVisualMapper.<Label>createPushReactiveChain(mixin)
            .always("{class: 'Label', alias: 'l', fields: 'id,ref,en,fr,es,de,pt,zhs,zht,el,vi,icon,organization.name', orderBy: 'id desc', limit: 100}")
            .ifNotNullOtherwiseEmpty(FXOrganizationId.organizationIdProperty(),
                orgId -> DqlStatement.where("organization=? or organization is null", orgId))
            // Database-side search across all language fields
            .ifTrimNotEmpty(searchTextProperty, s -> DqlStatement.where(
                "lower(en) like ? or lower(fr) like ? or lower(es) like ? or lower(de) like ? or lower(pt) like ? or lower(zhs) like ? or lower(zht) like ? or lower(el) like ? or lower(vi) like ?",
                "%" + s.toLowerCase() + "%", "%" + s.toLowerCase() + "%", "%" + s.toLowerCase() + "%",
                "%" + s.toLowerCase() + "%", "%" + s.toLowerCase() + "%", "%" + s.toLowerCase() + "%",
                "%" + s.toLowerCase() + "%", "%" + s.toLowerCase() + "%", "%" + s.toLowerCase() + "%"))
            .setEntityColumns(LABEL_COLUMNS)
            .visualizeResultInto(labelGrid.visualResultProperty())
            .addEntitiesHandler(entityList -> {
                Console.log("Labels loaded: " + entityList.size() + " labels");
                // Store entities for usage count lookup
                labels.setAll(entityList);
                // Hide loading spinner
                if (!initialLoadComplete) {
                    initialLoadComplete = true;
                }
                hideLoading();
                // Load usage counts for displayed labels
                loadUsageCounts();
            })
            .start();
    }

    /**
     * Shows the loading spinner (e.g., when searching).
     */
    private void showLoading() {
        loadingSpinner.setVisible(true);
    }

    /**
     * Hides the loading spinner.
     */
    private void hideLoading() {
        loadingSpinner.setVisible(false);
    }

    public Node getView() {
        return view;
    }

    private VBox createLabelManagementCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("labeleditor-card");
        card.setPadding(new Insets(24));

        // Header with title and button
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        javafx.scene.control.Label titleLabel = I18nControls.newLabel(LabelsTitle);
        titleLabel.getStyleClass().add("labeleditor-card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newLabelButton = Bootstrap.successButton(I18nControls.newButton(NewLabel));
        newLabelButton.setOnAction(e -> showCreateLabelDialog());

        header.getChildren().addAll(titleLabel, spacer, newLabelButton);

        // Search field with icon - bound to searchTextProperty for database search
        searchField = new TextField();
        I18n.bindI18nTextProperty(searchField.promptTextProperty(), SearchLabels);
        searchField.setPrefWidth(300);
        searchField.setPadding(new Insets(8, 35, 8, 12));
        searchTextProperty.bind(searchField.textProperty());

        javafx.scene.control.Label searchIcon = new javafx.scene.control.Label("\uD83D\uDD0D");
        searchIcon.setMouseTransparent(true);

        StackPane searchContainer = new StackPane();
        searchContainer.getChildren().addAll(searchField, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(searchIcon, new Insets(0, 12, 0, 0));

        card.getChildren().addAll(header, searchContainer, gridContainer);
        VBox.setVgrow(gridContainer, Priority.ALWAYS);
        VBox.setVgrow(labelGrid, Priority.ALWAYS);

        return card;
    }

    private void showCreateLabelDialog() {
        LabelEditorDialog.show(null, (ButtonFactoryMixin) mixin, getEntityStore(), this::refreshLabels);
    }

    void showEditLabelDialog(Label label) {
        LabelUsageDetails usageDetails = getUsageDetails(label);
        LabelEditorDialog.show(label, (ButtonFactoryMixin) mixin, getEntityStore(), this::refreshLabels, usageDetails);
    }

    void showDeleteConfirmation(Label label, int usageCount) {
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(400);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(600);

        javafx.scene.control.Label titleLabel = I18nControls.newLabel(Delete);
        titleLabel.getStyleClass().add("labeleditor-delete-dialog-title");

        String labelPreview = label.getEn() != null ? label.getEn() : (label.getFr() != null ? label.getFr() : "Label #" + label.getId());
        if (labelPreview.length() > 50) {
            labelPreview = labelPreview.substring(0, 47) + "...";
        }
        javafx.scene.control.Label messageLabel = new javafx.scene.control.Label();
        messageLabel.textProperty().bind(I18n.i18nTextProperty(DeleteLabelMessage, labelPreview));
        messageLabel.getStyleClass().add("labeleditor-delete-dialog-message");
        messageLabel.setWrapText(true);

        dialogContent.getChildren().addAll(titleLabel, messageLabel);

        // Show usage warning if label is in use
        if (usageCount > 0) {
            javafx.scene.control.Label warningLabel = new javafx.scene.control.Label();
            warningLabel.textProperty().bind(I18n.i18nTextProperty(LabelInUseWarning, usageCount));
            warningLabel.getStyleClass().add("labeleditor-warning-message");
            warningLabel.setWrapText(true);
            Bootstrap.textDanger(warningLabel);
            dialogContent.getChildren().add(warningLabel);
        }

        javafx.scene.control.Label confirmLabel = I18nControls.newLabel(ConfirmDeleteLabel);
        confirmLabel.getStyleClass().add("labeleditor-delete-dialog-confirm");
        confirmLabel.setWrapText(true);
        dialogContent.getChildren().add(confirmLabel);

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = Bootstrap.button(I18nControls.newButton(Cancel));
        Button deleteButton = Bootstrap.dangerButton(I18nControls.newButton(Delete));

        footer.getChildren().addAll(cancelButton, deleteButton);
        dialogContent.getChildren().add(footer);

        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        dev.webfx.extras.util.dialog.DialogCallback dialogCallback = dev.webfx.extras.util.dialog.DialogUtil.showModalNodeInGoldLayout(
            dialogPane, one.modality.base.client.mainframe.fx.FXMainFrameDialogArea.getDialogArea()
        );

        cancelButton.setOnAction(e -> dialogCallback.closeDialog());

        deleteButton.setOnAction(e -> {
            UpdateStore updateStore = UpdateStore.createAbove(getEntityStore());
            Label labelToDelete = updateStore.updateEntity(label);
            updateStore.deleteEntity(labelToDelete);
            updateStore.submitChanges()
                .onSuccess(result -> {
                    dialogCallback.closeDialog();
                    refreshLabels();
                })
                .onFailure(error -> {
                    dialogCallback.closeDialog();
                    showErrorDialog(error.getMessage());
                });
        });
    }

    private void showErrorDialog(String content) {
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        javafx.scene.control.Label titleLabel = I18nControls.newLabel(Error);
        titleLabel.getStyleClass().add("labeleditor-error-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.control.Label headerLabel = I18nControls.newLabel(FailedToDeleteLabel);
        headerLabel.setWrapText(true);
        headerLabel.setMaxWidth(Double.MAX_VALUE);
        headerLabel.getStyleClass().add("labeleditor-error-dialog-header");

        javafx.scene.control.Label contentLabel = new javafx.scene.control.Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.getStyleClass().add("labeleditor-error-dialog-content");

        dialogContent.getChildren().addAll(titleLabel, headerLabel, contentLabel);

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button okButton = Bootstrap.dangerButton(I18nControls.newButton(OK));

        footer.getChildren().add(okButton);
        dialogContent.getChildren().add(footer);

        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        dev.webfx.extras.util.dialog.DialogCallback dialogCallback = dev.webfx.extras.util.dialog.DialogUtil.showModalNodeInGoldLayout(
            dialogPane, one.modality.base.client.mainframe.fx.FXMainFrameDialogArea.getDialogArea()
        );

        okButton.setOnAction(e -> dialogCallback.closeDialog());
    }

    private EntityStore getEntityStore() {
        return labelMapper != null ? labelMapper.getStore() : EntityStore.create(DataSourceModelService.getDefaultDataSourceModel());
    }

    private void refreshLabels() {
        // Use refreshWhenActive to force the reactive query to re-fetch data
        if (labelMapper != null) {
            ReactiveDqlQuery<Label> reactiveDqlQuery = labelMapper.getReactiveDqlQuery();
            boolean wasActive = reactiveDqlQuery.isActive();
            Console.log("LabelEditorView.refreshLabels() called - isActive=" + wasActive);
            // Ensure the query is active before refreshing
            if (!wasActive) {
                reactiveDqlQuery.setActive(true);
            }
            labelMapper.refreshWhenActive();
        }
    }

    /**
     * Gets the usage count for a label.
     * This counts how many entities reference this label.
     */
    public int getUsageCount(Label label) {
        LabelUsageDetails details = usageDetailsCache.get(label.getPrimaryKey());
        return details != null ? details.getTotalCount() : 0;
    }

    /**
     * Gets detailed usage information for a label.
     * Returns null if no usage data is available.
     */
    public LabelUsageDetails getUsageDetails(Label label) {
        return usageDetailsCache.get(label.getPrimaryKey());
    }

    /**
     * Loads usage counts for all labels currently displayed.
     * Queries known entity types that have Label foreign key fields.
     * For events, fetches detailed info (name, dates). For others, just counts.
     */
    private void loadUsageCounts() {
        if (labels.isEmpty()) {
            return;
        }

        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        usageDetailsCache.clear();

        // Event queries with detailed info (id, name, start_date, end_date, label_id, field_used)
        // Format: [query, entityType, labelIdColumn, fieldUsedLabel]
        String[][] eventDetailQueries = {
            {"select id, name, start_date, end_date, label_id from event where label_id is not null", "Events", "label_id", "Name"},
            {"select id, name, start_date, end_date, short_description_label_id from event where short_description_label_id is not null", "Events", "short_description_label_id", "Short Description"},
            {"select id, name, start_date, end_date, long_description_label_id from event where long_description_label_id is not null", "Events", "long_description_label_id", "Long Description"},
            {"select id, name, start_date, end_date, fees_bottom_label_id from event where fees_bottom_label_id is not null", "Events", "fees_bottom_label_id", "Fees Bottom"},
            {"select id, name, start_date, end_date, livestream_message_label_id from event where livestream_message_label_id is not null", "Events", "livestream_message_label_id", "Livestream Message"}
        };

        // Execute event detail queries
        for (String[] queryDef : eventDetailQueries) {
            String query = queryDef[0];
            String entityType = queryDef[1];
            String fieldUsed = queryDef[3];

            QueryService.executeQuery(QueryArgument.builder()
                    .setStatement(query)
                    .setDataSourceId(dataSourceModel.getDataSourceId())
                    .build())
                .onSuccess(queryResult -> {
                    // Process results - each row has (id, name, start_date, end_date, label_id)
                    for (int row = 0; row < queryResult.getRowCount(); row++) {
                        Object eventId = queryResult.getValue(row, 0);
                        String eventName = (String) queryResult.getValue(row, 1);
                        Object startDate = queryResult.getValue(row, 2);
                        Object endDate = queryResult.getValue(row, 3);
                        Object labelId = queryResult.getValue(row, 4);

                        if (labelId != null) {
                            String dateRange = formatDateRange(startDate, endDate);
                            LabelUsageDetails details = usageDetailsCache.computeIfAbsent(labelId, k -> new LabelUsageDetails());
                            LabelUsageDetails.EntityReference ref = new LabelUsageDetails.EntityReference(
                                eventId, eventName, dateRange, fieldUsed
                            );
                            details.addEntityReference(entityType, ref);
                        }
                    }
                    refreshGrid();
                })
                .onFailure(error -> Console.log("Failed to load event details: " + error.getMessage()));
        }

        // Simple count queries for other entity types (no detailed info needed)
        // Format: [query, entityTypeDisplayName]
        String[][] countQueries = {
            {"select label_id, count(1) from item where label_id is not null group by label_id", "Items"},
            {"select label_id, count(1) from item_family where label_id is not null group by label_id", "Item Families"},
            {"select label_id, count(1) from scheduled_item where label_id is not null group by label_id", "Scheduled Items"},
            {"select comment_label_id, count(1) from scheduled_item where comment_label_id is not null group by comment_label_id", "Scheduled Items (Comment)"},
            {"select label_id, count(1) from option where label_id is not null group by label_id", "Options"},
            {"select label_id, count(1) from pool where label_id is not null group by label_id", "Pools"},
            {"select description_label_id, count(1) from pool where description_label_id is not null group by description_label_id", "Pools (Description)"}
        };

        // Execute count queries for non-event entities
        for (String[] queryDef : countQueries) {
            String query = queryDef[0];
            String entityType = queryDef[1];

            QueryService.executeQuery(QueryArgument.builder()
                    .setStatement(query)
                    .setDataSourceId(dataSourceModel.getDataSourceId())
                    .build())
                .onSuccess(queryResult -> {
                    for (int row = 0; row < queryResult.getRowCount(); row++) {
                        Object labelId = queryResult.getValue(row, 0);
                        Number count = (Number) queryResult.getValue(row, 1);
                        if (labelId != null && count != null && count.intValue() > 0) {
                            LabelUsageDetails details = usageDetailsCache.computeIfAbsent(labelId, k -> new LabelUsageDetails());
                            details.addUsage(entityType, count.intValue());
                        }
                    }
                    refreshGrid();
                })
                .onFailure(error -> Console.log("Failed to load usage counts: " + error.getMessage()));
        }
    }

    /**
     * Formats a date range for display.
     */
    private String formatDateRange(Object startDate, Object endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (startDate != null) {
            sb.append(formatDate(startDate));
        }
        if (endDate != null && !endDate.equals(startDate)) {
            if (sb.length() > 0) {
                sb.append(" - ");
            }
            sb.append(formatDate(endDate));
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Formats a single date for display.
     */
    private String formatDate(Object date) {
        if (date == null) {
            return "";
        }
        // Handle LocalDate, LocalDateTime, or string dates
        String dateStr = date.toString();
        // If it's a full timestamp, just take the date part
        if (dateStr.contains("T")) {
            dateStr = dateStr.substring(0, dateStr.indexOf("T"));
        }
        // If it's in ISO format (YYYY-MM-DD), convert to more readable format
        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = dateStr.split("-");
            return parts[2] + "/" + parts[1] + "/" + parts[0]; // DD/MM/YYYY
        }
        return dateStr;
    }

    /**
     * Refreshes the grid to show updated data.
     */
    private void refreshGrid() {
        if (labelMapper != null) {
            labelMapper.refreshWhenActive();
        }
    }

    public void setActive(boolean active) {
        // Start logic lazily on first activation (ensures activity context is available)
        if (active && labelMapper == null) {
            startLogic();
        }
        if (labelMapper != null) {
            ReactiveDqlQuery<Label> reactiveDqlQuery = labelMapper.getReactiveDqlQuery();
            reactiveDqlQuery.setActive(active);
        }
    }
}
