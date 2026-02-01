package one.modality.booking.frontoffice.bookingpage.components;

import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors.*;

/**
 * A reusable component that displays validation warning messages.
 * Positioned above navigation buttons to show why the form cannot proceed.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Aggregates validation messages from multiple sources</li>
 *   <li>Displays all messages in a yellow warning box</li>
 *   <li>Auto-hides when all validations pass</li>
 *   <li>Updates dynamically as validation states change</li>
 * </ul>
 *
 * <p>Design (from JSX mockup):</p>
 * <ul>
 *   <li>Background: #FFF9E6</li>
 *   <li>Border: 1px solid #FFD966</li>
 *   <li>Border radius: 8px</li>
 *   <li>Icon: Circle with exclamation (stroke #F59E0B)</li>
 *   <li>Text color: #92400E</li>
 *   <li>Font size: 13px</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public class ValidationWarningZone extends VBox {

    // Warning icon SVG path (circle with exclamation mark)
    private static final String ICON_WARNING = "M12 9v4m0 4h.01M12 3a9 9 0 1 1 0 18 9 9 0 0 1 0-18z";

    private final List<ValidationSource> validationSources = new ArrayList<>();
    private final VBox messagesContainer;

    /**
     * Represents a source of validation messages.
     */
    public static class ValidationSource {
        private final ObservableBooleanValue validProperty;
        private final Supplier<String> messageSupplier;

        /**
         * Creates a validation source.
         *
         * @param validProperty   Observable that indicates if the source is valid (true = valid, false = invalid)
         * @param messageSupplier Supplies the validation message when invalid (returns null when valid)
         */
        public ValidationSource(ObservableBooleanValue validProperty, Supplier<String> messageSupplier) {
            this.validProperty = validProperty;
            this.messageSupplier = messageSupplier;
        }

        public ObservableBooleanValue getValidProperty() {
            return validProperty;
        }

        public String getMessage() {
            return messageSupplier.get();
        }
    }

    public ValidationWarningZone() {
        setSpacing(8);
        setAlignment(Pos.TOP_LEFT);

        messagesContainer = new VBox(6);
        getChildren().add(messagesContainer);

        // Initially hidden
        setVisible(false);
        setManaged(false);
    }

    /**
     * Adds a validation source to monitor.
     * The warning zone will display the source's message when it becomes invalid.
     *
     * @param validProperty   Observable that indicates if the source is valid
     * @param messageSupplier Supplies the validation message when invalid
     */
    public void addValidationSource(ObservableBooleanValue validProperty, Supplier<String> messageSupplier) {
        ValidationSource source = new ValidationSource(validProperty, messageSupplier);
        validationSources.add(source);

        // Listen for validity changes
        validProperty.addListener((obs, oldVal, newVal) -> updateWarnings());

        // Initial update
        updateWarnings();
    }

    /**
     * Clears all validation sources.
     */
    public void clearValidationSources() {
        validationSources.clear();
        updateWarnings();
    }

    /**
     * Updates the warning display based on current validation states.
     */
    private void updateWarnings() {
        messagesContainer.getChildren().clear();

        List<String> messages = new ArrayList<>();
        for (ValidationSource source : validationSources) {
            if (!source.getValidProperty().get()) {
                String message = source.getMessage();
                if (message != null && !message.isEmpty()) {
                    messages.add(message);
                }
            }
        }

        if (messages.isEmpty()) {
            setVisible(false);
            setManaged(false);
        } else {
            // Create warning box with all messages
            HBox warningBox = createWarningBox(messages);
            messagesContainer.getChildren().add(warningBox);
            setVisible(true);
            setManaged(true);
        }
    }

    /**
     * Creates a warning box with the given messages.
     */
    private HBox createWarningBox(List<String> messages) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(12, 16, 12, 16));
        // Use CSS classes for styling
        box.getStyleClass().addAll(bookingpage_info_box, bookingpage_info_box_warning);

        // Warning icon
        SVGPath icon = new SVGPath();
        icon.setContent(ICON_WARNING);
        icon.setStroke(Color.web("#F59E0B"));
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.setScaleX(0.67);  // Scale 24px to ~16px
        icon.setScaleY(0.67);

        // Messages container
        VBox textContainer = new VBox(4);
        textContainer.setAlignment(Pos.TOP_LEFT);

        for (String message : messages) {
            Label messageLabel = new Label(message);
            messageLabel.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_warning);
            messageLabel.setWrapText(true);
            textContainer.getChildren().add(messageLabel);
        }

        box.getChildren().addAll(icon, textContainer);
        return box;
    }

    /**
     * Returns true if there are any validation errors currently displayed.
     */
    public boolean hasErrors() {
        for (ValidationSource source : validationSources) {
            if (!source.getValidProperty().get()) {
                return true;
            }
        }
        return false;
    }
}
