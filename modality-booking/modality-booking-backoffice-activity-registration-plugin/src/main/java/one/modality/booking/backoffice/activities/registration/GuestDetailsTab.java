package one.modality.booking.backoffice.activities.registration;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import one.modality.base.shared.entities.Document;

import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Guest Details tab for the Registration Edit Modal.
 * <p>
 * Features:
 * - Display and edit person information (firstName, lastName, email, phone, etc.)
 * - Show avatar with gender-colored border
 * - Age badge, nationality badge, language badge
 * - Linked account indicator
 * - Associated center indicator
 * <p>
 * Based on RegistrationDashboardFull.jsx Guest Details tab (lines 9912-10083).
 *
 * @author Claude Code
 */
public class GuestDetailsTab {

    private final ViewDomainActivityBase activity;
    private final RegistrationPresentationModel pm;
    private final Document document;
    private final UpdateStore updateStore;

    private final BooleanProperty activeProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty editingProperty = new SimpleBooleanProperty(false);

    // UI Components - Form Fields
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private TextField phoneField;
    // Gender selection using ToggleButtons (GWT-compatible replacement for ComboBox)
    private ToggleButton maleToggle;
    private ToggleButton femaleToggle;
    private ToggleGroup genderToggleGroup;
    private DatePicker birthDatePicker;
    private TextField streetField;
    private TextField cityField;
    private TextField postCodeField;
    private TextField countryField;
    private TextField languageField;

    public GuestDetailsTab(ViewDomainActivityBase activity, RegistrationPresentationModel pm, Document document, UpdateStore updateStore) {
        this.activity = activity;
        this.pm = pm;
        this.document = document;
        this.updateStore = updateStore;
    }

    /**
     * Builds the Guest Details tab UI.
     */
    public Node buildUi() {
        VBox container = new VBox(SPACING_LARGE);
        container.setPadding(PADDING_LARGE);

        // Header with avatar and edit toggle
        Node headerSection = createHeaderSection();

        // Person info badges row
        Node badgesRow = createBadgesRow();

        // Form sections
        Node personalInfoSection = createPersonalInfoSection();
        Node contactInfoSection = createContactInfoSection();
        Node addressSection = createAddressSection();

        container.getChildren().addAll(headerSection, badgesRow, personalInfoSection, contactInfoSection, addressSection);

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrollPane;
    }

    /**
     * Creates the header section with avatar and edit toggle.
     */
    private Node createHeaderSection() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(PADDING_MEDIUM);
        header.setBackground(createBackground(BG, BORDER_RADIUS_MEDIUM));
        header.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));

        // Avatar circle with initials
        String firstName = document.getStringFieldValue("person_firstName");
        String lastName = document.getStringFieldValue("person_lastName");
        Object isMale = document.getFieldValue("person_male");

        String initials = getInitials(firstName, lastName);
        Color borderColor = Boolean.TRUE.equals(isMale) ? Color.web("#3b82f6") : Color.web("#ec4899");

        StackPane avatar = createAvatar(initials, borderColor);

        // Name and info
        VBox nameInfo = new VBox(4);
        HBox.setHgrow(nameInfo, Priority.ALWAYS);

        String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        Label nameLabel = new Label(fullName.trim().isEmpty() ? "Unknown Guest" : fullName.trim());
        nameLabel.setFont(FONT_SUBTITLE);
        nameLabel.setTextFill(TEXT);

        Label emailLabel = new Label(document.getStringFieldValue("person_email"));
        emailLabel.setFont(FONT_SMALL);
        emailLabel.setTextFill(TEXT_MUTED);

        nameInfo.getChildren().addAll(nameLabel, emailLabel);

        // Edit toggle button
        Button editToggle = new Button("Edit");
        applySecondaryButtonStyle(editToggle);
        editToggle.setOnAction(e -> editingProperty.set(!editingProperty.get()));

        // Update button text based on editing state
        FXProperties.runNowAndOnPropertiesChange(() -> {
            editToggle.setText(editingProperty.get() ? "Done" : "Edit");
        }, editingProperty);

        header.getChildren().addAll(avatar, nameInfo, editToggle);
        return header;
    }

    /**
     * Creates the badges row showing age, nationality, language, etc.
     */
    private Node createBadgesRow() {
        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.setPadding(new Insets(8, 0, 8, 0));

        // Age badge
        Object ageObj = document.getFieldValue("person_age");
        if (ageObj instanceof Number) {
            int age = ((Number) ageObj).intValue();
            if (age > 0) {
                boolean isChild = age < 18;
                Label ageBadge = new Label(age + "y");
                ageBadge.setFont(FONT_BADGE);
                if (isChild) {
                    ageBadge.setBackground(createBackground(PURPLE_LIGHT, BORDER_RADIUS_SMALL));
                    ageBadge.setTextFill(PURPLE);
                } else {
                    ageBadge.setBackground(createBackground(BG, BORDER_RADIUS_SMALL));
                    ageBadge.setTextFill(TEXT_MUTED);
                }
                ageBadge.setPadding(new Insets(4, 8, 4, 8));
                badges.getChildren().add(ageBadge);
            }
        }

        // Ordained badge (if ordained)
        Object ordainedObj = document.getFieldValue("person_ordained");
        if (Boolean.TRUE.equals(ordainedObj)) {
            Label ordainedBadge = new Label("Ordained");
            ordainedBadge.setFont(FONT_BADGE);
            ordainedBadge.setBackground(createBackground(WARM_ORANGE_LIGHT, BORDER_RADIUS_SMALL));
            ordainedBadge.setTextFill(WARM_ORANGE);
            ordainedBadge.setPadding(new Insets(4, 8, 4, 8));
            badges.getChildren().add(ordainedBadge);
        }

        // Language badge
        String lang = document.getStringFieldValue("person_lang");
        if (lang != null && !lang.isEmpty()) {
            String flag = getLanguageFlag(lang);
            Label langBadge = new Label(flag + " " + lang.toUpperCase());
            langBadge.setFont(FONT_BADGE);
            langBadge.setBackground(createBackground(Color.web("#e0f2fe"), BORDER_RADIUS_SMALL));
            langBadge.setTextFill(Color.web("#0284c7"));
            langBadge.setPadding(new Insets(4, 8, 4, 8));
            badges.getChildren().add(langBadge);
        }

        return badges;
    }

    /**
     * Creates the personal information section.
     */
    private Node createPersonalInfoSection() {
        VBox section = new VBox(12);
        section.setPadding(PADDING_LARGE);
        section.setBackground(createBackground(BG_CARD, BORDER_RADIUS_MEDIUM));
        section.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));

        Label titleLabel = new Label("Personal Information");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setTextFill(TEXT);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        // First Name
        firstNameField = createFormField("First Name", document.getStringFieldValue("person_firstName"));
        addFormRow(grid, 0, "First Name", firstNameField);

        // Last Name
        lastNameField = createFormField("Last Name", document.getStringFieldValue("person_lastName"));
        addFormRow(grid, 1, "Last Name", lastNameField);

        // Gender (using ToggleButtons - GWT-compatible)
        genderToggleGroup = new ToggleGroup();
        maleToggle = new ToggleButton("Male");
        maleToggle.setToggleGroup(genderToggleGroup);
        maleToggle.disableProperty().bind(editingProperty.not());
        femaleToggle = new ToggleButton("Female");
        femaleToggle.setToggleGroup(genderToggleGroup);
        femaleToggle.disableProperty().bind(editingProperty.not());

        Object isMale = document.getFieldValue("person_male");
        if (Boolean.TRUE.equals(isMale)) {
            maleToggle.setSelected(true);
        } else {
            femaleToggle.setSelected(true);
        }

        HBox genderButtons = new HBox(8);
        genderButtons.getChildren().addAll(maleToggle, femaleToggle);
        addFormRow(grid, 2, "Gender", genderButtons);

        // Birth Date
        birthDatePicker = new DatePicker();
        birthDatePicker.disableProperty().bind(editingProperty.not());
        // TODO: Load birth date from person_birthdate field
        addFormRow(grid, 3, "Birth Date", birthDatePicker);

        section.getChildren().addAll(titleLabel, grid);
        return section;
    }

    /**
     * Creates the contact information section.
     */
    private Node createContactInfoSection() {
        VBox section = new VBox(12);
        section.setPadding(PADDING_LARGE);
        section.setBackground(createBackground(BG_CARD, BORDER_RADIUS_MEDIUM));
        section.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));

        Label titleLabel = new Label("Contact Information");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setTextFill(TEXT);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        // Email
        emailField = createFormField("Email", document.getStringFieldValue("person_email"));
        addFormRow(grid, 0, "Email", emailField);

        // Phone
        phoneField = createFormField("Phone", document.getStringFieldValue("person_phone"));
        addFormRow(grid, 1, "Phone", phoneField);

        // Language
        languageField = createFormField("Language", document.getStringFieldValue("person_lang"));
        addFormRow(grid, 2, "Language", languageField);

        section.getChildren().addAll(titleLabel, grid);
        return section;
    }

    /**
     * Creates the address section.
     */
    private Node createAddressSection() {
        VBox section = new VBox(12);
        section.setPadding(PADDING_LARGE);
        section.setBackground(createBackground(BG_CARD, BORDER_RADIUS_MEDIUM));
        section.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));

        Label titleLabel = new Label("Address");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setTextFill(TEXT);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);

        // Street
        streetField = createFormField("Street", document.getStringFieldValue("person_street"));
        addFormRow(grid, 0, "Street", streetField);

        // City
        cityField = createFormField("City", document.getStringFieldValue("person_cityName"));
        addFormRow(grid, 1, "City", cityField);

        // Post Code
        postCodeField = createFormField("Post Code", document.getStringFieldValue("person_postCode"));
        addFormRow(grid, 2, "Post Code", postCodeField);

        // Country
        countryField = createFormField("Country", document.getStringFieldValue("person_country"));
        addFormRow(grid, 3, "Country", countryField);

        section.getChildren().addAll(titleLabel, grid);
        return section;
    }

    /**
     * Creates a form text field.
     */
    private TextField createFormField(String prompt, String value) {
        TextField field = new TextField(value != null ? value : "");
        field.setPromptText(prompt);
        field.disableProperty().bind(editingProperty.not());
        return field;
    }

    /**
     * Adds a form row to a grid.
     */
    private void addFormRow(GridPane grid, int row, String labelText, Node field) {
        Label label = new Label(labelText);
        label.setFont(FONT_SMALL);
        label.setTextFill(TEXT_MUTED);
        label.setMinWidth(100);

        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    /**
     * Creates an avatar circle with initials.
     */
    private StackPane createAvatar(String initials, Color borderColor) {
        Circle circle = new Circle(24);
        circle.setFill(WARM_BROWN_LIGHT);
        circle.setStroke(borderColor);
        circle.setStrokeWidth(2);

        Label initialsLabel = new Label(initials);
        initialsLabel.setFont(FONT_BODY);
        initialsLabel.setTextFill(WARM_BROWN);

        StackPane avatar = new StackPane(circle, initialsLabel);
        avatar.setMinSize(48, 48);
        avatar.setMaxSize(48, 48);
        return avatar;
    }

    /**
     * Gets initials from first and last name.
     */
    private String getInitials(String firstName, String lastName) {
        String first = firstName != null && !firstName.isEmpty() ? firstName.substring(0, 1).toUpperCase() : "";
        String last = lastName != null && !lastName.isEmpty() ? lastName.substring(0, 1).toUpperCase() : "";
        return first + last;
    }

    /**
     * Gets a flag emoji for a language code.
     */
    private String getLanguageFlag(String lang) {
        if (lang == null) return "";
        return switch (lang.toLowerCase()) {
            case "en" -> "\uD83C\uDDEC\uD83C\uDDE7"; // GB flag
            case "de" -> "\uD83C\uDDE9\uD83C\uDDEA"; // DE flag
            case "fr" -> "\uD83C\uDDEB\uD83C\uDDF7"; // FR flag
            case "es" -> "\uD83C\uDDEA\uD83C\uDDF8"; // ES flag
            case "zh" -> "\uD83C\uDDE8\uD83C\uDDF3"; // CN flag
            case "pt" -> "\uD83C\uDDF5\uD83C\uDDF9"; // PT flag
            default -> "\uD83C\uDFF3\uFE0F"; // White flag
        };
    }

    /**
     * Sets the active state of the tab.
     */
    public void setActive(boolean active) {
        activeProperty.set(active);
    }

    /**
     * Gets the active property.
     */
    public BooleanProperty activeProperty() {
        return activeProperty;
    }

    /**
     * Gets the editing property.
     */
    public BooleanProperty editingProperty() {
        return editingProperty;
    }
}
