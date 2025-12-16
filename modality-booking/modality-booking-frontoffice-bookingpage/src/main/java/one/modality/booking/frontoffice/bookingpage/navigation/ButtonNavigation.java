package one.modality.booking.frontoffice.bookingpage.navigation;

import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.platform.async.Future;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.booking.frontoffice.bookingpage.BookingFormButton;
import one.modality.booking.frontoffice.bookingpage.BookingFormNavigation;
import one.modality.booking.frontoffice.bookingpage.MultiPageBookingForm;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Navigation buttons for the booking form.
 * Uses pure CSS for styling - colors and states handled via CSS pseudo-classes.
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .booking-form-btn-primary} - primary action buttons (Continue, Submit, Pay)</li>
 *   <li>{@code .booking-form-btn-back} - back/previous buttons</li>
 *   <li>{@code .booking-form-btn-secondary} - secondary action buttons (Register Another)</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public class ButtonNavigation implements BookingFormNavigation {

    private final HBox container = new HBox();
    private final Button continueButton = new Button();
    // We need to expose ToggleButton for the interface, but we use a Button for UI.
    private final ToggleButton nextToggleButton = new ToggleButton();
    private final ToggleButton backToggleButton = new ToggleButton();

    private MultiPageBookingForm bookingForm;

    // Kept for API compatibility - theming is now CSS-based, so this is a no-op
    private final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    public ButtonNavigation() {
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(40, 0, 0, 0));
        container.setSpacing(16);
        container.getStyleClass().add("buttons");

        // Apply primary button CSS class - all styling handled by CSS
        continueButton.getStyleClass().add("booking-form-btn-primary");
        // Padding set in Java per project conventions (CSS handles colors only)
        continueButton.setPadding(new Insets(14, 32, 14, 32));
        continueButton.setGraphicTextGap(16);

        // Bind ToggleButton (logic) to Button (UI)
        continueButton.disableProperty().bind(nextToggleButton.disableProperty());
        continueButton.setOnAction(e -> nextToggleButton.fire());

        container.getChildren().add(continueButton);

        // Initialize I18n
        I18nControls.bindI18nProperties(continueButton, BaseI18nKeys.Next);

        // Hidden toggle buttons for interface compatibility
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

    /**
     * @deprecated Color scheme is now handled via CSS classes on parent container.
     * Use theme classes like "theme-wisdom-blue" on a parent element instead.
     */
    @Deprecated
    public void setColorScheme(BookingFormColorScheme colorScheme) {
        this.colorScheme.set(colorScheme);
    }

    @Override
    public void setButtons(BookingFormButton... buttons) {
        container.getChildren().clear();

        if (buttons == null) {
            // Null means fallback to default Continue button - add spacer to push to right
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            container.getChildren().addAll(spacer, continueButton);
            return;
        }

        if (buttons.length == 0) {
            // Empty array means hide all buttons (section handles its own buttons)
            return;
        }

        // Separate back buttons from other buttons
        List<BookingFormButton> backButtons = new ArrayList<>();
        List<BookingFormButton> otherButtons = new ArrayList<>();

        for (BookingFormButton buttonDef : buttons) {
            String styleClass = buttonDef.getStyleClass();
            if (styleClass != null && styleClass.contains("btn-back")) {
                backButtons.add(buttonDef);
            } else {
                otherButtons.add(buttonDef);
            }
        }

        // Add back buttons first (left side) with arrow prefix
        for (BookingFormButton buttonDef : backButtons) {
            Button button = new Button();
            // Use graphic for arrow prefix (can't modify bound text property)
            Label arrowLabel = new Label("\u2190 "); // ← arrow
            arrowLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-text-secondary");
            button.setGraphic(arrowLabel);
            button.setContentDisplay(ContentDisplay.LEFT);
            // Bind text to i18n
            I18nControls.bindI18nProperties(button, buttonDef.getTextI18nKey());

            // Apply CSS class - all styling handled by CSS
            button.getStyleClass().add("booking-form-btn-back");
            // Padding set in Java per project conventions
            button.setPadding(new Insets(14, 32, 14, 32));

            if (buttonDef.getDisableProperty() != null) {
                button.disableProperty().bind(buttonDef.getDisableProperty());
            }

            // Handle async vs sync actions
            setButtonAction(button, buttonDef);
            container.getChildren().add(button);
        }

        // Add spacer between back and other buttons to push others to the right
        if (!backButtons.isEmpty() && !otherButtons.isEmpty()) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            container.getChildren().add(spacer);
        } else if (backButtons.isEmpty()) {
            // No back button, add spacer at start to push buttons to right
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            container.getChildren().add(0, spacer);
        }

        // Add other buttons (right side)
        for (BookingFormButton buttonDef : otherButtons) {
            Button button = new Button();
            // Support both i18n keys and StringProperty for dynamic text
            Object textKey = buttonDef.getTextI18nKey();
            if (textKey instanceof StringProperty textKeyProperty) {
                // Bind directly to StringProperty for dynamic text (e.g., "Pay £123 Now →")
                button.textProperty().bind(textKeyProperty);
            } else {
                // Use i18n binding for translation keys
                I18nControls.bindI18nProperties(button, textKey);
            }

            // Apply CSS class based on style - all styling handled by CSS
            String styleClass = buttonDef.getStyleClass();
            if (styleClass != null && styleClass.contains("btn-primary")) {
                button.getStyleClass().add("booking-form-btn-primary");
            } else if (styleClass != null && styleClass.contains("btn-secondary")) {
                button.getStyleClass().add("booking-form-btn-secondary");
            }
            // Padding set in Java per project conventions
            button.setPadding(new Insets(14, 32, 14, 32));
            button.setGraphicTextGap(16);

            if (buttonDef.getDisableProperty() != null) {
                button.disableProperty().bind(buttonDef.getDisableProperty());
            }

            // Handle async vs sync actions
            setButtonAction(button, buttonDef);
            container.getChildren().add(button);
        }
    }

    /**
     * Sets the action handler for a button, handling both sync and async actions.
     * For async actions, automatically shows a spinner during execution.
     *
     * IMPORTANT: AsyncSpinner uses FXProperties.setIfNotBound() to disable the button,
     * which won't work if the button's disableProperty is already bound. Therefore,
     * for async buttons with a bound disableProperty, we must:
     * 1. Unbind the disableProperty before calling AsyncSpinner
     * 2. Rebind it after the Future completes
     */
    private void setButtonAction(Button button, BookingFormButton buttonDef) {
        if (buttonDef.isAsync()) {
            // Async action - show spinner and disable during execution
            button.setOnAction(e -> {
                // Store the bound property (if any) to rebind later
                javafx.beans.value.ObservableValue<Boolean> boundProperty = buttonDef.getDisableProperty();

                // Unbind the disableProperty so AsyncSpinner can set it
                // (AsyncSpinner uses setIfNotBound which won't work on bound properties)
                if (boundProperty != null) {
                    button.disableProperty().unbind();
                }

                Future<?> future = buttonDef.getAsyncActionHandler().apply(button);
                if (future != null) {
                    // Wrap the future to rebind the property after completion
                    AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                        future.onComplete(ignored -> {
                            // Rebind the disableProperty after async operation completes
                            if (boundProperty != null) {
                                button.disableProperty().bind(boundProperty);
                            }
                        }),
                        button);
                } else {
                    // No future returned - rebind immediately
                    if (boundProperty != null) {
                        button.disableProperty().bind(boundProperty);
                    }
                }
            });
        } else if (buttonDef.getActionHandler() != null) {
            // Sync action - execute immediately
            button.setOnAction(buttonDef.getActionHandler());
        }
    }

    @Override
    public void updateState() {
        // Update button text based on next page title
        if (bookingForm != null) {
            boolean lastPage = bookingForm.getDisplayedPageIndex() == bookingForm.getPages().length - 1;
            I18nControls.bindI18nProperties(continueButton, lastPage ? BaseI18nKeys.Submit : BaseI18nKeys.Next);
        }
    }
}
