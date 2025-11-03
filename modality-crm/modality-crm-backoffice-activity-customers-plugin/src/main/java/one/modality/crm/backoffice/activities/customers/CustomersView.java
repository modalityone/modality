package one.modality.crm.backoffice.activities.customers;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.FrontendAccount;
import one.modality.base.shared.entities.Person;

import static dev.webfx.stack.orm.dql.DqlStatement.limit;
import static dev.webfx.stack.orm.dql.DqlStatement.where;
import static one.modality.crm.backoffice.activities.customers.CustomersI18nKeys.*;

/**
 * Main view for customer management.
 * Displays a table of persons with inline editing capabilities.
 *
 * @author David Hello
 * @author Claude Code
 */
final class CustomersView {

    private static final String CUSTOMER_COLUMNS = // language=JSON5
        """
            [
                {expression: 'id', label: 'ID'},
             //   {expression: 'abcNames', label: 'Name'},
                {expression: 'firstName', label: 'First Name'},
                {expression: 'lastName', label: 'Last Name'},
                {expression: 'email', label: 'Email'},
                {expression: 'organization.name', label: 'Center'},
                {expression: 'phone', label: 'Phone'}
            ]""";

    // Table header height for limit calculation
    private static final int TABLE_HEADER_HEIGHT = 29;
    private static final int TABLE_ROW_HEIGHT = 28;

    private final BorderPane view;
    private final VisualGrid customersGrid;
    private final BorderPane editPanel;
    private final CustomersPresentationModel pm;
    private final CustomersActivity activity;
    private final EntityStore entityStore = EntityStore.create();

    private ReactiveVisualMapper<Person> customersMapper;
    private final ObservableList<Person> customersFeed = FXCollections.observableArrayList();
    private final ObjectProperty<Person> selectedPersonProperty = new SimpleObjectProperty<>();

    // Search and filter controls
    private TextField searchBox;
    private CheckBox limitCheckBox;

    private Label editCustomerNameLabel;
    private Label editCustomerIdLabel;
    private Label editAccountBadge;
    private Button editModeButton;
    private Button saveButton;
    private Button cancelButton;

    // Personal Information tab controls
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField layNameField;
    private TextField emailField;
    private TextField phoneField;
    private EntityButtonSelector<Entity> organizationSelector;
    private TextField cityNameField;
    private TextField countryNameField;
    private TextField streetField;
    private TextField postCodeField;
    private EntityButtonSelector<Entity> languageSelector;
    private DatePicker birthdatePicker;
    private CheckBox maleCheckBox;
    private CheckBox ordainedCheckBox;
    private TextField nationalityField;
    private TextField passportField;
    private CheckBox emailingListCheckBox;

    // Account tab controls
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField passwordHashField;
    private VBox accountTypeSection;
    private VBox backofficeRolesBox;
    private VBox linkedAccountsBox;

    public CustomersView(CustomersPresentationModel pm, CustomersActivity activity) {
        this.pm = pm;
        this.activity = activity;

        // Main container
        view = new BorderPane();
        view.setId("customers");

        // Create search and filter controls
        createSearchControls();

        // Create customers grid
        customersGrid = new VisualGrid();
        customersGrid.getStyleClass().addAll("customers-grid");
        customersGrid.setPrefHeight(400);
        customersGrid.setMaxHeight(400);

        // Bind limit property - when checkbox is selected, limit to 100, otherwise no limit
        FXProperties.runNowAndOnPropertiesChange(() ->
            pm.limitProperty().setValue(limitCheckBox.isSelected() ? 100 : -1),
            limitCheckBox.selectedProperty());

        // Bind visual result from presentation model to grid
        customersGrid.visualResultProperty().bind(pm.masterVisualResultProperty());

        // Create edit panel (initially hidden)
        editPanel = createEditPanel();
        editPanel.setVisible(false);
        editPanel.setManaged(false);

        // Layout: grid on top, edit panel below
        VBox mainContent = new VBox();
        mainContent.getChildren().addAll(customersGrid, editPanel);
        VBox.setVgrow(customersGrid, Priority.ALWAYS);

        view.setCenter(mainContent);

        // Add search bar at the top
        view.setTop(createSearchBar());

        // Start data logic
        startLogic();

        // Bind selection
        bindSelection();
    }

    public Node getView() {
        return view;
    }

    private void createSearchControls() {
        // Create search box
        searchBox = new TextField();
        searchBox.setPromptText(I18n.getI18nText(SearchPlaceholder));
        searchBox.getStyleClass().add("search-box");

        // Bind search text to presentation model
        pm.searchTextProperty().bind(searchBox.textProperty());

        // Create limit checkbox
        limitCheckBox = new CheckBox(I18n.getI18nText(LimitTo100));
        limitCheckBox.setSelected(true);
        limitCheckBox.getStyleClass().add("limit-checkbox");
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox(12);
        searchBar.setPadding(new Insets(12, 20, 12, 20));
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.getStyleClass().add("search-bar");

        HBox.setHgrow(searchBox, Priority.ALWAYS);
        searchBar.getChildren().addAll(searchBox, limitCheckBox);

        return searchBar;
    }

    private void startLogic() {
        // Set up the reactive mapper for customers
        customersMapper = ReactiveVisualMapper.<Person>createPushReactiveChain(activity)
            .always("{class: 'Person', alias: 'p', " +
                    "columns: 'id,firstName,lastName,layName,email,phone,organization.(id,name),cityName,countryName,street,postCode,birthdate,male,ordained,emailingList,nationality,passport,owner,frontendAccount.(id,username,password),accountPerson.(id,firstName,lastName)', " +
                    "where: '!removed', " +
                    "orderBy: 'id desc'}")
            .setEntityColumns(CUSTOMER_COLUMNS)
            .setStore(entityStore)
            // Apply search filter
            .ifTrimNotEmpty(pm.searchTextProperty(), s -> {
                // If the search starts with a digit, search by ID
                DqlStatement searchByNameandEmail = where("lower(firstName) like ? or lower(lastName) like ? or lower(email) like ?", AbcNames.evaluate(s, true),AbcNames.evaluate(s, true),AbcNames.evaluate(s, true));

                if (Character.isDigit(s.charAt(0))) {
                    try {
                        return where("id = ?", Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        // If parsing fails, search by name
                        return searchByNameandEmail;
                    }
                }
                // If the search contains '@', search by email
                else if (s.contains("@")) {
                    return where("lower(email) like ?", "%" + s.toLowerCase() + "%");
                }
                // Otherwise, search by name using abc_names (first_name and last_name combined)
                else {
                    return searchByNameandEmail;
                }
            })
            // Apply limit - only when positive (checkbox checked = 100, unchecked = -1)
            .ifPositive(pm.limitProperty(), l -> limit(String.valueOf(l.intValue())))
            .setVisualSelectionProperty(customersGrid.visualSelectionProperty())
            .visualizeResultInto(pm.masterVisualResultProperty())
            .bindActivePropertyTo(pm.activeProperty())
            .setSelectedEntityHandler(this::onPersonSelected)
            .addEntitiesHandler(entityList -> Console.log("Customers loaded: " + entityList.size() + " customers"))
            .start();
    }

    private void bindSelection() {
        pm.selectedPersonProperty().bind(selectedPersonProperty);
    }

    private void onPersonSelected(Entity entity) {
        if (entity instanceof Person) {
            selectedPersonProperty.set((Person) entity);
            openEditPanel((Person) entity);
        } else {
            selectedPersonProperty.set(null);
            closeEditPanel();
        }
    }

    private BorderPane createEditPanel() {
        BorderPane panel = new BorderPane();
        panel.getStyleClass().add("edit-panel");

        // Header
        HBox editPanelHeader = createEditPanelHeader();
        panel.setTop(editPanelHeader);

        // Tabs
        // Edit panel components
        TabPane editTabPane = createEditTabs();
        panel.setCenter(editTabPane);

        return panel;
    }

    private HBox createEditPanelHeader() {
        HBox header = new HBox(20);
        header.getStyleClass().add("edit-panel-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 20, 24));

        // Left side: title and badges
        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Text icon = new Text("\uD83D\uDC64"); // 👤
        icon.setStyle("-fx-font-size: 20px;");

        editCustomerNameLabel = new Label();
        editCustomerNameLabel.getStyleClass().add("edit-panel-title");

        editCustomerIdLabel = new Label();
        editCustomerIdLabel.getStyleClass().add("edit-panel-id");

        editAccountBadge = new Label();

        titleBox.getChildren().addAll(icon, editCustomerNameLabel, editCustomerIdLabel, editAccountBadge);

        // Right side: action buttons
        HBox actionsBox = new HBox(12);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        editModeButton = I18nControls.newButton(EditButton);
        editModeButton = ModalityStyle.largeOutlinePrimaryEditButton(editModeButton);
        editModeButton.setOnAction(e -> setEditMode(true));

        saveButton = I18nControls.newButton(SaveButton);
        Bootstrap.largePrimaryButton(saveButton, false);
        saveButton.setOnAction(e -> saveChanges());
        saveButton.setVisible(false);
        saveButton.setManaged(false);

        cancelButton = I18nControls.newButton(CancelButton);
        Bootstrap.largeSecondaryButton(cancelButton, false);
        cancelButton.setOnAction(e -> cancelEditing());
        cancelButton.setVisible(false);
        cancelButton.setManaged(false);

        actionsBox.getChildren().addAll(cancelButton, saveButton, editModeButton);

        HBox.setHgrow(titleBox, Priority.ALWAYS);
        header.getChildren().addAll(titleBox, actionsBox);

        return header;
    }

    private TabPane createEditTabs() {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("edit-tabs");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Personal Information Tab
        Tab personalTab = new Tab();
        I18n.bindI18nTextProperty(personalTab.textProperty(), PersonalInfoTab);
        personalTab.setContent(createPersonalInfoPanel());
        personalTab.getStyleClass().add("edit-tab");

        // Account & Access Tab
        Tab accountTab = new Tab();
        I18n.bindI18nTextProperty(accountTab.textProperty(), AccountAccessTab);
        accountTab.setContent(createAccountAccessPanel());
        accountTab.getStyleClass().add("edit-tab");

        tabPane.getTabs().addAll(personalTab, accountTab);

        return tabPane;
    }

    private ScrollPane createPersonalInfoPanel() {
        VBox content = new VBox(20);
        content.getStyleClass().add("edit-content");
        content.setPadding(new Insets(20));

        // Basic Information Section (Card-like)
        VBox basicSection = createSectionCard(BasicInfoSection);
        GridPane basicGrid = new GridPane();
        basicGrid.getStyleClass().add("form-grid");
        basicGrid.setHgap(16);
        basicGrid.setVgap(16);

        firstNameField = createFormField(basicGrid, 0, 0, FirstNameLabel);
        lastNameField = createFormField(basicGrid, 1, 0, LastNameLabel);
        layNameField = createFormField(basicGrid, 0, 1, LayNameLabel);
        birthdatePicker = createDateField(basicGrid);
        GridPane.setColumnIndex(birthdatePicker.getParent(), 1);
        GridPane.setRowIndex(birthdatePicker.getParent(), 1);

        basicSection.getChildren().add(basicGrid);

        // Contact & Identity Section (Card-like)
        VBox contactSection = createSectionCard(ContactIdentitySection);
        GridPane contactGrid = new GridPane();
        contactGrid.getStyleClass().add("form-grid");
        contactGrid.setHgap(16);
        contactGrid.setVgap(16);

        emailField = createFormField(contactGrid, 0, 0, EmailLabel);
        phoneField = createFormField(contactGrid, 1, 0, PhoneLabel);
        nationalityField = createFormField(contactGrid, 0, 1, NationalityLabel);
        passportField = createFormField(contactGrid, 1, 1, PassportLabel);

        contactSection.getChildren().add(contactGrid);

        // Location Section (Card-like)
        VBox locationSection = createSectionCard(LocationSection);
        GridPane locationGrid = new GridPane();
        locationGrid.getStyleClass().add("form-grid");
        locationGrid.setHgap(16);
        locationGrid.setVgap(16);

        organizationSelector = createOrgComboField(locationGrid);
        languageSelector = createLanguageComboField(locationGrid);

        cityNameField = createFormField(locationGrid, 0, 1, CityLabel);
        countryNameField = createFormField(locationGrid, 1, 1, CountryLabel);

        streetField = createFormField(locationGrid, 0, 2, StreetLabel);
        GridPane.setColumnSpan(streetField.getParent(), 2);

        postCodeField = createFormField(locationGrid, 0, 3, PostCodeLabel);

        locationSection.getChildren().add(locationGrid);

        // Personal Attributes Section (Card-like with checkboxes)
        VBox attributesSection = createSectionCard(PersonalAttributesSection);
        HBox checkBoxRow = new HBox(32);
        checkBoxRow.setAlignment(Pos.CENTER_LEFT);
        checkBoxRow.setPadding(new Insets(4, 0, 4, 0));

        GridPane dummyGrid = new GridPane();
        maleCheckBox = createCheckBoxField(dummyGrid, 0, MaleLabel);
        ordainedCheckBox = createCheckBoxField(dummyGrid, 0, OrdainedLabel);
        emailingListCheckBox = createCheckBoxField(dummyGrid, 0, EmailingListLabel);

        checkBoxRow.getChildren().addAll(maleCheckBox, ordainedCheckBox, emailingListCheckBox);
        attributesSection.getChildren().add(checkBoxRow);

        content.getChildren().addAll(
            basicSection,
            contactSection,
            locationSection,
            attributesSection
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("edit-scroll-pane");
        return scrollPane;
    }

    private VBox createSectionCard(Object i18nKey) {
        VBox section = new VBox(12);
        section.getStyleClass().add("form-section-card");
        section.setPadding(new Insets(20));

        Label title = I18nControls.newLabel(i18nKey);
        title.getStyleClass().add("form-section-card-title");

        section.getChildren().add(title);
        return section;
    }

    private ScrollPane createAccountAccessPanel() {
        VBox content = new VBox(20);
        content.getStyleClass().add("edit-content");
        content.setPadding(new Insets(24));

        // Login Credentials Section
        VBox credentialsSection = new VBox(16);
        credentialsSection.getStyleClass().add("account-credentials-section");

        Label credentialsTitle = I18nControls.newLabel(LoginCredentialsSection);
        credentialsTitle.getStyleClass().add("form-section-title");

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setEditable(false);
        Label usernameLabel = I18nControls.newLabel(UsernameLabel);
        VBox usernameBox = ModalityStyle.createFormTextField(usernameLabel, usernameField, true);

        passwordHashField = new TextField();
        passwordHashField.setEditable(false);
        passwordHashField.getStyleClass().add("password-hash-display");
        Label passwordLabel = I18nControls.newLabel(PasswordHashLabel);
        VBox passwordBox = ModalityStyle.createFormTextField(passwordLabel, passwordHashField, true);

        credentialsSection.getChildren().addAll(credentialsTitle, usernameBox, passwordBox);

        // Account Type Section (dynamic content)
        accountTypeSection = new VBox(16);

        content.getChildren().addAll(credentialsSection, accountTypeSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("edit-scroll-pane");
        return scrollPane;
    }

    private TextField createFormField(GridPane grid, int col, int row, Object i18nKey) {
        Label label = I18nControls.newLabel(i18nKey);
        TextField field = new TextField();
        field.setEditable(false);

        VBox box = ModalityStyle.createFormTextField(label, field, true);
        grid.add(box, col, row);

        return field;
    }

    private EntityButtonSelector<Entity> createOrgComboField(GridPane grid) {
        Label label = I18nControls.newLabel(OrganizationLabel);

        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();

        // Create EntityButtonSelector for Organization entities
        String organizationJson = // language=JSON5
            "{class: 'Organization', alias: 'o', columns: [{expression: 'name'}], orderBy: 'name'}";

        ButtonSelectorParameters params = new ButtonSelectorParameters()
            .setButtonFactory(new ButtonFactoryMixin() {})
            .setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);

        EntityButtonSelector<Entity> selector = new EntityButtonSelector<>(
            organizationJson,
            dataSourceModel,
            params
        );

        Button selectorButton = selector.getButton();
        selectorButton.setDisable(true); // Initially disabled (read-only mode)
        selectorButton.setMaxWidth(Double.MAX_VALUE);

        // Bind readonly style class to disable property - add when disabled, remove when enabled
        selectorButton.disableProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Button became disabled, add readonly style
                if (!selectorButton.getStyleClass().contains("readonly-field")) {
                    selectorButton.getStyleClass().add("readonly-field");
                }
            } else {
                // Button became enabled, remove readonly style
                selectorButton.getStyleClass().remove("readonly-field");
            }
        });
        // Set initial state
        if (selectorButton.isDisabled()) {
            selectorButton.getStyleClass().add("readonly-field");
        }

        VBox box = ModalityStyle.createFormEntitySelector(label, selector);
        grid.add(box, 0, 0);

        return selector;
    }

    private EntityButtonSelector<Entity> createLanguageComboField(GridPane grid) {
        Label label = I18nControls.newLabel(LanguageLabel);

        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();

        // Create EntityButtonSelector for Language entities
        String languageJson = // language=JSON5
            "{class: 'Language', alias: 'l', columns: [{expression: 'name'}], orderBy: 'name'}";

        ButtonSelectorParameters params = new ButtonSelectorParameters()
            .setButtonFactory(new ButtonFactoryMixin() {})
            .setDialogParentGetter(FXMainFrameDialogArea::getDialogArea);

        EntityButtonSelector<Entity> selector = new EntityButtonSelector<>(
            languageJson,
            dataSourceModel,
            params
        );

        Button selectorButton = selector.getButton();
        selectorButton.setDisable(true); // Initially disabled (read-only mode)
        selectorButton.setMaxWidth(Double.MAX_VALUE);

        // Bind readonly style class to disable property - add when disabled, remove when enabled
        selectorButton.disableProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Button became disabled, add readonly style
                if (!selectorButton.getStyleClass().contains("readonly-field")) {
                    selectorButton.getStyleClass().add("readonly-field");
                }
            } else {
                // Button became enabled, remove readonly style
                selectorButton.getStyleClass().remove("readonly-field");
            }
        });
        // Set initial state
        if (selectorButton.isDisabled()) {
            selectorButton.getStyleClass().add("readonly-field");
        }

        VBox box = ModalityStyle.createFormEntitySelector(label, selector);
        grid.add(box, 1, 0);

        return selector;
    }

    private DatePicker createDateField(GridPane grid) {
        Label label = I18nControls.newLabel(BirthdateLabel);

        DatePicker picker = new DatePicker();
        picker.setEditable(false);
        picker.setDisable(true);

        VBox box = ModalityStyle.createFormDatePicker(label, picker);
        grid.add(box, 2, 2);

        return picker;
    }

    private CheckBox createCheckBoxField(GridPane grid, int col, Object i18nKey) {
        CheckBox checkBox = I18nControls.newCheckBox(i18nKey);
        checkBox.setDisable(true);
        ModalityStyle.createFormCheckBox(checkBox);

        grid.add(checkBox, col, 0);

        return checkBox;
    }

    private void openEditPanel(Person person) {
        if (person == null) return;

        // Update header
        String displayName = person.getFirstName() + " " + person.getLastName();
        editCustomerNameLabel.setText(I18n.getI18nText(EditingLabel) + ": " + displayName);
        editCustomerIdLabel.setText("ID: " + person.getId());

        // Determine account type and set badge
        FrontendAccount account = person.getFrontendAccount();
        boolean isOwner = Boolean.TRUE.equals(person.isOwner());
        Person accountPerson = person.getAccountPerson();

        updateAccountBadge(account, isOwner, accountPerson);

        // Populate personal info fields
        firstNameField.setText(person.getFirstName());
        lastNameField.setText(person.getLastName());
        layNameField.setText(person.getLayName());
        emailField.setText(person.getEmail());
        phoneField.setText(person.getPhone());

        Entity organization = person.getOrganization();
        if (organization != null) {
            organizationSelector.setSelectedItem(organization);
        }

        cityNameField.setText(person.getCityName());
        countryNameField.setText(person.getCountryName());
        streetField.setText(person.getStreet());
        postCodeField.setText(person.getPostCode());

        // Language selection
        Entity language = person.getLanguage();
        if (language != null) {
            languageSelector.setSelectedItem(language);
        }

        if (person.getBirthDate() != null) {
            birthdatePicker.setValue(person.getBirthDate());
        }

        maleCheckBox.setSelected(Boolean.TRUE.equals(person.isMale()));
        ordainedCheckBox.setSelected(Boolean.TRUE.equals(person.isOrdained()));
        emailingListCheckBox.setSelected(Boolean.TRUE.equals(person.isEmailingList()));
        nationalityField.setText(person.getNationality());
        passportField.setText(person.getPassport());

        // Populate account info
        if (account != null) {
            usernameField.setText(account.getUsername());
            passwordHashField.setText(account.getPassword());
        } else {
            usernameField.setText(I18n.getI18nText(NoAccountText));
            passwordHashField.setText("");
        }

        // Update account type section
        updateAccountTypeSection(account, isOwner, accountPerson);

        // Show panel
        setEditMode(false);
        editPanel.setVisible(true);
        editPanel.setManaged(true);

        // Scroll to edit panel
        UiScheduler.scheduleDelay(100, () -> {
            if (editPanel.getScene() != null) {
                editPanel.requestFocus();
            }
        });
    }

    private void updateAccountBadge(FrontendAccount account, boolean isOwner, Person accountPerson) {
        editAccountBadge.getStyleClass().clear();

        if (account != null) {
            if (isOwner) {
                editAccountBadge.setText(I18n.getI18nText(AccountTypeFrontofficeOwner));
                ModalityStyle.badgeUser(editAccountBadge);
            } else {
                editAccountBadge.setText(I18n.getI18nText(AccountTypeFrontoffice));
                Bootstrap.badgeLightPurple(editAccountBadge);
            }
        } else if (accountPerson != null) {
            editAccountBadge.setText(I18n.getI18nText(AccountTypeLinked));
            Bootstrap.badgeLightInfo(editAccountBadge);
        } else {
            editAccountBadge.setText(I18n.getI18nText(AccountTypeNone));
            Bootstrap.badgeLightGray(editAccountBadge);
        }
    }

    private void updateAccountTypeSection(FrontendAccount account, boolean isOwner, Person accountPerson) {
        accountTypeSection.getChildren().clear();

        if (account != null && isOwner) {
            // Show linked accounts (people using this account)
            Label title = I18nControls.newLabel(LinkedAccountsTitle);
            title.getStyleClass().add("form-section-title");

            VBox linkedBox = new VBox(12);
            linkedBox.getStyleClass().add("linked-accounts-box");

            Label description = I18nControls.newLabel(LinkedAccountsDescription);
            description.getStyleClass().add("linked-accounts-description");

            // Query for persons with this account_person
            // This would require a query - simplified for now
            VBox personItems = new VBox(8);
            Label placeholder = new Label(I18n.getI18nText(NoLinkedAccountsText));
            personItems.getChildren().add(placeholder);

            linkedBox.getChildren().addAll(description, personItems);
            accountTypeSection.getChildren().addAll(title, linkedBox);

        } else if (accountPerson != null) {
            // Show owner account
            Label title = I18nControls.newLabel(OwnerAccountTitle);
            title.getStyleClass().add("form-section-title");

            VBox ownerBox = new VBox(12);
            ownerBox.getStyleClass().add("linked-accounts-box");

            Label description = I18nControls.newLabel(OwnerAccountDescription);
            description.getStyleClass().add("linked-accounts-description");

            HBox ownerItem = new HBox(12);
            ownerItem.getStyleClass().add("linked-person-item");
            ownerItem.setAlignment(Pos.CENTER_LEFT);

            Text ownerIcon = new Text("\uD83D\uDC64"); // 👤
            Label ownerName = new Label(accountPerson.getStringFieldValue("first_name") + " " +
                                       accountPerson.getStringFieldValue("last_name"));
            ownerName.getStyleClass().add("linked-person-name");

            Label ownerEmail = new Label(accountPerson.getStringFieldValue("email"));
            ownerEmail.getStyleClass().add("linked-person-email");
            HBox.setHgrow(ownerEmail, Priority.ALWAYS);

            Label ownerId = new Label("ID: " + accountPerson.getId());
            ownerId.getStyleClass().add("linked-person-id");

            ownerItem.getChildren().addAll(ownerIcon, ownerName, ownerEmail, ownerId);

            ownerBox.getChildren().addAll(description, ownerItem);
            accountTypeSection.getChildren().addAll(title, ownerBox);
        }
    }

    private void closeEditPanel() {
        editPanel.setVisible(false);
        editPanel.setManaged(false);
        setEditMode(false);
    }

    private void setEditMode(boolean editMode) {

        // Toggle field editability
        firstNameField.setEditable(editMode);
        lastNameField.setEditable(editMode);
        layNameField.setEditable(editMode);
        emailField.setEditable(editMode);
        phoneField.setEditable(editMode);
        organizationSelector.getButton().setDisable(!editMode);
        cityNameField.setEditable(editMode);
        countryNameField.setEditable(editMode);
        streetField.setEditable(editMode);
        postCodeField.setEditable(editMode);
        languageSelector.getButton().setDisable(!editMode);
        birthdatePicker.setDisable(!editMode);
        maleCheckBox.setDisable(!editMode);
        ordainedCheckBox.setDisable(!editMode);
        emailingListCheckBox.setDisable(!editMode);
        nationalityField.setEditable(editMode);
        passportField.setEditable(editMode);

        // Toggle button visibility
        editModeButton.setVisible(!editMode);
        editModeButton.setManaged(!editMode);
        saveButton.setVisible(editMode);
        saveButton.setManaged(editMode);
        cancelButton.setVisible(editMode);
        cancelButton.setManaged(editMode);
    }

    private void saveChanges() {
        Person selectedPerson = selectedPersonProperty.get();
        if (selectedPerson == null) return;

        // Create an update store
        UpdateStore updateStore = UpdateStore.createAbove(selectedPerson.getStore());
        Person personToUpdate = updateStore.updateEntity(selectedPerson);

        // Update person entity with field values
        personToUpdate.setFieldValue("first_name", firstNameField.getText());
        personToUpdate.setFieldValue("last_name", lastNameField.getText());
        personToUpdate.setFieldValue("lay_name", layNameField.getText());
        personToUpdate.setFieldValue("email", emailField.getText());
        personToUpdate.setFieldValue("phone", phoneField.getText());
        personToUpdate.setFieldValue("city_name", cityNameField.getText());
        personToUpdate.setFieldValue("country_name", countryNameField.getText());
        personToUpdate.setFieldValue("street", streetField.getText());
        personToUpdate.setFieldValue("post_code", postCodeField.getText());
        if (birthdatePicker.getValue() != null) {
            personToUpdate.setFieldValue("birthdate", birthdatePicker.getValue());
        }
        personToUpdate.setFieldValue("male", maleCheckBox.isSelected());
        personToUpdate.setFieldValue("ordained", ordainedCheckBox.isSelected());
        personToUpdate.setFieldValue("emailing_list", emailingListCheckBox.isSelected());
        personToUpdate.setFieldValue("nationality", nationalityField.getText());
        personToUpdate.setFieldValue("passport", passportField.getText());

        Entity selectedOrg = organizationSelector.getSelectedItem();
        if (selectedOrg != null) {
            personToUpdate.setForeignField("organization", EntityId.create(selectedOrg.getDomainClass(), selectedOrg.getPrimaryKey()));
        }

        Entity selectedLanguage = languageSelector.getSelectedItem();
        if (selectedLanguage != null) {
            personToUpdate.setForeignField("language", EntityId.create(selectedLanguage.getDomainClass(), selectedLanguage.getPrimaryKey()));
        }

        // Submit changes to the data source
        updateStore.submitChanges()
            .onSuccess(result -> {
                setEditMode(false);
                // Refresh the grid
                customersMapper.refreshWhenActive();
            })
            .onFailure(error -> {
                // Handle error - could show a dialog here
                System.err.println("Error saving person: " + error.getMessage());
            });
    }

    private void cancelEditing() {
        // Reload the person data
        Person selectedPerson = selectedPersonProperty.get();
        if (selectedPerson != null) {
            openEditPanel(selectedPerson);
        }
        setEditMode(false);
    }

    public void setActive(boolean active) {
        pm.setActive(active);
    }

    public void refreshData() {
        if (customersMapper != null) {
            customersMapper.refreshWhenActive();
        }
    }

    public ObservableList<Person> getCustomersFeed() {
        return customersFeed;
    }
}
