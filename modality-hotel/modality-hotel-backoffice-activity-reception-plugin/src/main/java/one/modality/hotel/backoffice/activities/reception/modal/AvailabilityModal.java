package one.modality.hotel.backoffice.activities.reception.modal;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.hotel.backoffice.activities.reception.i18n.ReceptionI18nKeys;
import one.modality.hotel.backoffice.activities.reception.util.ReceptionStyles;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Modal dialog for checking room availability.
 * Supports two modes:
 * - By Event: Shows availability for a selected event's date range
 * - By Dates: Shows availability for custom date range
 *
 * @author David Hello
 * @author Claude Code
 */
public class AvailabilityModal implements ReceptionDialogManager.ManagedDialog {

    private final DataSourceModel dataSourceModel;
    private final Object organizationId;

    private final BooleanProperty canProceed = new SimpleBooleanProperty(true);
    private Runnable onSuccessCallback;
    private BiConsumer<LocalDate[], ResourceConfiguration> onBookRequested;

    // Mode selection
    private ToggleButton byEventButton;
    private ToggleButton byDatesButton;

    // By Event UI components
    private VBox eventSelectorBox;
    private VBox eventListContainer;
    private ScrollPane eventScrollPane;
    private Event selectedEvent;
    private HBox selectedEventRow;

    // By Dates UI components
    private VBox dateSelectorBox;
    private DatePicker checkInPicker;
    private DatePicker checkOutPicker;

    // Results
    private VBox resultsContainer;
    private Label statusLabel;

    // Data
    private final ObservableList<Event> events = FXCollections.observableArrayList();
    private final ObservableList<ResourceConfiguration> roomTypes = FXCollections.observableArrayList();
    private final Map<Object, Integer> totalRoomsByType = new HashMap<>();
    private final Map<Object, Integer> occupiedRoomsByType = new HashMap<>();

    public AvailabilityModal(DataSourceModel dataSourceModel, Object organizationId) {
        this.dataSourceModel = dataSourceModel;
        this.organizationId = organizationId;
    }

    public AvailabilityModal onBookRequested(BiConsumer<LocalDate[], ResourceConfiguration> callback) {
        this.onBookRequested = callback;
        return this;
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

        // Mode toggle (By Event / By Dates)
        HBox modeToggle = buildModeToggle();

        // Event selector (shown in By Event mode)
        eventSelectorBox = buildEventSelector();

        // Date selector (shown in By Dates mode)
        dateSelectorBox = buildDateSelector();
        dateSelectorBox.setVisible(false);
        dateSelectorBox.setManaged(false);

        // Check button
        Button checkButton = new Button("Check Availability");
        Bootstrap.primaryButton(checkButton);
        checkButton.setOnAction(e -> checkAvailability());

        // Results container
        resultsContainer = new VBox(ReceptionStyles.SPACING_SM);
        resultsContainer.setPadding(new Insets(16, 0, 0, 0));

        // Status label
        statusLabel = new Label("Select an event or date range and click 'Check Availability'");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        resultsContainer.getChildren().add(statusLabel);

        container.getChildren().addAll(header, modeToggle, eventSelectorBox, dateSelectorBox, checkButton, resultsContainer);

        // Load events
        loadEvents();

        return container;
    }

    private VBox buildHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(0, 0, 16, 0));

        Label icon = new Label("\uD83D\uDCC5"); // Calendar icon
        icon.setStyle("-fx-font-size: 32px;");

        Label title = I18nControls.newLabel(ReceptionI18nKeys.CheckAvailability);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");

        Label subtitle = new Label("Check room availability by event or dates");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        header.getChildren().addAll(icon, title, subtitle);

        return header;
    }

    private HBox buildModeToggle() {
        HBox toggleBox = new HBox(ReceptionStyles.SPACING_SM);
        toggleBox.setAlignment(Pos.CENTER_LEFT);
        toggleBox.setPadding(new Insets(0, 0, 8, 0));

        Label modeLabel = new Label("Search by:");
        modeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        byEventButton = new ToggleButton("By Event");
        byEventButton.setSelected(true);
        byEventButton.setPadding(new Insets(8, 16, 8, 16));
        byEventButton.setOnAction(e -> {
            byEventButton.setSelected(true);
            byDatesButton.setSelected(false);
            updateModeStyles();
            showEventMode();
        });

        byDatesButton = new ToggleButton("By Dates");
        byDatesButton.setPadding(new Insets(8, 16, 8, 16));
        byDatesButton.setOnAction(e -> {
            byDatesButton.setSelected(true);
            byEventButton.setSelected(false);
            updateModeStyles();
            showDatesMode();
        });

        updateModeStyles();

        toggleBox.getChildren().addAll(modeLabel, byEventButton, byDatesButton);

        return toggleBox;
    }

    private void updateModeStyles() {
        if (byEventButton.isSelected()) {
            byEventButton.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: 500;");
            byDatesButton.setStyle("-fx-background-color: white; -fx-text-fill: #6c757d; -fx-background-radius: 6; -fx-border-color: #dee2e6; -fx-border-radius: 6;");
        } else {
            byDatesButton.setStyle("-fx-background-color: #0d6efd; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: 500;");
            byEventButton.setStyle("-fx-background-color: white; -fx-text-fill: #6c757d; -fx-background-radius: 6; -fx-border-color: #dee2e6; -fx-border-radius: 6;");
        }
    }

    private void showEventMode() {
        eventSelectorBox.setVisible(true);
        eventSelectorBox.setManaged(true);
        dateSelectorBox.setVisible(false);
        dateSelectorBox.setManaged(false);
    }

    private void showDatesMode() {
        eventSelectorBox.setVisible(false);
        eventSelectorBox.setManaged(false);
        dateSelectorBox.setVisible(true);
        dateSelectorBox.setManaged(true);
    }

    private VBox buildEventSelector() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);

        Label label = new Label("Select event:");
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        // Use VBox in ScrollPane instead of ListView (GWT-compatible)
        eventListContainer = new VBox(4);
        eventListContainer.setPadding(new Insets(4));

        eventScrollPane = new ScrollPane(eventListContainer);
        eventScrollPane.setFitToWidth(true);
        eventScrollPane.setPrefHeight(180);
        eventScrollPane.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-background-radius: 8;");

        section.getChildren().addAll(label, eventScrollPane);

        return section;
    }

    private void updateEventList() {
        eventListContainer.getChildren().clear();

        if (events.isEmpty()) {
            Label placeholder = new Label("No events found");
            placeholder.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-padding: 20;");
            eventListContainer.getChildren().add(placeholder);
            return;
        }

        for (Event event : events) {
            HBox row = createEventRow(event);
            eventListContainer.getChildren().add(row);
        }
    }

    private HBox createEventRow(Event event) {
        HBox row = new HBox(ReceptionStyles.SPACING_MD);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 4; -fx-cursor: hand;");

        String name = event.getName() != null ? event.getName() : "Event";
        LocalDate startDate = event.getStartDate();
        LocalDate endDate = event.getEndDate();
        String dateRange = "";
        if (startDate != null && endDate != null) {
            dateRange = " (" + startDate + " - " + endDate + ")";
        }

        Label nameLabel = new Label(name + dateRange);
        nameLabel.setStyle("-fx-font-size: 13px;");

        row.getChildren().add(nameLabel);

        // Click to select
        row.setOnMouseClicked(e -> {
            // Deselect previous
            if (selectedEventRow != null) {
                selectedEventRow.setStyle("-fx-background-color: white; -fx-background-radius: 4; -fx-cursor: hand;");
            }
            // Select this row
            selectedEventRow = row;
            selectedEvent = event;
            row.setStyle("-fx-background-color: #cfe2ff; -fx-background-radius: 4; -fx-cursor: hand;");
        });

        // Hover effect
        row.setOnMouseEntered(e -> {
            if (row != selectedEventRow) {
                row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 4; -fx-cursor: hand;");
            }
        });
        row.setOnMouseExited(e -> {
            if (row != selectedEventRow) {
                row.setStyle("-fx-background-color: white; -fx-background-radius: 4; -fx-cursor: hand;");
            }
        });

        return row;
    }

    private VBox buildDateSelector() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);

        HBox dateRow = new HBox(ReceptionStyles.SPACING_MD);
        dateRow.setAlignment(Pos.CENTER_LEFT);

        // Check-in date
        VBox checkInBox = new VBox(4);
        Label checkInLabel = I18nControls.newLabel(ReceptionI18nKeys.CheckIn);
        checkInLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        checkInPicker = new DatePicker();
        checkInPicker.setValue(LocalDate.now());
        checkInPicker.setPrefWidth(180);
        checkInBox.getChildren().addAll(checkInLabel, checkInPicker);

        // Check-out date
        VBox checkOutBox = new VBox(4);
        Label checkOutLabel = I18nControls.newLabel(ReceptionI18nKeys.CheckOut);
        checkOutLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        checkOutPicker = new DatePicker();
        checkOutPicker.setValue(LocalDate.now().plusDays(1));
        checkOutPicker.setPrefWidth(180);
        checkOutBox.getChildren().addAll(checkOutLabel, checkOutPicker);

        dateRow.getChildren().addAll(checkInBox, checkOutBox);

        section.getChildren().add(dateRow);

        return section;
    }

    private void loadEvents() {
        EntityStore entityStore = EntityStore.create(dataSourceModel);
        entityStore.executeQuery(
                        "select id,name,startDate,endDate from Event " +
                                "where organization=? and startDate>=? order by startDate",
                        organizationId, LocalDate.now().minusMonths(1))
                .onSuccess(list -> {
                    events.clear();
                    for (Object obj : list) {
                        if (obj instanceof Event) {
                            events.add((Event) obj);
                        }
                    }
                    updateEventList();
                })
                .onFailure(e -> Console.log("Error loading events: " + e.getMessage()));
    }

    private void checkAvailability() {
        LocalDate checkIn;
        LocalDate checkOut;

        if (byEventButton.isSelected()) {
            // By Event mode
            if (selectedEvent == null) {
                statusLabel.setText("Please select an event");
                statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc3545;");
                return;
            }
            checkIn = selectedEvent.getStartDate();
            checkOut = selectedEvent.getEndDate();
            if (checkIn == null || checkOut == null) {
                statusLabel.setText("Selected event has no date range");
                statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc3545;");
                return;
            }
        } else {
            // By Dates mode
            checkIn = checkInPicker.getValue();
            checkOut = checkOutPicker.getValue();

            if (checkIn == null || checkOut == null) {
                statusLabel.setText("Please select both check-in and check-out dates");
                statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc3545;");
                return;
            }

            if (checkOut.isBefore(checkIn) || checkOut.equals(checkIn)) {
                statusLabel.setText("Check-out date must be after check-in date");
                statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc3545;");
                return;
            }
        }

        statusLabel.setText("Checking availability...");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        // Clear previous results
        resultsContainer.getChildren().clear();
        resultsContainer.getChildren().add(statusLabel);
        roomTypes.clear();
        totalRoomsByType.clear();
        occupiedRoomsByType.clear();

        // Load room types and their availability
        loadRoomTypes(checkIn, checkOut);
    }

    private void loadRoomTypes(LocalDate checkIn, LocalDate checkOut) {
        EntityStore entityStore = EntityStore.create(dataSourceModel);

        entityStore.executeQuery(
                        "select id,name,max,item.(id,name) from ResourceConfiguration " +
                                "where item.family.code='acco' and item.organization=? order by name",
                        organizationId)
                .onSuccess(rcList -> {
                    for (Object obj : rcList) {
                        if (obj instanceof ResourceConfiguration) {
                            ResourceConfiguration rc = (ResourceConfiguration) obj;
                            roomTypes.add(rc);
                            Integer max = rc.getMax();
                            totalRoomsByType.put(rc.getId(), max != null && max > 0 ? max : 1);
                            occupiedRoomsByType.put(rc.getId(), 0);
                        }
                    }

                    loadOccupiedRooms(entityStore, checkIn, checkOut);
                })
                .onFailure(e -> {
                    statusLabel.setText("Error loading room types: " + e.getMessage());
                    statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc3545;");
                });
    }

    private void loadOccupiedRooms(EntityStore entityStore, LocalDate checkIn, LocalDate checkOut) {
        entityStore.executeQuery(
                        "select id,resourceConfiguration from DocumentLine " +
                                "where resourceConfiguration.item.family.code='acco' " +
                                "and resourceConfiguration.item.organization=? " +
                                "and !cancelled " +
                                "and startDate<? and endDate>?",
                        organizationId, checkOut, checkIn)
                .onSuccess(dlList -> {
                    for (Object obj : dlList) {
                        if (obj instanceof DocumentLine) {
                            DocumentLine dl = (DocumentLine) obj;
                            if (dl.getResourceConfiguration() != null) {
                                Object rcId = dl.getResourceConfiguration().getId();
                                occupiedRoomsByType.merge(rcId, 1, Integer::sum);
                            }
                        }
                    }

                    displayResults(checkIn, checkOut);
                })
                .onFailure(e -> {
                    Console.log("Error loading occupied rooms: " + e.getMessage());
                    displayResults(checkIn, checkOut);
                });
    }

    private void displayResults(LocalDate checkIn, LocalDate checkOut) {
        resultsContainer.getChildren().clear();

        if (roomTypes.isEmpty()) {
            statusLabel.setText("No room types found for this organization");
            statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");
            resultsContainer.getChildren().add(statusLabel);
            return;
        }

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        String modeText = byEventButton.isSelected() ? "event dates" : "selected dates";
        Label summaryLabel = new Label("Availability for " + nights + " night" + (nights > 1 ? "s" : "") +
                " (" + checkIn + " to " + checkOut + ") — " + modeText);
        summaryLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500;");
        resultsContainer.getChildren().add(summaryLabel);

        // Display each room type
        int totalAvailable = 0;
        for (ResourceConfiguration rc : roomTypes) {
            HBox row = createRoomTypeRow(rc, checkIn, checkOut);
            resultsContainer.getChildren().add(row);

            int total = totalRoomsByType.getOrDefault(rc.getId(), 0);
            int occupied = occupiedRoomsByType.getOrDefault(rc.getId(), 0);
            totalAvailable += Math.max(0, total - occupied);
        }

        // Summary
        Label totalLabel = new Label(totalAvailable + " total rooms available");
        totalLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: " +
                (totalAvailable > 0 ? "#198754" : "#dc3545") + ";");
        totalLabel.setPadding(new Insets(8, 0, 0, 0));
        resultsContainer.getChildren().add(totalLabel);
    }

    private HBox createRoomTypeRow(ResourceConfiguration rc, LocalDate checkIn, LocalDate checkOut) {
        HBox row = new HBox(ReceptionStyles.SPACING_MD);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        // Room type name
        Label nameLabel = new Label(rc.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500;");
        nameLabel.setMinWidth(150);

        // Availability
        int total = totalRoomsByType.getOrDefault(rc.getId(), 0);
        int occupied = occupiedRoomsByType.getOrDefault(rc.getId(), 0);
        int available = Math.max(0, total - occupied);

        Label availLabel = new Label(available + " / " + total + " available");
        if (available > 0) {
            availLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #198754;");
        } else {
            availLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc3545;");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Book button (only if available)
        if (available > 0 && onBookRequested != null) {
            Button bookBtn = new Button("Book");
            Bootstrap.successButton(bookBtn);
            bookBtn.setPadding(new Insets(4, 12, 4, 12));
            bookBtn.setOnAction(e -> {
                LocalDate[] dates = new LocalDate[]{checkIn, checkOut};
                onBookRequested.accept(dates, rc);
            });
            row.getChildren().addAll(nameLabel, availLabel, spacer, bookBtn);
        } else {
            row.getChildren().addAll(nameLabel, availLabel, spacer);
        }

        return row;
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
        dialogCallback.closeDialog();
    }

    @Override
    public String getPrimaryButtonText() {
        return "Close";
    }

    @Override
    public String getCancelButtonText() {
        return "";
    }
}
