package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import static one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys.*;

/**
 * Default implementation of the roommate information section.
 * Displays input fields for collecting information about the person whose room is being shared.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Section header with users icon</li>
 *   <li>Text field for roommate's full name (mandatory, min 2 characters)</li>
 *   <li>Text field for registration number (optional)</li>
 *   <li>Validation message when name is not provided</li>
 * </ul>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-roommate-section} - section container</li>
 *   <li>{@code .bookingpage-form-field} - form field container</li>
 *   <li>{@code .bookingpage-text-input} - text input styling</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasRoommateInfoSection
 */
public class DefaultRoommateInfoSection implements HasRoommateInfoSection {

    // Minimum characters required for roommate name
    private static final int MIN_NAME_LENGTH = 2;

    // === COLOR SCHEME ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // === VISIBILITY ===
    protected final BooleanProperty visibleProperty = new SimpleBooleanProperty(false);

    // === DATA PROPERTIES ===
    protected final StringProperty roommateNameProperty = new SimpleStringProperty("");
    protected final StringProperty registrationNumberProperty = new SimpleStringProperty("");

    // === VALIDATION ===
    protected final BooleanProperty validProperty = new SimpleBooleanProperty(true);

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected TextField nameField;
    protected TextField registrationNumberField;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

    public DefaultRoommateInfoSection() {
        buildUI();
        setupBindings();
        updateValidity();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_LEFT);
        container.setSpacing(16);
        container.getStyleClass().add("bookingpage-roommate-section");

        // Section header with users icon (flat gray)
        HBox sectionHeader = new StyledSectionHeader(RoommateInformation, StyledSectionHeader.ICON_USERS);

        // Roommate name field (mandatory)
        VBox nameFieldContainer = createNameField();

        // Registration number field (optional)
        VBox regNumberFieldContainer = createRegistrationNumberField();

        container.getChildren().addAll(sectionHeader, nameFieldContainer, regNumberFieldContainer);
        VBox.setMargin(sectionHeader, new Insets(0, 0, 8, 0));
    }

    /**
     * Creates the roommate name field with label and required indicator.
     */
    protected VBox createNameField() {
        VBox fieldContainer = new VBox(6);
        fieldContainer.getStyleClass().add("bookingpage-form-field");

        // Label with required asterisk
        HBox labelRow = createRequiredFieldLabel(RoommateName);

        // Text field - CSS handles styling including focus state via :focused pseudo-class
        nameField = new TextField();
        I18n.bindI18nPromptProperty(nameField.promptTextProperty(), RoommateNamePlaceholder);
        nameField.getStyleClass().add("bookingpage-text-input");
        nameField.textProperty().bindBidirectional(roommateNameProperty);
        nameField.setPadding(new Insets(12, 14, 12, 14));
        nameField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameField, Priority.ALWAYS);

        fieldContainer.getChildren().addAll(labelRow, nameField);
        return fieldContainer;
    }

    /**
     * Creates the registration number field with label.
     */
    protected VBox createRegistrationNumberField() {
        VBox fieldContainer = new VBox(6);
        fieldContainer.getStyleClass().add("bookingpage-form-field");

        // Label (no asterisk - optional field)
        Label label = I18nControls.newLabel(RegistrationNumber);
        label.getStyleClass().add("bookingpage-form-label");

        // Text field - CSS handles styling including focus state via :focused pseudo-class
        registrationNumberField = new TextField();
        I18n.bindI18nPromptProperty(registrationNumberField.promptTextProperty(), RegistrationNumberPlaceholder);
        registrationNumberField.getStyleClass().add("bookingpage-text-input");
        registrationNumberField.textProperty().bindBidirectional(registrationNumberProperty);
        registrationNumberField.setPadding(new Insets(12, 14, 12, 14));
        registrationNumberField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(registrationNumberField, Priority.ALWAYS);

        fieldContainer.getChildren().addAll(label, registrationNumberField);
        return fieldContainer;
    }

    /**
     * Creates a label with a required field asterisk.
     */
    protected HBox createRequiredFieldLabel(Object i18nKey) {
        Label label = I18nControls.newLabel(i18nKey);
        label.getStyleClass().add("bookingpage-form-label");

        // Asterisk still needs programmatic styling (Text nodes don't support -fx-text-fill via styleClass)
        Text asterisk = new Text(" *");
        asterisk.setFill(Color.web("#dc3545")); // Danger red
        asterisk.setFont(Font.font(null, FontWeight.SEMI_BOLD, 14));

        HBox wrapper = new HBox(label, asterisk);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        return wrapper;
    }

    protected void setupBindings() {
        // Update validity when name changes
        roommateNameProperty.addListener((obs, oldVal, newVal) -> updateValidity());

        // Update validity when visibility changes
        visibleProperty.addListener((obs, oldVal, newVal) -> {
            container.setVisible(newVal);
            container.setManaged(newVal);
            updateValidity();
        });

        // Initial visibility state
        container.setVisible(visibleProperty.get());
        container.setManaged(visibleProperty.get());
    }

    /**
     * Updates the validity based on visibility and name field.
     * Section is valid if:
     * - It is not visible (hidden sections don't block navigation), OR
     * - The roommate name has at least MIN_NAME_LENGTH characters
     */
    protected void updateValidity() {
        boolean visible = visibleProperty.get();
        if (!visible) {
            // When not visible, section is valid (doesn't block navigation)
            validProperty.set(true);
        } else {
            // When visible, validate that name is provided
            String name = roommateNameProperty.get();
            boolean nameValid = name != null && name.trim().length() >= MIN_NAME_LENGTH;
            validProperty.set(nameValid);
        }
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return RoommateInformation;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // ========================================
    // HasRoommateInfoSection INTERFACE
    // ========================================

    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public BooleanProperty visibleProperty() {
        return visibleProperty;
    }

    @Override
    public void setVisible(boolean visible) {
        visibleProperty.set(visible);
    }

    @Override
    public boolean isVisible() {
        return visibleProperty.get();
    }

    @Override
    public StringProperty roommateNameProperty() {
        return roommateNameProperty;
    }

    @Override
    public StringProperty registrationNumberProperty() {
        return registrationNumberProperty;
    }

    @Override
    public String getValidationMessage() {
        // Only return a message if the section is visible and invalid
        if (!visibleProperty.get()) {
            return null;
        }
        String name = roommateNameProperty.get();
        if (name == null || name.trim().length() < MIN_NAME_LENGTH) {
            return I18n.getI18nText(RoommateRequiredWarning);
        }
        return null;
    }

    @Override
    public void reset() {
        roommateNameProperty.set("");
        registrationNumberProperty.set("");
        updateValidity();
    }
}
