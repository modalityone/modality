package one.modality.base.client.i18n;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.ReadOnlyAstArray;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.fetch.json.JsonFetch;
import dev.webfx.platform.scheduler.Scheduler;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author David Hello
 */
public class LabelTextField {

    private final String databaseSimpleFieldName;
    private final String databaseLabelFieldName;
    private Entity currentEntity;
    private VBox container;
    private TextField mainTextField;
    private Button translationButton;
    private Button autoTranslateButton;
    private VBox translationPane;
    private HBox mainFieldContainer;
    private boolean isTranslationPaneVisible = false;
    private final UpdateStore updateStore;
    private one.modality.base.shared.entities.Label initialLabel;
    private one.modality.base.shared.entities.Label workingLabel;
    private Label errorMessageLabel;
    private HBox errorMessageBox;
    private FadeTransition errorFadeIn;
    private FadeTransition errorFadeOut;

    private final Map<String, TextField> languageFields = new HashMap<>();
    private static final String[] LANGUAGES = {
        one.modality.base.shared.entities.Label.en,
        one.modality.base.shared.entities.Label.fr,
        one.modality.base.shared.entities.Label.es,
        one.modality.base.shared.entities.Label.de,
        one.modality.base.shared.entities.Label.pt,
        one.modality.base.shared.entities.Label.zh,
        one.modality.base.shared.entities.Label.yue,
 //       one.modality.base.shared.entities.Label.it,
        one.modality.base.shared.entities.Label.el,
        one.modality.base.shared.entities.Label.vi
    };


    // Language code mapping for translation API
    private static final Map<String, String> LANGUAGE_CODE_MAP;
    static {
        LANGUAGE_CODE_MAP = new HashMap<>();
        LANGUAGE_CODE_MAP.put(one.modality.base.shared.entities.Label.en, "en");
        LANGUAGE_CODE_MAP.put(one.modality.base.shared.entities.Label.fr, "fr");
        LANGUAGE_CODE_MAP.put(one.modality.base.shared.entities.Label.es, "es");
        LANGUAGE_CODE_MAP.put(one.modality.base.shared.entities.Label.de, "de");
        LANGUAGE_CODE_MAP.put(one.modality.base.shared.entities.Label.pt, "pt");
        LANGUAGE_CODE_MAP.put(one.modality.base.shared.entities.Label.zh, "zh");
        LANGUAGE_CODE_MAP.put(one.modality.base.shared.entities.Label.yue, "zh-yue");
        LANGUAGE_CODE_MAP.put(one.modality.base.shared.entities.Label.it, "it");
        LANGUAGE_CODE_MAP.put(one.modality.base.shared.entities.Label.el, "el");
        LANGUAGE_CODE_MAP.put(one.modality.base.shared.entities.Label.vi, "vi");
    }

    // Constants for consistent sizing
    private static final double LABEL_WIDTH = 120;
    private static final double TEXT_FIELD_WIDTH = 450;

    public LabelTextField(Entity currentEntity, String databaseSimpleFieldName, String databaseLabelFieldName, UpdateStore updateStore) {
        this.currentEntity = updateStore.updateEntity(currentEntity); //this entity will be updated in the database by positioning the foreign key pointing to the label
        this.databaseSimpleFieldName = databaseSimpleFieldName;
        this.databaseLabelFieldName = databaseLabelFieldName;
        this.updateStore = updateStore;
        initializeUI();
        if(this.currentEntity!=null) {
            initialLabel = this.currentEntity.getForeignEntity(databaseLabelFieldName);
            workingLabel = updateStore.updateEntity(initialLabel);
            populateFieldsFromEntity();
        }
    }

    private void initializeUI() {
        container = new VBox(5);
        mainFieldContainer = new HBox(5);
        mainFieldContainer.setAlignment(Pos.CENTER_LEFT);

        mainTextField = new TextField();
        mainTextField.setPromptText(I18n.getI18nText(BaseI18nKeys.DefaultPromptText));
        FXProperties.runOnPropertyChange(linkText -> {
            if (databaseSimpleFieldName != null && !Objects.equals(linkText, "")) {
                currentEntity.setFieldValue(databaseSimpleFieldName, linkText);
            } else {
                // When databaseSimpleFieldName is null, update the EN value of the label
                ensureWorkingLabelExists();
                if (!Objects.equals(linkText, "")) {
                    workingLabel.setEn(linkText);
                } else {
                    //If we work with a label and the English field Text is null, we reset the update store
                    resetChanges();
                }
            }
        }, mainTextField.textProperty());
        HBox.setHgrow(mainTextField, Priority.ALWAYS);

        translationButton = new Button(I18n.getI18nText(BaseI18nKeys.TranslateIcon));
        translationButton.getStyleClass().add("translation-button");
        translationButton.setOnAction(e -> toggleTranslationPane());
        translationButton.setAlignment(Pos.CENTER);

        mainFieldContainer.getChildren().addAll(mainTextField, translationButton);

        createTranslationPane();

        container.getChildren().add(mainFieldContainer);
    }

    /**
     * Ensures a working label exists
     */
    private void ensureWorkingLabelExists() {
        if (workingLabel == null) {
            workingLabel = updateStore.insertEntity(one.modality.base.shared.entities.Label.class);
            currentEntity.setForeignField(databaseLabelFieldName, workingLabel);
        }
    }

    private void resetChanges() {
        updateStore.cancelChanges();
        if(initialLabel==null)
            workingLabel = null;
    }

    private void createTranslationPane() {
        translationPane = new VBox(8);
        translationPane.setPadding(new Insets(10));
        translationPane.getStyleClass().add("translation-pane");
        translationPane.setVisible(false);
        translationPane.setManaged(false);

        autoTranslateButton = new Button("🌐 Help Translate");
        autoTranslateButton.getStyleClass().add("auto-translate-button");
        autoTranslateButton.setOnAction(e -> autoTranslateEmptyFields());
        autoTranslateButton.setMaxWidth(Double.MAX_VALUE);

        // Clear All button
        Button clearAllTranslationButton = new Button("🗑️ Clear All");
        clearAllTranslationButton.getStyleClass().add("clear-all-button");
        clearAllTranslationButton.setOnAction(e -> clearAllTranslationFields());
        clearAllTranslationButton.setMaxWidth(Double.MAX_VALUE);

        HBox buttonContainer = new HBox(15, autoTranslateButton, clearAllTranslationButton);
        buttonContainer.setAlignment(Pos.CENTER);

        // Create error message box
        createErrorMessageBox();

        // Add components in order: buttons, error box, language fields
        translationPane.getChildren().addAll(buttonContainer, errorMessageBox);

        for (String lang : LANGUAGES) {
            if (!lang.equals(one.modality.base.shared.entities.Label.en)) {
                String label = getLanguageLabel(lang);
                HBox fieldBox = createLanguageField(label, lang);
                translationPane.getChildren().add(fieldBox);
            }
        }

        container.getChildren().add(translationPane);
    }

    private HBox createLanguageField(String languageName, String langKey) {
        HBox fieldBox = new HBox(10);
        fieldBox.setAlignment(Pos.CENTER_LEFT);

        Label languageLabel = Bootstrap.strong(new Label(languageName));
        languageLabel.setMinWidth(LABEL_WIDTH);
        languageLabel.setPrefWidth(LABEL_WIDTH);
        languageLabel.setMaxWidth(LABEL_WIDTH);

        TextField textField = new TextField();
        textField.setPromptText(I18n.getI18nText(BaseI18nKeys.TranslateIn,languageName));
        textField.setPrefWidth(TEXT_FIELD_WIDTH);
        textField.setMinWidth(TEXT_FIELD_WIDTH);
        textField.setMaxWidth(TEXT_FIELD_WIDTH);
        HBox.setHgrow(textField, Priority.ALWAYS);
        languageFields.put(langKey, textField);

        FXProperties.runOnPropertyChange(linkText -> {
            if (workingLabel != null) {
                workingLabel.setFieldValue(langKey, linkText);
            }
        }, textField.textProperty());

        HBox textFieldContainer = new HBox(5, textField);
        textFieldContainer.setAlignment(Pos.CENTER_LEFT);

        fieldBox.getChildren().addAll(languageLabel, textFieldContainer);
        return fieldBox;
    }

    /**
     * Auto-translates empty fields using a free translation API
     */
    private void autoTranslateEmptyFields() {
        String sourceText = mainTextField.getText();
        if (sourceText == null || sourceText.trim().isEmpty()) {
            showTranslationError("Please enter text in the main field before translating.");
            return;
        }

        // Hide any existing error messages
        hideErrorMessage();

        // Disable the button to prevent multiple concurrent requests
        autoTranslateButton.setDisable(true);
        autoTranslateButton.setText("🔄 Translating...");

        Runnable translationRunnable = () -> {
            boolean hasErrors = false;

            for (String lang : LANGUAGES) {
                if (!lang.equals(one.modality.base.shared.entities.Label.en)) {
                    TextField field = languageFields.get(lang);
                    if (field != null && (field.getText() == null || field.getText().trim().isEmpty())) {
                        String targetLangCode = LANGUAGE_CODE_MAP.get(lang);
                        if (targetLangCode != null) {
                            Future<String> translatedText = translateWithDeepL(sourceText, targetLangCode);
                            translatedText.onSuccess(text -> {
                                if (text != null && !text.trim().isEmpty()) {
                                    Platform.runLater(() -> {
                                        field.setText(text);
                                        ensureWorkingLabelExists();
                                        if (workingLabel != null) {
                                            workingLabel.setFieldValue(lang, text);
                                        }
                                    });
                                }
                            }).onFailure(error -> {
                                Platform.runLater(() -> {
                                    String languageLabel = getLanguageLabel(lang);
                                    showTranslationError("Failed to translate to " + languageLabel + ": " + error.getMessage());
                                });
                            });
                        }
                    }
                }
            }

            // Update UI on completion
            Platform.runLater(() -> {
                autoTranslateButton.setDisable(false);
                getDeepLAvailableCharacters().onComplete(result -> Platform.runLater(() -> {
                    if (result.succeeded()) {
                        int remaining = result.result();
                        autoTranslateButton.setText("🌐 Help Translate (" + remaining + " chars left)");
                    } else {
                        autoTranslateButton.setText("🌐 Help Translate (usage unknown)");
                        Console.log("Failed to fetch DeepL usage: " + result.cause());
                    }
                }));
            });
        };

        Scheduler.runInBackground(translationRunnable);
    }


    private void clearAllTranslationFields() {
        for (TextField field : languageFields.values()) {
            field.clear();
        }

        if (workingLabel != null) {
            for (String lang : LANGUAGES) {
                if (!lang.equals(one.modality.base.shared.entities.Label.en)) {
                    workingLabel.setFieldValue(lang, "");
                }
            }
        }
    }

    private void clearTranslationTextFields() {
        for (TextField field : languageFields.values()) {
            field.clear();
        }
        mainTextField.clear();
    }


    private Future<String> translateWithDeepL(String text, String targetLang) {
        if("zh-yue".equals(targetLang)) targetLang = "ZH-HANT";
        Promise<String> promise = Promise.promise();

        String deeplTranslateAPIKey = SourcesConfig.getSourcesRootConfig()
            .childConfigAt("modality.base.client.i18n")
            .getString("deeplTranslatedApiKey");

        if (deeplTranslateAPIKey == null || deeplTranslateAPIKey.trim().isEmpty()) {
            promise.fail("DeepL API key not configured");
            return promise.future();
        }

        String params = "auth_key=" + URLEncoder.encode(deeplTranslateAPIKey, StandardCharsets.UTF_8)
            + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8)
            + "&source_lang=" + "en".toUpperCase()
            + "&target_lang=" + targetLang.toUpperCase();

        try {
            String apiUrl = "https://api-free.deepl.com/v2/translate?" + params;

            JsonFetch.fetchJsonObject(apiUrl)
                .onFailure(error -> {
                    Console.log("DeepL translation error", error);
                    String errorMessage = "Translation service unavailable.";
                    if (error.getMessage() != null) {
                        if (error.getMessage().contains("403")) {
                            errorMessage = "Invalid API key or quota exceeded";
                        } else if (error.getMessage().contains("429")) {
                            errorMessage = "Too many requests. Please wait and try again.";
                        } else if (error.getMessage().contains("network") || error.getMessage().contains("timeout")) {
                            errorMessage = "Network error. Please check your connection.";
                        }
                    }
                    promise.fail(errorMessage);
                })
                .onSuccess(response -> {
                    try {
                        ReadOnlyAstObject o = AST.parseObject(response.toString(), "json");
                        ReadOnlyAstArray translated = o.getArray("translations");
                        if (translated != null && translated.size() > 0) {
                            String translation = translated.getObject(0).get("text");
                            promise.complete(translation);
                        } else {
                            promise.fail("No translation received from service. Please check DEEPL_TRANSLATE_API_KEY variable (currently:" + deeplTranslateAPIKey+")");
                        }
                    } catch (Exception e) {
                        Console.log("Error parsing DeepL response JSON", e);
                        promise.fail("Invalid response from translation service");
                    }
                });
        } catch (Exception e) {
            Console.log("Error constructing DeepL request", e);
            promise.fail("Failed to create translation request");
        }

        return promise.future();
    }

    private void displayErrorMessageWhenTranslating(String deeplTranslateAPIKey) {

    }

    private String getLanguageLabel(String code) {
        return switch (code) {
            case one.modality.base.shared.entities.Label.en -> I18n.getI18nText(BaseI18nKeys.EnglishWithFlag);
            case one.modality.base.shared.entities.Label.fr -> I18n.getI18nText(BaseI18nKeys.FrenchWithFlag);
            case one.modality.base.shared.entities.Label.es -> I18n.getI18nText(BaseI18nKeys.SpanishWithFlag);
            case one.modality.base.shared.entities.Label.de -> I18n.getI18nText(BaseI18nKeys.GermanWithFlag);
            case one.modality.base.shared.entities.Label.pt -> I18n.getI18nText(BaseI18nKeys.PortugueseWithFlag);
            case one.modality.base.shared.entities.Label.zh -> I18n.getI18nText(BaseI18nKeys.MandarinWithFlag);
            case one.modality.base.shared.entities.Label.yue -> I18n.getI18nText(BaseI18nKeys.CantoneseWithFlag);
            case one.modality.base.shared.entities.Label.vi -> I18n.getI18nText(BaseI18nKeys.VietnameseWithFlag);
            case one.modality.base.shared.entities.Label.it -> I18n.getI18nText(BaseI18nKeys.ItalianWithFlag);
            case one.modality.base.shared.entities.Label.el -> I18n.getI18nText(BaseI18nKeys.GreekWithFlag);
            default -> code;
        };
    }

    private void populateFieldsFromEntity() {
        if (initialLabel != null) {
            mainTextField.setText(initialLabel.getEn());
            for (String lang : LANGUAGES) {
                if (!lang.equals(one.modality.base.shared.entities.Label.en) && languageFields.containsKey(lang)) {
                    languageFields.get(lang).setText(initialLabel.getStringFieldValue(lang));
                }
            }
            showTranslationPane();
        } else if (databaseSimpleFieldName != null) {
            String fallback = (String) currentEntity.getFieldValue(databaseSimpleFieldName);
            mainTextField.setText(fallback != null ? fallback : "");
            clearTranslationTextFields();
            if (isTranslationPaneVisible)
                hideTranslationPane();
        } else {
            // When both initialLabel and databaseSimpleFieldName are null, start with empty field
            mainTextField.setText("");
            clearTranslationTextFields();
            if (isTranslationPaneVisible)
                hideTranslationPane();
        }
    }

    private void toggleTranslationPane() {
        if (isTranslationPaneVisible) {
            hideTranslationPane();
            if (databaseSimpleFieldName != null && workingLabel != null && !Objects.equals(mainTextField.getText(), "")) {
                // Only delete the label if we have a simple field to fall back to
                updateStore.deleteEntity(workingLabel);
                workingLabel = null;
                currentEntity.setForeignField(databaseLabelFieldName, null);
            }
        } else {
            showTranslationPane();
            if (workingLabel != null && !Objects.equals(mainTextField.getText(), "")) {
                workingLabel.setEn(mainTextField.getText());
                for (String lang : LANGUAGES) {
                    if (!lang.equals(one.modality.base.shared.entities.Label.en)) {
                        workingLabel.setFieldValue(lang, languageFields.get(lang).getText());
                    }
                }
            }
        }
    }

    private void showTranslationPane() {
        if (!isTranslationPaneVisible) {
            isTranslationPaneVisible = true;
            moveMainFieldToEnglishField();
            translationPane.setVisible(true);
            translationPane.setManaged(true);
            translationButton.setText(I18n.getI18nText(BaseI18nKeys.CloseIcon));

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), translationPane);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(Interpolator.EASE_OUT);
            fadeIn.play();
        }
    }

    private void hideTranslationPane() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), translationPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(Interpolator.EASE_IN);
        fadeOut.setOnFinished(e -> {
            translationPane.setVisible(false);
            translationPane.setManaged(false);
            moveMainFieldBack();
            translationButton.setText(I18n.getI18nText(BaseI18nKeys.TranslateIcon));
            isTranslationPaneVisible = false;
        });
        fadeOut.play();
    }

    private void moveMainFieldToEnglishField() {
        mainFieldContainer.getChildren().remove(mainTextField);

        mainTextField.setPrefWidth(TEXT_FIELD_WIDTH);
        mainTextField.setMinWidth(TEXT_FIELD_WIDTH);
        mainTextField.setMaxWidth(TEXT_FIELD_WIDTH);
        HBox.setHgrow(mainTextField, Priority.ALWAYS);

        Label label = Bootstrap.strong(I18nControls.newLabel(BaseI18nKeys.EnglishWithFlag));
        label.setMinWidth(LABEL_WIDTH);
        label.setPrefWidth(LABEL_WIDTH);
        label.setMaxWidth(LABEL_WIDTH);

        HBox textFieldContainer = new HBox(5, mainTextField);
        textFieldContainer.setAlignment(Pos.CENTER_LEFT);

        HBox englishFieldBox = new HBox(10, label, textFieldContainer);
        englishFieldBox.setAlignment(Pos.CENTER_LEFT);

        // Insert after the auto-translate button (index 2)
        translationPane.getChildren().add(2, englishFieldBox);
    }

    private void moveMainFieldBack() {
        // Remove from index 1 (after auto-translate button)
        if (translationPane.getChildren().size() > 1) {
            translationPane.getChildren().remove(1);
        }
        mainTextField.setPrefWidth(-1);
        mainTextField.setMinWidth(-1);
        mainTextField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(mainTextField, Priority.ALWAYS);
        mainFieldContainer.getChildren().add(0, mainTextField);
    }

    public Node getView() {
        return container;
    }

    public void setMaxWidth(int maxWidth) {
        container.setMaxWidth(maxWidth);
        mainTextField.setMaxWidth(maxWidth);
    }

    public void setMinWidth(int minWidth) {
        container.setMinWidth(minWidth);
        mainTextField.setMinWidth(minWidth);
    }

    private Future<Integer> getDeepLAvailableCharacters() {
        Promise<Integer> promise = Promise.promise();

        String deeplTranslateAPIKey = SourcesConfig.getSourcesRootConfig()
            .childConfigAt("modality.base.client.i18n")
            .getString("deeplTranslatedApiKey");


        try {
            String url = "https://api-free.deepl.com/v2/usage?auth_key=" + URLEncoder.encode(deeplTranslateAPIKey, StandardCharsets.UTF_8);

            JsonFetch.fetchJsonObject(url)
                .onFailure(error -> {
                    Console.log("Error fetching DeepL usage", error);
                    promise.fail("Error fetching DeepL usage" + error); // complete with error value
                })
                .onSuccess(response -> {
                    try {
                        ReadOnlyAstObject o = AST.parseObject(response.toString(), "json");
                        int characterLimit = o.getInteger("character_limit");
                        int characterCount = o.getInteger("character_count");
                        promise.complete(characterLimit - characterCount);
                    } catch (Exception e) {
                        Console.log("Error parsing DeepL usage JSON", e);
                        promise.fail("Error parsing DeepL usage JSON: " +e);
                    }
                });
        } catch (Exception e) {
            Console.log("Error constructing DeepL usage request", e);
            promise.fail("Error constructing DeepL usage request");
        }

        return promise.future();
    }

    public void reloadOnNewEntity(Entity newEntity) {
        workingLabel = null;
        currentEntity = updateStore.updateEntity(newEntity);
        if(currentEntity!=null) {
            initialLabel = currentEntity.getForeignEntity(databaseLabelFieldName);
            workingLabel = updateStore.updateEntity(initialLabel);
            clearTranslationTextFields();
            populateFieldsFromEntity();
            updateStore.cancelChanges();
        }
    }

    private void showTranslationError(String message) {
        Platform.runLater(() -> {
            errorMessageLabel.setText(message);
            showErrorMessage();
        });
    }

    private void createErrorMessageBox() {
        errorMessageLabel = new Label();
        errorMessageLabel.getStyleClass().addAll("error-message-text", "text-danger");
        errorMessageLabel.setWrapText(true);
        errorMessageLabel.setMaxWidth(Double.MAX_VALUE);

        // Create error icon (you can replace with your preferred icon)
        Label errorIcon = new Label("⚠️");
        errorIcon.getStyleClass().add("error-icon");

        errorMessageBox = new HBox(8, errorIcon, errorMessageLabel);
        errorMessageBox.getStyleClass().add("error-message-box");
        errorMessageBox.setAlignment(Pos.CENTER_LEFT);
        errorMessageBox.setPadding(new Insets(10, 15, 10, 15));
        errorMessageBox.setMaxWidth(Double.MAX_VALUE);
        errorMessageBox.setVisible(false);
        errorMessageBox.setManaged(false);

        // Create fade animations
        errorFadeIn = new FadeTransition(Duration.millis(200), errorMessageBox);
        errorFadeIn.setFromValue(0.0);
        errorFadeIn.setToValue(1.0);
        errorFadeIn.setInterpolator(Interpolator.EASE_OUT);

        errorFadeOut = new FadeTransition(Duration.millis(200), errorMessageBox);
        errorFadeOut.setFromValue(1.0);
        errorFadeOut.setToValue(0.0);
        errorFadeOut.setInterpolator(Interpolator.EASE_IN);
        errorFadeOut.setOnFinished(e -> {
            errorMessageBox.setVisible(false);
            errorMessageBox.setManaged(false);
        });
    }

    private void showErrorMessage() {
        // Stop any existing animation
        if (errorFadeOut != null) {
            errorFadeOut.stop();
        }
        if (errorFadeIn != null) {
            errorFadeIn.stop();
        }

        errorMessageBox.setVisible(true);
        errorMessageBox.setManaged(true);
        errorMessageBox.setOpacity(0.0);

        errorFadeIn.play();

        // Auto-hide after 5 seconds
        Scheduler.scheduleDelay(5000, () -> Platform.runLater(this::hideErrorMessage));
    }

    private void hideErrorMessage() {
        if (errorMessageBox.isVisible()) {
            errorFadeOut.play();
        }
    }
}