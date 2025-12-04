package one.modality.crm.backoffice.activities.operationsandroles;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.backoffice.claude.FormField;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Operation;
import one.modality.base.shared.entities.OperationGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static one.modality.base.backoffice.claude.FormFieldHelper.addSearchFilter;
import static one.modality.base.backoffice.claude.FormFieldHelper.createTextField;
import static one.modality.crm.backoffice.activities.operationsandroles.OperationsAndRolesI18nKeys.*;

/**
 * Dialog for creating and editing operation groups.
 *
 * @author Claude Code
 */
public class OperationGroupDialog {

    /**
     * Shows the create/edit operation group dialog.
     *
     * @param group Existing operation group to edit (null for new group)
     * @param store EntityStore to use for all operations (ensures consistency)
     * @param onSuccess Callback to execute after successful save
     */
    public static void show(OperationGroup group, EntityStore store, Runnable onSuccess) {
        boolean isEdit = group != null;

        // Use the provided store and create an update store above it
        UpdateStore updateStore = UpdateStore.createAbove(store);

        // Load operations: only backend operations with no group, or belonging to the current group (if editing)
        String query = isEdit
            ? "select id,name,group from Operation where backend=true and (group is null or group=?) order by name"
            : "select id,name,group from Operation where backend=true and group is null order by name";

        if (isEdit) {
            store.<Operation>executeQuery(query, group.getPrimaryKey())
                .onSuccess(availableOperations -> {
                    // Determine which operations are currently in this group
                    Set<Object> selectedOperationIds = new HashSet<>();
                    for (Operation op : availableOperations) {
                        if (op.getGroup() != null && op.getGroup().getPrimaryKey().equals(group.getPrimaryKey())) {
                            selectedOperationIds.add(op.getPrimaryKey());
                        }
                    }
                    Platform.runLater(() -> buildAndShowDialog(group, true, updateStore, availableOperations, selectedOperationIds, onSuccess));
                });
        } else {
            store.<Operation>executeQuery(query)
                .onSuccess(availableOperations -> Platform.runLater(() -> buildAndShowDialog(null, false, updateStore, availableOperations, new HashSet<>(), onSuccess)));
        }
    }

    private static void buildAndShowDialog(
        OperationGroup group,
        boolean isEdit,
        UpdateStore updateStore,
        EntityList<Operation> allOperations,
        Set<Object> selectedOperationIds,
        Runnable onSuccess
    ) {
        // Main dialog container
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(24));
        dialogContent.setMinWidth(500);
        dialogContent.setPrefWidth(700);
        dialogContent.setMaxWidth(900);

        // Header with title
        Object titleKey = isEdit ? EditGroupTitle : CreateGroupTitle;
        Label titleLabel = Bootstrap.strong(I18nControls.newLabel(titleKey));
        titleLabel.getStyleClass().add("modal-title");

        // Form fields
        VBox formFields = new VBox(20);
        formFields.setMaxWidth(Double.MAX_VALUE);

        // Group Name field (required)
        FormField<TextField> nameFormField = createTextField(GroupName, GroupNamePlaceholder, null);
        TextField nameInput = nameFormField.inputField();
        if (isEdit) {
            nameInput.setText(group.getName());
        }

        // === SELECTED OPERATIONS DISPLAY (On Top - Always Visible) ===
        VBox selectedItemsContainer = new VBox(12);
        selectedItemsContainer.setMaxWidth(Double.MAX_VALUE);

        Label selectedItemsTitle = I18nControls.newLabel(SelectedOperations);
        selectedItemsTitle.getStyleClass().add("role-dialog-permissions-title");

        // Selected operations header with count
        Label selectedCountLabel = new Label();
        selectedCountLabel.getStyleClass().add("operation-selection-count-label");

        // Selected Operations Display Panel
        FlowPane selectedChipsPane = new FlowPane();
        selectedChipsPane.setHgap(6);
        selectedChipsPane.setVgap(6);
        selectedChipsPane.setPadding(new Insets(12));
        selectedChipsPane.setMinHeight(80);
        selectedChipsPane.setMaxWidth(Double.MAX_VALUE);
        selectedChipsPane.getStyleClass().add("operation-selection-panel");

        // Initial empty message
        Label emptyMessage = I18nControls.newLabel(NoOperationsSelectedYet);
        emptyMessage.getStyleClass().add("operation-selection-empty-message");
        selectedChipsPane.getChildren().add(emptyMessage);

        selectedItemsContainer.getChildren().addAll(selectedItemsTitle, selectedCountLabel, selectedChipsPane);

        // === INFO BANNER ===
        VBox infoBanner = new VBox(6);
        infoBanner.getStyleClass().add("admin-info-banner");
        infoBanner.setPadding(new Insets(12));
        infoBanner.setMaxWidth(Double.MAX_VALUE);

        Label infoText = I18nControls.newLabel(BackofficeOperationsOnly);
        infoText.getStyleClass().add("admin-info-banner-text");
        infoText.setWrapText(true);
        infoText.setMaxWidth(Double.MAX_VALUE);

        infoBanner.getChildren().add(infoText);

        // === SEARCH OPERATIONS SECTION (Below Selected Operations) ===
        VBox operationsField = new VBox(12);
        operationsField.setMaxWidth(Double.MAX_VALUE);

        // Section label (not marked as required)
        Label operationsLabel = I18nControls.newLabel(SearchOperations);
        operationsLabel.getStyleClass().add("section-label");

        // Search box for filtering operations
        TextField searchOperations = new TextField();
        I18n.bindI18nTextProperty(searchOperations.promptTextProperty(), SearchOperationsPlaceholder);
        searchOperations.setPrefWidth(USE_COMPUTED_SIZE);
        searchOperations.setMaxWidth(Double.MAX_VALUE);
        searchOperations.setPadding(new Insets(8, 12, 8, 12));

        // ScrollPane for checkbox list
        ScrollPane checkboxScroll = new ScrollPane();
        checkboxScroll.setFitToWidth(true);
        checkboxScroll.setPrefHeight(250);

        VBox checkboxGroup = new VBox(12);
        checkboxGroup.setPadding(new Insets(12));

        List<CheckBox> operationCheckboxes = new ArrayList<>();
        for (Operation operation : allOperations) {
            CheckBox checkbox = new CheckBox(operation.getName());
            checkbox.setUserData(operation);
            // Pre-select if this operation belongs to the group
            if (selectedOperationIds.contains(operation.getPrimaryKey())) {
                checkbox.setSelected(true);
            }
            operationCheckboxes.add(checkbox);
            checkboxGroup.getChildren().add(checkbox);
        }

        // Add search filter listener
        addSearchFilter(searchOperations, operationCheckboxes);

        checkboxScroll.setContent(checkboxGroup);
        operationsField.getChildren().addAll(operationsLabel, searchOperations, checkboxScroll);

        // Add fields to form
        formFields.getChildren().addAll(nameFormField.container(), selectedItemsContainer, infoBanner, operationsField);

        // Create the entity to save
        OperationGroup groupToSave = group != null ? updateStore.updateEntity(group) : updateStore.insertEntity(OperationGroup.class);

        // Create validation support and add required field validations
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.addRequiredInput(nameInput);

        // Add validation that at least one operation must be selected
        validationSupport.addValidationRule(
            new BooleanBinding() {
                {
                    for (CheckBox cb : operationCheckboxes) {
                        bind(cb.selectedProperty());
                    }
                }
                @Override
                protected boolean computeValue() {
                    return operationCheckboxes.stream().anyMatch(CheckBox::isSelected);
                }
            },
            checkboxScroll,
            I18nControls.newLabel(AtLeastOneOperationError).textProperty()
        );

        // Create a BooleanBinding that checks if updateStore has no changes
        BooleanBinding hasNoChangesBinding = new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return !updateStore.hasChanges();
            }
        };

        // Add listeners to form fields to update entity and invalidate binding
        nameInput.textProperty().addListener((obs, oldVal, newVal) -> {
            groupToSave.setName(newVal.trim());
            hasNoChangesBinding.invalidate();
        });

        // Method to update the selected operations display
        Runnable updateSelectedOperationsDisplay = () -> {
            // Count selected operations
            long selectedCount = operationCheckboxes.stream().filter(CheckBox::isSelected).count();
            selectedCountLabel.setText(I18n.getI18nText(SelectedOperationsCount).replace("{0}", String.valueOf(selectedCount)));

            // Clear and rebuild chips
            selectedChipsPane.getChildren().clear();

            if (selectedCount == 0) {
                Label empty = I18nControls.newLabel(NoOperationsSelectedYet);
                empty.getStyleClass().add("operation-selection-empty-message");
                selectedChipsPane.getChildren().add(empty);
            } else {
                for (CheckBox cb : operationCheckboxes) {
                    if (cb.isSelected()) {
                        // Create chip with remove button
                        HBox chipContainer = new HBox(4);
                        chipContainer.setAlignment(Pos.CENTER);
                        chipContainer.setPadding(new Insets(6, 8, 6, 12));
                        chipContainer.getStyleClass().add("operation-selection-chip");

                        // Operation name label
                        Label nameLabel = new Label(cb.getText());
                        nameLabel.getStyleClass().add("operation-selection-chip-label");

                        // Remove button (×)
                        Label removeBtn = new Label("×");
                        removeBtn.getStyleClass().add("operation-selection-chip-remove");
                        removeBtn.setOnMouseClicked(e -> {
                            // Uncheck the corresponding checkbox
                            cb.setSelected(false);
                        });

                        chipContainer.getChildren().addAll(nameLabel, removeBtn);
                        selectedChipsPane.getChildren().add(chipContainer);
                    }
                }
            }
        };

        // Add listeners to checkboxes to invalidate binding and update operations
        for (int i = 0; i < operationCheckboxes.size(); i++) {
            CheckBox cb = operationCheckboxes.get(i);
            Operation operation = allOperations.get(i);
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                // Update the operation's group reference
                Operation operationToUpdate = updateStore.updateEntity(operation);
                if (newVal) {
                    // Assign this operation to the group
                    operationToUpdate.setGroup(groupToSave);
                } else {
                    // Remove the group assignment
                    operationToUpdate.setGroup(null);
                }
                hasNoChangesBinding.invalidate();

                // Update selected operations display
                updateSelectedOperationsDisplay.run();
            });
        }

        // Initial update of selected operations display
        updateSelectedOperationsDisplay.run();

        // Footer buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = Bootstrap.button(I18nControls.newButton(BaseI18nKeys.Cancel));
        Object saveButtonKey = isEdit ? BaseI18nKeys.SaveChanges : CreateGroupButton;
        Button saveButton = Bootstrap.successButton(I18nControls.newButton(saveButtonKey));

        // Bind save button disable property to hasNoChangesBinding
        saveButton.disableProperty().bind(hasNoChangesBinding);

        footer.getChildren().addAll(cancelButton, saveButton);

        // Add all to dialog content
        dialogContent.getChildren().addAll(titleLabel, formFields, footer);

        // Show dialog
        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        // Button actions
        cancelButton.setOnAction(e -> dialogCallback.closeDialog());

        saveButton.setOnAction(e -> {
            // Validate form using ValidationSupport
            if (!validationSupport.isValid()) {
                return;
            }

            // Submit all changes (group and operations) in one transaction
            updateStore.submitChanges().onSuccess(result -> {
                dialogCallback.closeDialog();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }).onFailure(error -> {
                // Show error dialog
                showErrorDialog(error.getMessage());
            });
        });
    }

    /**
     * Shows an error dialog with the specified content.
     */
    private static void showErrorDialog(String content) {
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        // Title
        Label titleLabel = Bootstrap.strong(I18nControls.newLabel(BaseI18nKeys.Error));
        titleLabel.getStyleClass().add("error-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Header
        Label headerLabel = I18nControls.newLabel(OperationsAndRolesI18nKeys.FailedToSaveGroup);
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

        Button okButton = Bootstrap.dangerButton(I18nControls.newButton(BaseI18nKeys.OK));

        footer.getChildren().add(okButton);
        dialogContent.getChildren().add(footer);

        // Show dialog
        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        // Button action
        okButton.setOnAction(e -> dialogCallback.closeDialog());
    }
}
