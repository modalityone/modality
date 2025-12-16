package one.modality.crm.backoffice.activities.customers;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.visual.controls.grid.VisualGrid;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.windowhistory.spi.BrowsingHistory;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.ButtonSelectorParameters;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import dev.webfx.stack.orm.reactive.entities.dql_to_entities.ReactiveEntitiesMapper;
import dev.webfx.stack.orm.reactive.mapping.entities_to_visual.ReactiveVisualMapper;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import one.modality.base.client.bootstrap.ModalityStyle;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.domainmodel.functions.AbcNames;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.FrontendAccount;
import one.modality.base.shared.entities.Person;
import one.modality.booking.backoffice.activities.bookings.BookingsRouting;

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
                {expression: 'this', label: 'ID', renderer: 'customerId', prefWidth: 50, hShrink: true, textAlign: 'center'},
                {expression: 'fullName', label: 'Name', prefWidth: 100, hShrink: true, textAlign: 'center'},
                {expression: 'email', label: 'Email', prefWidth: 180, hShrink: true, textAlign: 'center'},
                {expression: 'organization.name', label: 'Center', prefWidth: 250, hShrink: true, textAlign: 'center'},
                {expression: 'this', label: 'Account Type', renderer: 'customerAccountType', prefWidth: 250, hShrink: true, textAlign: 'center'},
                {expression: 'this', label: 'Status', renderer: 'customerStatus', prefWidth: 120, hShrink: true, textAlign: 'center'},
                {expression: 'this', label: '#Members / Owner', renderer: 'customerRolesLinks', prefWidth: 200, hShrink: true, textAlign: 'center'}
            ]""";

    // Table header height for limit calculation

    private final BorderPane view;
    private final VisualGrid customersGrid;
    private final BorderPane editPanel;
    private final SplitPane splitPane;
    private final CustomersPresentationModel pm;
    private final CustomersActivity activity;
    private final EntityStore entityStore = EntityStore.create();
    private final Region loadingSpinner;

    private UpdateStore updateStore;
    private Person personToUpdate;
    private BooleanBinding hasNoChangesBinding;

    private ReactiveVisualMapper<Person> customersMapper;
    private final ObservableList<Person> membersFeed = FXCollections.observableArrayList();
    private final ObservableList<Document> registrationsFeed = FXCollections.observableArrayList();
    private final ObservableList<Person> allMembersForCounting = FXCollections.observableArrayList();
    private final ObservableList<Person> currentCustomersFeed = FXCollections.observableArrayList();
    private final ObjectProperty<Person> selectedPersonProperty = new SimpleObjectProperty<>();
    private ReactiveEntitiesMapper<Person> membersMapper;
    private ReactiveEntitiesMapper<Document> registrationsMapper;
    private ReactiveEntitiesMapper<Person> allMembersMapper;

    // Filter controls
    private Label accountTypeBadge;
    private Button editModeButton;
    private Button saveButton;
    private Button cancelButton;

    // VBox container for ordained name field (for show/hide)
    private VBox layNameBox;

    // Badge components for read mode
    private Label genderBadge;
    private Label ordainedBadge;
    private HBox attributeBadgesBox;
    private HBox checkBoxRow;

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
    private DatePicker birthdatePicker;
    private CheckBox maleCheckBox;
    private CheckBox ordainedCheckBox;
    private TextField nationalityField;

    // Account tab controls
    private TextField usernameField;
    private TextField passwordHashField;
    private VBox accountTypeSection;

    // Registrations tab controls
    private VBox registrationsContainer;

    public CustomersView(CustomersPresentationModel pm, CustomersActivity activity) {
        this.pm = pm;
        this.activity = activity;

        // Register custom cell renderers
        CustomersRenderers.setView(this);
        CustomersRenderers.registerRenderers();

        // Main container
        view = new BorderPane();
        view.setId("customers");

        // Create limit checkbox

        // Create customers grid
        customersGrid = VisualGrid.createVisualGridWithTableLayoutSkin();
        customersGrid.setMinRowHeight(32);
        customersGrid.setPrefRowHeight(32);
        customersGrid.setPrefHeight(600);
        customersGrid.setMaxHeight(Double.MAX_VALUE);
        customersGrid.setMinWidth(0);
        customersGrid.setPrefWidth(Double.MAX_VALUE);
        customersGrid.setMaxWidth(Double.MAX_VALUE);
        customersGrid.getStyleClass().addAll("customers-grid");

        // Create loading indicator
        loadingSpinner = Controls.createSpinner(50);

        // Wrap grid and loading indicator in a StackPane
        StackPane gridContainer = new StackPane();
        gridContainer.getChildren().addAll(customersGrid, loadingSpinner);
        StackPane.setAlignment(loadingSpinner, Pos.CENTER);

        // Bind limit property - when checkbox is selected, limit to 100, otherwise no limit
        pm.setLimit(100);

        // Bind grid to visual result property to receive updates from filter changes
        // We also manually update it when member counts change (see listener below on allMembersForCounting)
        customersGrid.visualResultProperty().bind(pm.masterVisualResultProperty());

        // Show/hide loading indicator based on whether we're waiting for data
        // Listen to search and filter changes to show loading indicator
        FXProperties.runOnPropertiesChange(() -> {
            // Show loading indicator when filters change (data will be reloaded)
            loadingSpinner.setVisible(true);
        }, pm.searchTextProperty(), pm.accountTypeFilterProperty(), pm.activeStatusFilterProperty(), pm.limitProperty());

        // Create edit panel (initially hidden)
        editPanel = createEditPanel();

        // Create split pane with vertical orientation (grid on top, edit panel below)
        splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        // Initially only show the grid container (with loading indicator)
        splitPane.getItems().add(gridContainer);
        // Set the divider position to 60% for the grid, 40% for the edit panel when visible
        splitPane.setDividerPositions(0.6);

        view.setCenter(splitPane);

        // Start data logic
        startLogic();

        // Bind selection
        bindSelection();
    }

    public Node getView() {
        return view;
    }

    private void startLogic() {
        // IMPORTANT: Load ALL persons with frontend accounts (both owners and members) FIRST
        // This must be loaded before the customers table so that owner information is available for rendering
        // We load both owners and members so we can find the owner for any frontendAccountId
        //JSON5
        allMembersMapper = ReactiveEntitiesMapper.<Person>createPushReactiveChain(activity)
            .always(//language=JSON5
                """
                    { class: 'Person',
                      fields: 'id, firstName, lastName, fullName, owner, frontendAccount',
                      where: '!removed and frontendAccount!=null',
                      limit: 100
                      }""")
            .storeEntitiesInto(allMembersForCounting)
            .addEntitiesHandler(entityList -> {
                Console.log("All members/owners loaded: " + entityList.size() + " persons");
                // Now that owner data is loaded, start the customers mapper
                if (customersMapper != null) {
                    customersMapper.start();
                }
            }).start();

        // Set up the reactive mapper for customers (but don't start it yet - will start after allMembersMapper finishes)
        customersMapper = ReactiveVisualMapper.<Person>createPushReactiveChain(activity)
            .always(//language=JSON5
                """
                    { class: 'Person', alias: 'p',
                      fields: 'firstName, lastName, layName,phone,cityName,countryName,street,postCode,owner,removed,male,ordained,accountPerson.(id,fullName,frontendAccount), frontendAccount.(id,username,password,backoffice,disabled)',
                      orderBy: 'removed, frontendAccount.disabled, id desc',
                      limit: 100
                      }""")
            .setEntityColumns(CUSTOMER_COLUMNS)
            .setStore(entityStore)
            // Apply search filter
            .ifTrimNotEmpty(pm.searchTextProperty(), s ->
                Character.isDigit(s.charAt(0)) ? where("id = ?", Integer.parseInt(s))
                    : s.contains("@") ? where("lower(email) like ?", "%" + s.toLowerCase() + "%")
                    : where("abcNames like ?",AbcNames.evaluate(s, true)))
            // Apply account type filter (null = show all, otherwise filter by type)
            .ifNotNull(pm.accountTypeFilterProperty(), accountType -> switch (accountType) {
                case "frontoffice" -> where("frontendAccount!=null and !frontendAccount.backoffice");
                case "backoffice" -> where("frontendAccount!=null and frontendAccount.backoffice");
                case "owner" -> where("frontendAccount!=null and !frontendAccount.backoffice and owner");
                case "non-owner" -> where("!owner or frontendAccount=null or frontendAccount.backoffice");
                default -> where("true"); // Show all if unknown filter value
            })
            // Apply active status filter (null = show all, "active" = only active, "inactive" = disabled or removed)
            .ifNotNull(pm.activeStatusFilterProperty(), status ->
                "active".equals(status) ? where("!removed and (frontendAccount=null or !frontendAccount.disabled)")
                    : where("removed or frontendAccount.disabled"))
            // Apply limit - only when positive (checkbox checked = 100, unchecked = -1)
            .ifPositive(pm.limitProperty(), l -> limit(String.valueOf(l.intValue())))
            .setVisualSelectionProperty(customersGrid.visualSelectionProperty())
            .visualizeResultInto(pm.masterVisualResultProperty())
            .setSelectedEntityHandler(this::onPersonSelected)
            .addEntitiesHandler(entityList -> {
                Console.log("Customers loaded: " + entityList.size() + " customers");
                // Store current entities so we can manually recreate visual result when member counts change
                currentCustomersFeed.setAll(entityList);
                // Hide loading indicator now that data is loaded
                UiScheduler.runInUiThread(() -> loadingSpinner.setVisible(false));
            });
        // Note: .start() NOT called here - will be called after allMembersMapper finishes loading

        // Set up the reactive mapper for members (persons using the selected person's frontend account)
        // Using the same logic as MembersActivity - this loads full details for display in the Account tab
        // Note: This mapper is NOT started here - it will be started lazily when the Account & Access tab is selected
        membersMapper = ReactiveEntitiesMapper.<Person>createPushReactiveChain(activity)
            .always(// language=JSON5
                """
                    { class: 'Person', alias: 'la',
                      fields: 'firstName, lastName, fullName, email, accountPerson.(id,fullName)',
                      where: '!owner and !removed',
                      orderBy: 'id'
                      }""")
            .ifNotNullOtherwiseEmpty(selectedPersonProperty, person -> {
                // Get the frontend account - either from the person directly or from their accountPerson
                Object accountId = null;
                if (person.getFrontendAccount() != null) {
                    accountId = person.getFrontendAccount().getPrimaryKey();
                } else if (person.getAccountPerson() != null && person.getAccountPerson().getFrontendAccount() != null) {
                    accountId = person.getAccountPerson().getFrontendAccount().getPrimaryKey();
                }
                if (accountId != null) {
                    return where("frontendAccount=?", accountId);
                }
                // If no account found, return empty (no linked accounts)
                return where("false"); // Always false condition
            })
            .storeEntitiesInto(membersFeed);
        // Note: .start() NOT called here - will be called lazily when Account & Access tab is selected

        // Set up the reactive mapper for registrations (documents for the selected person)
        // Using the same logic as OrdersActivity - accountCanAccessPersonOrders function
        // Note: This mapper is NOT started here - it will be started lazily when the Registrations tab is selected
        registrationsMapper = ReactiveEntitiesMapper.<Document>createPushReactiveChain(activity)
            .always(//language=JSON5
                """
                    { class: 'Document', alias: 'd',
                      fields: 'ref, cancelled, price_deposit, price_net, person.(id,firstName,lastName), event.(name, startDate, endDate, organization.(id, name))',
                      where: '!abandoned or price_deposit>0',
                      orderBy: 'event.startDate desc',
                      limit: 20
                      }""")
            .ifNotNullOtherwiseEmpty(selectedPersonProperty, person -> {
                // Get the frontend account - either from the person directly or from their accountPerson
                Object accountId = null;
                if (person.getFrontendAccount() != null) {
                    accountId = person.getFrontendAccount().getPrimaryKey();
                } else if (person.getAccountPerson() != null && person.getAccountPerson().getFrontendAccount() != null) {
                    accountId = person.getAccountPerson().getFrontendAccount().getPrimaryKey();
                }
                if (accountId != null) {
                    return where("accountCanAccessPersonOrders(?, person)", accountId);
                }
                // If no account found, show documents where person matches directly
                return where("person=?", person.getPrimaryKey());
            })
            .addEntitiesHandler(entityList -> {
                // Update the feed
                registrationsFeed.setAll(entityList);
                // Directly update the UI to ensure "Loading..." message is replaced
                // This is necessary because setAll() on empty list might not trigger change listener
                UiScheduler.runInUiThread(() -> {
                    registrationsContainer.getChildren().clear();
                    if (entityList.isEmpty()) {
                        Label noRegistrationsLabel = I18nControls.newLabel(NoRegistrationsText);
                        Bootstrap.textSecondary(noRegistrationsLabel);
                        registrationsContainer.getChildren().add(noRegistrationsLabel);
                    } else {
                        for (Document doc : entityList) {
                            Node registrationCard = createRegistrationCard(doc);
                            registrationsContainer.getChildren().add(registrationCard);
                        }
                    }
                });
            });
        // Note: .start() NOT called here - will be called lazily when Registrations tab is selected

        // Refresh the grid when linked accounts change
        ObservableLists.runNowAndOnListChange(change -> {
            if (customersMapper != null) {
                customersMapper.refreshWhenActive();
            }
        }, membersFeed);

        // Refresh the grid when the members counting list changes (to update member counts in table)
        // Trigger mapper to refresh so that cell renderers see updated member counts
        ObservableLists.runNowAndOnListChange(change -> {
            if (customersMapper != null && !currentCustomersFeed.isEmpty()) {
                // Trigger mapper refresh to update visual result with new member counts
                customersMapper.refreshWhenActive();
            }
        }, allMembersForCounting);

        // Update the account type section when linked accounts change
        membersFeed.addListener((InvalidationListener) observable -> {
            Person selectedPerson = selectedPersonProperty.get();
            if (selectedPerson != null && accountTypeSection != null) {
                FrontendAccount account = selectedPerson.getFrontendAccount();
                boolean isOwner = Boolean.TRUE.equals(selectedPerson.isOwner());
                Person accountPerson = selectedPerson.getAccountPerson();
                updateAccountTypeSection(account, isOwner, accountPerson);
            }
        });
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
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(12, 20, 12, 20));

        // Left side: form fields in a single horizontal line
        HBox fieldsBox = new HBox(12);
        fieldsBox.setAlignment(Pos.BOTTOM_LEFT);

        // Add user icon at the beginning
        SVGPath userIcon = SvgIcons.createUserIcon();
        userIcon.setFill(Color.web("#00A3FF")); // Bootstrap primary color
        StackPane iconPane = new StackPane(userIcon);
        iconPane.setPrefSize(32, 32);
        iconPane.setMinSize(32, 32);
        iconPane.setMaxSize(32, 32);

        // Wrap icon in VBox to center it vertically
        VBox iconWrapper = new VBox(iconPane);
        iconWrapper.setAlignment(Pos.CENTER);

        // Create fields using ModalityStyle helper
        Label firstNameLabel = I18nControls.newLabel(FirstNameLabel);
        firstNameField = new TextField();
        firstNameField.setEditable(false);
        VBox firstNameBox = ModalityStyle.createFormTextField(firstNameLabel, firstNameField, true);

        Label lastNameLabel = I18nControls.newLabel(LastNameLabel);
        lastNameField = new TextField();
        lastNameField.setEditable(false);
        VBox lastNameBox = ModalityStyle.createFormTextField(lastNameLabel, lastNameField, true);

        Label usernameLabel = I18nControls.newLabel(UsernameLabel);
        usernameField = new TextField();
        usernameField.setEditable(false);
        VBox usernameBox = ModalityStyle.createFormTextField(usernameLabel, usernameField, true);

        Label passwordLabel = I18nControls.newLabel(PasswordHashLabel);
        passwordHashField = new TextField();
        passwordHashField.setEditable(false);
        passwordHashField.getStyleClass().add("password-hash-display");
        VBox passwordBox = ModalityStyle.createFormTextField(passwordLabel, passwordHashField, true);

        accountTypeBadge = new Label();

        fieldsBox.getChildren().addAll(
            iconWrapper,
            firstNameBox,
            lastNameBox,
            usernameBox,
            passwordBox
        );

        // Right side: badge, action buttons and close button
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

        // Discrete close button with × symbol
        Button closeButton = I18nControls.newButton(CloseButtonSymbol);
        closeButton.setPadding(new Insets(4, 8, 4, 8));
        closeButton.getStyleClass().addAll("btn", "btn-close");
        Bootstrap.button(closeButton);
        closeButton.setOnAction(e -> closeEditPanel());

        actionsBox.getChildren().addAll(accountTypeBadge, cancelButton, saveButton, editModeButton, closeButton);

        HBox.setHgrow(fieldsBox, Priority.ALWAYS);
        header.getChildren().addAll(fieldsBox, actionsBox);

        return header;
    }

    private TabPane createEditTabs() {
        TabPane tabPane = ModalityStyle.modernTabPane(new TabPane());

        // Personal Information Tab
        Tab personalTab = new Tab();
        I18n.bindI18nTextProperty(personalTab.textProperty(), PersonalInfoTab);
        personalTab.setContent(ModalityStyle.wrapTabContent(createPersonalInfoPanel()));

        // Account & Access Tab
        Tab accountTab = new Tab();
        I18n.bindI18nTextProperty(accountTab.textProperty(), AccountAccessTab);
        accountTab.setContent(ModalityStyle.wrapTabContent(createAccountAccessPanel()));

        // Registrations Tab
        Tab registrationsTab = new Tab();
        I18n.bindI18nTextProperty(registrationsTab.textProperty(), RegistrationsTab);
        registrationsTab.setContent(ModalityStyle.wrapTabContent(createRegistrationsPanel()));

        tabPane.getTabs().addAll(personalTab, accountTab, registrationsTab);

        // Lazy load data when tabs are selected
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == accountTab && membersMapper != null) {
                // Start the mapper if not already started
                if (!membersMapper.isStarted()) {
                    membersMapper.start();
                }
            }
            if (newTab == registrationsTab && registrationsMapper != null) {
                // Start the mapper if not already started
                if (!registrationsMapper.isStarted()) {
                    registrationsMapper.start();
                }
            }
        });

        return tabPane;
    }

    private ScrollPane createPersonalInfoPanel() {
        VBox content = new VBox(10);
        content.getStyleClass().add("edit-content");
        content.setPadding(new Insets(16, 20, 20, 20));

        // Form grid organized in logical sections
        GridPane formGrid = new GridPane();
        formGrid.getStyleClass().add("form-grid");
        formGrid.setHgap(16);
        formGrid.setVgap(6);

        int row = 0;

        // === Contact Information ===
        emailField = createFormField(formGrid, 0, row, EmailLabel);
        phoneField = createFormField(formGrid, 1, row, PhoneLabel);
        organizationSelector = createOrgComboField(formGrid, row);
        row++;

        // === Personal Attributes ===
        GridPane dummyGrid = new GridPane();
        maleCheckBox = createCheckBoxField(dummyGrid, MaleLabel);
        ordainedCheckBox = createCheckBoxField(dummyGrid, OrdainedLabel);

        // Ordained Name (only visible when ordained is checked)
        Label layNameLabel = I18nControls.newLabel(LayNameLabel);
        layNameField = new TextField();
        layNameField.setEditable(false);
        layNameBox = ModalityStyle.createFormTextField(layNameLabel, layNameField, true);

        // Initially hide if not ordained
        layNameBox.setVisible(false);
        layNameBox.setManaged(false);

        // Add listener to show/hide ordained name based on ordained checkbox
        ordainedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            layNameBox.setVisible(newVal);
            layNameBox.setManaged(newVal);
        });

        // Create badges for read mode (styling applied in updateAttributeBadges)
        genderBadge = new Label();

        ordainedBadge = I18nControls.newLabel(OrdainedLabel);

        attributeBadgesBox = new HBox(12);
        attributeBadgesBox.setAlignment(Pos.CENTER_LEFT);
        attributeBadgesBox.setPadding(new Insets(4, 0, 0, 0));
        attributeBadgesBox.getChildren().addAll(genderBadge, ordainedBadge);

        // Create checkBoxRow as instance variable for easy toggle
        checkBoxRow = new HBox(32);
        checkBoxRow.setAlignment(Pos.CENTER_LEFT);
        checkBoxRow.setPadding(new Insets(4, 0, 0, 0));
        checkBoxRow.getChildren().addAll(maleCheckBox, ordainedCheckBox, layNameBox);

        // Initially show badges (read mode)
        checkBoxRow.setVisible(false);
        checkBoxRow.setManaged(false);

        formGrid.add(attributeBadgesBox, 0, row, 3, 1);
        formGrid.add(checkBoxRow, 0, row, 3, 1);
        row++;

        // === Address ===
        postCodeField = createFormField(formGrid, 0, row, PostCodeLabel);
        cityNameField = createFormField(formGrid, 1, row, CityLabel);
        streetField = createFormField(formGrid, 2, row, StreetLabel);
        row++;

        // === Location & Birth ===
        countryNameField = createFormField(formGrid, 0, row, CountryLabel);
        nationalityField = createFormField(formGrid, 1, row, NationalityLabel);
        birthdatePicker = createDateField(formGrid, row);

        content.getChildren().add(formGrid);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("edit-scroll-pane");
        return scrollPane;
    }

    private ScrollPane createAccountAccessPanel() {
        VBox content = new VBox(20);
        content.getStyleClass().add("edit-content");
        content.setPadding(new Insets(20));

        // Account Type Section (dynamic content for linked accounts or owner account)
        accountTypeSection = new VBox(16);

        content.getChildren().add(accountTypeSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("edit-scroll-pane");
        return scrollPane;
    }

    private ScrollPane createRegistrationsPanel() {
        VBox content = new VBox(20);
        content.getStyleClass().add("edit-content");
        content.setPadding(new Insets(20));

        // Registrations section box (similar to members box in Account&Access tab)
        VBox registrationsBox = new VBox();
        registrationsBox.setSpacing(16);
        registrationsBox.setPadding(new Insets(20));
        registrationsBox.getStyleClass().add("members-box");

        Label title = I18nControls.newLabel(RegistrationsTab);
        title.getStyleClass().add("form-section-title");

        // Registrations list container
        registrationsContainer = new VBox(8);

        // Initially show loading text (will be replaced when data loads via handler)
        Label loadingLabel = I18nControls.newLabel(LoadingRegistrationsText);
        Bootstrap.textSecondary(loadingLabel);
        registrationsContainer.getChildren().add(loadingLabel);

        registrationsBox.getChildren().addAll(title, registrationsContainer);
        content.getChildren().add(registrationsBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("edit-scroll-pane");
        return scrollPane;
    }

    private Node createRegistrationCard(Document doc) {
        GridPane card = new GridPane();
        card.getStyleClass().add("member-item");
        card.setHgap(16);
        card.setPadding(new Insets(12));

        // Column 0: Organization badge (colored based on organization ID % 4)
        if (doc.getEvent() != null && doc.getEvent().getOrganization() != null) {
            Label orgBadge = new Label(doc.getEvent().getOrganization().getName());
            // Choose badge color based on organization ID modulo 4
            Object orgId = doc.getEvent().getOrganization().getId();
            int colorIndex = Math.abs(orgId.hashCode()) % 4;
            switch (colorIndex) {
                case 0 -> ModalityStyle.badgeLightInfo(orgBadge);
                case 1 -> ModalityStyle.badgeLightSuccess(orgBadge);
                case 2 -> ModalityStyle.badgeLightWarning(orgBadge);
                case 3 -> ModalityStyle.badgeLightPurple(orgBadge);
            }
            card.add(orgBadge, 0, 0);
        }

        // Column 1: Event name with dates (grows to take available space)
        StringBuilder eventText = new StringBuilder();
        if (doc.getEvent() != null) {
            eventText.append(doc.getEvent().getName());
            if (doc.getEvent().getStartDate() != null && doc.getEvent().getEndDate() != null) {
                eventText.append(" (")
                    .append(doc.getEvent().getStartDate().toString())
                    .append(" - ")
                    .append(doc.getEvent().getEndDate().toString())
                    .append(")");
            }
        }
        Label eventLabel = new Label(eventText.toString());
        Bootstrap.strong(eventLabel);
        GridPane.setHgrow(eventLabel, Priority.ALWAYS);
        card.add(eventLabel, 1, 0);

        // Column 2: Booking ref (clickable hyperlink to booking details)
        Hyperlink refLink = new Hyperlink("#" + doc.getRef());
        refLink.setMinWidth(80);
        refLink.setOnAction(e -> {
            BrowsingHistory history = activity.getHistory();
            if (history != null) {
                history.push(BookingsRouting.getDocumentBookingPath(doc.getPrimaryKey()));
            }
        });
        card.add(refLink, 2, 0);

        // Column 3: Attendee name (who the booking is for) - format as "#ID FirstName LastName"
        Label attendeeLabel = new Label();
        if (doc.getPerson() != null) {
            Person attendee = doc.getPerson();
            String attendeeName = "#" + attendee.getId().getPrimaryKey().toString() + " " + attendee.getFullName();
            attendeeLabel.setText(attendeeName);
        }
        Bootstrap.textSecondary(attendeeLabel);
        attendeeLabel.setMinWidth(200);
        card.add(attendeeLabel, 3, 0);

        // Column 4: Status badge (at the end)
        Label statusBadge = new Label();
        if (doc.isCancelled()) {
            statusBadge.setText(I18n.getI18nText(StatusCancelled));
            Bootstrap.dangerBadge(statusBadge);
        } else if (doc.getPriceDeposit() >= doc.getPriceNet()) {
            statusBadge.setText(I18n.getI18nText(StatusPaid));
            Bootstrap.successBadge(statusBadge);
        } else if (doc.getPriceDeposit() > 0) {
            statusBadge.setText(I18n.getI18nText(StatusPartial));
            Bootstrap.warningBadge(statusBadge);
        } else {
            statusBadge.setText(I18n.getI18nText(StatusPending));
            Bootstrap.secondaryBadge(statusBadge);
        }
        card.add(statusBadge, 4, 0);

        return card;
    }

    /**
     * Creates a read-only text field with an i18n label and adds it to the specified grid position.
     *
     * @param grid The GridPane to add the field to
     * @param col The column position in the grid
     * @param row The row position in the grid
     * @param i18nKey The i18n key for the field label
     * @return The created TextField (initially read-only)
     */
    private TextField createFormField(GridPane grid, int col, int row, Object i18nKey) {
        Label label = I18nControls.newLabel(i18nKey);
        TextField field = new TextField();
        field.setEditable(false);

        VBox box = ModalityStyle.createFormTextField(label, field, true);
        grid.add(box, col, row);

        return field;
    }

    private EntityButtonSelector<Entity> createOrgComboField(GridPane grid, int row) {
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
        grid.add(box, 2, row);

        return selector;
    }


    private DatePicker createDateField(GridPane grid, int row) {
        Label label = I18nControls.newLabel(BirthdateLabel);

        DatePicker picker = new DatePicker();
        picker.setEditable(false);
        picker.setDisable(true);

        VBox box = ModalityStyle.createFormDatePicker(label, picker);
        grid.add(box, 2, row);

        return picker;
    }

    private CheckBox createCheckBoxField(GridPane grid, Object i18nKey) {
        CheckBox checkBox = I18nControls.newCheckBox(i18nKey);
        checkBox.setDisable(true);
        ModalityStyle.createFormCheckBox(checkBox);

        grid.add(checkBox, 0, 0);

        return checkBox;
    }

    private void openEditPanel(Person person) {
        if (person == null) return;

        // Unbind save button from previous binding if any
        if (saveButton != null && hasNoChangesBinding != null) {
            saveButton.disableProperty().unbind();
        }

        // Create UpdateStore for this person
        updateStore = UpdateStore.createAbove(person.getStore());
        personToUpdate = updateStore.updateEntity(person);

        // Create a BooleanBinding that checks if updateStore has no changes
        hasNoChangesBinding = new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return updateStore == null || !updateStore.hasChanges();
            }
        };

        // Determine account type and set badge
        FrontendAccount account = person.getFrontendAccount();
        boolean isOwner = Boolean.TRUE.equals(person.isOwner());
        Person accountPerson = person.getAccountPerson();

        updateAccountBadge(account, isOwner, accountPerson);

        // Refresh lazy-loaded mappers if they're already started
        // This ensures data refreshes when switching between users
        if (membersMapper != null && membersMapper.isStarted()) {
            membersMapper.refreshWhenActive();
        }
        if (registrationsMapper != null && registrationsMapper.isStarted()) {
            registrationsMapper.refreshWhenActive();
        }

        // Populate login credentials (now in Personal Info tab)
        if (account != null) {
            usernameField.setText(account.getUsername());
            passwordHashField.setText(account.getPassword());
        } else {
            usernameField.setText(I18n.getI18nText(NoAccountText));
            passwordHashField.setText("");
        }

        // Populate personal info fields
        maleCheckBox.setSelected(Boolean.TRUE.equals(person.isMale()));
        ordainedCheckBox.setSelected(Boolean.TRUE.equals(person.isOrdained()));
        firstNameField.setText(person.getFirstName());
        lastNameField.setText(person.getLastName());

        // Contact information
        emailField.setText(person.getEmail());
        phoneField.setText(person.getPhone());

        // Identity & Organization
        nationalityField.setText(person.getNationality());
        Entity organization = person.getOrganization();
        if (organization != null) {
            organizationSelector.setSelectedItem(organization);
        }

        // Address
        streetField.setText(person.getStreet());
        cityNameField.setText(person.getCityName());
        countryNameField.setText(person.getCountryName());
        postCodeField.setText(person.getPostCode());

        // Additional information
        if (person.getBirthDate() != null) {
            birthdatePicker.setValue(person.getBirthDate());
        }
        layNameField.setText(person.getLayName());

        // Show/hide ordained name field based on ordained status
        boolean isOrdained = Boolean.TRUE.equals(person.isOrdained());
        layNameBox.setVisible(isOrdained);
        layNameBox.setManaged(isOrdained);

        // Update attribute badges
        updateAttributeBadges(person);

        // Update account type section
        updateAccountTypeSection(account, isOwner, accountPerson);

        // Set up field listeners to update entity in real-time
        setupFieldListeners();

        // Bind save button disable property to hasNoChangesBinding
        saveButton.disableProperty().bind(hasNoChangesBinding);

        // Show panel by adding it to the split pane if not already present
        setEditMode(false);
        if (!splitPane.getItems().contains(editPanel)) {
            splitPane.getItems().add(editPanel);
            // Set the divider position to give 60% to the grid, 40% to the edit panel
            splitPane.setDividerPositions(0.5);
        }

        // Scroll to edit panel
        UiScheduler.scheduleDelay(100, () -> {
            if (editPanel.getScene() != null) {
                editPanel.requestFocus();
            }
        });
    }

    /**
     * Updates the account type badge in the header based on the person's account status.
     *
     * @param account The person's frontend account (may be null)
     * @param isOwner Whether the person is the owner of their account
     * @param accountPerson The account owner person (for linked accounts, may be null)
     */
    private void updateAccountBadge(FrontendAccount account, boolean isOwner, Person accountPerson) {
        accountTypeBadge.getStyleClass().clear();

        if (account != null) {
            if (Boolean.TRUE.equals(account.isBackoffice())) {
                // Backoffice account
                accountTypeBadge.setText(I18n.getI18nText(AccountTypeBackoffice));
                Bootstrap.warningBadge(accountTypeBadge);
            } else if (isOwner) {
                // Frontoffice Owner
                accountTypeBadge.setText(I18n.getI18nText(AccountTypeFrontofficeOwner));
                Bootstrap.successBadge(accountTypeBadge);
            } else {
                // Frontoffice (non-owner)
                accountTypeBadge.setText(I18n.getI18nText(AccountTypeFrontoffice));
                Bootstrap.primaryBadge(accountTypeBadge);
            }
        } else if (accountPerson != null) {
            accountTypeBadge.setText(I18n.getI18nText(AccountTypeLinked));
            ModalityStyle.badgeLightInfo(accountTypeBadge);
        } else {
            accountTypeBadge.setText(I18n.getI18nText(AccountTypeNone));
            ModalityStyle.badgeLightGray(accountTypeBadge);
        }
    }

    private void updateAccountTypeSection(FrontendAccount account, boolean isOwner, Person accountPerson) {
        accountTypeSection.getChildren().clear();

        if (account != null && isOwner) {
            // Show members (people using this account)
            Label title = I18nControls.newLabel(MembersTitle);
            title.getStyleClass().add("form-section-title");

            VBox linkedBox = new VBox();
            linkedBox.setSpacing(16);
            linkedBox.setPadding(new Insets(20));
            linkedBox.getStyleClass().add("members-box");

            Label description = I18nControls.newLabel(MembersDescription);
            description.getStyleClass().add("members-description");

            // Get members for this person
            Person selectedPerson = selectedPersonProperty.get();
            VBox personItems = new VBox();
            personItems.setSpacing(8);
            personItems.getStyleClass().add("member-items");

            if (selectedPerson != null) {
                // The membersFeed already contains only accounts using the selected person's frontendAccount
                // No additional filtering needed - just check if the list is empty
                if (membersFeed.isEmpty()) {
                    Label placeholder = new Label(I18n.getI18nText(NoMembersText));
                    personItems.getChildren().add(placeholder);
                } else {
                    // Display all members from the feed
                    membersFeed.forEach(member -> {
                            HBox personItem = new HBox();
                            personItem.setSpacing(12);
                            personItem.setPadding(new Insets(12));
                            personItem.getStyleClass().add("member-item");
                            personItem.setAlignment(Pos.CENTER_LEFT);

                            Text checkIcon = new Text("✓");
                            checkIcon.getStyleClass().add("member-check-icon");

                            Label personName = new Label("#" + member.getId().getPrimaryKey().toString() + " " + member.getFirstName() + " " + member.getLastName());
                            personName.getStyleClass().add("member-name");

                            Label personEmail = new Label(member.getEmail());
                            personEmail.getStyleClass().add("member-email");
                            HBox.setHgrow(personEmail, Priority.ALWAYS);


                            // Add badge if this person is linked to another account
                            Label linkedBadge = null;
                            if (member.getAccountPerson() != null) {
                                linkedBadge = new Label();
                                linkedBadge.setText(I18n.getI18nText(LinkedToAccountText,
                                    member.getAccountPerson().getId().getPrimaryKey().toString(),
                                    member.getAccountPerson().getFullName()));
                                ModalityStyle.badgeLightInfo(linkedBadge);
                                linkedBadge.getStyleClass().add("member-account-badge");
                            }

                            // Add delete button (visible only in edit mode)
                            Button deleteButton = I18nControls.newButton(DeleteMemberButtonSymbol);
                            deleteButton.setPadding(new Insets(4, 8, 4, 8));
                            deleteButton.getStyleClass().addAll("action-button", "danger");
                            deleteButton.setOnAction(e -> deleteMember(member));
                            // Bind visibility to edit mode (edit mode = saveButton is visible)
                            deleteButton.visibleProperty().bind(saveButton.visibleProperty());
                            deleteButton.managedProperty().bind(saveButton.visibleProperty());

                            if (linkedBadge != null) {
                                personItem.getChildren().addAll(checkIcon, personName, personEmail, linkedBadge, deleteButton);
                            } else {
                                personItem.getChildren().addAll(checkIcon, personName, personEmail, deleteButton);
                            }
                            personItems.getChildren().add(personItem);
                        });
                }
            } else {
                Label placeholder = new Label(I18n.getI18nText(NoMembersText));
                personItems.getChildren().add(placeholder);
            }

            linkedBox.getChildren().addAll(description, personItems);
            accountTypeSection.getChildren().addAll(title, linkedBox);

        } else if (account != null) {
            // Non-owner with frontend account - show both owner and linked account (if different)
            Person selectedPerson = selectedPersonProperty.get();
            if (selectedPerson != null) {
                // Find the actual owner of this frontend account
                Person owner = getOwnerForFrontendAccount(account.getPrimaryKey());
                // Use the accountPerson parameter passed to this method

                VBox infoBox = new VBox();
                infoBox.setSpacing(20);

                // Show Owner section
                if (owner != null) {
                    Label ownerTitle = I18nControls.newLabel(OwnerAccountTitle);
                    ownerTitle.getStyleClass().add("form-section-title");

                    VBox ownerBox = new VBox();
                    ownerBox.setSpacing(16);
                    ownerBox.setPadding(new Insets(20));
                    ownerBox.getStyleClass().add("members-box");

                    Label ownerDescription = I18nControls.newLabel(OwnerAccountDescription);
                    ownerDescription.getStyleClass().add("members-description");

                    HBox ownerItem = new HBox();
                    ownerItem.setSpacing(12);
                    ownerItem.setPadding(new Insets(12));
                    ownerItem.getStyleClass().add("member-item");
                    ownerItem.setAlignment(Pos.CENTER_LEFT);

                    Text ownerIcon = new Text("\uD83D\uDC64"); // 👤
                    ownerIcon.getStyleClass().add("member-user-icon");

                    Label ownerName = new Label("#" + owner.getPrimaryKey().toString() + " " + owner.getFirstName() + " " + owner.getLastName());
                    ownerName.getStyleClass().add("member-name");

                    Label ownerEmail = new Label(owner.getEmail());
                    ownerEmail.getStyleClass().add("member-email");
                    HBox.setHgrow(ownerEmail, Priority.ALWAYS);

                    ownerItem.getChildren().addAll(ownerIcon, ownerName, ownerEmail);
                    ownerBox.getChildren().addAll(ownerDescription, ownerItem);
                    infoBox.getChildren().addAll(ownerTitle, ownerBox);
                }

                // Show Linked Account section (if different from owner)
                if (accountPerson != null && (owner == null || !accountPerson.getPrimaryKey().equals(owner.getPrimaryKey()))) {
                    Label linkedTitle = I18nControls.newLabel(LinkedAccountTitle);
                    linkedTitle.getStyleClass().add("form-section-title");

                    VBox linkedBox = new VBox();
                    linkedBox.setSpacing(16);
                    linkedBox.setPadding(new Insets(20));
                    linkedBox.getStyleClass().add("members-box");

                    Label linkedDescription = I18nControls.newLabel(LinkedAccountDescription);
                    linkedDescription.getStyleClass().add("members-description");

                    HBox linkedItem = new HBox();
                    linkedItem.setSpacing(12);
                    linkedItem.setPadding(new Insets(12));
                    linkedItem.getStyleClass().add("member-item");
                    linkedItem.setAlignment(Pos.CENTER_LEFT);

                    Text linkedIcon = new Text("🔗"); // Link icon
                    linkedIcon.getStyleClass().add("member-user-icon");

                    Label linkedName = new Label("#" + accountPerson.getPrimaryKey().toString() + " " + accountPerson.getFirstName() + " " + accountPerson.getLastName());
                    linkedName.getStyleClass().add("member-name");

                    Label linkedEmail = new Label(accountPerson.getEmail());
                    linkedEmail.getStyleClass().add("member-email");
                    HBox.setHgrow(linkedEmail, Priority.ALWAYS);

                    linkedItem.getChildren().addAll(linkedIcon, linkedName, linkedEmail);
                    linkedBox.getChildren().addAll(linkedDescription, linkedItem);
                    infoBox.getChildren().addAll(linkedTitle, linkedBox);
                }

                if (!infoBox.getChildren().isEmpty()) {
                    accountTypeSection.getChildren().add(infoBox);
                } else {
                    VBox placeholderBox = new VBox();
                    placeholderBox.setSpacing(16);
                    placeholderBox.setPadding(new Insets(20));
                    placeholderBox.getStyleClass().add("members-box");
                    Label placeholder = new Label(I18n.getI18nText(NoOwnerText));
                    placeholderBox.getChildren().add(placeholder);
                    accountTypeSection.getChildren().add(placeholderBox);
                }
            }
        } else {
            VBox ownerBox = new VBox();
            ownerBox.setSpacing(16);
            ownerBox.setPadding(new Insets(20));
            ownerBox.getStyleClass().add("members-box");
            Label placeholder = new Label(I18n.getI18nText(NoOwnerText));
            ownerBox.getChildren().add(placeholder);
            accountTypeSection.getChildren().add(ownerBox);
        }
    }

    private void closeEditPanel() {
        // Unbind save button
        if (saveButton != null && hasNoChangesBinding != null) {
            saveButton.disableProperty().unbind();
        }

        // Stop members mapper if it was started
        if (membersMapper != null && membersMapper.isStarted()) {
            membersMapper.stop();
        }

        // Stop registrations mapper if it was started
        if (registrationsMapper != null && registrationsMapper.isStarted()) {
            registrationsMapper.stop();
        }

        // Clear references
        updateStore = null;
        personToUpdate = null;
        hasNoChangesBinding = null;

        // Clear feeds
        membersFeed.clear();
        registrationsFeed.clear();

        // Clear selection through the mapper (proper way according to ReactiveVisualMapper)
        customersMapper.setSelectedEntity(null);

        // Remove edit panel from split pane
        splitPane.getItems().remove(editPanel);
        setEditMode(false);
    }

    private void deleteMember(Person member) {
        if (member == null) return;

        // Create an UpdateStore to modify the member
        UpdateStore deleteStore = UpdateStore.createAbove(member.getStore());
        Person personToUnlink = deleteStore.updateEntity(member);

        // Remove the link by clearing the frontendAccount
        personToUnlink.setFrontendAccount(null);

        // Submit the change
        deleteStore.submitChanges()
            .onSuccess(result -> {
                Console.log("Member removed successfully: " + member.getId());

                // Update UI on the FX application thread
                UiScheduler.runInUiThread(() -> {
                    // Remove from the membersFeed by finding the person with matching ID
                    membersFeed.removeIf(p -> p.getPrimaryKey().equals(member.getPrimaryKey()));

                    // Refresh allMembersMapper to update the count (will re-query and exclude the deleted member)
                    // The listener on allMembersForCounting will automatically refresh the table when the list updates
                    if (allMembersMapper != null) {
                        allMembersMapper.refreshWhenActive();
                    }
                });
            })
            .onFailure(error -> Console.log("Error removing member: " + error.getMessage()));
    }

    private void updateAttributeBadges(Person person) {
        if (person == null) return;

        // Update gender badge with color
        boolean isMale = Boolean.TRUE.equals(person.isMale());
        genderBadge.setText(I18n.getI18nText(isMale ? GenderMale : GenderFemale));
        // Clear previous styles
        genderBadge.getStyleClass().clear();
        // Apply color: Primary for Male, Pink for Female
        if (isMale) {
            Bootstrap.primaryBadge(genderBadge);
        } else {
            ModalityStyle.badgeLightPink(genderBadge);
        }

        // Update ordained badge visibility and color
        boolean isOrdained = Boolean.TRUE.equals(person.isOrdained());
        ordainedBadge.setVisible(isOrdained);
        ordainedBadge.setManaged(isOrdained);
        // Clear previous styles and apply danger color
        ordainedBadge.getStyleClass().clear();
        Bootstrap.dangerBadge(ordainedBadge);
    }

    /**
     * Toggles between read mode and edit mode for the edit panel.
     * In read mode, fields are disabled and badges are shown.
     * In edit mode, fields are editable and checkboxes are shown.
     *
     * @param editMode true to enable edit mode, false for read mode
     */
    private void setEditMode(boolean editMode) {

        // Toggle between badges (read mode) and checkboxes (edit mode)
        attributeBadgesBox.setVisible(!editMode);
        attributeBadgesBox.setManaged(!editMode);

        checkBoxRow.setVisible(editMode);
        checkBoxRow.setManaged(editMode);

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
        birthdatePicker.setDisable(!editMode);
        maleCheckBox.setDisable(!editMode);
        ordainedCheckBox.setDisable(!editMode);
        nationalityField.setEditable(editMode);

        // Toggle button visibility
        editModeButton.setVisible(!editMode);
        editModeButton.setManaged(!editMode);
        saveButton.setVisible(editMode);
        saveButton.setManaged(editMode);
        cancelButton.setVisible(editMode);
        cancelButton.setManaged(editMode);
    }

    /**
     * Sets up listeners on all form fields to automatically update the personToUpdate entity
     * and invalidate the hasNoChangesBinding when values change.
     * This enables real-time entity updates and proper save button state management.
     */
    private void setupFieldListeners() {
        if (personToUpdate == null || hasNoChangesBinding == null) return;

        // Text field listeners
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            personToUpdate.setFirstName(newVal != null ? newVal.trim() : null);
            hasNoChangesBinding.invalidate();
        });

        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            personToUpdate.setLastName(newVal != null ? newVal.trim() : null);
            hasNoChangesBinding.invalidate();
        });

        layNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            personToUpdate.setLayName(newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
            hasNoChangesBinding.invalidate();
        });

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            personToUpdate.setEmail(newVal != null ? newVal.trim() : null);
            hasNoChangesBinding.invalidate();
        });

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            personToUpdate.setPhone(newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
            hasNoChangesBinding.invalidate();
        });

        nationalityField.textProperty().addListener((obs, oldVal, newVal) -> {
            personToUpdate.setNationality(newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
            hasNoChangesBinding.invalidate();
        });

        cityNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            personToUpdate.setCityName(newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
            hasNoChangesBinding.invalidate();
        });

        countryNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            personToUpdate.setCountryName(newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
            hasNoChangesBinding.invalidate();
        });

        streetField.textProperty().addListener((obs, oldVal, newVal) -> {
            personToUpdate.setStreet(newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
            hasNoChangesBinding.invalidate();
        });

        postCodeField.textProperty().addListener((obs, oldVal, newVal) -> {
            personToUpdate.setPostCode(newVal != null && !newVal.trim().isEmpty() ? newVal.trim() : null);
            hasNoChangesBinding.invalidate();
        });

        // Checkbox listeners
        maleCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            personToUpdate.setMale(newVal);
            hasNoChangesBinding.invalidate();
            // Update badge immediately for visual feedback with color
            genderBadge.setText(I18n.getI18nText(newVal ? GenderMale : GenderFemale));
            genderBadge.getStyleClass().clear();
            if (newVal) {
                Bootstrap.primaryBadge(genderBadge);
            } else {
                ModalityStyle.badgeLightPink(genderBadge);
            }
        });

        ordainedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            personToUpdate.setOrdained(newVal);
            hasNoChangesBinding.invalidate();
            // Update badge visibility immediately
            ordainedBadge.setVisible(newVal);
            ordainedBadge.setManaged(newVal);
        });

        // DatePicker listener
        birthdatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            personToUpdate.setBirthDate(newVal);
            hasNoChangesBinding.invalidate();
        });

        // EntityButtonSelector listeners
        organizationSelector.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                personToUpdate.setForeignField("organization", EntityId.create(newVal.getDomainClass(), newVal.getPrimaryKey()));
            } else {
                personToUpdate.setForeignField("organization", null);
            }
            hasNoChangesBinding.invalidate();
        });
    }

    private void saveChanges() {
        if (updateStore == null || personToUpdate == null) return;

        // Submit changes to the data source (field values already set via listeners)
        updateStore.submitChanges()
            .onSuccess(result -> {
                setEditMode(false);
                // Refresh the grid
                customersMapper.refreshWhenActive();
                Console.log("Person saved successfully: " + personToUpdate.getId());
            })
            .onFailure(error -> {
                // Handle error
                Console.log("Error saving person: " + error.getMessage());
            });
    }

    private void cancelEditing() {
        // Discard changes by reopening the panel (this will create a fresh UpdateStore)
        Person selectedPerson = selectedPersonProperty.get();
        if (selectedPerson != null) {
            // Reload the original person from the entity store
            Person freshPerson = selectedPerson.getStore().getEntity(Person.class, selectedPerson.getPrimaryKey());
            if (freshPerson != null) {
                openEditPanel(freshPerson);
            }
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

    /**
     * Helper method for renderers to get the count of members (non-owners) for a specific person's frontend account.
     * Returns the number of persons who use the given person's frontend account but are not owners.
     * Uses allMembersForCounting which contains data for all persons with frontend accounts.
     */
    int getMembersCount(Person person) {
        if (person == null) return 0;

        // Get the frontend account for this person
        Object accountId = null;
        if (person.getFrontendAccount() != null) {
            accountId = person.getFrontendAccount().getPrimaryKey();
        } else if (person.getAccountPerson() != null && person.getAccountPerson().getFrontendAccount() != null) {
            accountId = person.getAccountPerson().getFrontendAccount().getPrimaryKey();
        }

        if (accountId == null) return 0;

        // Count persons in allMembersForCounting who have the same frontendAccount and are NOT owners
        // This list contains data for ALL persons with accounts (both owners and members), loaded once at startup
        final Object finalAccountId = accountId;
        return (int) allMembersForCounting.stream()
            .filter(p -> p.getFrontendAccount() != null &&
                        p.getFrontendAccount().getPrimaryKey().equals(finalAccountId) &&
                        !Boolean.TRUE.equals(p.isOwner()))
            .count();
    }

    /**
     * Helper method for renderers to find the owner Person for a given frontend account.
     * Returns the Person who has owner=true for the specified frontendAccountId.
     * Uses allMembersForCounting which contains data for all persons with frontend accounts.
     */
    Person getOwnerForFrontendAccount(Object frontendAccountId) {
        if (frontendAccountId == null) return null;

        return allMembersForCounting.stream()
            .filter(p -> p.getFrontendAccount() != null &&
                        p.getFrontendAccount().getPrimaryKey().equals(frontendAccountId) &&
                        Boolean.TRUE.equals(p.isOwner()))
            .findFirst()
            .orElse(null);
    }

}
