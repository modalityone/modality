package one.modality.crm.backoffice.activities.admin;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.platform.async.CompositeFuture;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.cloud.deepl.client.ClientDeeplService;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.application.Platform;
import javafx.beans.binding.BooleanExpression;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Label;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static one.modality.crm.backoffice.activities.admin.Admin18nKeys.*;

/**
 * Dialog for creating and editing labels with multilingual support.
 *
 * @author Claude Code
 */
public class LabelEditorDialog {

    private static final String[] LANGUAGES = {
        Label.en, Label.fr, Label.es, Label.de, Label.pt,
        Label.zhs, Label.zht, Label.el, Label.vi
    };

    private static final Map<String, String> DEEPL_LANG_MAP = new HashMap<>();
    static {
        DEEPL_LANG_MAP.put(Label.en, "EN");
        DEEPL_LANG_MAP.put(Label.fr, "FR");
        DEEPL_LANG_MAP.put(Label.es, "ES");
        DEEPL_LANG_MAP.put(Label.de, "DE");
        DEEPL_LANG_MAP.put(Label.pt, "PT");
        DEEPL_LANG_MAP.put(Label.zhs, "ZH-HANS");
        DEEPL_LANG_MAP.put(Label.zht, "ZH-HANT");
        DEEPL_LANG_MAP.put(Label.el, "EL");
        DEEPL_LANG_MAP.put(Label.vi, "VI");
    }

    private static final Map<String, String> LANGUAGE_NAMES = new HashMap<>();
    static {
        LANGUAGE_NAMES.put(Label.en, "English");
        LANGUAGE_NAMES.put(Label.fr, "French");
        LANGUAGE_NAMES.put(Label.es, "Spanish");
        LANGUAGE_NAMES.put(Label.de, "German");
        LANGUAGE_NAMES.put(Label.pt, "Portuguese");
        LANGUAGE_NAMES.put(Label.zhs, "Chinese (Simplified)");
        LANGUAGE_NAMES.put(Label.zht, "Chinese (Traditional)");
        LANGUAGE_NAMES.put(Label.el, "Greek");
        LANGUAGE_NAMES.put(Label.vi, "Vietnamese");
    }

    public static void show(Label existingLabel, ButtonFactoryMixin buttonFactory, EntityStore entityStore, Runnable onSaveCallback) {
        show(existingLabel, buttonFactory, entityStore, onSaveCallback, null);
    }

    public static void show(Label existingLabel, ButtonFactoryMixin buttonFactory, EntityStore entityStore, Runnable onSaveCallback, LabelUsageDetails usageDetails) {
        UpdateStore updateStore = UpdateStore.createAbove(entityStore);

        // Create or get the label entity
        Label labelEntity;
        boolean isNew = existingLabel == null;
        if (isNew) {
            labelEntity = updateStore.insertEntity(Label.class);
            labelEntity.setFieldValue("organization", FXOrganizationId.getOrganizationId());
            labelEntity.setRef(Label.en); // Default reference language
        } else {
            labelEntity = updateStore.updateEntity(existingLabel);
        }

        // Store usage details for later use
        final LabelUsageDetails labelUsageDetails = usageDetails;
        final int labelUsageCount = usageDetails != null ? usageDetails.getTotalCount() : 0;

        // Main dialog container
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30, 40, 30, 40));
        dialogContent.setMinWidth(700);
        dialogContent.setPrefWidth(800);
        dialogContent.setMaxWidth(900);

        // Title
        javafx.scene.control.Label titleLabel = I18nControls.newLabel(isNew ? CreateLabel : EditLabel);
        titleLabel.getStyleClass().add("labeleditor-dialog-title");
        Bootstrap.h2(titleLabel);

        // Language fields container (created first so reference selector can update highlighting)
        VBox languageFieldsContainer = new VBox(12);
        languageFieldsContainer.setPadding(new Insets(10, 0, 10, 0));

        Map<String, TextArea> languageFields = new HashMap<>();

        for (String lang : LANGUAGES) {
            VBox fieldContainer = createLanguageField(lang, labelEntity, languageFields);
            languageFieldsContainer.getChildren().add(fieldContainer);
        }

        // Reference language selector (needs languageFieldsContainer for highlight updates)
        HBox refLangContainer = createReferenceLangSelector(labelEntity, languageFieldsContainer);

        // Separator
        Separator separator1 = new Separator();

        // Language fields container with scroll
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPrefHeight(400);
        scrollPane.setMaxHeight(500);

        scrollPane.setContent(languageFieldsContainer);

        // Action buttons (Auto-translate, Clear All)
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);

        Button autoTranslateButton = Bootstrap.primaryButton(I18nControls.newButton(AutoTranslate));
        Button clearAllButton = Bootstrap.secondaryButton(I18nControls.newButton(ClearAll));

        autoTranslateButton.setOnAction(e -> autoTranslate(labelEntity, languageFields, autoTranslateButton, clearAllButton));
        clearAllButton.setOnAction(e -> clearAllFields(labelEntity, languageFields));

        actionButtons.getChildren().addAll(autoTranslateButton, clearAllButton);

        // Footer with Cancel and Save
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(20, 0, 0, 0));

        Button cancelButton = Bootstrap.button(I18nControls.newButton(Cancel));
        Button saveButton = Bootstrap.successButton(I18nControls.newButton(Save));

        // Bind save button disabled state to hasChanges - enabled only when there are changes
        saveButton.disableProperty().bind(EntityBindings.hasChangesProperty(updateStore).not());

        footer.getChildren().addAll(cancelButton, saveButton);

        // Add all to dialog
        dialogContent.getChildren().addAll(
            titleLabel,
            refLangContainer,
            separator1,
            actionButtons,
            scrollPane,
            footer
        );

        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");

        DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(
            dialogPane, FXMainFrameDialogArea.getDialogArea()
        );

        cancelButton.setOnAction(e -> dialogCallback.closeDialog());

        saveButton.setOnAction(e -> {
            // Fields are synced to entity in real-time via listeners, so no need to sync here

            // If editing an existing label that's in use, ask user what to do
            if (!isNew && labelUsageCount > 0 && labelUsageDetails != null) {
                showUsageConfirmationDialog(labelUsageDetails, updateStore, labelEntity, entityStore,
                    dialogCallback, saveButton, cancelButton, onSaveCallback);
            } else {
                // No usages or new label - just save directly
                performSave(updateStore, dialogCallback, saveButton, cancelButton, onSaveCallback);
            }
        });

        // Populate fields with existing data
        if (!isNew) {
            populateFields(labelEntity, languageFields);
        }

        // Update field highlighting based on reference language
        updateRefLanguageHighlight(labelEntity.getRef(), languageFieldsContainer);
    }

    private static HBox createReferenceLangSelector(Label labelEntity, VBox languageFieldsContainer) {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(10, 0, 10, 0));

        javafx.scene.control.Label label = I18nControls.newLabel(ReferenceLanguage);
        Bootstrap.strong(label);

        ToggleGroup refLangGroup = new ToggleGroup();
        HBox buttonContainer = new HBox(5);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);

        String currentRef = labelEntity.getRef();
        if (currentRef == null || currentRef.isEmpty()) {
            currentRef = Label.en;
        }

        for (String lang : LANGUAGES) {
            ToggleButton btn = new ToggleButton(lang.toUpperCase());
            btn.setToggleGroup(refLangGroup);
            btn.setUserData(lang);
            btn.setPadding(new Insets(4, 8, 4, 8));
            btn.getStyleClass().add("labeleditor-lang-toggle");
            if (lang.equals(currentRef)) {
                btn.setSelected(true);
            }
            buttonContainer.getChildren().add(btn);
        }

        // Update entity and highlight when reference language changes
        refLangGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String newRef = (String) newVal.getUserData();
                labelEntity.setRef(newRef);
                updateRefLanguageHighlight(newRef, languageFieldsContainer);
            }
        });

        container.getChildren().addAll(label, buttonContainer);
        return container;
    }

    private static VBox createLanguageField(String lang, Label labelEntity, Map<String, TextArea> languageFields) {
        VBox container = new VBox(5);
        container.setPadding(new Insets(5, 15, 5, 15));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        javafx.scene.control.Label langLabel = new javafx.scene.control.Label(
            lang.toUpperCase() + " - " + LANGUAGE_NAMES.get(lang)
        );
        Bootstrap.strong(langLabel);
        langLabel.setMinWidth(200);

        header.getChildren().add(langLabel);

        TextArea textArea = new TextArea();
        I18n.bindI18nTextProperty(textArea.promptTextProperty(), EnterTextIn, LANGUAGE_NAMES.get(lang));
        textArea.setPrefRowCount(2);
        textArea.setWrapText(true);
        textArea.setMaxHeight(80);

        // Sync text changes to entity immediately so hasChangesProperty detects them
        textArea.textProperty().addListener((obs, oldVal, newVal) -> {
            String value = (newVal != null && !newVal.trim().isEmpty()) ? newVal.trim() : null;
            syncFieldToEntity(lang, value, labelEntity);
        });

        languageFields.put(lang, textArea);

        container.getChildren().addAll(header, textArea);
        container.setUserData(lang); // Store language code for highlighting

        return container;
    }

    private static void syncFieldToEntity(String lang, String value, Label labelEntity) {
        switch (lang) {
            case Label.en -> labelEntity.setEn(value);
            case Label.fr -> labelEntity.setFr(value);
            case Label.es -> labelEntity.setEs(value);
            case Label.de -> labelEntity.setDe(value);
            case Label.pt -> labelEntity.setPt(value);
            case Label.zhs -> labelEntity.setZhs(value);
            case Label.zht -> labelEntity.setZht(value);
            case Label.el -> labelEntity.setEl(value);
            case Label.vi -> labelEntity.setVi(value);
        }
    }

    private static void populateFields(Label labelEntity, Map<String, TextArea> languageFields) {
        setFieldText(languageFields, Label.en, labelEntity.getEn());
        setFieldText(languageFields, Label.fr, labelEntity.getFr());
        setFieldText(languageFields, Label.es, labelEntity.getEs());
        setFieldText(languageFields, Label.de, labelEntity.getDe());
        setFieldText(languageFields, Label.pt, labelEntity.getPt());
        setFieldText(languageFields, Label.zhs, labelEntity.getZhs());
        setFieldText(languageFields, Label.zht, labelEntity.getYue());
        setFieldText(languageFields, Label.el, labelEntity.getEl());
        setFieldText(languageFields, Label.vi, labelEntity.getVi());
    }

    private static void setFieldText(Map<String, TextArea> fields, String lang, String text) {
        TextArea field = fields.get(lang);
        if (field != null && text != null) {
            field.setText(text);
        }
    }

    private static void syncFieldsToEntity(Label labelEntity, Map<String, TextArea> languageFields) {
        labelEntity.setEn(getFieldText(languageFields, Label.en));
        labelEntity.setFr(getFieldText(languageFields, Label.fr));
        labelEntity.setEs(getFieldText(languageFields, Label.es));
        labelEntity.setDe(getFieldText(languageFields, Label.de));
        labelEntity.setPt(getFieldText(languageFields, Label.pt));
        labelEntity.setZhs(getFieldText(languageFields, Label.zhs));
        labelEntity.setZht(getFieldText(languageFields, Label.zht));
        labelEntity.setEl(getFieldText(languageFields, Label.el));
        labelEntity.setVi(getFieldText(languageFields, Label.vi));
    }

    private static String getFieldText(Map<String, TextArea> fields, String lang) {
        TextArea field = fields.get(lang);
        if (field != null) {
            String text = field.getText();
            return (text != null && !text.trim().isEmpty()) ? text.trim() : null;
        }
        return null;
    }

    private static void autoTranslate(Label labelEntity, Map<String, TextArea> languageFields,
                                        Button autoTranslateButton, Button clearAllButton) {
        String refLang = labelEntity.getRef();
        if (refLang == null || refLang.isEmpty()) {
            refLang = Label.en;
        }

        TextArea refField = languageFields.get(refLang);
        if (refField == null) {
            showError(I18n.getI18nText(TranslationError) + ": Reference language field not found.");
            return;
        }

        String sourceText = refField.getText();
        if (sourceText == null || sourceText.trim().isEmpty()) {
            showError(I18n.getI18nText(TranslationErrorEmptySource));
            return;
        }

        String sourceLangCode = DEEPL_LANG_MAP.get(refLang);
        final String finalRefLang = refLang;

        // Collect all translation futures
        List<Future<?>> translationFutures = new ArrayList<>();
        List<String> targetLanguages = new ArrayList<>(); // Track which languages we're translating

        for (String targetLang : LANGUAGES) {
            if (!targetLang.equals(finalRefLang)) {
                TextArea targetField = languageFields.get(targetLang);
                if (targetField != null && (targetField.getText() == null || targetField.getText().trim().isEmpty())) {
                    String targetLangCode = DEEPL_LANG_MAP.get(targetLang);
                    if (targetLangCode != null) {
                        targetLanguages.add(targetLang);
                        Future<String> translationFuture = ClientDeeplService.translate(sourceText, sourceLangCode, targetLangCode)
                            .onSuccess(text -> Platform.runLater(() -> {
                                if (text != null && !text.trim().isEmpty()) {
                                    targetField.setText(text);
                                }
                            }));
                        translationFutures.add(translationFuture);
                    }
                }
            }
        }

        // If no translations needed
        if (translationFutures.isEmpty()) {
            return;
        }

        // Use Future.join() to wait for all translations (doesn't fail fast)
        CompositeFuture allTranslations = Future.join(translationFutures);

        // Use AsyncSpinner to show spinner and disable buttons during translation
        AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
            allTranslations
                .onComplete(ar -> Platform.runLater(() -> {
                    // Check for any failures and display them
                    if (ar.failed() || hasAnyFailure(allTranslations)) {
                        StringBuilder errorMessages = new StringBuilder();
                        for (int i = 0; i < allTranslations.size(); i++) {
                            if (allTranslations.failed(i)) {
                                Throwable cause = allTranslations.cause(i);
                                String langName = i < targetLanguages.size() ? LANGUAGE_NAMES.get(targetLanguages.get(i)) : "Unknown";
                                if (errorMessages.length() > 0) {
                                    errorMessages.append("\n");
                                }
                                errorMessages.append("• ").append(langName).append(": ")
                                    .append(cause != null ? cause.getMessage() : "Unknown error");
                                Console.log("Translation failed for " + langName + ": " +
                                    (cause != null ? cause.getMessage() : "Unknown error"));
                            }
                        }
                        if (errorMessages.length() > 0) {
                            showError(I18n.getI18nText(TranslationError) + ":\n" + errorMessages);
                        }
                    }
                })),
            autoTranslateButton, clearAllButton
        );
    }

    private static boolean hasAnyFailure(CompositeFuture cf) {
        for (int i = 0; i < cf.size(); i++) {
            if (cf.failed(i)) {
                return true;
            }
        }
        return false;
    }

    private static void clearAllFields(Label labelEntity, Map<String, TextArea> languageFields) {
        String refLang = labelEntity.getRef();
        if (refLang == null || refLang.isEmpty()) {
            refLang = Label.en;
        }

        for (String lang : LANGUAGES) {
            if (!lang.equals(refLang)) {
                TextArea field = languageFields.get(lang);
                if (field != null) {
                    field.clear();
                }
            }
        }
    }

    private static void updateRefLanguageHighlight(String refLang, VBox languageFieldsContainer) {
        if (refLang == null || refLang.isEmpty()) {
            refLang = Label.en;
        }

        for (javafx.scene.Node node : languageFieldsContainer.getChildren()) {
            if (node instanceof VBox fieldContainer) {
                String lang = (String) fieldContainer.getUserData();
                if (lang != null) {
                    if (lang.equals(refLang)) {
                        fieldContainer.getStyleClass().add("labeleditor-ref-lang-highlight");
                    } else {
                        fieldContainer.getStyleClass().remove("labeleditor-ref-lang-highlight");
                    }
                }
            }
        }
    }

    private static void showError(String message) {
        Platform.runLater(() -> {
            VBox dialogContent = new VBox(15);
            dialogContent.setPadding(new Insets(20));
            dialogContent.setMinWidth(300);
            dialogContent.setPrefWidth(400);

            javafx.scene.control.Label titleLabel = I18nControls.newLabel(Error);
            titleLabel.getStyleClass().add("labeleditor-error-dialog-title");
            Bootstrap.h3(titleLabel);

            javafx.scene.control.Label messageLabel = new javafx.scene.control.Label(message);
            messageLabel.setWrapText(true);

            Button okButton = Bootstrap.dangerButton(I18nControls.newButton(OK));

            HBox footer = new HBox();
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.getChildren().add(okButton);

            dialogContent.getChildren().addAll(titleLabel, messageLabel, footer);

            BorderPane dialogPane = new BorderPane(dialogContent);
            dialogPane.getStyleClass().add("modal-dialog-pane");

            DialogCallback callback = DialogUtil.showModalNodeInGoldLayout(
                dialogPane, FXMainFrameDialogArea.getDialogArea()
            );

            okButton.setOnAction(e -> callback.closeDialog());
        });
    }

    private static void performSave(UpdateStore updateStore, DialogCallback dialogCallback,
                                    Button saveButton, Button cancelButton, Runnable onSaveCallback) {
        BooleanExpression hasChangesExpression = EntityBindings.hasChangesProperty(updateStore);

        // Unbind disableProperty before AsyncSpinner
        saveButton.disableProperty().unbind();

        // Submit changes with async spinner
        AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
            updateStore.submitChanges()
                .onSuccess(result -> Platform.runLater(() -> {
                    saveButton.disableProperty().bind(hasChangesExpression.not());
                    dialogCallback.closeDialog();
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                }))
                .onFailure(error -> Platform.runLater(() -> {
                    saveButton.disableProperty().bind(hasChangesExpression.not());
                    Console.log("Failed to save label: " + error.getMessage());
                    showError(I18n.getI18nText(FailedToSaveLabel) + ": " + error.getMessage());
                })),
            saveButton, cancelButton
        );
    }

    private static void showUsageConfirmationDialog(LabelUsageDetails usageDetails, UpdateStore updateStore, Label labelEntity,
                                                    EntityStore entityStore, DialogCallback parentDialogCallback,
                                                    Button saveButton, Button cancelButton, Runnable onSaveCallback) {
        int usageCount = usageDetails.getTotalCount();

        VBox dialogContent = new VBox(16);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setMinWidth(450);
        dialogContent.setPrefWidth(550);
        dialogContent.setMaxWidth(650);

        // Title
        javafx.scene.control.Label titleLabel = I18nControls.newLabel(LabelInUseTitle);
        Bootstrap.h3(titleLabel);

        // Warning message
        javafx.scene.control.Label messageLabel = new javafx.scene.control.Label();
        messageLabel.textProperty().bind(I18n.i18nTextProperty(LabelInUseMessage, usageCount));
        messageLabel.setWrapText(true);
        Bootstrap.textWarning(messageLabel);

        // Usage breakdown panel showing where the label is used
        VBox usageBreakdownPanel = LabelEditorRenderers.createUsageBreakdownPanel(usageDetails);

        // Question
        javafx.scene.control.Label questionLabel = I18nControls.newLabel(LabelInUseQuestion);
        questionLabel.setWrapText(true);

        // Option buttons
        VBox optionsBox = new VBox(12);
        optionsBox.setPadding(new Insets(10, 0, 10, 0));

        Button updateExistingButton = Bootstrap.primaryButton(I18nControls.newButton(UpdateExistingLabel));
        updateExistingButton.setMaxWidth(Double.MAX_VALUE);

        Button createCopyButton = Bootstrap.successButton(I18nControls.newButton(CreateLabelCopy));
        createCopyButton.setMaxWidth(Double.MAX_VALUE);

        Button cancelConfirmButton = Bootstrap.button(I18nControls.newButton(Cancel));
        cancelConfirmButton.setMaxWidth(Double.MAX_VALUE);

        optionsBox.getChildren().addAll(updateExistingButton, createCopyButton, cancelConfirmButton);

        dialogContent.getChildren().addAll(titleLabel, messageLabel, usageBreakdownPanel, questionLabel, optionsBox);

        BorderPane dialogPane = new BorderPane(dialogContent);
        dialogPane.getStyleClass().add("modal-dialog-pane");

        DialogCallback confirmDialogCallback = DialogUtil.showModalNodeInGoldLayout(
            dialogPane, FXMainFrameDialogArea.getDialogArea()
        );

        // Update existing - save changes to current label
        updateExistingButton.setOnAction(e -> {
            confirmDialogCallback.closeDialog();
            performSave(updateStore, parentDialogCallback, saveButton, cancelButton, onSaveCallback);
        });

        // Create copy - create new label with current values
        createCopyButton.setOnAction(e -> {
            confirmDialogCallback.closeDialog();
            createLabelCopy(labelEntity, entityStore, parentDialogCallback, saveButton, cancelButton, onSaveCallback);
        });

        // Cancel - close confirmation dialog, return to edit dialog
        cancelConfirmButton.setOnAction(e -> confirmDialogCallback.closeDialog());
    }

    private static void createLabelCopy(Label sourceLabel, EntityStore entityStore, DialogCallback parentDialogCallback,
                                        Button saveButton, Button cancelButton, Runnable onSaveCallback) {
        UpdateStore copyUpdateStore = UpdateStore.createAbove(entityStore);

        // Create a new label entity with copied values
        Label newLabel = copyUpdateStore.insertEntity(Label.class);
        newLabel.setFieldValue("organization", FXOrganizationId.getOrganizationId());
        newLabel.setRef(sourceLabel.getRef());
        newLabel.setEn(sourceLabel.getEn());
        newLabel.setFr(sourceLabel.getFr());
        newLabel.setEs(sourceLabel.getEs());
        newLabel.setDe(sourceLabel.getDe());
        newLabel.setPt(sourceLabel.getPt());
        newLabel.setZhs(sourceLabel.getZhs());
        newLabel.setZht(sourceLabel.getYue());
        newLabel.setEl(sourceLabel.getEl());
        newLabel.setVi(sourceLabel.getVi());

        BooleanExpression hasChangesExpression = EntityBindings.hasChangesProperty(copyUpdateStore);

        // Unbind disableProperty before AsyncSpinner
        saveButton.disableProperty().unbind();

        // Submit the new label
        AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
            copyUpdateStore.submitChanges()
                .onSuccess(result -> Platform.runLater(() -> {
                    saveButton.disableProperty().bind(hasChangesExpression.not());
                    parentDialogCallback.closeDialog();
                    if (onSaveCallback != null) {
                        onSaveCallback.run();
                    }
                }))
                .onFailure(error -> Platform.runLater(() -> {
                    saveButton.disableProperty().bind(hasChangesExpression.not());
                    Console.log("Failed to create label copy: " + error.getMessage());
                    showError(I18n.getI18nText(FailedToSaveLabel) + ": " + error.getMessage());
                })),
            saveButton, cancelButton
        );
    }
}
