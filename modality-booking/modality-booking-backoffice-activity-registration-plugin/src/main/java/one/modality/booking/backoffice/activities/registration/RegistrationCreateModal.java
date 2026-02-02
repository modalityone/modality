package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.time.LocalDate;

import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Create Registration Modal for creating new registrations.
 * <p>
 * Sections:
 * 1. Guest Section - Search existing person or create new
 * 2. Event Section - Select event, arrival/departure dates
 * 3. Footer - Cancel and Create buttons
 * <p>
 * Based on RegistrationDashboardFull.jsx CreateRegistrationModal (lines 6670-7568).
 *
 * @author Claude Code
 */
public class RegistrationCreateModal {

    private final ViewDomainActivityBase activity;
    private final RegistrationPresentationModel pm;
    private final Runnable onSuccess;

    // Entity handling
    private UpdateStore updateStore;
    private Document newDocument;
    private Person newPerson;

    // UI Components
    private DialogCallback dialogCallback;

    // Guest section
    private EntityButtonSelector<Person> personSelector;
    private CheckBox createNewPersonCheck;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private TextField phoneField;
    // Gender selection using ToggleButtons (GWT-compatible replacement for ComboBox)
    private ToggleButton maleToggle;
    private ToggleButton femaleToggle;
    private ToggleGroup genderToggleGroup;

    // Event section
    private EntityButtonSelector<Event> eventSelector;
    private DatePicker arrivalDatePicker;
    private DatePicker departureDatePicker;

    // Validation
    private ValidationSupport validationSupport;

    public RegistrationCreateModal(ViewDomainActivityBase activity, RegistrationPresentationModel pm, Runnable onSuccess) {
        this.activity = activity;
        this.pm = pm;
        this.onSuccess = onSuccess;
    }

    /**
     * Shows the create registration modal.
     */
    public void show() {
        // Initialize stores
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        EntityStore store = EntityStore.create(dataSourceModel);
        updateStore = UpdateStore.createAbove(store);

        // Create new document entity
        newDocument = updateStore.insertEntity(Document.class);

        // Set organization from current context
        Object orgId = FXOrganizationId.getOrganizationId();
        if (orgId != null) {
            newDocument.setForeignField("organization", orgId);
        }

        // Build dialog UI
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox dialogContent = new VBox(SPACING_LARGE);
        dialogContent.setPadding(new Insets(24));
        dialogContent.setMinWidth(500);
        dialogContent.setPrefWidth(560);
        dialogContent.setMaxWidth(700);

        // Header
        Node header = createHeader();

        // Guest section
        Node guestSection = createGuestSection(dataSourceModel);

        // Event section
        Node eventSection = createEventSection(dataSourceModel);

        // Footer
        Node footer = createFooter();

        dialogContent.getChildren().addAll(header, guestSection, eventSection, footer);

        scrollPane.setContent(dialogContent);
        scrollPane.setMaxHeight(700);

        // Show dialog
        BorderPane dialogPane = new BorderPane(scrollPane);
        dialogPane.setBackground(createBackground(BG_CARD, BORDER_RADIUS_LARGE));
        dialogPane.setBorder(createBorder(BORDER, BORDER_RADIUS_LARGE));

        dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());

        // Set up validation
        setupValidation();
    }

    /**
     * Creates the header section.
     */
    private Node createHeader() {
        VBox header = new VBox(4);

        Label titleLabel = new Label("New Registration");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setTextFill(TEXT);

        Label subtitleLabel = new Label("Create a new booking for a guest");
        subtitleLabel.setFont(FONT_SMALL);
        subtitleLabel.setTextFill(TEXT_MUTED);

        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }

    /**
     * Creates the guest section.
     */
    private Node createGuestSection(DataSourceModel dataSourceModel) {
        VBox section = new VBox(12);
        section.setPadding(PADDING_LARGE);
        section.setBackground(createBackground(BG, BORDER_RADIUS_MEDIUM));
        section.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));

        // Section title
        Label sectionTitle = new Label("Guest Information");
        sectionTitle.setFont(FONT_SUBTITLE);
        sectionTitle.setTextFill(TEXT);

        // Search existing person
        VBox searchField = new VBox(4);
        Label searchLabel = new Label("Search Existing Guest");
        searchLabel.setFont(FONT_SMALL);
        searchLabel.setTextFill(TEXT_MUTED);

        ButtonSelectorParameters selectorParams = new ButtonSelectorParameters()
            .setButtonFactory(new ButtonFactoryMixin() {})
            .setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);

        personSelector = new EntityButtonSelector<>(
            "{class: 'Person', alias: 'p', columns: [{expression: 'firstName'}, {expression: 'lastName'}, {expression: 'email'}], orderBy: 'lastName,firstName'}",
            dataSourceModel,
            selectorParams
        );
        Button searchButton = personSelector.getButton();
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.setText("Search for existing guest...");

        // Bind person selection to document
        personSelector.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                newDocument.setPerson(newVal);
                // Disable new person fields when existing person selected
                setNewPersonFieldsDisabled(true);
            }
        });

        searchField.getChildren().addAll(searchLabel, searchButton);

        // Divider
        HBox dividerRow = new HBox();
        dividerRow.setAlignment(Pos.CENTER);
        dividerRow.setPadding(new Insets(8, 0, 8, 0));

        Region leftLine = new Region();
        leftLine.setMinHeight(1);
        leftLine.setBackground(createBackground(BORDER, 0));
        HBox.setHgrow(leftLine, Priority.ALWAYS);

        Label orLabel = new Label("OR");
        orLabel.setFont(FONT_TINY);
        orLabel.setTextFill(TEXT_MUTED);
        orLabel.setPadding(new Insets(0, 12, 0, 12));

        Region rightLine = new Region();
        rightLine.setMinHeight(1);
        rightLine.setBackground(createBackground(BORDER, 0));
        HBox.setHgrow(rightLine, Priority.ALWAYS);

        dividerRow.getChildren().addAll(leftLine, orLabel, rightLine);

        // Create new person checkbox
        createNewPersonCheck = new CheckBox("Create New Guest");
        createNewPersonCheck.setFont(FONT_BODY);
        createNewPersonCheck.setOnAction(e -> {
            boolean createNew = createNewPersonCheck.isSelected();
            setNewPersonFieldsDisabled(!createNew);
            if (createNew) {
                personSelector.setSelectedItem(null);
                // Create new person entity
                newPerson = updateStore.insertEntity(Person.class);
            } else {
                newPerson = null;
            }
        });

        // New person form fields
        VBox newPersonFields = new VBox(12);
        newPersonFields.setPadding(new Insets(12, 0, 0, 0));

        // Name row
        HBox nameRow = new HBox(12);
        nameRow.setMaxWidth(Double.MAX_VALUE);

        VBox firstNameFieldContainer = new VBox(4);
        Label firstNameLabel = new Label("First Name *");
        firstNameLabel.setFont(FONT_SMALL);
        firstNameLabel.setTextFill(TEXT_MUTED);
        firstNameField = new TextField();
        firstNameField.setPromptText("First name");
        applyInputFieldStyle(firstNameField);
        firstNameField.setDisable(true);
        HBox.setHgrow(firstNameFieldContainer, Priority.ALWAYS);
        firstNameFieldContainer.getChildren().addAll(firstNameLabel, firstNameField);

        VBox lastNameFieldContainer = new VBox(4);
        Label lastNameLabel = new Label("Last Name *");
        lastNameLabel.setFont(FONT_SMALL);
        lastNameLabel.setTextFill(TEXT_MUTED);
        lastNameField = new TextField();
        lastNameField.setPromptText("Last name");
        applyInputFieldStyle(lastNameField);
        lastNameField.setDisable(true);
        HBox.setHgrow(lastNameFieldContainer, Priority.ALWAYS);
        lastNameFieldContainer.getChildren().addAll(lastNameLabel, lastNameField);

        nameRow.getChildren().addAll(firstNameFieldContainer, lastNameFieldContainer);

        // Contact row
        HBox contactRow = new HBox(12);
        contactRow.setMaxWidth(Double.MAX_VALUE);

        VBox emailFieldContainer = new VBox(4);
        Label emailLabel = new Label("Email *");
        emailLabel.setFont(FONT_SMALL);
        emailLabel.setTextFill(TEXT_MUTED);
        emailField = new TextField();
        emailField.setPromptText("guest@example.com");
        applyInputFieldStyle(emailField);
        emailField.setDisable(true);
        HBox.setHgrow(emailFieldContainer, Priority.ALWAYS);
        emailFieldContainer.getChildren().addAll(emailLabel, emailField);

        VBox phoneFieldContainer = new VBox(4);
        Label phoneLabel = new Label("Phone");
        phoneLabel.setFont(FONT_SMALL);
        phoneLabel.setTextFill(TEXT_MUTED);
        phoneField = new TextField();
        phoneField.setPromptText("+44...");
        applyInputFieldStyle(phoneField);
        phoneField.setDisable(true);
        HBox.setHgrow(phoneFieldContainer, Priority.ALWAYS);
        phoneFieldContainer.getChildren().addAll(phoneLabel, phoneField);

        contactRow.getChildren().addAll(emailFieldContainer, phoneFieldContainer);

        // Gender (using ToggleButtons - GWT-compatible)
        VBox genderFieldContainer = new VBox(4);
        Label genderLabel = new Label("Gender");
        genderLabel.setFont(FONT_SMALL);
        genderLabel.setTextFill(TEXT_MUTED);

        genderToggleGroup = new ToggleGroup();
        maleToggle = new ToggleButton("Male");
        maleToggle.setToggleGroup(genderToggleGroup);
        maleToggle.setDisable(true);
        femaleToggle = new ToggleButton("Female");
        femaleToggle.setToggleGroup(genderToggleGroup);
        femaleToggle.setDisable(true);

        HBox genderButtons = new HBox(8);
        genderButtons.getChildren().addAll(maleToggle, femaleToggle);
        genderFieldContainer.getChildren().addAll(genderLabel, genderButtons);

        newPersonFields.getChildren().addAll(nameRow, contactRow, genderFieldContainer);

        section.getChildren().addAll(sectionTitle, searchField, dividerRow, createNewPersonCheck, newPersonFields);
        return section;
    }

    /**
     * Creates the event section.
     */
    private Node createEventSection(DataSourceModel dataSourceModel) {
        VBox section = new VBox(12);
        section.setPadding(PADDING_LARGE);
        section.setBackground(createBackground(BG, BORDER_RADIUS_MEDIUM));
        section.setBorder(createBorder(BORDER, BORDER_RADIUS_MEDIUM));

        // Section title
        Label sectionTitle = new Label("Event & Dates");
        sectionTitle.setFont(FONT_SUBTITLE);
        sectionTitle.setTextFill(TEXT);

        // Event selector
        VBox eventField = new VBox(4);
        Label eventLabel = new Label("Event *");
        eventLabel.setFont(FONT_SMALL);
        eventLabel.setTextFill(TEXT_MUTED);

        ButtonSelectorParameters selectorParams = new ButtonSelectorParameters()
            .setButtonFactory(new ButtonFactoryMixin() {})
            .setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);

        // Only show events for current organization
        Object orgId = FXOrganizationId.getOrganizationId();
        String eventQuery = "{class: 'Event', alias: 'e', columns: [{expression: 'name'}, {expression: 'startDate'}], " +
            "where: 'organization=" + orgId + " and state.id=2', orderBy: 'startDate desc'}";

        eventSelector = new EntityButtonSelector<>(
            eventQuery,
            dataSourceModel,
            selectorParams
        );
        Button eventButton = eventSelector.getButton();
        eventButton.setMaxWidth(Double.MAX_VALUE);
        eventButton.setText("Select an event...");

        // Bind event selection to document
        eventSelector.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                newDocument.setEvent(newVal);
                // Auto-fill dates from event if available
                // LocalDate eventStart = newVal.getStartDate();
                // LocalDate eventEnd = newVal.getEndDate();
                // if (eventStart != null) arrivalDatePicker.setValue(eventStart);
                // if (eventEnd != null) departureDatePicker.setValue(eventEnd);
            }
        });

        eventField.getChildren().addAll(eventLabel, eventButton);

        // Dates row
        HBox datesRow = new HBox(12);
        datesRow.setMaxWidth(Double.MAX_VALUE);

        VBox arrivalField = new VBox(4);
        Label arrivalLabel = new Label("Arrival Date *");
        arrivalLabel.setFont(FONT_SMALL);
        arrivalLabel.setTextFill(TEXT_MUTED);
        arrivalDatePicker = new DatePicker();
        arrivalDatePicker.setValue(LocalDate.now()); // GWT-compatible: set value after construction
        arrivalField.getChildren().addAll(arrivalLabel, arrivalDatePicker);

        VBox departureField = new VBox(4);
        Label departureLabel = new Label("Departure Date *");
        departureLabel.setFont(FONT_SMALL);
        departureLabel.setTextFill(TEXT_MUTED);
        departureDatePicker = new DatePicker();
        departureDatePicker.setValue(LocalDate.now().plusDays(3)); // GWT-compatible: set value after construction
        departureField.getChildren().addAll(departureLabel, departureDatePicker);

        datesRow.getChildren().addAll(arrivalField, departureField);

        section.getChildren().addAll(sectionTitle, eventField, datesRow);
        return section;
    }

    /**
     * Creates the footer with action buttons.
     */
    private Node createFooter() {
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 0, 0, 0));

        Button cancelButton = new Button("Cancel");
        applySecondaryButtonStyle(cancelButton);
        cancelButton.setOnAction(e -> closeDialog());

        Button createButton = Bootstrap.primaryButton(new Button("Create & Edit"));
        applyPrimaryButtonStyle(createButton);
        createButton.setOnAction(e -> handleCreate());

        footer.getChildren().addAll(cancelButton, createButton);
        return footer;
    }

    /**
     * Sets up form validation.
     */
    private void setupValidation() {
        validationSupport = new ValidationSupport();

        // Bind field changes to entities
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newPerson != null) {
                newPerson.setFirstName(newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
            }
        });

        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newPerson != null) {
                newPerson.setLastName(newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
            }
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newPerson != null) {
                newPerson.setEmail(newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
            }
        });

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newPerson != null) {
                newPerson.setPhone(newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
            }
        });
    }

    /**
     * Handles the create action.
     */
    private void handleCreate() {
        // Validate required fields
        boolean valid = true;
        StringBuilder errors = new StringBuilder();

        // Must have either selected person or new person data
        if (personSelector.getSelectedItem() == null && !createNewPersonCheck.isSelected()) {
            errors.append("Please select an existing guest or create a new one.\n");
            valid = false;
        }

        if (createNewPersonCheck.isSelected()) {
            if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
                errors.append("First name is required.\n");
                valid = false;
            }
            if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
                errors.append("Last name is required.\n");
                valid = false;
            }
            if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
                errors.append("Email is required.\n");
                valid = false;
            }
        }

        // Must have event
        if (eventSelector.getSelectedItem() == null) {
            errors.append("Please select an event.\n");
            valid = false;
        }

        // Must have dates
        if (arrivalDatePicker.getValue() == null) {
            errors.append("Arrival date is required.\n");
            valid = false;
        }
        if (departureDatePicker.getValue() == null) {
            errors.append("Departure date is required.\n");
            valid = false;
        }

        if (!valid) {
            showErrorDialog(errors.toString());
            return;
        }

        // Set person on document
        if (createNewPersonCheck.isSelected() && newPerson != null) {
            newDocument.setPerson(newPerson);
        }

        // TODO: Set dates on document
        // newDocument.setArrivalDate(arrivalDatePicker.getValue());
        // newDocument.setDepartureDate(departureDatePicker.getValue());

        // Submit changes
        updateStore.submitChanges()
            .onSuccess(result -> {
                closeDialog();
                if (onSuccess != null) {
                    onSuccess.run();
                }
                // Open edit modal for the new document
                pm.openEditModal(newDocument);
            })
            .onFailure(error -> {
                showErrorDialog("Failed to create registration: " + error.getMessage());
            });
    }

    /**
     * Sets the disabled state for new person fields.
     */
    private void setNewPersonFieldsDisabled(boolean disabled) {
        firstNameField.setDisable(disabled);
        lastNameField.setDisable(disabled);
        emailField.setDisable(disabled);
        phoneField.setDisable(disabled);
        maleToggle.setDisable(disabled);
        femaleToggle.setDisable(disabled);
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        if (dialogCallback != null) {
            dialogCallback.closeDialog();
        }
        pm.closeCreateModal();
    }

    /**
     * Applies consistent styling to input fields.
     */
    private void applyInputFieldStyle(TextField field) {
        field.setBackground(createBackground(BG_CARD, BORDER_RADIUS_SMALL));
        field.setBorder(createBorder(BORDER, BORDER_RADIUS_SMALL));
    }

    /**
     * Shows an error dialog.
     */
    private void showErrorDialog(String message) {
        VBox content = new VBox(16);
        content.setPadding(new Insets(24));
        content.setMinWidth(300);
        content.setMaxWidth(400);

        Label titleLabel = new Label("Validation Error");
        titleLabel.setFont(FONT_SUBTITLE);
        titleLabel.setTextFill(DANGER);

        Label messageLabel = new Label(message);
        messageLabel.setFont(FONT_BODY);
        messageLabel.setTextFill(TEXT);
        messageLabel.setWrapText(true);

        Button okBtn = new Button("OK");
        applySecondaryButtonStyle(okBtn);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(okBtn);

        content.getChildren().addAll(titleLabel, messageLabel, buttonBox);

        BorderPane dialogPane = new BorderPane(content);
        dialogPane.setBackground(createBackground(BG_CARD, BORDER_RADIUS_LARGE));
        dialogPane.setBorder(createBorder(BORDER, BORDER_RADIUS_LARGE));

        DialogCallback errorDialog = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());
        okBtn.setOnAction(e -> errorDialog.closeDialog());
    }
}
