package one.modality.base.backoffice.claude;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Helper class for creating standardized form fields across admin dialogs.
 * Provides type-safe access to form components and eliminates brittle casting patterns.
 *
 * @author Claude Code
 */
public class FormFieldHelper {

    /**
     * Creates a text field with label and optional help text.
     *
     * @param labelKey I18n key for the label
     * @param placeholderKey I18n key for placeholder text (can be null)
     * @param helpKey I18n key for help text (can be null)
     * @return FormField containing the container and TextField
     */
    public static FormField<TextField> createTextField(Object labelKey, Object placeholderKey, Object helpKey) {
        VBox field = new VBox(8);
        field.setMaxWidth(Double.MAX_VALUE);

        // Label
        Label label = I18nControls.newLabel(labelKey);
        label.getStyleClass().add("form-field-label");

        // Input container
        VBox inputContainer = new VBox(4);
        inputContainer.setMaxWidth(Double.MAX_VALUE);

        // Text input
        TextField input = new TextField();
        input.setMinWidth(200);
        input.setPrefWidth(Region.USE_COMPUTED_SIZE);
        input.setMaxWidth(Double.MAX_VALUE);
        input.setPadding(new Insets(10, 12, 10, 12));
        input.getStyleClass().add("form-field-input");

        if (placeholderKey != null) {
            I18n.bindI18nTextProperty(input.promptTextProperty(), placeholderKey);
        }

        // Make input grow horizontally
        HBox.setHgrow(input, Priority.ALWAYS);
        VBox.setVgrow(input, Priority.NEVER);

        inputContainer.getChildren().add(input);

        // Help text (optional)
        if (helpKey != null) {
            Label helpText = I18nControls.newLabel(helpKey);
            helpText.getStyleClass().add("form-field-help");
            helpText.setWrapText(true);
            helpText.setMaxWidth(Double.MAX_VALUE);
            inputContainer.getChildren().add(helpText);
        }

        field.getChildren().addAll(label, inputContainer);
        return new FormField<>(field, input);
    }

    /**
     * Creates a text area with label and optional help text.
     *
     * @param labelKey I18n key for the label
     * @param placeholderKey I18n key for placeholder text (can be null)
     * @param helpKey I18n key for help text (can be null)
     * @param rowCount Preferred number of rows for the text area
     * @return FormField containing the container and TextArea
     */
    public static FormField<TextArea> createTextArea(Object labelKey, Object placeholderKey, Object helpKey, int rowCount) {
        VBox field = new VBox(8);
        field.setMaxWidth(Double.MAX_VALUE);

        // Label
        Label label = I18nControls.newLabel(labelKey);
        label.getStyleClass().add("form-field-label");

        // Input container
        VBox inputContainer = new VBox(4);
        inputContainer.setMaxWidth(Double.MAX_VALUE);

        // Text area
        TextArea textArea = new TextArea();
        textArea.setPrefRowCount(rowCount);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.getStyleClass().add("form-field-input");

        if (placeholderKey != null) {
            I18n.bindI18nTextProperty(textArea.promptTextProperty(), placeholderKey);
        }

        inputContainer.getChildren().add(textArea);

        // Help text (optional)
        if (helpKey != null) {
            Label helpText = I18nControls.newLabel(helpKey);
            helpText.getStyleClass().add("form-field-help");
            helpText.setWrapText(true);
            helpText.setMaxWidth(Double.MAX_VALUE);
            inputContainer.getChildren().add(helpText);
        }

        field.getChildren().addAll(label, inputContainer);
        return new FormField<>(field, textArea);
    }

    /**
     * Adds a search filter listener to a text field that shows/hides checkboxes based on search text.
     *
     * @param searchField The text field for search input
     * @param checkboxes List of checkboxes to filter
     */
    public static void addSearchFilter(TextField searchField, List<CheckBox> checkboxes) {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String searchText = newVal != null ? newVal.toLowerCase().trim() : "";
            for (CheckBox cb : checkboxes) {
                if (searchText.isEmpty()) {
                    // Show all checkboxes when search is empty
                    cb.setVisible(true);
                    cb.setManaged(true);
                } else {
                    // Show only checkboxes that match the search
                    String cbText = cb.getText();
                    boolean matches = cbText != null && cbText.toLowerCase().contains(searchText);
                    cb.setVisible(matches);
                    cb.setManaged(matches);
                }
            }
        });
    }

    /**
     * Adds change listeners to all checkboxes in a list.
     *
     * @param checkboxes List of checkboxes
     * @param onChange Callback to execute when any checkbox changes
     */
    public static void addCheckboxListeners(List<CheckBox> checkboxes, Runnable onChange) {
        for (CheckBox cb : checkboxes) {
            cb.selectedProperty().addListener((obs, oldVal, newVal) -> onChange.run());
        }
    }
}
