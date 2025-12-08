package one.modality.booking.frontoffice.bookingpage;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import one.modality.base.client.i18n.BaseI18nKeys;

/**
 * @author Bruno Salmon
 */
public class ButtonNavigation implements BookingFormNavigation {

    private final HBox container = new HBox();
    private final Button continueButton = new Button();
    // We need to expose ToggleButton for the interface, but we use a Button for UI.
    // We can wrap the button or use a hidden ToggleButton and bind properties.
    // Or just use ToggleButton and style it as a Button.
    private final ToggleButton nextToggleButton = new ToggleButton();
    private final ToggleButton backToggleButton = new ToggleButton();

    private MultiPageBookingForm bookingForm;

    public ButtonNavigation() {
        container.setAlignment(Pos.CENTER_RIGHT);
        container.setPadding(new javafx.geometry.Insets(40, 0, 0, 0));
        container.setSpacing(16); // Add spacing between buttons
        container.getStyleClass().add("buttons");

        continueButton.getStyleClass().addAll("btn", "btn-primary");
        continueButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #0096D6, #007AB8); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 16px 40px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0, 150, 214, 0.3), 12, 0, 0, 4); -fx-font-size: 16px; -fx-font-family: 'Poppins';");

        // Bind ToggleButton (logic) to Button (UI)
        continueButton.disableProperty().bind(nextToggleButton.disableProperty());
        continueButton.setOnAction(e -> nextToggleButton.fire());

        // Back button is hidden in this design or we can add it if needed.
        // For now, we only show Continue as per mockup.

        container.getChildren().add(continueButton);

        // Initialize I18n
        I18nControls.bindI18nProperties(continueButton, BaseI18nKeys.Next); // Or specific key "Continue to Options ->"

        // We need to ensure the ToggleButton reflects the state
        nextToggleButton.setVisible(false);
        backToggleButton.setVisible(false);
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public ToggleButton getBackButton() {
        return backToggleButton;
    }

    @Override
    public ToggleButton getNextButton() {
        return nextToggleButton;
    }

    @Override
    public void setBookingForm(MultiPageBookingForm bookingForm) {
        this.bookingForm = bookingForm;
    }

    @Override
    public void setButtons(BookingFormButton... buttons) {
        container.getChildren().clear();

        if (buttons == null || buttons.length == 0) {
            // Fallback to default Continue button
            container.getChildren().add(continueButton);
            return;
        }

        // Create buttons from the provided BookingFormButton array
        for (BookingFormButton buttonDef : buttons) {
            Button button = new Button();
            I18nControls.bindI18nProperties(button, buttonDef.getTextI18nKey());
            button.getStyleClass().addAll("btn", buttonDef.getStyleClass());

            // Apply styling based on style class
            if ("btn-primary".equals(buttonDef.getStyleClass())) {
                button.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #0096D6, #007AB8); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 16px 40px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0, 150, 214, 0.3), 12, 0, 0, 4); -fx-font-size: 16px; -fx-font-family: 'Poppins';");
            } else if ("btn-back".equals(buttonDef.getStyleClass())) {
                button.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #0096D6; -fx-font-weight: bold; -fx-padding: 16px 40px; -fx-background-radius: 8px; -fx-cursor: hand; -fx-border-color: #0096D6; -fx-border-width: 2px; -fx-border-radius: 8px; -fx-font-size: 16px; -fx-font-family: 'Poppins';");
            }

            if (buttonDef.getDisableProperty() != null) {
                button.disableProperty().bind(buttonDef.getDisableProperty());
            }

            button.setOnAction(buttonDef.getActionHandler());
            container.getChildren().add(button);
        }
    }

    @Override
    public void updateState() {
        // Update button text based on next page title?
        // Mockup says "Continue to Options ->"
        // We can try to get the next page title.
        if (bookingForm != null) {
            boolean lastPage = bookingForm.getDisplayedPageIndex() == bookingForm.getPages().length - 1;
            I18nControls.bindI18nProperties(continueButton, lastPage ? BaseI18nKeys.Submit : BaseI18nKeys.Next);
        }
    }
}
