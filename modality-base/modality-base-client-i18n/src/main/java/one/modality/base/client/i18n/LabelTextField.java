package one.modality.base.client.i18n;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
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

import java.util.HashMap;
import java.util.Map;

/**
 * @author David Hello
 */
public class LabelTextField {

    private final String databaseSimpleFieldName;
    private final String databaseLabelFieldName;
    private final Entity currentEntity;
    private VBox container;
    private TextField mainTextField;
    private Button translationButton;
    private VBox translationPane;
    private HBox mainFieldContainer;
    private boolean isTranslationPaneVisible = false;
    private one.modality.base.shared.entities.Label commentLabel;
    private final UpdateStore updateStore;
    private one.modality.base.shared.entities.Label initialLabel;
    private one.modality.base.shared.entities.Label workingLabel;

    private final Map<String, TextField> languageFields = new HashMap<>();
    private static final String[] LANGUAGES = {
        one.modality.base.shared.entities.Label.en,
        one.modality.base.shared.entities.Label.fr,
        one.modality.base.shared.entities.Label.es,
        one.modality.base.shared.entities.Label.de,
        one.modality.base.shared.entities.Label.pt,
        one.modality.base.shared.entities.Label.zh,
        one.modality.base.shared.entities.Label.yue,
        one.modality.base.shared.entities.Label.el,
        one.modality.base.shared.entities.Label.vi
    };

    // Constants for consistent sizing
    private static final double LABEL_WIDTH = 120;
    private static final double TEXT_FIELD_WIDTH = 450;

    public LabelTextField(Entity currentEntity, String databaseSimpleFieldName, String databaseLabelFieldName, UpdateStore updateStore) {
        this.currentEntity = currentEntity;
        this.databaseSimpleFieldName = databaseSimpleFieldName;
        this.databaseLabelFieldName = databaseLabelFieldName;
        this.updateStore = updateStore;
        initialLabel = currentEntity.getForeignEntity(databaseLabelFieldName);
        workingLabel = updateStore.updateEntity(initialLabel);

        initializeUI();
        setupEventHandlers();
        populateFieldsFromEntity();
    }

    private void initializeUI() {
        container = new VBox(5);

        mainFieldContainer = new HBox(5);
        mainFieldContainer.setAlignment(Pos.CENTER_LEFT);

        mainTextField = new TextField();
        mainTextField.setPromptText(I18n.getI18nText(BaseI18nKeys.DefaultPromptText));
        FXProperties.runOnPropertyChange(linkText ->
                currentEntity.setFieldValue(databaseSimpleFieldName, linkText),
            mainTextField.textProperty());
        HBox.setHgrow(mainTextField, Priority.ALWAYS);

        translationButton = new Button(I18n.getI18nText(BaseI18nKeys.TranslateIcon));
        translationButton.getStyleClass().add("translation-button");
        translationButton.setOnAction(e -> toggleTranslationPane());
        translationButton.setAlignment(Pos.CENTER);

        mainFieldContainer.getChildren().addAll(mainTextField, translationButton);

        createTranslationPane();

        container.getChildren().add(mainFieldContainer);
    }

    private void createTranslationPane() {
        translationPane = new VBox(8);
        translationPane.setPadding(new Insets(10));
        translationPane.getStyleClass().add("translation-pane");
        translationPane.setVisible(false);
        translationPane.setManaged(false);

        for (String lang : LANGUAGES) {
            if (!lang.equals(one.modality.base.shared.entities.Label.en)) {
                String label = getLanguageLabel(lang);
                HBox fieldBox = createLanguageField(label, lang.toUpperCase(), lang);
                translationPane.getChildren().add(fieldBox);
            }
        }

        container.getChildren().add(translationPane);
    }

    private HBox createLanguageField(String languageName, String languageCode, String langKey) {
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

        FXProperties.runOnPropertyChange(linkText ->
                workingLabel.setFieldValue(langKey, linkText),
            textField.textProperty());


        HBox textFieldContainer = new HBox(5, textField);
        textFieldContainer.setAlignment(Pos.CENTER_LEFT);

        fieldBox.getChildren().addAll(languageLabel, textFieldContainer);
        return fieldBox;
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
            case one.modality.base.shared.entities.Label.el -> I18n.getI18nText(BaseI18nKeys.GreekWithFlag);
            case one.modality.base.shared.entities.Label.vi -> I18n.getI18nText(BaseI18nKeys.VietnameseWithFlag);
            default -> code;
        };
    }

    private void setupEventHandlers() {

        mainTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                if (isTranslationPaneVisible) {
                    workingLabel.setEn(mainTextField.getText());
                    for (String lang : LANGUAGES) {
                        if (!lang.equals(one.modality.base.shared.entities.Label.en)) {
                            workingLabel.setFieldValue(lang, languageFields.get(lang).getText());
                        }
                    }
                } else {
                    currentEntity.setFieldValue(databaseSimpleFieldName, mainTextField.getText());
                }
            }
        });
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
        } else {
            String fallback = (String) currentEntity.getFieldValue(databaseSimpleFieldName);
            mainTextField.setText(fallback != null ? fallback : "");
        }
    }


    private void toggleTranslationPane() {
        if (isTranslationPaneVisible) {
            hideTranslationPane();
            if (workingLabel != null) {
                updateStore.deleteEntity(workingLabel);
                workingLabel = null;
                currentEntity.setForeignField(databaseLabelFieldName, null);
            }
        } else {
            showTranslationPane();
            if (workingLabel == null) {
                workingLabel = updateStore.insertEntity(one.modality.base.shared.entities.Label.class);
                workingLabel.setEn(mainTextField.getText());
                for (String lang : LANGUAGES) {
                    if (!lang.equals(one.modality.base.shared.entities.Label.en)) {
                        workingLabel.setFieldValue(lang, languageFields.get(lang).getText());
                    }
                }
                currentEntity.setForeignField(databaseLabelFieldName, workingLabel);
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

        translationPane.getChildren().add(0, englishFieldBox);
    }

    private void moveMainFieldBack() {
        if (!translationPane.getChildren().isEmpty()) {
            translationPane.getChildren().remove(0);
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
}
