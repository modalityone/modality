package one.modality.hotel.backoffice.activities.reception.modal;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Person;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.activities.reception.i18n.ReceptionI18nKeys;
import one.modality.hotel.backoffice.activities.reception.util.ReceptionColors;
import one.modality.hotel.backoffice.activities.reception.util.ReceptionStyles;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Modal dialog for creating a new accommodation booking.
 * Matches JSX mockup with:
 * - Guest details (name, phone required, email optional)
 * - Event selector with "Independent stay" option
 * - Room type button selector with availability
 * - First/last meal selection
 * - Diet preferences
 * - Deposit toggle with suggested amount (30%)
 * - Summary box with balance calculation
 *
 * @author David Hello
 * @author Claude Code
 */
public class NewBookingModal implements ReceptionDialogManager.ManagedDialog {

    private final DataSourceModel dataSourceModel;
    private final Object organizationId;

    private final BooleanProperty canProceed = new SimpleBooleanProperty(false);
    private Runnable onSuccessCallback;

    // Meal options
    private static final String[][] MEALS = {
        {"breakfast", "Breakfast"},
        {"lunch", "Lunch"},
        {"dinner", "Dinner"}
    };

    // Diet options
    private static final String[][] DIETS = {
        {"standard", "Standard"},
        {"vegetarian", "Vegetarian"},
        {"vegan", "Vegan"},
        {"wheat-free", "Wheat-free"}
    };

    // Pre-fill data (from availability modal)
    private Object prefillEventId;
    private LocalDate prefillArrival;
    private LocalDate prefillDeparture;
    private String prefillRoomType;

    // Form fields - Guest section
    private TextField nameField;
    private TextField phoneField;
    private TextField emailField;

    // Form fields - Booking section
    private Button eventSelectorButton;
    private VBox eventDropdownList;
    private ScrollPane eventDropdownScrollPane;
    private Event selectedEvent;
    private VBox dateSectionContainer;
    private DatePicker arrivalDatePicker;
    private DatePicker departureDatePicker;
    private ToggleButton[] roomTypeButtons;
    private String selectedRoomType = "single";

    // Form fields - Meals section
    private ToggleButton[] firstMealButtons;
    private ToggleButton[] lastMealButtons;
    private String selectedFirstMeal = "dinner";
    private String selectedLastMeal = "breakfast";
    private ToggleButton[] dietButtons;
    private final ObservableList<String> selectedDiets = FXCollections.observableArrayList("standard");

    // Form fields - Deposit section
    private CheckBox depositCheckbox;
    private TextField depositAmountField;
    private VBox depositAmountContainer;

    // Form fields - Notes
    private TextArea notesArea;

    // Summary section
    private VBox summaryBox;
    private Label nightsLabel;
    private Label totalLabel;
    private Label mealsLabel;
    private Label dietLabel;
    private VBox dietRow;
    private Label depositLabel;
    private VBox depositRow;
    private Label balanceLabel;

    // Data
    private final ObservableList<Event> events = FXCollections.observableArrayList();
    private final ObservableList<RoomTypeOption> roomTypes = FXCollections.observableArrayList();

    public NewBookingModal(DataSourceModel dataSourceModel, Object organizationId) {
        this(dataSourceModel, organizationId, null, null, null, null);
    }

    public NewBookingModal(DataSourceModel dataSourceModel, Object organizationId,
                          Object prefillEventId, LocalDate prefillArrival, LocalDate prefillDeparture, String prefillRoomType) {
        this.dataSourceModel = dataSourceModel;
        this.organizationId = organizationId;
        this.prefillEventId = prefillEventId;
        this.prefillArrival = prefillArrival;
        this.prefillDeparture = prefillDeparture;
        this.prefillRoomType = prefillRoomType;
        if (prefillRoomType != null) {
            this.selectedRoomType = prefillRoomType;
        }
        loadEvents();
        loadRoomTypes();
    }

    private void loadEvents() {
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        entityStore.executeQuery("select id,name,startDate,endDate from Event where organization=? and endDate>=? order by startDate",
                        organizationId, LocalDate.now())
                .onSuccess(list -> {
                    events.clear();
                    for (Object obj : list) {
                        if (obj instanceof Event) {
                            events.add((Event) obj);
                        }
                    }
                    updateEventDropdown();
                })
                .onFailure(e -> Console.log("Error loading events: " + e.getMessage()));
    }

    private void loadRoomTypes() {
        // Add placeholder room types - in production, would load from ResourceConfiguration
        roomTypes.clear();
        roomTypes.add(new RoomTypeOption("single", "Single Room", 65, 5));
        roomTypes.add(new RoomTypeOption("double", "Double Room", 85, 3));
        roomTypes.add(new RoomTypeOption("twin", "Twin Room", 80, 4));
        roomTypes.add(new RoomTypeOption("dormitory", "Dormitory", 45, 12));
    }

    private void updateEventDropdown() {
        if (eventDropdownList != null) {
            eventDropdownList.getChildren().clear();

            // Add independent stay option
            HBox independentRow = createEventDropdownRow(null, "Independent stay (custom dates)");
            eventDropdownList.getChildren().add(independentRow);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMM");
            for (Event event : events) {
                String label = event.getName();
                if (event.getStartDate() != null && event.getEndDate() != null) {
                    label += " (" + event.getStartDate().format(fmt) + " → " + event.getEndDate().format(fmt) + ")";
                }
                HBox eventRow = createEventDropdownRow(event, label);
                eventDropdownList.getChildren().add(eventRow);
            }
        }
    }

    private HBox createEventDropdownRow(Event event, String displayText) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: white; -fx-cursor: hand;");

        Label label = new Label(displayText);
        label.setStyle("-fx-font-size: 12px;");
        row.getChildren().add(label);

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f0f0f0; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white; -fx-cursor: hand;"));

        row.setOnMouseClicked(e -> {
            selectedEvent = event;
            eventSelectorButton.setText(displayText);
            eventDropdownScrollPane.setVisible(false);
            eventDropdownScrollPane.setManaged(false);

            if (event != null) {
                // Auto-set dates from event
                arrivalDatePicker.setValue(event.getStartDate());
                departureDatePicker.setValue(event.getEndDate());
                // Hide date section
                dateSectionContainer.setVisible(false);
                dateSectionContainer.setManaged(false);
            } else {
                // Show date section for independent stay
                dateSectionContainer.setVisible(true);
                dateSectionContainer.setManaged(true);
            }
            validateForm();
            updateSummary();
        });

        return row;
    }

    @Override
    public Node buildView() {
        VBox container = new VBox(ReceptionStyles.SPACING_MD);
        container.setPadding(new Insets(24));
        container.setMinWidth(520);
        container.setMaxWidth(580);
        container.getStyleClass().add(ReceptionStyles.RECEPTION_CARD);

        // Header
        VBox header = buildHeader();

        // Guest details section
        VBox guestSection = buildGuestSection();

        // Event selector
        VBox eventSection = buildEventSection();

        // Room type section
        VBox roomSection = buildRoomSection();

        // Meals section
        VBox mealsSection = buildMealsSection();

        // Deposit section
        VBox depositSection = buildDepositSection();

        // Notes section
        VBox notesSection = buildNotesSection();

        // Summary section
        summaryBox = buildSummarySection();

        container.getChildren().addAll(header, guestSection, eventSection, roomSection,
                                       mealsSection, depositSection, notesSection, summaryBox);

        // Initial state
        updateSummary();

        return container;
    }

    private VBox buildHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(0, 0, 16, 0));

        Label icon = new Label("📋");
        icon.setStyle("-fx-font-size: 32px;");

        Label title = I18nControls.newLabel(ReceptionI18nKeys.NewBooking);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");

        String subtitleText = prefillEventId != null ? "Pre-filled from availability" : "Create a new accommodation booking";
        Label subtitle = new Label(subtitleText);
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        header.getChildren().addAll(icon, title, subtitle);

        return header;
    }

    private VBox buildGuestSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);

        // Name field (full width)
        Label nameLabel = new Label("Name *");
        nameLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        nameField = new TextField();
        nameField.setPromptText("Full name");
        nameField.setStyle("-fx-font-size: 12px;");
        nameField.textProperty().addListener((obs, old, val) -> validateForm());

        // Phone and Email row
        GridPane grid = new GridPane();
        grid.setHgap(ReceptionStyles.SPACING_MD);
        grid.setVgap(ReceptionStyles.SPACING_XS);

        Label phoneLabel = new Label("Phone *");
        phoneLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        phoneField = new TextField();
        phoneField.setPromptText("+33 6 ...");
        phoneField.setStyle("-fx-font-size: 12px;");
        phoneField.textProperty().addListener((obs, old, val) -> validateForm());

        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        emailField = new TextField();
        emailField.setPromptText("email@example.com");
        emailField.setStyle("-fx-font-size: 12px;");

        grid.add(phoneLabel, 0, 0);
        grid.add(phoneField, 0, 1);
        grid.add(emailLabel, 1, 0);
        grid.add(emailField, 1, 1);

        GridPane.setHgrow(phoneField, Priority.ALWAYS);
        GridPane.setHgrow(emailField, Priority.ALWAYS);

        section.getChildren().addAll(nameLabel, nameField, grid);

        return section;
    }

    private VBox buildEventSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_XS);

        Label eventLabel = new Label("Event");
        eventLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        // Custom dropdown using Button + VBox (GWT-compatible)
        VBox selectorContainer = new VBox(0);

        eventSelectorButton = new Button("Select event");
        eventSelectorButton.setMaxWidth(Double.MAX_VALUE);
        eventSelectorButton.setStyle("-fx-font-size: 12px; -fx-background-color: white; -fx-border-color: #dee2e6; " +
            "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12; -fx-alignment: CENTER_LEFT;");

        // Dropdown list container
        eventDropdownList = new VBox(2);
        eventDropdownList.setPadding(new Insets(4));

        eventDropdownScrollPane = new ScrollPane(eventDropdownList);
        eventDropdownScrollPane.setFitToWidth(true);
        eventDropdownScrollPane.setMaxHeight(200);
        eventDropdownScrollPane.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 0 0 6 6;");
        eventDropdownScrollPane.setVisible(false);
        eventDropdownScrollPane.setManaged(false);

        // Populate dropdown
        updateEventDropdown();

        // Toggle dropdown on button click
        eventSelectorButton.setOnAction(e -> {
            boolean isVisible = eventDropdownScrollPane.isVisible();
            eventDropdownScrollPane.setVisible(!isVisible);
            eventDropdownScrollPane.setManaged(!isVisible);
        });

        selectorContainer.getChildren().addAll(eventSelectorButton, eventDropdownScrollPane);

        // Date section (shown only for independent stay)
        dateSectionContainer = new VBox(ReceptionStyles.SPACING_XS);
        dateSectionContainer.setPadding(new Insets(8, 0, 0, 0));

        GridPane dateGrid = new GridPane();
        dateGrid.setHgap(ReceptionStyles.SPACING_MD);
        dateGrid.setVgap(ReceptionStyles.SPACING_XS);

        Label arrivalLabel = new Label("Arrival");
        arrivalLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        arrivalDatePicker = new DatePicker();
        arrivalDatePicker.setValue(prefillArrival != null ? prefillArrival : LocalDate.now());
        arrivalDatePicker.setStyle("-fx-font-size: 12px;");
        arrivalDatePicker.valueProperty().addListener((obs, old, val) -> {
            validateForm();
            updateSummary();
        });

        Label departureLabel = new Label("Departure");
        departureLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        departureDatePicker = new DatePicker();
        departureDatePicker.setValue(prefillDeparture != null ? prefillDeparture : LocalDate.now().plusDays(2));
        departureDatePicker.setStyle("-fx-font-size: 12px;");
        departureDatePicker.valueProperty().addListener((obs, old, val) -> {
            validateForm();
            updateSummary();
        });

        dateGrid.add(arrivalLabel, 0, 0);
        dateGrid.add(arrivalDatePicker, 0, 1);
        dateGrid.add(departureLabel, 1, 0);
        dateGrid.add(departureDatePicker, 1, 1);

        GridPane.setHgrow(arrivalDatePicker, Priority.ALWAYS);
        GridPane.setHgrow(departureDatePicker, Priority.ALWAYS);

        dateSectionContainer.getChildren().add(dateGrid);

        // Populate dropdown
        updateEventDropdown();

        section.getChildren().addAll(eventLabel, selectorContainer, dateSectionContainer);

        return section;
    }

    private VBox buildRoomSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_XS);

        Label roomLabel = new Label("Room type");
        roomLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        GridPane grid = new GridPane();
        grid.setHgap(ReceptionStyles.SPACING_SM);
        grid.setVgap(ReceptionStyles.SPACING_SM);

        roomTypeButtons = new ToggleButton[roomTypes.size()];

        for (int i = 0; i < roomTypes.size(); i++) {
            RoomTypeOption rt = roomTypes.get(i);

            ToggleButton btn = new ToggleButton();
            btn.setUserData(rt.id);

            VBox btnContent = new VBox(2);
            btnContent.setPadding(new Insets(8, 12, 8, 12));
            btnContent.setAlignment(Pos.CENTER_LEFT);

            Label nameLabel = new Label(rt.name);
            nameLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 500;");

            Label priceLabel = new Label("€" + rt.price + "/night · " + rt.available + " left");
            priceLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

            btnContent.getChildren().addAll(nameLabel, priceLabel);
            btn.setGraphic(btnContent);
            btn.setText("");

            btn.setPrefWidth(220);
            btn.setPrefHeight(60);

            int index = i;
            btn.setOnAction(e -> selectRoomType(index));

            // Initial selection
            if (rt.id.equals(selectedRoomType)) {
                btn.setSelected(true);
                styleRoomTypeButton(btn, true);
            } else {
                styleRoomTypeButton(btn, false);
            }

            roomTypeButtons[i] = btn;
            grid.add(btn, i % 2, i / 2);
        }

        section.getChildren().addAll(roomLabel, grid);

        return section;
    }

    private void selectRoomType(int index) {
        selectedRoomType = (String) roomTypeButtons[index].getUserData();

        for (int i = 0; i < roomTypeButtons.length; i++) {
            boolean isSelected = i == index;
            roomTypeButtons[i].setSelected(isSelected);
            styleRoomTypeButton(roomTypeButtons[i], isSelected);
        }

        updateSummary();
    }

    private void styleRoomTypeButton(ToggleButton btn, boolean selected) {
        if (selected) {
            btn.setStyle("-fx-background-color: #cfe2ff; -fx-border-color: #0d6efd; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        } else {
            btn.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        }
    }

    private VBox buildMealsSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);
        section.setPadding(new Insets(12));
        section.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        // Meals header
        HBox mealsHeader = new HBox(ReceptionStyles.SPACING_SM);
        mealsHeader.setAlignment(Pos.CENTER_LEFT);
        Label mealsIcon = new Label("🍽️");
        mealsIcon.setStyle("-fx-font-size: 16px;");
        Label mealsTitle = new Label("Meals");
        mealsTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: 500;");
        mealsHeader.getChildren().addAll(mealsIcon, mealsTitle);

        // First meal / Last meal row
        GridPane mealsGrid = new GridPane();
        mealsGrid.setHgap(ReceptionStyles.SPACING_MD);
        mealsGrid.setVgap(ReceptionStyles.SPACING_XS);

        // First meal
        Label firstMealLabel = new Label("First meal");
        firstMealLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        HBox firstMealButtons = new HBox(4);
        this.firstMealButtons = new ToggleButton[MEALS.length];
        for (int i = 0; i < MEALS.length; i++) {
            ToggleButton btn = new ToggleButton(MEALS[i][1]);
            btn.setUserData(MEALS[i][0]);
            btn.setPadding(new Insets(6, 10, 6, 10));
            HBox.setHgrow(btn, Priority.ALWAYS);

            int index = i;
            btn.setOnAction(e -> selectFirstMeal(index));

            if (MEALS[i][0].equals(selectedFirstMeal)) {
                btn.setSelected(true);
                styleMealButton(btn, true);
            } else {
                styleMealButton(btn, false);
            }

            this.firstMealButtons[i] = btn;
            firstMealButtons.getChildren().add(btn);
        }

        // Last meal
        Label lastMealLabel = new Label("Last meal");
        lastMealLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        HBox lastMealButtons = new HBox(4);
        this.lastMealButtons = new ToggleButton[MEALS.length];
        for (int i = 0; i < MEALS.length; i++) {
            ToggleButton btn = new ToggleButton(MEALS[i][1]);
            btn.setUserData(MEALS[i][0]);
            btn.setPadding(new Insets(6, 10, 6, 10));
            HBox.setHgrow(btn, Priority.ALWAYS);

            int index = i;
            btn.setOnAction(e -> selectLastMeal(index));

            if (MEALS[i][0].equals(selectedLastMeal)) {
                btn.setSelected(true);
                styleMealButton(btn, true);
            } else {
                styleMealButton(btn, false);
            }

            this.lastMealButtons[i] = btn;
            lastMealButtons.getChildren().add(btn);
        }

        mealsGrid.add(firstMealLabel, 0, 0);
        mealsGrid.add(firstMealButtons, 0, 1);
        mealsGrid.add(lastMealLabel, 1, 0);
        mealsGrid.add(lastMealButtons, 1, 1);

        // Diet section
        VBox dietSection = new VBox(ReceptionStyles.SPACING_XS);
        dietSection.setPadding(new Insets(8, 0, 0, 0));

        Label dietLabel = new Label("Diet");
        dietLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        HBox dietButtonsRow = new HBox(ReceptionStyles.SPACING_SM);
        dietButtons = new ToggleButton[DIETS.length];

        for (int i = 0; i < DIETS.length; i++) {
            ToggleButton btn = new ToggleButton(DIETS[i][1]);
            btn.setUserData(DIETS[i][0]);
            btn.setPadding(new Insets(6, 12, 6, 12));

            int index = i;
            btn.setOnAction(e -> toggleDiet(index));

            if (selectedDiets.contains(DIETS[i][0])) {
                btn.setSelected(true);
                styleDietButton(btn, true);
            } else {
                styleDietButton(btn, false);
            }

            dietButtons[i] = btn;
            dietButtonsRow.getChildren().add(btn);
        }

        dietSection.getChildren().addAll(dietLabel, dietButtonsRow);

        section.getChildren().addAll(mealsHeader, mealsGrid, dietSection);

        return section;
    }

    private void selectFirstMeal(int index) {
        selectedFirstMeal = (String) firstMealButtons[index].getUserData();
        for (int i = 0; i < firstMealButtons.length; i++) {
            boolean isSelected = i == index;
            firstMealButtons[i].setSelected(isSelected);
            styleMealButton(firstMealButtons[i], isSelected);
        }
        updateSummary();
    }

    private void selectLastMeal(int index) {
        selectedLastMeal = (String) lastMealButtons[index].getUserData();
        for (int i = 0; i < lastMealButtons.length; i++) {
            boolean isSelected = i == index;
            lastMealButtons[i].setSelected(isSelected);
            styleMealButton(lastMealButtons[i], isSelected);
        }
        updateSummary();
    }

    private void toggleDiet(int index) {
        String dietId = (String) dietButtons[index].getUserData();

        if (dietId.equals("standard")) {
            // Standard clears other diets
            selectedDiets.clear();
            selectedDiets.add("standard");
        } else {
            // Remove standard and toggle the selected diet
            selectedDiets.remove("standard");
            if (selectedDiets.contains(dietId)) {
                selectedDiets.remove(dietId);
            } else {
                selectedDiets.add(dietId);
            }
            // If no diets selected, default to standard
            if (selectedDiets.isEmpty()) {
                selectedDiets.add("standard");
            }
        }

        // Update button states
        for (int i = 0; i < dietButtons.length; i++) {
            String id = (String) dietButtons[i].getUserData();
            boolean isSelected = selectedDiets.contains(id);
            dietButtons[i].setSelected(isSelected);
            styleDietButton(dietButtons[i], isSelected);
        }

        updateSummary();
    }

    private void styleMealButton(ToggleButton btn, boolean selected) {
        if (selected) {
            btn.setStyle("-fx-background-color: #cfe2ff; -fx-text-fill: #0d6efd; -fx-border-color: #0d6efd; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 11px;");
        } else {
            btn.setStyle("-fx-background-color: white; -fx-text-fill: #6c757d; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 11px;");
        }
    }

    private void styleDietButton(ToggleButton btn, boolean selected) {
        if (selected) {
            btn.setStyle("-fx-background-color: #cfe2ff; -fx-text-fill: #0d6efd; -fx-border-color: #0d6efd; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 11px;");
        } else {
            btn.setStyle("-fx-background-color: white; -fx-text-fill: #6c757d; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 11px;");
        }
    }

    private VBox buildDepositSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);
        section.setPadding(new Insets(12));
        section.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Checkbox for deposit received
        HBox checkboxRow = new HBox(ReceptionStyles.SPACING_SM);
        checkboxRow.setAlignment(Pos.CENTER_LEFT);

        depositCheckbox = new CheckBox("Deposit received");
        depositCheckbox.setStyle("-fx-font-size: 11px;");

        checkboxRow.getChildren().add(depositCheckbox);

        // Deposit amount field (shown when checkbox is checked)
        depositAmountContainer = new VBox(ReceptionStyles.SPACING_XS);
        depositAmountContainer.setPadding(new Insets(8, 0, 0, 0));
        depositAmountContainer.setVisible(false);
        depositAmountContainer.setManaged(false);

        depositAmountField = new TextField();
        depositAmountField.setPromptText("Suggested: €0");
        depositAmountField.setStyle("-fx-font-size: 12px;");
        depositAmountField.textProperty().addListener((obs, old, val) -> updateSummary());

        depositAmountContainer.getChildren().add(depositAmountField);

        depositCheckbox.setOnAction(e -> {
            boolean checked = depositCheckbox.isSelected();
            depositAmountContainer.setVisible(checked);
            depositAmountContainer.setManaged(checked);
            if (checked) {
                // Set suggested deposit (30% of total)
                int total = calculateTotal();
                int suggested = (int) Math.round(total * 0.3);
                depositAmountField.setText(String.valueOf(suggested));
                depositAmountField.setPromptText("Suggested: €" + suggested);
            }
            updateSummary();
        });

        section.getChildren().addAll(checkboxRow, depositAmountContainer);

        return section;
    }

    private VBox buildNotesSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_XS);

        Label notesLabel = new Label("Notes");
        notesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        notesArea = new TextArea();
        notesArea.setPromptText("Special requests...");
        notesArea.setPrefRowCount(2);
        notesArea.setWrapText(true);
        notesArea.setStyle("-fx-font-size: 12px;");

        section.getChildren().addAll(notesLabel, notesArea);

        return section;
    }

    private VBox buildSummarySection() {
        VBox section = new VBox(ReceptionStyles.SPACING_XS);
        section.setPadding(new Insets(12));
        section.setStyle("-fx-background-color: #cfe2ff; -fx-border-color: #0d6efd; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Nights × price = total
        HBox nightsRow = new HBox();
        nightsRow.setAlignment(Pos.CENTER_LEFT);
        nightsLabel = new Label("0 nights × €0");
        nightsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        totalLabel = new Label("€0");
        totalLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 500;");
        HBox.setHgrow(nightsLabel, Priority.ALWAYS);
        nightsRow.getChildren().addAll(nightsLabel, totalLabel);

        // Meals row
        HBox mealsRow = new HBox();
        mealsRow.setAlignment(Pos.CENTER_LEFT);
        Label mealsLabelStatic = new Label("Meals");
        mealsLabelStatic.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        mealsLabel = new Label("");
        mealsLabel.setStyle("-fx-font-size: 11px;");
        HBox.setHgrow(mealsLabelStatic, Priority.ALWAYS);
        mealsRow.getChildren().addAll(mealsLabelStatic, mealsLabel);

        // Diet row (hidden when standard)
        dietRow = new VBox();
        HBox dietHBox = new HBox();
        dietHBox.setAlignment(Pos.CENTER_LEFT);
        Label dietLabelStatic = new Label("Diet");
        dietLabelStatic.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        dietLabel = new Label("");
        dietLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #0d6efd;");
        HBox.setHgrow(dietLabelStatic, Priority.ALWAYS);
        dietHBox.getChildren().addAll(dietLabelStatic, dietLabel);
        dietRow.getChildren().add(dietHBox);
        dietRow.setVisible(false);
        dietRow.setManaged(false);

        // Deposit row (hidden when no deposit)
        depositRow = new VBox();
        HBox depositHBox = new HBox();
        depositHBox.setAlignment(Pos.CENTER_LEFT);
        Label depositLabelStatic = new Label("Deposit");
        depositLabelStatic.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        depositLabel = new Label("");
        depositLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #198754;");
        HBox.setHgrow(depositLabelStatic, Priority.ALWAYS);
        depositHBox.getChildren().addAll(depositLabelStatic, depositLabel);
        depositRow.getChildren().add(depositHBox);
        depositRow.setVisible(false);
        depositRow.setManaged(false);

        // Balance due row
        HBox balanceRow = new HBox();
        balanceRow.setAlignment(Pos.CENTER_LEFT);
        balanceRow.setPadding(new Insets(8, 0, 0, 0));
        balanceRow.setStyle("-fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");
        Label balanceLabelStatic = new Label("Balance due on arrival");
        balanceLabelStatic.setStyle("-fx-font-size: 12px; -fx-font-weight: 500;");
        balanceLabel = new Label("€0");
        balanceLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #0d6efd;");
        HBox.setHgrow(balanceLabelStatic, Priority.ALWAYS);
        balanceRow.getChildren().addAll(balanceLabelStatic, balanceLabel);

        section.getChildren().addAll(nightsRow, mealsRow, dietRow, depositRow, balanceRow);

        return section;
    }

    private int calculateNights() {
        LocalDate arrival = arrivalDatePicker.getValue();
        LocalDate departure = departureDatePicker.getValue();
        if (arrival != null && departure != null && !departure.isBefore(arrival)) {
            return (int) ChronoUnit.DAYS.between(arrival, departure);
        }
        return 0;
    }

    private int getRoomPrice() {
        for (RoomTypeOption rt : roomTypes) {
            if (rt.id.equals(selectedRoomType)) {
                return rt.price;
            }
        }
        return 0;
    }

    private int calculateTotal() {
        return calculateNights() * getRoomPrice();
    }

    private void updateSummary() {
        if (summaryBox == null) return;

        int nights = calculateNights();
        int price = getRoomPrice();
        int total = nights * price;

        // Update nights row
        nightsLabel.setText(nights + " nights × €" + price);
        totalLabel.setText("€" + total);

        // Update meals row
        String firstMealName = "";
        String lastMealName = "";
        for (String[] meal : MEALS) {
            if (meal[0].equals(selectedFirstMeal)) firstMealName = meal[1];
            if (meal[0].equals(selectedLastMeal)) lastMealName = meal[1];
        }
        mealsLabel.setText(firstMealName + " → " + lastMealName);

        // Update diet row
        boolean hasNonStandardDiet = selectedDiets.stream().anyMatch(d -> !d.equals("standard"));
        if (hasNonStandardDiet) {
            StringBuilder dietText = new StringBuilder();
            for (String dietId : selectedDiets) {
                if (!dietId.equals("standard")) {
                    for (String[] diet : DIETS) {
                        if (diet[0].equals(dietId)) {
                            if (dietText.length() > 0) dietText.append(", ");
                            dietText.append(diet[1]);
                        }
                    }
                }
            }
            dietLabel.setText(dietText.toString());
            dietRow.setVisible(true);
            dietRow.setManaged(true);
        } else {
            dietRow.setVisible(false);
            dietRow.setManaged(false);
        }

        // Update deposit row
        int depositAmount = 0;
        if (depositCheckbox.isSelected()) {
            try {
                depositAmount = Integer.parseInt(depositAmountField.getText().trim());
            } catch (NumberFormatException e) {
                depositAmount = 0;
            }
            depositLabel.setText("- €" + depositAmount);
            depositRow.setVisible(true);
            depositRow.setManaged(true);
        } else {
            depositRow.setVisible(false);
            depositRow.setManaged(false);
        }

        // Update balance row
        int balance = total - depositAmount;
        balanceLabel.setText("€" + balance);

        // Update deposit field placeholder
        int suggested = (int) Math.round(total * 0.3);
        depositAmountField.setPromptText("Suggested: €" + suggested);
    }

    private void validateForm() {
        boolean hasName = nameField.getText() != null && !nameField.getText().trim().isEmpty();
        boolean hasPhone = phoneField.getText() != null && !phoneField.getText().trim().isEmpty();
        boolean hasArrival = arrivalDatePicker.getValue() != null;
        boolean hasDeparture = departureDatePicker.getValue() != null;
        boolean validDates = hasArrival && hasDeparture &&
                !((LocalDate) departureDatePicker.getValue()).isBefore((LocalDate) arrivalDatePicker.getValue());

        canProceed.set(hasName && hasPhone && validDates);
    }

    @Override
    public BooleanProperty canProceedProperty() {
        return canProceed;
    }

    @Override
    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    @Override
    public void performAction(DialogCallback dialogCallback) {
        UpdateStore updateStore = UpdateStore.create(dataSourceModel);

        try {
            // Parse name into first/last
            String fullName = nameField.getText().trim();
            String[] nameParts = fullName.split("\\s+", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            // 1. Create Person
            Person person = updateStore.insertEntity(Person.class);
            person.setFirstName(firstName);
            person.setLastName(lastName);
            if (emailField.getText() != null && !emailField.getText().trim().isEmpty()) {
                person.setEmail(emailField.getText().trim());
            }
            if (phoneField.getText() != null && !phoneField.getText().trim().isEmpty()) {
                person.setPhone(phoneField.getText().trim());
            }
            person.setOrganization(organizationId);

            // 2. Create Document
            Document document = updateStore.insertEntity(Document.class);
            document.setPerson(person);

            // Set event if selected
            if (selectedEvent != null) {
                document.setEvent(selectedEvent);
            }

            document.setConfirmed(true);

            // Calculate and set prices
            int total = calculateTotal();
            document.setPriceNet(total);

            // Set deposit
            int deposit = 0;
            if (depositCheckbox.isSelected()) {
                try {
                    deposit = Integer.parseInt(depositAmountField.getText().trim());
                } catch (NumberFormatException e) {
                    deposit = 0;
                }
            }
            document.setPriceDeposit(deposit);

            // Set notes as request
            if (notesArea.getText() != null && !notesArea.getText().trim().isEmpty()) {
                document.setRequest(notesArea.getText().trim());
            }

            // 3. Create DocumentLine for accommodation
            DocumentLine documentLine = updateStore.insertEntity(DocumentLine.class);
            documentLine.setDocument(document);
            documentLine.setStartDate(arrivalDatePicker.getValue());
            documentLine.setEndDate(departureDatePicker.getValue());

            // Submit changes
            updateStore.submitChanges()
                    .onFailure(error -> {
                        Console.log("Error creating booking: " + error.getMessage());
                        dialogCallback.closeDialog();
                    })
                    .onSuccess(result -> {
                        Console.log("Booking created successfully for " + fullName);
                        if (onSuccessCallback != null) {
                            onSuccessCallback.run();
                        }
                        dialogCallback.closeDialog();
                    });

        } catch (Exception e) {
            Console.log("Error creating booking: " + e.getMessage());
            dialogCallback.closeDialog();
        }
    }

    @Override
    public String getPrimaryButtonText() {
        return "Create booking";
    }

    // Helper classes

    private static class RoomTypeOption {
        final String id;
        final String name;
        final int price;
        final int available;

        RoomTypeOption(String id, String name, int price, int available) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.available = available;
        }
    }
}
