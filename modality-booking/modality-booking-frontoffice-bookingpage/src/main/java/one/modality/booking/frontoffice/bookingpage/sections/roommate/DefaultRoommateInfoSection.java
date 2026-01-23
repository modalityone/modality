package one.modality.booking.frontoffice.bookingpage.sections.roommate;

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
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.ArrayList;
import java.util.List;

import static one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys.*;

/**
 * Default implementation of the roommate information section.
 * Supports two modes:
 *
 * <h3>1. Share Accommodation Mode (isRoomBooker = false)</h3>
 * <p>Shows a single field for the room booker's name and an optional registration number.
 * Used when the current user is sharing someone else's room.</p>
 *
 * <h3>2. Room Booking Mode (isRoomBooker = true)</h3>
 * <p>Shows multiple fields for roommate names/booking references based on room capacity.
 * The number of fields is (capacity - 1) and (minOccupancy - 1) of them are mandatory.
 * Includes an info text explaining that roommates must register separately.</p>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-roommate-section} - section container</li>
 *   <li>{@code .bookingpage-form-field} - form field container</li>
 *   <li>{@code .bookingpage-text-input} - text input styling</li>
 *   <li>{@code .bookingpage-info-text} - info text styling</li>
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

    // === ROOM BOOKING CONFIGURATION ===
    protected final IntegerProperty roomCapacityProperty = new SimpleIntegerProperty(1);
    protected final IntegerProperty minOccupancyProperty = new SimpleIntegerProperty(1);
    protected final BooleanProperty isRoomBookerProperty = new SimpleBooleanProperty(false);

    // === DATA PROPERTIES (for Share Accommodation mode - single roommate) ===
    protected final StringProperty roommateNameProperty = new SimpleStringProperty("");
    protected final StringProperty registrationNumberProperty = new SimpleStringProperty("");

    // === DATA PROPERTIES (for Room Booking mode - multiple roommates) ===
    protected final List<StringProperty> roommateNameProperties = new ArrayList<>();
    protected final List<TextField> roommateTextFields = new ArrayList<>();

    // === VALIDATION ===
    protected final BooleanProperty validProperty = new SimpleBooleanProperty(true);

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected HBox sectionHeader;
    protected HBox infoBox;  // Info box for Room Booking mode
    protected VBox fieldsContainer;

    // Legacy fields for Share Accommodation mode
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

        // Section header with users icon
        sectionHeader = new StyledSectionHeader(RoommateInformation, StyledSectionHeader.ICON_USERS);
        VBox.setMargin(sectionHeader, new Insets(0, 0, 8, 0));

        // Info box (only shown in Room Booking mode) - styled info box with neutral (gray) background
        infoBox = BookingPageUIBuilder.createInfoBox(RoomBookingRoommatesInfo, BookingPageUIBuilder.InfoBoxType.NEUTRAL);
        infoBox.setVisible(false);
        infoBox.setManaged(false);
        VBox.setMargin(infoBox, new Insets(0, 0, 8, 0));

        // Container for dynamic fields
        fieldsContainer = new VBox(12);

        container.getChildren().addAll(sectionHeader, infoBox, fieldsContainer);

        // Build initial UI for Share Accommodation mode
        rebuildFields();
    }

    /**
     * Rebuilds the fields based on current mode (Share Accommodation vs Room Booking).
     */
    protected void rebuildFields() {
        fieldsContainer.getChildren().clear();
        roommateNameProperties.clear();
        roommateTextFields.clear();

        boolean isRoomBooker = isRoomBookerProperty.get();

        if (isRoomBooker) {
            // Room Booking mode: show multiple roommate fields
            int capacity = roomCapacityProperty.get();
            int minOccupancy = minOccupancyProperty.get();
            int numFields = Math.max(0, capacity - 1);
            int numMandatory = Math.max(0, minOccupancy - 1);

            // Show info box
            infoBox.setVisible(true);
            infoBox.setManaged(true);

            // Create fields for each roommate
            for (int i = 0; i < numFields; i++) {
                boolean isMandatory = i < numMandatory;
                // Only show number if there are multiple roommate fields
                VBox fieldContainer = createRoommateField(i + 1, isMandatory, numFields > 1);
                fieldsContainer.getChildren().add(fieldContainer);
            }
        } else {
            // Share Accommodation mode: show single combined field for name and reference
            infoBox.setVisible(false);
            infoBox.setManaged(false);

            VBox nameFieldContainer = createShareAccommodationNameField();
            fieldsContainer.getChildren().add(nameFieldContainer);
        }

        updateValidity();
    }

    /**
     * Creates a roommate field for Room Booking mode.
     * @param roommateNumber the roommate number (1-based)
     * @param isMandatory whether this field is mandatory
     * @param showNumber whether to show the roommate number (false for single roommate)
     */
    protected VBox createRoommateField(int roommateNumber, boolean isMandatory, boolean showNumber) {
        VBox fieldContainer = new VBox(6);
        fieldContainer.getStyleClass().add("bookingpage-form-field");

        // Create property for this field
        StringProperty nameProperty = new SimpleStringProperty("");
        roommateNameProperties.add(nameProperty);

        // Label with or without roommate number
        HBox labelRow;
        if (isMandatory) {
            labelRow = createNumberedRequiredFieldLabel(roommateNumber, showNumber);
        } else {
            labelRow = createNumberedOptionalFieldLabel(roommateNumber, showNumber);
        }

        // Text field
        TextField textField = new TextField();
        textField.setPromptText(I18n.getI18nText(RoommateNamePlaceholder));
        textField.getStyleClass().add("bookingpage-text-input");
        textField.textProperty().bindBidirectional(nameProperty);
        textField.setPadding(new Insets(12, 14, 12, 14));
        textField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(textField, Priority.ALWAYS);
        roommateTextFields.add(textField);

        // Listen for changes to update validity
        nameProperty.addListener((obs, oldVal, newVal) -> updateValidity());

        fieldContainer.getChildren().addAll(labelRow, textField);
        return fieldContainer;
    }

    /**
     * Creates a numbered label with required asterisk for Room Booking mode.
     * @param roommateNumber the roommate number (1-based)
     * @param showNumber whether to show the number (false shows just "Roommate")
     */
    protected HBox createNumberedRequiredFieldLabel(int roommateNumber, boolean showNumber) {
        // "Roommate 1" with asterisk, or just "Roommate" if showNumber is false
        String labelText = showNumber
            ? I18n.getI18nText(RoommateNumbered, roommateNumber)
            : I18n.getI18nText(Roommate);
        Label label = new Label(labelText);
        label.getStyleClass().add("bookingpage-form-label");

        Text asterisk = new Text(" *");
        asterisk.setFill(Color.web("#dc3545")); // Danger red
        asterisk.setFont(Font.font(null, FontWeight.SEMI_BOLD, 14));

        HBox wrapper = new HBox(label, asterisk);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        return wrapper;
    }

    /**
     * Creates a numbered label with (optional) suffix for Room Booking mode.
     * @param roommateNumber the roommate number (1-based)
     * @param showNumber whether to show the number (false shows just "Roommate")
     */
    protected HBox createNumberedOptionalFieldLabel(int roommateNumber, boolean showNumber) {
        // "Roommate 2 (optional)", or "Roommate (optional)" if showNumber is false
        String labelText = showNumber
            ? I18n.getI18nText(RoommateNumbered, roommateNumber)
            : I18n.getI18nText(Roommate);
        String optionalSuffix = " " + I18n.getI18nText(RoommateOptionalSuffix);
        Label label = new Label(labelText);
        label.getStyleClass().add("bookingpage-form-label");

        Text optional = new Text(optionalSuffix);
        optional.setFill(Color.web("#6c757d")); // Muted gray
        optional.setFont(Font.font(null, FontWeight.NORMAL, 12));

        HBox wrapper = new HBox(label, optional);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        return wrapper;
    }

    /**
     * Creates the combined roommate name and reference field for Share Accommodation mode.
     * This single field replaces the previous separate name and registration number fields.
     */
    protected VBox createShareAccommodationNameField() {
        VBox fieldContainer = new VBox(6);
        fieldContainer.getStyleClass().add("bookingpage-form-field");

        // Label with required asterisk - combined label for name and reference
        HBox labelRow = createRequiredFieldLabel(RoommateNameAndReference);

        // Text field - single combined field
        nameField = new TextField();
        I18n.bindI18nPromptProperty(nameField.promptTextProperty(), RoommateNameAndReferencePlaceholder);
        nameField.getStyleClass().add("bookingpage-text-input");
        nameField.textProperty().bindBidirectional(roommateNameProperty);
        nameField.setPadding(new Insets(12, 14, 12, 14));
        nameField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameField, Priority.ALWAYS);

        fieldContainer.getChildren().addAll(labelRow, nameField);
        return fieldContainer;
    }

    /**
     * Creates a label with a required field asterisk.
     */
    protected HBox createRequiredFieldLabel(Object i18nKey) {
        Label label = I18nControls.newLabel(i18nKey);
        label.getStyleClass().add("bookingpage-form-label");

        Text asterisk = new Text(" *");
        asterisk.setFill(Color.web("#dc3545")); // Danger red
        asterisk.setFont(Font.font(null, FontWeight.SEMI_BOLD, 14));

        HBox wrapper = new HBox(label, asterisk);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        return wrapper;
    }

    protected void setupBindings() {
        // Update validity when name changes (Share Accommodation mode)
        roommateNameProperty.addListener((obs, oldVal, newVal) -> updateValidity());

        // Update validity when visibility changes
        visibleProperty.addListener((obs, oldVal, newVal) -> {
            container.setVisible(newVal);
            container.setManaged(newVal);
            updateValidity();
        });

        // Rebuild fields when mode or capacity changes
        isRoomBookerProperty.addListener((obs, oldVal, newVal) -> rebuildFields());
        roomCapacityProperty.addListener((obs, oldVal, newVal) -> {
            if (isRoomBookerProperty.get()) {
                rebuildFields();
            }
        });
        minOccupancyProperty.addListener((obs, oldVal, newVal) -> {
            if (isRoomBookerProperty.get()) {
                rebuildFields();
            }
        });

        // Initial visibility state
        container.setVisible(visibleProperty.get());
        container.setManaged(visibleProperty.get());
    }

    /**
     * Updates the validity based on visibility and field values.
     * Section is valid if:
     * - It is not visible (hidden sections don't block navigation), OR
     * - In Share Accommodation mode: the roommate name has at least MIN_NAME_LENGTH characters
     * - In Room Booking mode: all mandatory roommate fields have at least MIN_NAME_LENGTH characters
     */
    protected void updateValidity() {
        boolean visible = visibleProperty.get();
        if (!visible) {
            // When not visible, section is valid (doesn't block navigation)
            validProperty.set(true);
            return;
        }

        boolean isRoomBooker = isRoomBookerProperty.get();

        if (isRoomBooker) {
            // Room Booking mode: validate mandatory fields
            int minOccupancy = minOccupancyProperty.get();
            int numMandatory = Math.max(0, minOccupancy - 1);

            boolean allMandatoryValid = true;
            for (int i = 0; i < numMandatory && i < roommateNameProperties.size(); i++) {
                String name = roommateNameProperties.get(i).get();
                if (name == null || name.trim().length() < MIN_NAME_LENGTH) {
                    allMandatoryValid = false;
                    break;
                }
            }
            validProperty.set(allMandatoryValid);
        } else {
            // Share Accommodation mode: validate single name field
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
    public IntegerProperty roomCapacityProperty() {
        return roomCapacityProperty;
    }

    @Override
    public void setRoomCapacity(int capacity) {
        roomCapacityProperty.set(capacity);
    }

    @Override
    public IntegerProperty minOccupancyProperty() {
        return minOccupancyProperty;
    }

    @Override
    public void setMinOccupancy(int minOccupancy) {
        minOccupancyProperty.set(minOccupancy);
    }

    @Override
    public BooleanProperty isRoomBookerProperty() {
        return isRoomBookerProperty;
    }

    @Override
    public void setIsRoomBooker(boolean isRoomBooker) {
        isRoomBookerProperty.set(isRoomBooker);
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

        boolean isRoomBooker = isRoomBookerProperty.get();

        if (isRoomBooker) {
            // Room Booking mode: check mandatory fields
            int minOccupancy = minOccupancyProperty.get();
            int numMandatory = Math.max(0, minOccupancy - 1);

            for (int i = 0; i < numMandatory && i < roommateNameProperties.size(); i++) {
                String name = roommateNameProperties.get(i).get();
                if (name == null || name.trim().length() < MIN_NAME_LENGTH) {
                    return I18n.getI18nText(RoommatesRequiredWarning);
                }
            }
        } else {
            // Share Accommodation mode: check single name field
            String name = roommateNameProperty.get();
            if (name == null || name.trim().length() < MIN_NAME_LENGTH) {
                return I18n.getI18nText(RoommateRequiredWarning);
            }
        }
        return null;
    }

    @Override
    public void reset() {
        // Reset Share Accommodation fields
        roommateNameProperty.set("");
        registrationNumberProperty.set("");

        // Reset Room Booking fields
        for (StringProperty prop : roommateNameProperties) {
            prop.set("");
        }

        updateValidity();
    }

    @Override
    public List<String> getAllRoommateNames() {
        List<String> names = new ArrayList<>();
        boolean isRoomBooker = isRoomBookerProperty.get();

        if (isRoomBooker) {
            // Room Booking mode: return all non-empty roommate names
            for (StringProperty prop : roommateNameProperties) {
                String name = prop.get();
                if (name != null && !name.trim().isEmpty()) {
                    names.add(name.trim());
                }
            }
        } else {
            // Share Accommodation mode: return single roommate name
            String name = roommateNameProperty.get();
            if (name != null && !name.trim().isEmpty()) {
                names.add(name.trim());
            }
        }

        return names;
    }
}
