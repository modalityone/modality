package one.modality.crm.backoffice.activities.admin;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Operation;

import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;
import static one.modality.crm.backoffice.activities.admin.Admin18nKeys.*;

/**
 * Dialog for creating and editing operations and routes.
 *
 * @author Claude Code
 */
public class OperationDialog {

    /**
     * Shows the create/edit operation or route dialog.
     *
     * @param operation Existing operation/route to edit (null for new operation/route)
     * @param isRoute True if this is a route (enforces "Route" prefix), false for regular operation (only used when creating)
     * @param onSuccess Callback to execute after successful save
     */
    public static void show(Entity operation, boolean isRoute, Runnable onSuccess) {
        boolean isEdit = operation != null;

        // When editing, determine if it's a route based on the entity itself
        final boolean isRouteEntity;
        if (isEdit) {
            String operationCode = operation.getStringFieldValue("operationCode");
            isRouteEntity = operationCode != null && operationCode.startsWith("Route");
        } else {
            isRouteEntity = isRoute;
        }

        // Main dialog container
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(24));
        dialogContent.setMinWidth(500);
        dialogContent.setPrefWidth(700);
        dialogContent.setMaxWidth(900);

        // Header with title
        Object titleKey = isEdit
            ? (isRouteEntity ? EditRouteTitle : EditOperationTitle)
            : (isRouteEntity ? CreateRouteTitle : CreateOperationTitle);
        Label titleLabel = Bootstrap.strong(I18nControls.newLabel(titleKey));
        titleLabel.getStyleClass().add("modal-title");

        // Form fields
        VBox formFields = new VBox(20);
        formFields.setMaxWidth(Double.MAX_VALUE);

        // Operation/Route Name field (required)
        VBox nameField = createFormField(
            isRouteEntity ? RouteName : OperationName,
            isRouteEntity ? RouteNamePlaceholder : OperationNamePlaceholder,
            isRouteEntity ? RouteNameHelp : null
        );
        TextField nameInput = (TextField) ((VBox) nameField.getChildren().get(1)).getChildren().get(0);
        if (isEdit) {
            nameInput.setText(operation.getStringFieldValue("name"));
        } else if (isRouteEntity) {
            // Pre-populate with "Route" for new routes
            nameInput.setText("Route");
        }

        // Operation Code field (required)
        VBox codeField = createFormField(
            OperationCode,
            isRouteEntity ? RouteCodePlaceholder : OperationCodePlaceholder,
            OperationCodeHelp
        );
        TextField codeInput = (TextField) ((VBox) codeField.getChildren().get(1)).getChildren().get(0);
        if (isEdit) {
            codeInput.setText(operation.getStringFieldValue("operationCode"));
        }

        // Grant Route field (only for routes)
        VBox routeField = null;
        TextField routeInput = null;
        if (isRouteEntity) {
            routeField = createFormField(
                GrantRoute,
                GrantRoutePlaceholder,
                GrantRouteHelp
            );
            routeInput = (TextField) ((VBox) routeField.getChildren().get(1)).getChildren().get(0);
            if (isEdit) {
                String grantRoute = operation.getStringFieldValue("grantRoute");
                if (grantRoute != null) {
                    routeInput.setText(grantRoute);
                }
            }
        }

        // i18nCode field
        VBox i18nField = createFormField(
            I18nCodeLabel,
            I18nCodePlaceholder,
            I18nCodeHelp
        );
        TextField i18nInput = (TextField) ((VBox) i18nField.getChildren().get(1)).getChildren().get(0);
        if (isEdit) {
            String i18nCode = operation.getStringFieldValue("i18nCode");
            if (i18nCode != null) {
                i18nInput.setText(i18nCode);
            }
        }

        // Access Settings checkboxes
        VBox accessSettings = new VBox(12);
        Label accessLabel = I18nControls.newLabel(AccessSettings);
        accessLabel.getStyleClass().add("form-field-label");

        GridPane checkboxGrid = new GridPane();
        checkboxGrid.setHgap(12);
        checkboxGrid.setVgap(12);

        CheckBox backendCheck = I18nControls.newCheckBox(Backend);
        CheckBox frontendCheck = I18nControls.newCheckBox(Frontend);
        CheckBox guestCheck = I18nControls.newCheckBox(GuestAccess);
        CheckBox publicCheck = I18nControls.newCheckBox(PublicAccess);

        if (isEdit) {
            backendCheck.setSelected(operation.getBooleanFieldValue("backend"));
            frontendCheck.setSelected(operation.getBooleanFieldValue("frontend"));
            guestCheck.setSelected(operation.getBooleanFieldValue("guest"));
            publicCheck.setSelected(operation.getBooleanFieldValue("public"));
        } else {
            backendCheck.setSelected(true); // Default to backend checked
        }

        checkboxGrid.add(backendCheck, 0, 0);
        checkboxGrid.add(frontendCheck, 1, 0);
        checkboxGrid.add(guestCheck, 0, 1);
        checkboxGrid.add(publicCheck, 1, 1);

        accessSettings.getChildren().addAll(accessLabel, checkboxGrid);

        // Add fields conditionally based on whether this is a route or operation
        formFields.getChildren().addAll(nameField, codeField);
        if (isRouteEntity) {
            formFields.getChildren().add(routeField);
        }
        formFields.getChildren().addAll(i18nField, accessSettings);

        // Create entity store and update store before buttons
        EntityStore store = operation != null ? operation.getStore() : EntityStore.create(DataSourceModelService.getDefaultDataSourceModel());
        UpdateStore updateStore = UpdateStore.createAbove(store);
        Operation operationToSave = operation != null ? updateStore.updateEntity((Operation) operation) : updateStore.insertEntity(Operation.class);

        // Create validation support and add required field validations
        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.addRequiredInput(nameInput);
        validationSupport.addRequiredInput(codeInput);

        // Add alphanumeric validation for code (letters and numbers only, no spaces)
        validationSupport.addAlphanumericNoSpacesValidation(
            codeInput,
            codeInput,
            I18nControls.newLabel(OperationCodeValidationError).textProperty()
        );

        // Make grantRoute required for routes
        if (isRouteEntity) {
            validationSupport.addRequiredInput(routeInput);
        }

        // Add custom validation to ensure code starts with "Route" for routes
        if (isRouteEntity) {
            validationSupport.addValidationRule(
                Bindings.createBooleanBinding(() -> {
                    String code = codeInput.getText();
                    return code != null && code.trim().startsWith("Route");
                }, codeInput.textProperty()),
                codeInput,
                I18nControls.newLabel(RouteCodeValidationError).textProperty()
            );
        }

        // Create a BooleanBinding that checks if updateStore has no changes
        BooleanBinding hasNoChangesBinding = new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return !updateStore.hasChanges();
            }
        };

        // Add listeners to form fields to update entity and invalidate binding
        nameInput.textProperty().addListener((obs, oldVal, newVal) -> {
            operationToSave.setFieldValue("name", newVal.trim());
            hasNoChangesBinding.invalidate();
        });

        codeInput.textProperty().addListener((obs, oldVal, newVal) -> {
            operationToSave.setFieldValue("operationCode", newVal.trim());
            hasNoChangesBinding.invalidate();
        });

        // Only add routeInput listener if this is a route
        if (isRouteEntity && routeInput != null) {
            routeInput.textProperty().addListener((obs, oldVal, newVal) -> {
                operationToSave.setFieldValue("grantRoute", newVal.trim().isEmpty() ? null : newVal.trim());
                hasNoChangesBinding.invalidate();
            });
        }

        i18nInput.textProperty().addListener((obs, oldVal, newVal) -> {
            operationToSave.setFieldValue("i18nCode", newVal.trim().isEmpty() ? null : newVal.trim());
            hasNoChangesBinding.invalidate();
        });

        backendCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            operationToSave.setFieldValue("backend", newVal);
            hasNoChangesBinding.invalidate();
        });

        frontendCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            operationToSave.setFieldValue("frontend", newVal);
            hasNoChangesBinding.invalidate();
        });

        guestCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            operationToSave.setFieldValue("guest", newVal);
            hasNoChangesBinding.invalidate();
        });

        publicCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            operationToSave.setFieldValue("public", newVal);
            hasNoChangesBinding.invalidate();
        });

        // Footer buttons
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = Bootstrap.button(I18nControls.newButton(Cancel));
        Object saveButtonKey = isEdit ? SaveChanges : (isRouteEntity ? CreateRouteButton : CreateOperationButton);
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

            // Submit changes (field values already set via listeners)
            updateStore.submitChanges().onSuccess(result -> {
                dialogCallback.closeDialog();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            }).onFailure(error -> {
                // Show error dialog
                Object errorHeaderKey = isRouteEntity ? FailedToSaveRoute : FailedToSaveOperation;
                showErrorDialog(errorHeaderKey, error.getMessage());
            });
        });
    }


    /**
     * Creates a form field with label, input, and optional help text.
     */
    private static VBox createFormField(Object labelKey, Object placeholderKey, Object helpKey) {
        VBox field = new VBox(8);
        field.setMaxWidth(Double.MAX_VALUE);

        Label label = I18nControls.newLabel(labelKey);
        label.getStyleClass().add("form-field-label");

        VBox inputContainer = new VBox(4);
        inputContainer.setMaxWidth(Double.MAX_VALUE);

        TextField input = new TextField();
        I18n.bindI18nTextProperty(input.promptTextProperty(), placeholderKey);
        input.setMinWidth(200);
        input.setPrefWidth(USE_COMPUTED_SIZE);
        input.setMaxWidth(Double.MAX_VALUE);
        input.setPadding(new Insets(10, 12, 10, 12));
        input.getStyleClass().add("form-field-input");

        // Make input grow horizontally
        HBox.setHgrow(input, Priority.ALWAYS);
        VBox.setVgrow(input, Priority.NEVER);

        inputContainer.getChildren().add(input);

        if (helpKey != null) {
            Label help = I18nControls.newLabel(helpKey);
            help.getStyleClass().add("form-field-help");
            help.setWrapText(true);
            help.setMaxWidth(Double.MAX_VALUE);
            inputContainer.getChildren().add(help);
        }

        field.getChildren().addAll(label, inputContainer);
        return field;
    }

    /**
     * Shows an error dialog with the specified title, header, and content.
     */
    private static void showErrorDialog(Object headerKey, String content) {
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(350);
        dialogContent.setPrefWidth(500);
        dialogContent.setMaxWidth(700);

        // Title
        Label titleLabel = Bootstrap.strong(I18nControls.newLabel(Admin18nKeys.Error));
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
