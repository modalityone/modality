package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.ScheduledResource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Modal dialog for room/bed allocation.
 * <p>
 * Features:
 * - Shows Gantt chart of room/bed availability
 * - Displays existing bookings as occupancy bars
 * - Allows selecting a room/bed for the current booking
 * - Saves allocation changes
 * <p>
 * Based on RegistrationDashboardFull.jsx RoomAllocationModal (lines 2658-3350).
 *
 * @author Claude Code
 */
public class RoomAllocationModal {

    private final ViewDomainActivityBase activity;
    private final Document document;
    private final DocumentLine accommodationLine;
    private final UpdateStore updateStore;

    private RoomAllocationCanvas canvas;
    private DialogCallback dialogCallback;
    private VBox contentBox;
    private ProgressIndicator loadingIndicator;
    private Label errorLabel;

    // Booking dates
    private LocalDate arrivalDate;
    private LocalDate departureDate;

    // Current allocation
    private RoomAllocationCanvas.RoomAllocation currentAllocation;

    public RoomAllocationModal(ViewDomainActivityBase activity, Document document, DocumentLine accommodationLine, UpdateStore updateStore) {
        this.activity = activity;
        this.document = document;
        this.accommodationLine = accommodationLine;
        this.updateStore = updateStore;
    }

    /**
     * Shows the room allocation modal.
     */
    public void show() {
        // Create dialog content
        VBox dialogPane = createDialogContent();
        dialogPane.setPrefWidth(900);
        dialogPane.setPrefHeight(600);
        dialogPane.setMaxWidth(1000);
        dialogPane.setMaxHeight(700);

        // Show modal
        dialogCallback = DialogUtil.showModalNodeInGoldLayout(
            dialogPane,
            FXMainFrameDialogArea.getDialogArea()
        );

        // Load data
        loadRoomData();
    }

    /**
     * Creates the dialog content.
     */
    private VBox createDialogContent() {
        VBox container = new VBox(0);
        container.setBackground(createBackground(BG_CARD, BORDER_RADIUS_LARGE));

        // Header
        Node header = createHeader();

        // Content area (will contain canvas or loading state)
        contentBox = new VBox();
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(PADDING_LARGE);
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        // Show loading initially
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(48, 48);
        contentBox.getChildren().add(loadingIndicator);

        // Error label (hidden initially)
        errorLabel = new Label();
        errorLabel.setTextFill(DANGER);
        errorLabel.setVisible(false);

        // Footer
        Node footer = createFooter();

        container.getChildren().addAll(header, contentBox, footer);
        return container;
    }

    /**
     * Creates the header section.
     */
    private Node createHeader() {
        HBox header = new HBox(12);
        header.setPadding(PADDING_LARGE);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setBackground(new Background(new BackgroundFill(
            BG, new CornerRadii(BORDER_RADIUS_LARGE, BORDER_RADIUS_LARGE, 0, 0, false), null
        )));
        header.setBorder(new Border(new BorderStroke(
            BORDER, BorderStrokeStyle.SOLID,
            new CornerRadii(BORDER_RADIUS_LARGE, BORDER_RADIUS_LARGE, 0, 0, false),
            new BorderWidths(0, 0, 1, 0)
        )));

        // Icon
        Label iconLabel = new Label("🛏️");
        iconLabel.setFont(FONT_TITLE);

        // Title and subtitle
        VBox titleBox = new VBox(2);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label titleLabel = new Label("Room Allocation");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setTextFill(TEXT);

        // Get accommodation type name
        String accommodationType = "Accommodation";
        // TODO: Get from accommodationLine.getItem().getName()

        Label subtitleLabel = new Label("Select a room/bed for " + accommodationType);
        subtitleLabel.setFont(FONT_BODY);
        subtitleLabel.setTextFill(TEXT_MUTED);

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);

        // Close button
        Button closeButton = new Button("×");
        closeButton.setFont(FONT_TITLE);
        closeButton.setTextFill(TEXT_MUTED);
        closeButton.setBackground(Background.EMPTY);
        closeButton.setBorder(Border.EMPTY);
        closeButton.setCursor(javafx.scene.Cursor.HAND);
        closeButton.setOnAction(e -> close());

        header.getChildren().addAll(iconLabel, titleBox, closeButton);
        return header;
    }

    /**
     * Creates the footer section with buttons.
     */
    private Node createFooter() {
        HBox footer = new HBox(12);
        footer.setPadding(PADDING_LARGE);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setBackground(new Background(new BackgroundFill(
            BG, new CornerRadii(0, 0, BORDER_RADIUS_LARGE, BORDER_RADIUS_LARGE, false), null
        )));
        footer.setBorder(new Border(new BorderStroke(
            BORDER, BorderStrokeStyle.SOLID,
            new CornerRadii(0, 0, BORDER_RADIUS_LARGE, BORDER_RADIUS_LARGE, false),
            new BorderWidths(1, 0, 0, 0)
        )));

        // Info label
        Label infoLabel = new Label("Click on an available bed to select it");
        infoLabel.setFont(FONT_SMALL);
        infoLabel.setTextFill(TEXT_MUTED);
        HBox.setHgrow(infoLabel, Priority.ALWAYS);

        // Cancel button
        Button cancelButton = new Button("Cancel");
        applySecondaryButtonStyle(cancelButton);
        cancelButton.setOnAction(e -> close());

        // Save button
        Button saveButton = new Button("Save Allocation");
        applyPrimaryButtonStyle(saveButton);
        saveButton.setOnAction(e -> saveAllocation());

        // Disable save if no selection change
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (canvas == null) {
                saveButton.setDisable(true);
                return;
            }
            RoomAllocationCanvas.RoomAllocation selected = canvas.getSelectedAllocation();
            boolean hasSelection = selected != null;
            boolean isDifferent = currentAllocation == null || !selected.equals(currentAllocation);
            saveButton.setDisable(!hasSelection || !isDifferent);
        }, canvas != null ? canvas.selectedAllocationProperty() : null);

        footer.getChildren().addAll(infoLabel, cancelButton, saveButton);
        return footer;
    }

    /**
     * Loads room and occupancy data from the database.
     */
    private void loadRoomData() {
        // Get booking dates from document lines
        // TODO: Get actual dates from document/attendance
        arrivalDate = LocalDate.now();
        departureDate = LocalDate.now().plusDays(3);

        // Get current allocation
        // TODO: Get from attendance record
        currentAllocation = null;

        // Load rooms and occupancies
        EntityStore entityStore = EntityStore.create(activity.getDataSourceModel());

        // Query for rooms/beds that match the accommodation type
        // TODO: Filter by item/site from accommodationLine
        entityStore.<ResourceConfiguration>executeQuery(
            "{class: 'ResourceConfiguration', alias: 'rc', " +
            "columns: ['name', 'resource.name', 'resource.site.name', 'max'], " +
            "where: 'resource.site.organization=?', " +
            "orderBy: 'resource.name,name'}",
            document.getOrganization() != null ? document.getOrganization().getPrimaryKey() : 1
        ).onFailure(error -> {
            showError("Failed to load rooms: " + error.getMessage());
        }).onSuccess(rooms -> {
            // Query for existing occupancies during the period
            LocalDate queryStart = arrivalDate.minusDays(5);
            LocalDate queryEnd = departureDate.plusDays(5);

            entityStore.<Attendance>executeQuery(
                "{class: 'Attendance', alias: 'a', " +
                "columns: ['date', 'resourceConfiguration.id', 'documentLine.document.person_firstName', 'documentLine.document.person_lastName', 'documentLine.document.id'], " +
                "where: 'date>=? and date<? and resourceConfiguration!=null', " +
                "orderBy: 'resourceConfiguration.id,date'}",
                queryStart, queryEnd
            ).onFailure(error -> {
                showError("Failed to load occupancies: " + error.getMessage());
            }).onSuccess(attendances -> {
                buildCanvasWithData(rooms, attendances);
            });
        });
    }

    /**
     * Builds the canvas with loaded data.
     */
    private void buildCanvasWithData(List<ResourceConfiguration> rooms, List<Attendance> attendances) {
        // Group attendances by resource configuration to build occupancy data
        Map<Object, List<Attendance>> attendancesByRoom = new HashMap<>();
        for (Attendance att : attendances) {
            // Attendance -> ScheduledResource -> ResourceConfiguration
            ScheduledResource sr = att.getScheduledResource();
            ResourceConfiguration rc = sr != null ? sr.getResourceConfiguration() : null;
            if (rc != null) {
                attendancesByRoom.computeIfAbsent(rc.getPrimaryKey(), k -> new ArrayList<>()).add(att);
            }
        }

        // Build room rows
        List<RoomAllocationCanvas.RoomRow> rows = new ArrayList<>();

        for (ResourceConfiguration room : rooms) {
            String roomId = room.getPrimaryKey() != null ? room.getPrimaryKey().toString() : "0";
            String roomName = room.getName();
            if (roomName == null && room.getResource() != null) {
                roomName = room.getResource().getName();
            }
            if (roomName == null) {
                roomName = "Room " + roomId;
            }

            Integer maxCapacity = room.getMax();
            int capacity = maxCapacity != null ? maxCapacity : 1;

            if (capacity > 1) {
                // Multi-bed room - add header row then bed rows
                RoomAllocationCanvas.RoomRow headerRow = new RoomAllocationCanvas.RoomRow(
                    roomId, roomName, -1, null, true
                );
                rows.add(headerRow);

                for (int bed = 0; bed < capacity; bed++) {
                    RoomAllocationCanvas.RoomRow bedRow = new RoomAllocationCanvas.RoomRow(
                        roomId, roomName, bed, "Bed " + (bed + 1), false
                    );
                    addOccupancies(bedRow, attendancesByRoom.get(room.getPrimaryKey()), bed);
                    rows.add(bedRow);
                }
            } else {
                // Single bed room
                RoomAllocationCanvas.RoomRow row = new RoomAllocationCanvas.RoomRow(
                    roomId, roomName, 0, null, false
                );
                addOccupancies(row, attendancesByRoom.get(room.getPrimaryKey()), 0);
                rows.add(row);
            }
        }

        // Initialize and display canvas
        canvas = new RoomAllocationCanvas();
        canvas.initialize(arrivalDate, departureDate, currentAllocation);
        canvas.setRows(rows);

        // Update content box
        contentBox.getChildren().clear();
        contentBox.getChildren().add(canvas.buildUi());
    }

    /**
     * Adds occupancies to a room row from attendance records.
     */
    private void addOccupancies(RoomAllocationCanvas.RoomRow row, List<Attendance> attendances, int bedIndex) {
        if (attendances == null || attendances.isEmpty()) return;

        // Group consecutive dates by document to form occupancy bars
        // For simplicity, we'll create one occupancy per unique document
        Map<Object, List<Attendance>> byDocument = new HashMap<>();
        for (Attendance att : attendances) {
            DocumentLine dl = att.getDocumentLine();
            if (dl != null && dl.getDocument() != null) {
                byDocument.computeIfAbsent(dl.getDocument().getPrimaryKey(), k -> new ArrayList<>()).add(att);
            }
        }

        for (Map.Entry<Object, List<Attendance>> entry : byDocument.entrySet()) {
            List<Attendance> docAttendances = entry.getValue();
            if (docAttendances.isEmpty()) continue;

            // Find date range
            LocalDate startDate = null;
            LocalDate endDate = null;
            String guestName = null;
            boolean isCurrentBooking = false;

            for (Attendance att : docAttendances) {
                LocalDate date = att.getDate();
                if (date != null) {
                    if (startDate == null || date.isBefore(startDate)) startDate = date;
                    if (endDate == null || date.isAfter(endDate)) endDate = date;
                }

                if (guestName == null) {
                    DocumentLine dl = att.getDocumentLine();
                    if (dl != null && dl.getDocument() != null) {
                        Document doc = dl.getDocument();
                        String firstName = doc.getFirstName();
                        String lastName = doc.getLastName();
                        guestName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                        guestName = guestName.trim();
                        if (guestName.isEmpty()) guestName = "Guest";

                        // Check if this is the current document
                        if (doc.getPrimaryKey() != null && doc.getPrimaryKey().equals(document.getPrimaryKey())) {
                            isCurrentBooking = true;
                        }
                    }
                }
            }

            if (startDate != null && endDate != null) {
                // End date is exclusive, so add 1 day
                row.occupancies.add(new RoomAllocationCanvas.Occupancy(
                    guestName, startDate, endDate.plusDays(1), isCurrentBooking
                ));
            }
        }
    }

    /**
     * Shows an error message.
     */
    private void showError(String message) {
        contentBox.getChildren().clear();
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        contentBox.getChildren().add(errorLabel);
    }

    /**
     * Saves the selected room allocation.
     */
    private void saveAllocation() {
        if (canvas == null) return;

        RoomAllocationCanvas.RoomAllocation selected = canvas.getSelectedAllocation();
        if (selected == null) return;

        // TODO: Update attendance records with new room allocation
        // 1. Get/create attendance records for each date in the booking period
        // 2. Set resourceConfiguration to the selected room/bed
        // 3. Submit update store changes

        System.out.println("Saving allocation: Room " + selected.roomId + ", Bed " + selected.bedIndex);

        // For now, just close the dialog
        close();
    }

    /**
     * Closes the dialog.
     */
    private void close() {
        if (dialogCallback != null) {
            dialogCallback.closeDialog();
        }
    }
}
