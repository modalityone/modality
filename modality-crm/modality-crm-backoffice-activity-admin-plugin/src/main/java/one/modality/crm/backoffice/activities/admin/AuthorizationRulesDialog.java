package one.modality.crm.backoffice.activities.admin;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.AuthorizationRule;

import static one.modality.crm.backoffice.activities.admin.Admin18nKeys.*;
import static one.modality.crm.backoffice.activities.admin.FormFieldHelper.*;

/**
 * Dialog for creating and editing authorization rules.
 *
 * @author Claude Code
 */
public class AuthorizationRulesDialog {

    /**
     * Shows the creation/edit authorization rule dialog.
     *
     * @param rule Existing rule to edit (null for new rule)
     * @param onSuccess Callback to execute after successful save
     */
    public static void show(Entity rule, Runnable onSuccess) {
        boolean isEdit = rule != null;

        // Create EntityStore and UpdateStore
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);

        // Create or update entity
        AuthorizationRule ruleToSave;
        if (isEdit) {
            ruleToSave = updateStore.updateEntity((AuthorizationRule) rule);
        } else {
            ruleToSave = updateStore.insertEntity(AuthorizationRule.class);
        }

        // Main dialog container
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(24));
        dialogContent.setMinWidth(500);
        dialogContent.setPrefWidth(700);
        dialogContent.setMaxWidth(900);

        // Header with title
        Object titleKey = isEdit ? EditRuleTitle : CreateRuleTitle;
        Label titleLabel = Bootstrap.strong(I18nControls.newLabel(titleKey));
        titleLabel.getStyleClass().add("modal-title");

        // Form fields
        VBox formFields = new VBox(20);
        formFields.setMaxWidth(Double.MAX_VALUE);

        // Rule Name field (required)
        FormField<TextField> nameFormField = createTextField(RuleName, RuleNamePlaceholder, null);
        TextField nameInput = nameFormField.inputField();
        if (isEdit) {
            nameInput.setText(rule.getStringFieldValue("name"));
        }

        // Rule Expression field (required)
        FormField<TextArea> ruleFormField = createTextArea(RuleExpression, RuleExpressionPlaceholder, RuleExpressionHelp, 4);
        TextArea ruleInput = ruleFormField.inputField();
        if (isEdit) {
            String ruleExpression = rule.getStringFieldValue("rule");
            if (ruleExpression != null) {
                ruleInput.setText(ruleExpression);
            }
        }

        // Add form fields to container
        formFields.getChildren().addAll(nameFormField.container(), ruleFormField.container());

        // Validation
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.addRequiredInput(nameInput);
        validationSupport.addRequiredInput(ruleInput);

        // Create a BooleanBinding that checks if updateStore has no changes
        BooleanBinding hasNoChangesBinding = new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return !updateStore.hasChanges();
            }
        };

        // Add listeners to form fields to update entity and invalidate binding
        nameInput.textProperty().addListener((obs, oldVal, newVal) -> {
            ruleToSave.setFieldValue("name", newVal.trim());
            hasNoChangesBinding.invalidate();
        });

        ruleInput.textProperty().addListener((obs, oldVal, newVal) -> {
            ruleToSave.setFieldValue("rule", newVal.trim());
            hasNoChangesBinding.invalidate();
        });

        // Footer buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = Bootstrap.button(I18nControls.newButton(Cancel));
        Object saveButtonKey = isEdit ? SaveChanges : CreateRuleButton;
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
                // Show validation error dialog
                showErrorDialog(ValidationError, I18n.getI18nText(RequiredFieldsMissing));
                return;
            }

            // Submit changes (field values already set via listeners)
            updateStore.submitChanges().onSuccess(result -> {
                // Close dialog on success
                dialogCallback.closeDialog();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }).onFailure(error -> {
                // Show error dialog but keep the form dialog open so user can fix the issue
                showErrorDialog(FailedToSaveRule, error.getMessage());
            });
        });
    }

    /**
     * Shows an error dialog with the specified content.
     */
    private static void showErrorDialog(Object headerKey, String content) {
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        // Title
        Label titleLabel = Bootstrap.strong(I18nControls.newLabel(Error));
        titleLabel.getStyleClass().add("error-dialog-title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        // Header
        Label headerLabel = I18nControls.newLabel(headerKey);
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

        Button okButton = Bootstrap.dangerButton(I18nControls.newButton(OK));

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
