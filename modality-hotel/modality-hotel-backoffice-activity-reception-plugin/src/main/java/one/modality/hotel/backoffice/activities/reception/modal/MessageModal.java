package one.modality.hotel.backoffice.activities.reception.modal;

import dev.webfx.extras.i18n.controls.I18nControls;
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
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.hotel.backoffice.activities.reception.i18n.ReceptionI18nKeys;
import one.modality.hotel.backoffice.activities.reception.util.ReceptionColors;
import one.modality.hotel.backoffice.activities.reception.util.ReceptionStyles;

import java.util.List;
import java.util.Objects;

/**
 * Modal dialog for sending messages to departments.
 * Features styled department buttons with icons, optional guest linking,
 * room/location field, and priority toggle (Normal/Urgent).
 *
 * Matches JSX mockup with:
 * - Department button cards (Household, Maintenance, Accounting, Treasurer)
 * - Guest dropdown for in-house guests
 * - Room/location input when no guest selected
 * - Normal/Urgent priority toggle
 * - Summary box showing linked guest/room
 *
 * @author David Hello
 * @author Claude Code
 */
public class MessageModal implements ReceptionDialogManager.ManagedDialog {

    private final DataSourceModel dataSourceModel;
    private final BooleanProperty canProceed = new SimpleBooleanProperty(false);
    private Runnable onSuccessCallback;

    // Department definitions with colors (using HEX strings)
    private static final String[][] DEPARTMENTS = {
        {"household", "Household", "🏠", ReceptionColors.TEAL_HEX},
        {"maintenance", "Maintenance", "🔧", ReceptionColors.WARNING_HEX},
        {"accounting", "Accounting", "📋", ReceptionColors.PRIMARY_HEX},
        {"treasurer", "Treasurer", "💵", ReceptionColors.SUCCESS_HEX}
    };

    // Optional pre-selected guest context
    private final Document preSelectedGuest;

    // In-house guests list
    private final ObservableList<Document> inHouseGuests = FXCollections.observableArrayList();

    // UI components
    private String selectedDepartment = "household";
    private ToggleButton[] departmentButtons;
    private Button guestSelectorButton;
    private VBox guestDropdownList;
    private ScrollPane guestDropdownScrollPane;
    private Document selectedGuest;
    private TextField roomField;
    private ToggleButton normalButton;
    private ToggleButton urgentButton;
    private TextArea messageArea;
    private VBox summaryBox;
    private Label summaryLabel;

    /**
     * Creates a message modal without pre-selected guest.
     */
    public MessageModal(DataSourceModel dataSourceModel, List<Document> allGuests) {
        this(dataSourceModel, allGuests, null);
    }

    /**
     * Creates a message modal with optional pre-selected guest context.
     */
    public MessageModal(DataSourceModel dataSourceModel, List<Document> allGuests, Document preSelectedGuest) {
        this.dataSourceModel = dataSourceModel;
        this.preSelectedGuest = preSelectedGuest;

        // Filter to in-house guests (checked-in, not checked out)
        if (allGuests != null) {
            allGuests.stream()
                .filter(doc -> doc.isArrived() != null && doc.isArrived() &&
                              (doc.getCheckedOut() == null || !doc.getCheckedOut()))
                .forEach(inHouseGuests::add);
        }
    }

    @Override
    public Node buildView() {
        VBox container = new VBox(ReceptionStyles.SPACING_MD);
        container.setPadding(new Insets(24));
        container.setMinWidth(480);
        container.getStyleClass().add(ReceptionStyles.RECEPTION_CARD);

        // Header
        VBox header = buildHeader();

        // Department selector buttons
        VBox departmentSection = buildDepartmentSection();

        // Guest selector (if no pre-selected guest)
        VBox guestSection = null;
        if (preSelectedGuest == null) {
            guestSection = buildGuestSection();
        }

        // Room/Location field (shown when no guest linked)
        VBox roomSection = buildRoomSection();

        // Priority toggle
        VBox prioritySection = buildPrioritySection();

        // Message text area
        VBox messageSection = buildMessageSection();

        // Summary box (hidden initially)
        summaryBox = buildSummaryBox();

        // Build container
        container.getChildren().add(header);
        container.getChildren().add(departmentSection);
        if (guestSection != null) {
            container.getChildren().add(guestSection);
        }
        container.getChildren().add(roomSection);
        container.getChildren().add(prioritySection);
        container.getChildren().add(messageSection);
        container.getChildren().add(summaryBox);

        // If pre-selected guest, update state
        if (preSelectedGuest != null) {
            updateSummary();
            roomSection.setVisible(false);
            roomSection.setManaged(false);
        }

        return container;
    }

    private VBox buildHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(0, 0, 16, 0));

        Label icon = new Label("✉️");
        icon.setStyle("-fx-font-size: 32px;");

        Label title = I18nControls.newLabel(ReceptionI18nKeys.SendMessage);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");

        String subtitleText = preSelectedGuest != null ?
            "Related to " + getGuestName(preSelectedGuest) :
            "To household or maintenance";
        Label subtitle = new Label(subtitleText);
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        header.getChildren().addAll(icon, title, subtitle);

        return header;
    }

    private VBox buildDepartmentSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);

        Label label = new Label("Send to");
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        HBox buttons = new HBox(ReceptionStyles.SPACING_SM);
        departmentButtons = new ToggleButton[DEPARTMENTS.length];

        for (int i = 0; i < DEPARTMENTS.length; i++) {
            String[] dept = DEPARTMENTS[i];
            String id = dept[0];
            String name = dept[1];
            String iconEmoji = dept[2];
            String color = dept[3];

            ToggleButton btn = new ToggleButton();
            btn.setUserData(id);

            // Button content: icon + label stacked
            VBox btnContent = new VBox(4);
            btnContent.setAlignment(Pos.CENTER);
            btnContent.setPadding(new Insets(8, 12, 8, 12));

            Label iconLabel = new Label(iconEmoji);
            iconLabel.setStyle("-fx-font-size: 20px;");

            Label nameLabel = new Label(name);
            nameLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 500;");

            btnContent.getChildren().addAll(iconLabel, nameLabel);
            btn.setGraphic(btnContent);
            btn.setText("");

            HBox.setHgrow(btn, Priority.ALWAYS);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(72);

            int index = i;
            btn.setOnAction(e -> selectDepartment(index, color));

            // Initial selection
            if (id.equals("household")) {
                btn.setSelected(true);
                styleDepartmentButton(btn, true, color);
            } else {
                styleDepartmentButton(btn, false, color);
            }

            departmentButtons[i] = btn;
            buttons.getChildren().add(btn);
        }

        section.getChildren().addAll(label, buttons);
        return section;
    }

    private void selectDepartment(int index, String color) {
        selectedDepartment = (String) departmentButtons[index].getUserData();

        for (int i = 0; i < departmentButtons.length; i++) {
            boolean isSelected = i == index;
            departmentButtons[i].setSelected(isSelected);
            styleDepartmentButton(departmentButtons[i], isSelected, DEPARTMENTS[i][3]);
        }
    }

    private void styleDepartmentButton(ToggleButton btn, boolean selected, String color) {
        if (selected) {
            btn.setStyle("-fx-background-color: " + color + "15; -fx-border-color: " + color +
                "; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;");
            // Update child label colors
            if (btn.getGraphic() instanceof VBox) {
                VBox content = (VBox) btn.getGraphic();
                content.getChildren().forEach(node -> {
                    if (node instanceof Label) {
                        ((Label) node).setStyle(((Label) node).getStyle() + " -fx-text-fill: " + color + ";");
                    }
                });
            }
        } else {
            btn.setStyle(
                "-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-width: 2; -fx-border-radius: 8; -fx-background-radius: 8;"
            );
            // Update child label colors to muted
            if (btn.getGraphic() instanceof VBox) {
                VBox content = (VBox) btn.getGraphic();
                content.getChildren().forEach(node -> {
                    if (node instanceof Label) {
                        ((Label) node).setStyle(((Label) node).getStyle().replaceAll("-fx-text-fill: [^;]+;", "") + " -fx-text-fill: #6c757d;");
                    }
                });
            }
        }
    }

    private VBox buildGuestSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_XS);

        Label label = new Label("Related to guest (optional)");
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        // Custom dropdown using Button + VBox (GWT-compatible)
        VBox selectorContainer = new VBox(0);

        guestSelectorButton = new Button("General message (no guest)");
        guestSelectorButton.setMaxWidth(Double.MAX_VALUE);
        guestSelectorButton.setStyle("-fx-font-size: 12px; -fx-background-color: white; -fx-border-color: #dee2e6; " +
            "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 8 12; -fx-alignment: CENTER_LEFT;");

        // Dropdown list container
        guestDropdownList = new VBox(2);
        guestDropdownList.setPadding(new Insets(4));

        guestDropdownScrollPane = new ScrollPane(guestDropdownList);
        guestDropdownScrollPane.setFitToWidth(true);
        guestDropdownScrollPane.setMaxHeight(150);
        guestDropdownScrollPane.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 0 0 6 6;");
        guestDropdownScrollPane.setVisible(false);
        guestDropdownScrollPane.setManaged(false);

        // Populate dropdown
        populateGuestDropdown();

        // Toggle dropdown on button click
        guestSelectorButton.setOnAction(e -> {
            boolean isVisible = guestDropdownScrollPane.isVisible();
            guestDropdownScrollPane.setVisible(!isVisible);
            guestDropdownScrollPane.setManaged(!isVisible);
        });

        selectorContainer.getChildren().addAll(guestSelectorButton, guestDropdownScrollPane);

        section.getChildren().addAll(label, selectorContainer);
        return section;
    }

    private void populateGuestDropdown() {
        guestDropdownList.getChildren().clear();

        // Add "no guest" option
        HBox noGuestRow = createGuestDropdownRow(null, "General message (no guest)");
        guestDropdownList.getChildren().add(noGuestRow);

        // Add in-house guests
        for (Document guest : inHouseGuests) {
            String name = getGuestName(guest);
            String room = getRoomNumber(guest);
            String displayText = name + (room != null && !room.isEmpty() ? " — " + room : "");
            HBox guestRow = createGuestDropdownRow(guest, displayText);
            guestDropdownList.getChildren().add(guestRow);
        }
    }

    private HBox createGuestDropdownRow(Document guest, String displayText) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 10, 6, 10));
        row.setStyle("-fx-background-color: white; -fx-cursor: hand;");

        Label label = new Label(displayText);
        label.setStyle("-fx-font-size: 12px;");
        row.getChildren().add(label);

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #f0f0f0; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: white; -fx-cursor: hand;"));

        row.setOnMouseClicked(e -> {
            selectedGuest = guest;
            guestSelectorButton.setText(displayText);
            guestDropdownScrollPane.setVisible(false);
            guestDropdownScrollPane.setManaged(false);
            updateRoomVisibility();
            updateSummary();
        });

        return row;
    }

    private void updateRoomVisibility() {
        boolean showRoom = selectedGuest == null;
        if (roomField != null && roomField.getParent() instanceof VBox) {
            VBox roomSection = (VBox) roomField.getParent();
            roomSection.setVisible(showRoom);
            roomSection.setManaged(showRoom);
        }
    }

    private VBox buildRoomSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_XS);

        Label label = new Label("Room / Location");
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        roomField = new TextField();
        roomField.setPromptText("e.g., S-102, Kitchen, Meditation Hall...");
        roomField.setStyle("-fx-font-size: 12px;");

        roomField.textProperty().addListener((obs, old, newVal) -> updateSummary());

        section.getChildren().addAll(label, roomField);
        return section;
    }

    private VBox buildPrioritySection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);

        Label label = new Label("Priority");
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        HBox buttons = new HBox(ReceptionStyles.SPACING_SM);

        normalButton = new ToggleButton("Normal");
        normalButton.setPrefWidth(100);
        normalButton.setPadding(new Insets(10, 16, 10, 16));
        normalButton.setSelected(true);

        urgentButton = new ToggleButton("Urgent");
        urgentButton.setPrefWidth(100);
        urgentButton.setPadding(new Insets(10, 16, 10, 16));

        updatePriorityStyles();

        normalButton.setOnAction(e -> {
            normalButton.setSelected(true);
            urgentButton.setSelected(false);
            updatePriorityStyles();
        });

        urgentButton.setOnAction(e -> {
            urgentButton.setSelected(true);
            normalButton.setSelected(false);
            updatePriorityStyles();
        });

        buttons.getChildren().addAll(normalButton, urgentButton);
        section.getChildren().addAll(label, buttons);
        return section;
    }

    private void updatePriorityStyles() {
        if (normalButton.isSelected()) {
            normalButton.setStyle("-fx-background-color: #cfe2ff; -fx-text-fill: #0d6efd; -fx-background-radius: 8; -fx-border-color: #0d6efd; -fx-border-radius: 8;");
            urgentButton.setStyle("-fx-background-color: white; -fx-text-fill: #6c757d; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");
        } else {
            urgentButton.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #dc3545; -fx-background-radius: 8; -fx-border-color: #dc3545; -fx-border-radius: 8;");
            normalButton.setStyle("-fx-background-color: white; -fx-text-fill: #6c757d; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");
        }
    }

    private VBox buildMessageSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_XS);

        Label label = new Label("Message *");
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        messageArea = new TextArea();
        messageArea.setPromptText("Describe what needs to be done...");
        messageArea.setPrefRowCount(3);
        messageArea.setWrapText(true);
        messageArea.setStyle("-fx-font-size: 12px;");
        VBox.setVgrow(messageArea, Priority.ALWAYS);

        // Validation
        messageArea.textProperty().addListener((obs, old, newVal) -> validateForm());

        section.getChildren().addAll(label, messageArea);
        return section;
    }

    private VBox buildSummaryBox() {
        VBox box = new VBox(ReceptionStyles.SPACING_XS);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        box.setVisible(false);
        box.setManaged(false);

        HBox row = new HBox(ReceptionStyles.SPACING_SM);
        row.setAlignment(Pos.CENTER_LEFT);

        Label userIcon = new Label("👤");
        userIcon.setStyle("-fx-font-size: 14px;");

        summaryLabel = new Label();
        summaryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

        row.getChildren().addAll(userIcon, summaryLabel);
        box.getChildren().add(row);

        return box;
    }

    private void updateSummary() {
        String guestName = null;
        String roomNumber = null;

        if (preSelectedGuest != null) {
            guestName = getGuestName(preSelectedGuest);
            roomNumber = getRoomNumber(preSelectedGuest);
        } else if (selectedGuest != null) {
            guestName = getGuestName(selectedGuest);
            roomNumber = getRoomNumber(selectedGuest);
        }

        if (roomField != null && roomField.isVisible()) {
            String roomInput = roomField.getText();
            if (roomInput != null && !roomInput.trim().isEmpty()) {
                roomNumber = roomInput.trim();
            }
        }

        if (guestName != null || (roomNumber != null && !roomNumber.isEmpty())) {
            StringBuilder text = new StringBuilder();
            if (guestName != null) text.append(guestName);
            if (guestName != null && roomNumber != null && !roomNumber.isEmpty()) text.append(" · ");
            if (roomNumber != null && !roomNumber.isEmpty()) text.append(roomNumber);

            summaryLabel.setText(text.toString());
            summaryBox.setVisible(true);
            summaryBox.setManaged(true);
        } else {
            summaryBox.setVisible(false);
            summaryBox.setManaged(false);
        }
    }

    private void validateForm() {
        boolean hasMessage = messageArea.getText() != null && !messageArea.getText().trim().isEmpty();
        canProceed.set(hasMessage);
    }

    private String getGuestName(Document doc) {
        if (doc == null) return "";
        String firstName = doc.getStringFieldValue("person_firstName");
        String lastName = doc.getStringFieldValue("person_lastName");
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }

    private String getRoomNumber(Document doc) {
        // This would be derived from DocumentLine - simplified for now
        return null;
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
        String message = messageArea.getText().trim();
        boolean isUrgent = urgentButton.isSelected();

        String linkedGuestName = null;
        String roomNumber = null;

        if (preSelectedGuest != null) {
            linkedGuestName = getGuestName(preSelectedGuest);
            roomNumber = getRoomNumber(preSelectedGuest);
        } else if (selectedGuest != null) {
            linkedGuestName = getGuestName(selectedGuest);
            roomNumber = getRoomNumber(selectedGuest);
        }

        if (roomField != null && roomField.isVisible()) {
            String roomInput = roomField.getText();
            if (roomInput != null && !roomInput.trim().isEmpty()) {
                roomNumber = roomInput.trim();
            }
        }

        // TODO: Create Message entity in database
        Console.log("Message sent to " + selectedDepartment + (isUrgent ? " (URGENT)" : "") + ": " + message);
        if (linkedGuestName != null) {
            Console.log("  Related to: " + linkedGuestName);
        }
        if (roomNumber != null) {
            Console.log("  Room/Location: " + roomNumber);
        }

        if (onSuccessCallback != null) {
            onSuccessCallback.run();
        }
        dialogCallback.closeDialog();
    }

    @Override
    public String getPrimaryButtonText() {
        return "Send message";
    }
}
