package one.modality.hotel.backoffice.activities.reception.row;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import one.modality.base.shared.entities.Document;
import one.modality.hotel.backoffice.activities.reception.ReceptionPresentationModel;
import one.modality.hotel.backoffice.activities.reception.i18n.ReceptionI18nKeys;
import one.modality.hotel.backoffice.activities.reception.util.ReceptionStyles;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Renders a single guest row in the reception dashboard guest list.
 *
 * Row layout:
 * [Checkbox] [Avatar] [Name/Contact] [Event] [Room] [Dates] [Balance] [Badges] [Actions]
 *
 * @author David Hello
 * @author Claude Code
 */
public class GuestRow {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d");

    private final ReceptionPresentationModel pm;
    private final Document document;
    private final LocalDate arrivalDate;
    private final LocalDate departureDate;
    private final String roomName;
    private final String roomType;

    // Callbacks for actions
    private Consumer<Document> onCheckIn;
    private Consumer<Document> onCheckOut;
    private Consumer<Document> onView;
    private Consumer<Document> onPayment;
    private Consumer<Document> onCancel;
    private Consumer<Document> onConfirm;

    // Derived status
    private String status;
    private boolean isArriving;
    private boolean isDeparting;
    private boolean isCheckedIn;
    private boolean isCheckedOut;
    private boolean isNoShow;
    private boolean isCancelled;
    private boolean hasBalance;
    private boolean hasDeposit;
    private boolean isOffsite;

    public GuestRow(ReceptionPresentationModel pm, Document document,
                    LocalDate arrivalDate, LocalDate departureDate,
                    String roomName, String roomType) {
        this.pm = pm;
        this.document = document;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.roomName = roomName;
        this.roomType = roomType;
        deriveStatus();
    }

    /**
     * Derives the guest status from the document state.
     */
    private void deriveStatus() {
        LocalDate today = pm.getCurrentDate();

        isCancelled = document.isCancelled() != null && document.isCancelled();
        isCheckedOut = document.getCheckedOut() != null && document.getCheckedOut();
        isCheckedIn = document.isArrived() != null && document.isArrived();

        // Get expected date from document or document lines
        LocalDate expectedDate = getExpectedDate();

        isNoShow = !isCheckedIn && !isCancelled && expectedDate != null && expectedDate.isBefore(today);
        isArriving = !isCheckedIn && !isCancelled && !isNoShow && expectedDate != null && expectedDate.equals(today);
        isDeparting = isCheckedIn && getDepartureDate() != null && getDepartureDate().equals(today);

        // Check if offsite (no room)
        isOffsite = getRoomNumber() == null || getRoomNumber().isEmpty();

        // Calculate balance
        Integer totalAmount = document.getPriceNet();
        Integer paidAmount = getPaidAmount();
        hasBalance = totalAmount != null && paidAmount != null && (totalAmount - paidAmount) > 0;

        // Check deposit
        Integer depositAmount = document.getPriceDeposit();
        hasDeposit = depositAmount != null && depositAmount > 0;

        // Set status string
        if (isCancelled) status = "cancelled";
        else if (isCheckedOut) status = "checked-out";
        else if (isCheckedIn) status = "checked-in";
        else if (isNoShow) status = "no-show";
        else if (!isConfirmed()) status = "pre-booked";
        else status = "expected";
    }

    /**
     * Builds the guest row UI component.
     */
    public HBox build() {
        HBox row = ReceptionStyles.createGuestRow();

        // Checkbox for bulk selection (only visible in bulk mode)
        CheckBox checkbox = new CheckBox();
        checkbox.setMinWidth(24);
        checkbox.setPrefWidth(24);
        checkbox.setMaxWidth(24);
        checkbox.visibleProperty().bind(pm.bulkModeProperty());
        checkbox.managedProperty().bind(pm.bulkModeProperty());
        checkbox.setSelected(pm.getSelectedGuests().contains(document));
        checkbox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                if (!pm.getSelectedGuests().contains(document)) {
                    pm.getSelectedGuests().add(document);
                }
            } else {
                pm.getSelectedGuests().remove(document);
            }
        });

        // Guest info section (name, contact)
        VBox guestInfo = buildGuestInfo();
        HBox.setHgrow(guestInfo, Priority.ALWAYS);

        // Event column
        VBox eventCol = buildEventColumn();
        eventCol.setPrefWidth(120);

        // Room column
        VBox roomCol = buildRoomColumn();
        roomCol.setPrefWidth(100);

        // Dates column
        VBox datesCol = buildDatesColumn();
        datesCol.setPrefWidth(100);

        // Balance column
        VBox balanceCol = buildBalanceColumn();
        balanceCol.setPrefWidth(80);
        balanceCol.setAlignment(Pos.CENTER_RIGHT);

        // Status badges
        HBox badges = buildBadges();

        // Action buttons
        HBox actions = buildActions();

        row.getChildren().addAll(checkbox, guestInfo, eventCol, roomCol, datesCol, balanceCol, badges, actions);

        // Click to select
        row.setOnMouseClicked(e -> {
            if (pm.isBulkMode()) {
                checkbox.setSelected(!checkbox.isSelected());
            } else {
                pm.setCurrentGuest(document);
            }
        });

        return row;
    }

    /**
     * Adds this guest row to a GridPane at the specified row index.
     * This method places each column's content directly into the grid cells
     * for perfect alignment with the header row.
     *
     * @param grid The GridPane to add cells to
     * @param rowIndex The row index in the grid (0 is typically the header)
     */
    public void addToGrid(GridPane grid, int rowIndex) {
        String rowBgStyle = rowIndex % 2 == 0
            ? "-fx-background-color: #ffffff; -fx-padding: 12 0 12 0;"
            : "-fx-background-color: #f8f9fa; -fx-padding: 12 0 12 0;";

        int col = 0;

        // Checkbox column (only in bulk mode)
        if (pm.isBulkMode()) {
            CheckBox checkbox = new CheckBox();
            checkbox.setSelected(pm.getSelectedGuests().contains(document));
            checkbox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (isSelected) {
                    if (!pm.getSelectedGuests().contains(document)) {
                        pm.getSelectedGuests().add(document);
                    }
                } else {
                    pm.getSelectedGuests().remove(document);
                }
            });
            HBox checkboxWrapper = new HBox(checkbox);
            checkboxWrapper.setAlignment(Pos.CENTER);
            checkboxWrapper.setStyle(rowBgStyle);
            grid.add(checkboxWrapper, col++, rowIndex);
        }

        // Guest info column
        VBox guestInfo = buildGuestInfo();
        guestInfo.setStyle(rowBgStyle);
        guestInfo.setMaxWidth(Double.MAX_VALUE);
        grid.add(guestInfo, col++, rowIndex);

        // Event column
        VBox eventCol = buildEventColumn();
        eventCol.setStyle(rowBgStyle);
        eventCol.setMaxWidth(Double.MAX_VALUE);
        grid.add(eventCol, col++, rowIndex);

        // Room column
        VBox roomCol = buildRoomColumn();
        roomCol.setStyle(rowBgStyle);
        roomCol.setMaxWidth(Double.MAX_VALUE);
        grid.add(roomCol, col++, rowIndex);

        // Dates column
        VBox datesCol = buildDatesColumn();
        datesCol.setStyle(rowBgStyle);
        datesCol.setMaxWidth(Double.MAX_VALUE);
        grid.add(datesCol, col++, rowIndex);

        // Balance column
        VBox balanceCol = buildBalanceColumn();
        balanceCol.setStyle(rowBgStyle);
        balanceCol.setMaxWidth(Double.MAX_VALUE);
        grid.add(balanceCol, col++, rowIndex);

        // Status badges column
        HBox badges = buildBadges();
        badges.setStyle(rowBgStyle);
        badges.setMaxWidth(Double.MAX_VALUE);
        grid.add(badges, col++, rowIndex);

        // Actions column
        HBox actions = buildActions();
        actions.setStyle(rowBgStyle);
        actions.setMaxWidth(Double.MAX_VALUE);
        grid.add(actions, col, rowIndex);
    }

    /**
     * Builds the guest name and contact info section.
     */
    private VBox buildGuestInfo() {
        VBox info = new VBox(2);
        info.setAlignment(Pos.CENTER_LEFT);

        // Name
        Label nameLabel = new Label(getGuestName());
        nameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 13px;");

        // Contact (email or phone)
        String contact = getContact();
        Label contactLabel = new Label(contact);
        contactLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        info.getChildren().addAll(nameLabel, contactLabel);
        return info;
    }

    /**
     * Builds the event column.
     */
    private VBox buildEventColumn() {
        VBox col = new VBox(2);
        col.setAlignment(Pos.CENTER_LEFT);

        String eventName = getEventName();
        if (eventName != null && !eventName.isEmpty()) {
            Label eventLabel = new Label(eventName);
            eventLabel.setStyle("-fx-font-size: 11px;");
            eventLabel.setWrapText(true);
            col.getChildren().add(eventLabel);
        } else {
            Label independentLabel = I18nControls.newLabel(ReceptionI18nKeys.Independent);
            independentLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
            col.getChildren().add(independentLabel);
        }

        return col;
    }

    /**
     * Builds the room column.
     */
    private VBox buildRoomColumn() {
        VBox col = new VBox(2);
        col.setAlignment(Pos.CENTER_LEFT);

        String roomNumber = getRoomNumber();
        String roomType = getRoomType();

        if (roomNumber != null && !roomNumber.isEmpty()) {
            Label roomLabel = new Label(roomNumber);
            roomLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 500;");
            col.getChildren().add(roomLabel);

            if (roomType != null && !roomType.isEmpty()) {
                Label typeLabel = new Label(roomType);
                typeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d;");
                col.getChildren().add(typeLabel);
            }
        } else {
            Label offsiteLabel = I18nControls.newLabel(ReceptionI18nKeys.StatusOffsite);
            offsiteLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6f42c1;");
            col.getChildren().add(offsiteLabel);
        }

        return col;
    }

    /**
     * Builds the dates column.
     */
    private VBox buildDatesColumn() {
        VBox col = new VBox(2);
        col.setAlignment(Pos.CENTER_LEFT);

        LocalDate expected = getExpectedDate();
        LocalDate departure = getDepartureDate();

        if (expected != null) {
            Label expectedLabel = new Label(DATE_FORMATTER.format(expected));
            expectedLabel.setStyle("-fx-font-size: 11px;");
            col.getChildren().add(expectedLabel);
        }

        if (departure != null) {
            Label departureLabel = new Label("- " + DATE_FORMATTER.format(departure));
            departureLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d;");
            col.getChildren().add(departureLabel);
        }

        return col;
    }

    /**
     * Builds the balance column.
     */
    private VBox buildBalanceColumn() {
        VBox col = new VBox(2);
        col.setAlignment(Pos.CENTER_RIGHT);

        Integer total = document.getPriceNet();
        Integer paid = getPaidAmount();
        int balance = (total != null ? total : 0) - (paid != null ? paid : 0);

        if (balance > 0) {
            Label balanceLabel = new Label("€" + balance);
            balanceLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 12px; -fx-text-fill: #dc3545;");
            col.getChildren().add(balanceLabel);

            Label dueLabel = new Label("due");
            dueLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #6c757d;");
            col.getChildren().add(dueLabel);
        } else if (total != null && total > 0) {
            Label paidLabel = I18nControls.newLabel(ReceptionI18nKeys.Paid);
            paidLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #198754;");
            col.getChildren().add(paidLabel);
        }

        return col;
    }

    /**
     * Builds the status badges.
     */
    private HBox buildBadges() {
        HBox badges = new HBox(4);
        badges.setAlignment(Pos.CENTER_LEFT);
        badges.setPrefWidth(120);

        if (isCancelled) {
            badges.getChildren().add(createBadge("CANCELLED", "#dc3545", "#f8d7da"));
        } else if (isCheckedOut) {
            badges.getChildren().add(createBadge("OUT", "#495057", "#e9ecef"));
        } else if (isCheckedIn) {
            if (isDeparting) {
                badges.getChildren().add(createBadge("DEPARTING", "#495057", "#e9ecef"));
            } else if (!hasBalance) {
                badges.getChildren().add(createBadge("IN", "#198754", "#e8f5e9"));
            }
        }

        if (isOffsite && !isCheckedOut && !isCancelled) {
            badges.getChildren().add(createBadge("OFFSITE", "#6f42c1", "#f3e8ff"));
        }

        if (!hasDeposit && !isCheckedIn && !isCheckedOut && !isCancelled) {
            badges.getChildren().add(createBadge("NO DEPOSIT", "#fd7e14", "#fff3cd"));
        }

        return badges;
    }

    /**
     * Creates a status badge label.
     */
    private Label createBadge(String text, String textColor, String bgColor) {
        Label badge = new Label(text);
        badge.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor +
            "; -fx-padding: 2 6 2 6; -fx-background-radius: 4; -fx-font-size: 10px; -fx-font-weight: 500;");
        return badge;
    }

    /**
     * Builds the action buttons.
     */
    private HBox buildActions() {
        HBox actions = new HBox(ReceptionStyles.SPACING_XS);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPrefWidth(180);

        if (isCancelled) {
            // Cancelled: Reactivate, View
            Button reactivateBtn = createActionButton(ReceptionI18nKeys.Reactivate, "success");
            Button viewBtn = createActionButton(ReceptionI18nKeys.View, "secondary");
            actions.getChildren().addAll(reactivateBtn, viewBtn);
        } else if (isCheckedOut) {
            // Checked out: View only
            Button viewBtn = createActionButton(ReceptionI18nKeys.View, "secondary");
            actions.getChildren().add(viewBtn);
        } else if (isNoShow) {
            // No-show: Mark arrived, Cancel
            Button arrivedBtn = createActionButton(ReceptionI18nKeys.MarkArrived, "success");
            arrivedBtn.setOnAction(e -> { if (onCheckIn != null) onCheckIn.accept(document); });
            Button cancelBtn = createActionButton(ReceptionI18nKeys.Cancel, "danger");
            cancelBtn.setOnAction(e -> { if (onCancel != null) onCancel.accept(document); });
            actions.getChildren().addAll(arrivedBtn, cancelBtn);
        } else if (isCheckedIn) {
            // Checked in: Check out (+ collect payment if balance), View
            if (hasBalance && !isDeparting) {
                Button payBtn = createActionButton(ReceptionI18nKeys.CollectPayment, "warning");
                payBtn.setOnAction(e -> { if (onPayment != null) onPayment.accept(document); });
                actions.getChildren().add(payBtn);
            }
            Button checkoutBtn = createActionButton(ReceptionI18nKeys.CheckOut, isDeparting ? "primary" : "secondary");
            checkoutBtn.setOnAction(e -> { if (onCheckOut != null) onCheckOut.accept(document); });
            Button viewBtn = createActionButton(ReceptionI18nKeys.View, "secondary");
            viewBtn.setOnAction(e -> { if (onView != null) onView.accept(document); });
            actions.getChildren().addAll(checkoutBtn, viewBtn);
        } else if ("pre-booked".equals(status)) {
            // Pre-booked: Confirm, View
            Button confirmBtn = createActionButton(ReceptionI18nKeys.Confirm, "success");
            confirmBtn.setOnAction(e -> { if (onConfirm != null) onConfirm.accept(document); });
            Button viewBtn = createActionButton(ReceptionI18nKeys.View, "secondary");
            viewBtn.setOnAction(e -> { if (onView != null) onView.accept(document); });
            actions.getChildren().addAll(confirmBtn, viewBtn);
        } else {
            // Expected: Check in
            Button checkinBtn = createActionButton(ReceptionI18nKeys.CheckIn, "success");
            checkinBtn.setOnAction(e -> { if (onCheckIn != null) onCheckIn.accept(document); });
            actions.getChildren().add(checkinBtn);
        }

        return actions;
    }

    /**
     * Creates an action button with the specified style.
     */
    private Button createActionButton(String i18nKey, String variant) {
        Button btn = I18nControls.newButton(i18nKey);
        btn.setPadding(new Insets(4, 12, 4, 12));
        btn.setStyle("-fx-font-size: 11px;");

        switch (variant) {
            case "success":
                Bootstrap.successButton(btn);
                break;
            case "danger":
                Bootstrap.dangerButton(btn);
                break;
            case "warning":
                btn.getStyleClass().addAll("btn", "warning");
                break;
            case "primary":
                Bootstrap.primaryButton(btn);
                break;
            default:
                Bootstrap.secondaryButton(btn);
        }

        return btn;
    }

    // ==========================================
    // Data accessor methods (to be refined based on actual entity structure)
    // ==========================================

    private String getGuestName() {
        String firstName = document.getStringFieldValue("person_firstName");
        String lastName = document.getStringFieldValue("person_lastName");
        if (firstName == null) firstName = "";
        if (lastName == null) lastName = "";
        return (firstName + " " + lastName).trim();
    }

    private String getContact() {
        String email = document.getStringFieldValue("person_email");
        String phone = document.getStringFieldValue("person_phone");
        return email != null && !email.isEmpty() ? email : (phone != null ? phone : "");
    }

    private String getEventName() {
        Object event = document.getForeignEntity("event");
        if (event instanceof one.modality.base.shared.entities.Event) {
            return ((one.modality.base.shared.entities.Event) event).getName();
        }
        return null;
    }

    private String getRoomNumber() {
        return roomName;
    }

    private String getRoomType() {
        return roomType;
    }

    private LocalDate getExpectedDate() {
        return arrivalDate;
    }

    private LocalDate getDepartureDate() {
        return departureDate;
    }

    private Integer getPaidAmount() {
        // TODO: Sum from MoneyTransfer records
        return 0;
    }

    private boolean isConfirmed() {
        Boolean confirmed = document.isConfirmed();
        return confirmed != null && confirmed;
    }

    // ==========================================
    // Callback setters
    // ==========================================

    public GuestRow onCheckIn(Consumer<Document> callback) {
        this.onCheckIn = callback;
        return this;
    }

    public GuestRow onCheckOut(Consumer<Document> callback) {
        this.onCheckOut = callback;
        return this;
    }

    public GuestRow onView(Consumer<Document> callback) {
        this.onView = callback;
        return this;
    }

    public GuestRow onPayment(Consumer<Document> callback) {
        this.onPayment = callback;
        return this;
    }

    public GuestRow onCancel(Consumer<Document> callback) {
        this.onCancel = callback;
        return this;
    }

    public GuestRow onConfirm(Consumer<Document> callback) {
        this.onConfirm = callback;
        return this;
    }

    public Document getDocument() {
        return document;
    }

    public String getStatus() {
        return status;
    }
}
