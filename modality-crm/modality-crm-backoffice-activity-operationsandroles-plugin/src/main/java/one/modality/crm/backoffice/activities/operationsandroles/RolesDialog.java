package one.modality.crm.backoffice.activities.operationsandroles;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.backoffice.claude.FormField;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static one.modality.base.backoffice.claude.FormFieldHelper.*;
import static one.modality.crm.backoffice.activities.operationsandroles.OperationsAndRolesI18nKeys.*;

/**
 * Dialog for creating and editing authorization roles.
 *
 * @author Claude Code
 */
public class RolesDialog {

    /**
     * Shows the create/edit role dialog.
     *
     * @param role Existing role to edit (null for new role)
     * @param store EntityStore to use for all operations (ensures consistency)
     * @param onSuccess Callback to execute after successful save
     */
    public static void show(AuthorizationRole role, EntityStore store, Runnable onSuccess) {
        show(role, store, onSuccess, false);
    }

    /**
     * Shows the create/edit role dialog.
     *
     * @param role Existing role to edit (null for new role)
     * @param store EntityStore to use for all operations (ensures consistency)
     * @param onSuccess Callback to execute after successful save
     * @param isDuplicate If true, creates a new role with the same permissions
     */
    public static void show(AuthorizationRole role, EntityStore store, Runnable onSuccess, boolean isDuplicate) {
        boolean isEdit = role != null && !isDuplicate;

        // Use the provided store and create an update store above it
        UpdateStore updateStore = UpdateStore.createAbove(store);

        // Load data using executeQueryBatch (filtering only backend operations)
        if (role != null) {
            // Editing or duplicating - load all data including role-specific data
            store.executeQueryBatch(
                new EntityStoreQuery("select id,name from Operation where backend=true order by name"),
                new EntityStoreQuery("select id,name from OperationGroup order by name"),
                new EntityStoreQuery("select id,name,rule from AuthorizationRule order by name"),
                new EntityStoreQuery("select id,operation,operationGroup from AuthorizationRoleOperation where role=$1 order by id", role.getPrimaryKey()),
                new EntityStoreQuery("select id,name,rule from AuthorizationRule where role=$1 order by name", role.getPrimaryKey())
            ).inUiThread().onSuccess(entityLists -> buildAndShowDialog(
                role, isEdit, isDuplicate, updateStore,
                entityLists[0], // operations
                entityLists[1], // groups
                entityLists[2], // rules
                entityLists[3], // roleOperations
                entityLists[4], // assignedRules
                onSuccess
            ));
        } else {
            // Creating new - only load operations, groups, and rules (filtering only backend operations)
            store.executeQueryBatch(
                new EntityStoreQuery("select id,name from Operation where backend=true order by name"),
                new EntityStoreQuery("select id,name from OperationGroup order by name"),
                new EntityStoreQuery("select id,name,rule from AuthorizationRule order by name")
            ).inUiThread().onSuccess(entityLists -> buildAndShowDialog(
                role, isEdit, isDuplicate, updateStore,
                entityLists[0], // operations
                entityLists[1], // groups
                entityLists[2], // rules
                EntityList.create("emptyRoleOperations", store),
                EntityList.create("emptyAssignedRules", store),
                onSuccess
            ));
        }
    }

    private static void buildAndShowDialog(
        AuthorizationRole role,
        boolean isEdit,
        boolean isDuplicate,
        UpdateStore updateStore,
        EntityList<Operation> allOperations,
        EntityList<OperationGroup> allGroups,
        EntityList<AuthorizationRule> allRules,
        EntityList<AuthorizationRoleOperation> existingRoleOperations,
        EntityList<AuthorizationRule> assignedRules,
        Runnable onSuccess
    ) {
        // Main dialog container
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(24));
        dialogContent.setMinWidth(500);
        dialogContent.setPrefWidth(800);
        dialogContent.setMaxWidth(1000);

        // Header with title
        Object titleKey = isEdit ? EditRoleTitle : (isDuplicate ? DuplicateRoleTitle : CreateRoleTitle);
        Label titleLabel = Bootstrap.strong(I18nControls.newLabel(titleKey));
        titleLabel.getStyleClass().add("modal-title");

        // Form fields
        VBox formFields = new VBox(20);
        formFields.setMaxWidth(Double.MAX_VALUE);

        // Role Name field (required)
        FormField<TextField> nameFormField = createTextField(RoleName, RoleNamePlaceholder, null);
        TextField nameInput = nameFormField.inputField();
        if (role != null) {
            nameInput.setText(isDuplicate ? role.getName() + I18n.getI18nText(CopySuffix) : role.getName());
        }

        // Selected items tracking
        Set<Object> selectedOperationIds = new HashSet<>();
        Set<Object> selectedGroupIds = new HashSet<>();
        Set<Object> selectedRuleIds = new HashSet<>();

        // Pre-fill selections from existing role operations
        for (AuthorizationRoleOperation roleOp : existingRoleOperations) {
            if (roleOp.getOperation() != null) {
                selectedOperationIds.add(roleOp.getOperation().getPrimaryKey());
            } else if (roleOp.getOperationGroup() != null) {
                selectedGroupIds.add(roleOp.getOperationGroup().getPrimaryKey());
            }
        }

        // Pre-fill selections from rules already assigned to this role
        for (AuthorizationRule assignedRule : assignedRules) {
            selectedRuleIds.add(assignedRule.getPrimaryKey());
        }

        // === SELECTED ITEMS DISPLAY (Outside TabPane - Always Visible) ===
        VBox selectedItemsContainer = new VBox(12);
        selectedItemsContainer.setMaxWidth(Double.MAX_VALUE);

        Label selectedItemsTitle = new Label();
        I18n.bindI18nTextProperty(selectedItemsTitle.textProperty(), OperationsAndRolesI18nKeys.Permissions);
        selectedItemsTitle.getStyleClass().add("role-dialog-permissions-title");

        // Combined display for both operations and groups
        FlowPane selectedItemsPane = new FlowPane();
        selectedItemsPane.setHgap(6);
        selectedItemsPane.setVgap(6);
        selectedItemsPane.setPadding(new Insets(12));
        selectedItemsPane.setMinHeight(80);
        selectedItemsPane.getStyleClass().add("role-dialog-permissions-pane");

        Label selectedCountLabel = new Label();
        selectedCountLabel.getStyleClass().add("role-dialog-permissions-count");

        selectedItemsContainer.getChildren().addAll(selectedItemsTitle, selectedCountLabel, selectedItemsPane);

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

        // === TABS FOR OPERATIONS AND OPERATION GROUPS ===
        TabPane selectionTabs = new TabPane();
        selectionTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        selectionTabs.getStyleClass().add("super-admin-sub-tabs");
        selectionTabs.setMinHeight(300);
        selectionTabs.setPrefHeight(350);

        // === OPERATIONS TAB ===
        Tab operationsTab = new Tab();
        I18n.bindI18nTextProperty(operationsTab.textProperty(), Operations);

        VBox operationsContent = new VBox(12);
        operationsContent.setPadding(new Insets(16));

        // Search box for operations
        TextField searchOperations = new TextField();
        I18n.bindI18nTextProperty(searchOperations.promptTextProperty(), SearchOperations);
        searchOperations.setPrefWidth(USE_COMPUTED_SIZE);
        searchOperations.setMaxWidth(Double.MAX_VALUE);
        searchOperations.setPadding(new Insets(8, 12, 8, 12));

        // ScrollPane for operation checkboxes
        ScrollPane operationsScroll = new ScrollPane();
        operationsScroll.setFitToWidth(true);
        operationsScroll.setPrefHeight(250);

        VBox operationsCheckboxGroup = new VBox(12);
        operationsCheckboxGroup.setPadding(new Insets(12));

        List<CheckBox> operationCheckboxes = new ArrayList<>();
        for (Operation operation : allOperations) {
            CheckBox checkbox = new CheckBox(operation.getName());
            checkbox.setUserData(operation);
            if (selectedOperationIds.contains(operation.getPrimaryKey())) {
                checkbox.setSelected(true);
            }
            operationCheckboxes.add(checkbox);
            operationsCheckboxGroup.getChildren().add(checkbox);
        }

        operationsScroll.setContent(operationsCheckboxGroup);
        operationsContent.getChildren().addAll(searchOperations, operationsScroll);
        operationsTab.setContent(operationsContent);

        // === OPERATION GROUPS TAB ===
        Tab groupsTab = new Tab();
        I18n.bindI18nTextProperty(groupsTab.textProperty(), OperationGroups);

        VBox groupsContent = new VBox(12);
        groupsContent.setPadding(new Insets(16));

        // Search box for groups
        TextField searchGroups = new TextField();
        I18n.bindI18nTextProperty(searchGroups.promptTextProperty(), SearchGroups);
        searchGroups.setPrefWidth(USE_COMPUTED_SIZE);
        searchGroups.setMaxWidth(Double.MAX_VALUE);
        searchGroups.setPadding(new Insets(8, 12, 8, 12));

        // ScrollPane for group checkboxes
        ScrollPane groupsScroll = new ScrollPane();
        groupsScroll.setFitToWidth(true);
        groupsScroll.setPrefHeight(250);

        VBox groupsCheckboxGroup = new VBox(12);
        groupsCheckboxGroup.setPadding(new Insets(12));

        List<CheckBox> groupCheckboxes = new ArrayList<>();
        for (OperationGroup group : allGroups) {
            CheckBox checkbox = new CheckBox(group.getName());
            checkbox.setUserData(group);
            if (selectedGroupIds.contains(group.getPrimaryKey())) {
                checkbox.setSelected(true);
            }
            groupCheckboxes.add(checkbox);
            groupsCheckboxGroup.getChildren().add(checkbox);
        }

        groupsScroll.setContent(groupsCheckboxGroup);
        groupsContent.getChildren().addAll(searchGroups, groupsScroll);
        groupsTab.setContent(groupsContent);

        // === AUTHORIZATION RULES TAB ===
        Tab rulesTab = new Tab();
        I18n.bindI18nTextProperty(rulesTab.textProperty(), OperationsAndRolesI18nKeys.Rules);

        VBox rulesContent = new VBox(12);
        rulesContent.setPadding(new Insets(16));

        // Search box for rules
        TextField searchRules = new TextField();
        I18n.bindI18nTextProperty(searchRules.promptTextProperty(), SearchRules);
        searchRules.setPrefWidth(USE_COMPUTED_SIZE);
        searchRules.setMaxWidth(Double.MAX_VALUE);
        searchRules.setPadding(new Insets(8, 12, 8, 12));

        // ScrollPane for rule checkboxes
        ScrollPane rulesScroll = new ScrollPane();
        rulesScroll.setFitToWidth(true);
        rulesScroll.setPrefHeight(250);

        VBox rulesCheckboxGroup = new VBox(12);
        rulesCheckboxGroup.setPadding(new Insets(12));

        List<CheckBox> ruleCheckboxes = new ArrayList<>();
        for (AuthorizationRule rule : allRules) {
            CheckBox checkbox = new CheckBox(rule.getName());
            checkbox.setUserData(rule);
            if (selectedRuleIds.contains(rule.getPrimaryKey())) {
                checkbox.setSelected(true);
            }
            ruleCheckboxes.add(checkbox);
            rulesCheckboxGroup.getChildren().add(checkbox);
        }

        rulesScroll.setContent(rulesCheckboxGroup);
        rulesContent.getChildren().addAll(searchRules, rulesScroll);
        rulesTab.setContent(rulesContent);

        selectionTabs.getTabs().addAll(groupsTab, operationsTab, rulesTab);

        // Add fields to form
        formFields.getChildren().addAll(nameFormField.container(), selectedItemsContainer, infoBanner, selectionTabs);

        // Create the entity to save
        AuthorizationRole roleToSave = (role != null && !isDuplicate)
            ? updateStore.updateEntity(role)
            : updateStore.insertEntity(AuthorizationRole.class);

        // Create validation support
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.addRequiredInput(nameInput);

        // Add validation that at least one operation, group, or rule must be selected
        validationSupport.addValidationRule(
            new BooleanBinding() {
                {
                    for (CheckBox cb : operationCheckboxes) {
                        bind(cb.selectedProperty());
                    }
                    for (CheckBox cb : groupCheckboxes) {
                        bind(cb.selectedProperty());
                    }
                    for (CheckBox cb : ruleCheckboxes) {
                        bind(cb.selectedProperty());
                    }
                }
                @Override
                protected boolean computeValue() {
                    return operationCheckboxes.stream().anyMatch(CheckBox::isSelected) ||
                           groupCheckboxes.stream().anyMatch(CheckBox::isSelected) ||
                           ruleCheckboxes.stream().anyMatch(CheckBox::isSelected);
                }
            },
            selectionTabs,
            I18nControls.newLabel(AtLeastOnePermissionError).textProperty()
        );

        // Method to update combined selected items display
        Runnable updateSelectedItemsDisplay = () -> {
            long opsCount = operationCheckboxes.stream().filter(CheckBox::isSelected).count();
            long groupsCount = groupCheckboxes.stream().filter(CheckBox::isSelected).count();
            long rulesCount = ruleCheckboxes.stream().filter(CheckBox::isSelected).count();
            long totalCount = opsCount + groupsCount + rulesCount;

            // Build dynamic text with proper pluralization
            String permissionText = totalCount == 1
                ? I18n.getI18nText(PermissionSingular)
                : I18n.getI18nText(PermissionPlural);
            String operationText = opsCount == 1
                ? I18n.getI18nText(OperationSingular)
                : I18n.getI18nText(OperationPlural);
            String groupText = groupsCount == 1
                ? I18n.getI18nText(GroupSingular)
                : I18n.getI18nText(GroupPlural);
            String ruleText = rulesCount == 1
                ? I18n.getI18nText(RuleSingular)
                : I18n.getI18nText(RulePlural);

            selectedCountLabel.setText(totalCount + " " + permissionText + " " + I18n.getI18nText(Selected) +
                " (" + opsCount + " " + operationText + ", " + groupsCount + " " + groupText + ", " + rulesCount + " " + ruleText + ")");

            selectedItemsPane.getChildren().clear();
            if (totalCount == 0) {
                Label empty = I18nControls.newLabel(NoPermissionsSelected);
                empty.getStyleClass().add("role-dialog-empty-message");
                selectedItemsPane.getChildren().add(empty);
            } else {
                // Add selected operations (blue chips)
                for (CheckBox cb : operationCheckboxes) {
                    if (cb.isSelected()) {
                        HBox chip = createRemovableChip(cb.getText(), () -> cb.setSelected(false), "operation");
                        selectedItemsPane.getChildren().add(chip);
                    }
                }
                // Add selected groups (yellow chips)
                for (CheckBox cb : groupCheckboxes) {
                    if (cb.isSelected()) {
                        HBox chip = createRemovableChip(cb.getText(), () -> cb.setSelected(false), "group");
                        selectedItemsPane.getChildren().add(chip);
                    }
                }
                // Add selected rules (green chips)
                for (CheckBox cb : ruleCheckboxes) {
                    if (cb.isSelected()) {
                        HBox chip = createRemovableChip(cb.getText(), () -> cb.setSelected(false), "rule");
                        selectedItemsPane.getChildren().add(chip);
                    }
                }
            }
        };

        // Add search filter listeners
        addSearchFilter(searchOperations, operationCheckboxes);
        addSearchFilter(searchGroups, groupCheckboxes);
        addSearchFilter(searchRules, ruleCheckboxes);

        // Add checkbox listeners
        addCheckboxListeners(operationCheckboxes, updateSelectedItemsDisplay);
        addCheckboxListeners(groupCheckboxes, updateSelectedItemsDisplay);
        addCheckboxListeners(ruleCheckboxes, updateSelectedItemsDisplay);

        // Initial display update
        updateSelectedItemsDisplay.run();

        // Footer buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = Bootstrap.button(I18nControls.newButton(BaseI18nKeys.Cancel));
        Object saveButtonKey = isEdit ? BaseI18nKeys.SaveChanges : (CreateRoleButton);
        Button saveButton = Bootstrap.successButton(I18nControls.newButton(saveButtonKey));

        // Disable save button when no operations, groups, or rules are selected
        BooleanBinding noSelectionBinding = new BooleanBinding() {
            {
                for (CheckBox cb : operationCheckboxes) {
                    bind(cb.selectedProperty());
                }
                for (CheckBox cb : groupCheckboxes) {
                    bind(cb.selectedProperty());
                }
                for (CheckBox cb : ruleCheckboxes) {
                    bind(cb.selectedProperty());
                }
            }
            @Override
            protected boolean computeValue() {
                return operationCheckboxes.stream().noneMatch(CheckBox::isSelected) &&
                       groupCheckboxes.stream().noneMatch(CheckBox::isSelected) &&
                       ruleCheckboxes.stream().noneMatch(CheckBox::isSelected);
            }
        };
        saveButton.disableProperty().bind(noSelectionBinding);

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
            // Validate form
            if (!validationSupport.isValid()) {
                return;
            }

            // Update role name
            roleToSave.setName(nameInput.getText().trim());

            // Delete existing role operations if editing (not duplicating)
            if (isEdit) {
                for (AuthorizationRoleOperation roleOp : existingRoleOperations) {
                    updateStore.deleteEntity(roleOp);
                }
                // Clear role assignment from previously assigned rules
                for (AuthorizationRule assignedRule : assignedRules) {
                    AuthorizationRule ruleToUpdate = updateStore.updateEntity(assignedRule);
                    ruleToUpdate.setRole(null);
                }
            }

            // Create new role operations for selected items
            for (CheckBox cb : operationCheckboxes) {
                if (cb.isSelected()) {
                    Operation operation = (Operation) cb.getUserData();
                    AuthorizationRoleOperation roleOp = updateStore.insertEntity(AuthorizationRoleOperation.class);
                    roleOp.setRole(roleToSave);
                    roleOp.setOperation(operation);
                }
            }

            for (CheckBox cb : groupCheckboxes) {
                if (cb.isSelected()) {
                    OperationGroup group = (OperationGroup) cb.getUserData();
                    AuthorizationRoleOperation roleOp = updateStore.insertEntity(AuthorizationRoleOperation.class);
                    roleOp.setRole(roleToSave);
                    roleOp.setOperationGroup(group);
                }
            }

            // Assign selected rules to this role
            for (CheckBox cb : ruleCheckboxes) {
                if (cb.isSelected()) {
                    AuthorizationRule rule = (AuthorizationRule) cb.getUserData();
                    AuthorizationRule ruleToUpdate = updateStore.updateEntity(rule);
                    ruleToUpdate.setRole(roleToSave);
                }
            }

            // Submit all changes
            updateStore.submitChanges().onSuccess(result -> {
                dialogCallback.closeDialog();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }).onFailure(error -> showErrorDialog(error.getMessage()));
        });
    }

    /**
     * Creates a removable chip component.
     * @param text The text to display on the chip
     * @param onRemove Callback when remove button is clicked
     * @param type Type of chip: "operation" (blue), "group" (yellow), or "rule" (green)
     */
    private static HBox createRemovableChip(String text, Runnable onRemove, String type) {
        HBox chip = new HBox(4);
        chip.setAlignment(Pos.CENTER);
        chip.setPadding(new Insets(6, 8, 6, 12));
        chip.getStyleClass().add("permission-chip-" + type);

        Label nameLabel = new Label(text);
        nameLabel.getStyleClass().add("permission-chip-label-" + type);

        Label removeBtn = new Label("×");
        removeBtn.getStyleClass().addAll(
            "permission-chip-remove-btn",
            "permission-chip-remove-btn-" + type
        );
        removeBtn.setOnMouseClicked(e -> onRemove.run());

        chip.getChildren().addAll(nameLabel, removeBtn);
        return chip;
    }

    /**
     * Shows an error dialog.
     */
    private static void showErrorDialog(String content) {
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        Label titleLabel = Bootstrap.strong(I18nControls.newLabel(BaseI18nKeys.Error));
        titleLabel.getStyleClass().add("error-dialog-title");

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
        Button okButton = Bootstrap.dangerButton(I18nControls.newButton(BaseI18nKeys.OK));
        footer.getChildren().add(okButton);
        dialogContent.getChildren().add(footer);

        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");
        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        okButton.setOnAction(e -> dialogCallback.closeDialog());
    }
}
