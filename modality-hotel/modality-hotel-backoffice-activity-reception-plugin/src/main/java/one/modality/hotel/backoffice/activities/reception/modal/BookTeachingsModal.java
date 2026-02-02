package one.modality.hotel.backoffice.activities.reception.modal;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
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
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Document;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.MoneyTransfer;
import one.modality.base.shared.entities.Person;
import one.modality.hotel.backoffice.activities.reception.i18n.ReceptionI18nKeys;
import one.modality.hotel.backoffice.activities.reception.util.ReceptionStyles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modal dialog for booking teaching sessions for day visitors.
 * Supports multiple guests with different types (adult/teen/child/infant).
 * Each guest becomes a separate Document with shared teaching_group_id.
 *
 * @author David Hello
 * @author Claude Code
 */
public class BookTeachingsModal implements ReceptionDialogManager.ManagedDialog {

    private final DataSourceModel dataSourceModel;
    private final Object organizationId;

    private final BooleanProperty canProceed = new SimpleBooleanProperty(false);
    private Runnable onSuccessCallback;

    // Teaching types with prices
    private static final String[] TEACHING_IDS = {"gp", "gp1h", "kids"};
    private static final String[] TEACHING_LABELS = {"General Programme (€15)", "GP 1 hour (€8)", "Dharma for Kids (€5)"};
    private static final String[] TEACHING_SHORT = {"GP", "GP 1h", "Kids"};
    private static final int[] TEACHING_PRICES = {15, 8, 5};

    // Guest types with price multipliers
    private static final String[] GUEST_TYPE_IDS = {"adult", "teen", "child", "infant"};
    private static final String[] GUEST_TYPE_LABELS = {"Adult (full price)", "Teen 12-17 (50%)", "Child 4-11 (free)", "Infant 0-3 (free)"};
    private static final double[] GUEST_TYPE_MULTIPLIERS = {1.0, 0.5, 0.0, 0.0};

    // Form fields
    private DatePicker datePicker;
    private Button teachingTypeButton;
    private VBox teachingTypeDropdownList;
    private TextField bookerNameField;
    private TextField bookerEmailField;
    private TextField bookerPhoneField;
    private VBox guestsContainer;
    private CheckBox payNowCheckbox;
    private ToggleButton cardButton;
    private ToggleButton cashButton;
    private HBox paymentMethodBox;
    private Label totalLabel;
    private Label guestSummaryLabel;

    // Data
    private final ObservableList<GuestEntry> guests = FXCollections.observableArrayList();
    private String teachingGroupId;
    private int selectedTeachingIndex = 0;

    /**
     * Generates a random ID (GWT-compatible replacement for UUID).
     */
    private static String generateRandomId() {
        return "id-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000000);
    }

    /**
     * Represents a guest entry in the booking.
     */
    private static class GuestEntry {
        String id = generateRandomId();
        String type = "adult";
        String firstName = "";
        String lastName = "";
        boolean showNameFields = false;

        double getMultiplier() {
            for (int i = 0; i < GUEST_TYPE_IDS.length; i++) {
                if (GUEST_TYPE_IDS[i].equals(type)) {
                    return GUEST_TYPE_MULTIPLIERS[i];
                }
            }
            return 1.0;
        }
    }

    public BookTeachingsModal(DataSourceModel dataSourceModel, Object organizationId) {
        this.dataSourceModel = dataSourceModel;
        this.organizationId = organizationId;
        this.teachingGroupId = generateRandomId();
        // Add first guest by default
        guests.add(new GuestEntry());
    }

    @Override
    public Node buildView() {
        VBox container = new VBox(ReceptionStyles.SPACING_MD);
        container.setPadding(new Insets(24));
        container.setMinWidth(500);
        container.setMaxWidth(550);
        container.getStyleClass().add(ReceptionStyles.RECEPTION_CARD);

        // Header
        VBox header = buildHeader();

        // Date and teaching type selection
        VBox dateAndTeaching = buildDateAndTeachingSection();

        // Booker/Contact section
        VBox bookerSection = buildBookerSection();

        // Guests section
        VBox guestsSection = buildGuestsSection();

        // Summary and payment section
        VBox summarySection = buildSummarySection();

        container.getChildren().addAll(header, dateAndTeaching, bookerSection, guestsSection, summarySection);

        // Initialize totals and validation now that UI is fully built
        updateTotal();
        validateForm();

        return container;
    }

    private VBox buildHeader() {
        VBox header = new VBox(4);
        header.setPadding(new Insets(0, 0, 16, 0));

        Label icon = new Label("\uD83D\uDCDA"); // Books icon
        icon.setStyle("-fx-font-size: 32px;");

        Label title = I18nControls.newLabel(ReceptionI18nKeys.BookTeachings);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 600;");

        Label subtitle = new Label("Book teaching sessions for day visitors");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        header.getChildren().addAll(icon, title, subtitle);

        return header;
    }

    private VBox buildDateAndTeachingSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);

        HBox row = new HBox(ReceptionStyles.SPACING_MD);
        row.setAlignment(Pos.CENTER_LEFT);

        // Date picker
        VBox dateBox = new VBox(4);
        Label dateLabel = I18nControls.newLabel(ReceptionI18nKeys.Date);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.setPrefWidth(150);
        dateBox.getChildren().addAll(dateLabel, datePicker);

        // Teaching type selector (custom dropdown)
        VBox teachingBox = new VBox(4);
        Label teachingLabel = new Label("Teaching type *");
        teachingLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        VBox teachingDropdownContainer = new VBox();
        teachingTypeButton = new Button(TEACHING_LABELS[0] + " ▼");
        teachingTypeButton.setPrefWidth(210);
        teachingTypeButton.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 10;");

        teachingTypeDropdownList = new VBox();
        teachingTypeDropdownList.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-background-radius: 4;");
        teachingTypeDropdownList.setVisible(false);
        teachingTypeDropdownList.setManaged(false);

        for (int i = 0; i < TEACHING_LABELS.length; i++) {
            final int teachingIndex = i;
            Label teachingOption = new Label(TEACHING_LABELS[i]);
            teachingOption.setPadding(new Insets(8, 10, 8, 10));
            teachingOption.setMaxWidth(Double.MAX_VALUE);
            teachingOption.setStyle("-fx-cursor: hand;");
            teachingOption.setOnMouseEntered(e -> teachingOption.setStyle("-fx-background-color: #f8f9fa; -fx-cursor: hand;"));
            teachingOption.setOnMouseExited(e -> teachingOption.setStyle("-fx-cursor: hand;"));
            teachingOption.setOnMouseClicked(e -> {
                selectedTeachingIndex = teachingIndex;
                teachingTypeButton.setText(TEACHING_LABELS[teachingIndex] + " ▼");
                teachingTypeDropdownList.setVisible(false);
                teachingTypeDropdownList.setManaged(false);
                updateTotal();
                rebuildGuestsList(); // Update prices
            });
            teachingTypeDropdownList.getChildren().add(teachingOption);
        }

        teachingTypeButton.setOnAction(e -> {
            boolean isVisible = teachingTypeDropdownList.isVisible();
            teachingTypeDropdownList.setVisible(!isVisible);
            teachingTypeDropdownList.setManaged(!isVisible);
        });

        teachingDropdownContainer.getChildren().addAll(teachingTypeButton, teachingTypeDropdownList);
        teachingBox.getChildren().addAll(teachingLabel, teachingDropdownContainer);
        HBox.setHgrow(teachingBox, Priority.ALWAYS);

        row.getChildren().addAll(dateBox, teachingBox);

        section.getChildren().add(row);

        return section;
    }

    private VBox buildBookerSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);
        section.setPadding(new Insets(16, 0, 0, 0));

        Label sectionTitle = new Label("Booker / Contact");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");

        HBox row1 = new HBox(ReceptionStyles.SPACING_MD);

        VBox nameBox = new VBox(4);
        Label nameLabel = new Label("Name *");
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        bookerNameField = new TextField();
        bookerNameField.setPromptText("Contact name");
        bookerNameField.textProperty().addListener((obs, old, val) -> validateForm());
        nameBox.getChildren().addAll(nameLabel, bookerNameField);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        VBox emailBox = new VBox(4);
        Label emailLabel = new Label("Email");
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        bookerEmailField = new TextField();
        bookerEmailField.setPromptText("Optional");
        emailBox.getChildren().addAll(emailLabel, bookerEmailField);
        HBox.setHgrow(emailBox, Priority.ALWAYS);

        VBox phoneBox = new VBox(4);
        Label phoneLabel = new Label("Phone");
        phoneLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");
        bookerPhoneField = new TextField();
        bookerPhoneField.setPromptText("Optional");
        phoneBox.getChildren().addAll(phoneLabel, bookerPhoneField);
        HBox.setHgrow(phoneBox, Priority.ALWAYS);

        row1.getChildren().addAll(nameBox, emailBox, phoneBox);

        section.getChildren().addAll(sectionTitle, row1);

        return section;
    }

    private VBox buildGuestsSection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);
        section.setPadding(new Insets(16, 0, 0, 0));

        // Header with add button
        HBox headerRow = new HBox(ReceptionStyles.SPACING_MD);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label sectionTitle = new Label("Attendees");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addGuestBtn = new Button("+ Add attendee");
        addGuestBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #0d6efd; -fx-font-size: 12px; -fx-cursor: hand;");
        addGuestBtn.setOnAction(e -> addGuest());

        headerRow.getChildren().addAll(sectionTitle, spacer, addGuestBtn);

        // Guests container
        guestsContainer = new VBox(ReceptionStyles.SPACING_SM);
        guestsContainer.setPadding(new Insets(8));
        guestsContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        rebuildGuestsList();

        section.getChildren().addAll(headerRow, guestsContainer);

        return section;
    }

    private void rebuildGuestsList() {
        guestsContainer.getChildren().clear();

        for (int i = 0; i < guests.size(); i++) {
            GuestEntry guest = guests.get(i);
            VBox guestRow = createGuestRow(guest, i);
            guestsContainer.getChildren().add(guestRow);
        }

        updateTotal();
        validateForm();
    }

    private VBox createGuestRow(GuestEntry guest, int index) {
        VBox guestBox = new VBox(ReceptionStyles.SPACING_XS);
        guestBox.setPadding(new Insets(12));
        guestBox.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        // Main row: type selector, price, remove button
        HBox mainRow = new HBox(ReceptionStyles.SPACING_SM);
        mainRow.setAlignment(Pos.CENTER_LEFT);

        // Price label (declare early for reference in type button handler)
        Label priceLabel = new Label(formatGuestPrice(guest));
        priceLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #198754;");
        priceLabel.setMinWidth(60);
        priceLabel.setAlignment(Pos.CENTER_RIGHT);

        // Guest type dropdown (custom button + VBox dropdown)
        VBox typeDropdownContainer = new VBox();
        Button typeButton = new Button(getGuestTypeLabel(guest.type) + " ▼");
        typeButton.setPrefWidth(190);
        typeButton.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 10;");

        VBox typeDropdownList = new VBox();
        typeDropdownList.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 4; -fx-background-radius: 4;");
        typeDropdownList.setVisible(false);
        typeDropdownList.setManaged(false);

        for (int i = 0; i < GUEST_TYPE_LABELS.length; i++) {
            final int typeIndex = i;
            Label typeOption = new Label(GUEST_TYPE_LABELS[i]);
            typeOption.setPadding(new Insets(8, 10, 8, 10));
            typeOption.setMaxWidth(Double.MAX_VALUE);
            typeOption.setStyle("-fx-cursor: hand;");
            typeOption.setOnMouseEntered(e -> typeOption.setStyle("-fx-background-color: #f8f9fa; -fx-cursor: hand;"));
            typeOption.setOnMouseExited(e -> typeOption.setStyle("-fx-cursor: hand;"));
            typeOption.setOnMouseClicked(e -> {
                guest.type = GUEST_TYPE_IDS[typeIndex];
                typeButton.setText(GUEST_TYPE_LABELS[typeIndex] + " ▼");
                typeDropdownList.setVisible(false);
                typeDropdownList.setManaged(false);
                priceLabel.setText(formatGuestPrice(guest));
                updateTotal();
            });
            typeDropdownList.getChildren().add(typeOption);
        }

        typeButton.setOnAction(e -> {
            boolean isVisible = typeDropdownList.isVisible();
            typeDropdownList.setVisible(!isVisible);
            typeDropdownList.setManaged(!isVisible);
        });

        typeDropdownContainer.getChildren().addAll(typeButton, typeDropdownList);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Name toggle button
        Button nameToggle = new Button(guest.showNameFields ? "▼" : "▶");
        nameToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: #6c757d; -fx-font-size: 10px;");
        nameToggle.setOnAction(e -> {
            guest.showNameFields = !guest.showNameFields;
            rebuildGuestsList();
        });

        // Remove button (only if more than 1 guest)
        Button removeBtn = new Button("✕");
        removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc3545; -fx-font-size: 12px; -fx-cursor: hand;");
        removeBtn.setVisible(guests.size() > 1);
        removeBtn.setOnAction(e -> {
            guests.remove(guest);
            rebuildGuestsList();
        });

        mainRow.getChildren().addAll(typeDropdownContainer, spacer, priceLabel, nameToggle, removeBtn);

        guestBox.getChildren().add(mainRow);

        // Name fields (expandable)
        if (guest.showNameFields) {
            HBox nameRow = new HBox(ReceptionStyles.SPACING_SM);
            nameRow.setPadding(new Insets(8, 0, 0, 0));

            TextField firstNameField = new TextField(guest.firstName);
            firstNameField.setPromptText("First name (optional)");
            firstNameField.textProperty().addListener((obs, old, val) -> guest.firstName = val);
            HBox.setHgrow(firstNameField, Priority.ALWAYS);

            TextField lastNameField = new TextField(guest.lastName);
            lastNameField.setPromptText("Last name (optional)");
            lastNameField.textProperty().addListener((obs, old, val) -> guest.lastName = val);
            HBox.setHgrow(lastNameField, Priority.ALWAYS);

            nameRow.getChildren().addAll(firstNameField, lastNameField);
            guestBox.getChildren().add(nameRow);
        }

        return guestBox;
    }

    private String getGuestTypeLabel(String typeId) {
        for (int i = 0; i < GUEST_TYPE_IDS.length; i++) {
            if (GUEST_TYPE_IDS[i].equals(typeId)) {
                return GUEST_TYPE_LABELS[i];
            }
        }
        return GUEST_TYPE_LABELS[0];
    }

    private int getTeachingPrice() {
        if (selectedTeachingIndex >= 0 && selectedTeachingIndex < TEACHING_PRICES.length) {
            return TEACHING_PRICES[selectedTeachingIndex];
        }
        return TEACHING_PRICES[0];
    }

    private String getTeachingShort() {
        if (selectedTeachingIndex >= 0 && selectedTeachingIndex < TEACHING_SHORT.length) {
            return TEACHING_SHORT[selectedTeachingIndex];
        }
        return TEACHING_SHORT[0];
    }

    private String formatGuestPrice(GuestEntry guest) {
        int basePrice = getTeachingPrice();
        double price = basePrice * guest.getMultiplier();
        if (price == 0) {
            return "Free";
        }
        return "€" + (int) price;
    }

    private void addGuest() {
        guests.add(new GuestEntry());
        rebuildGuestsList();
    }

    private VBox buildSummarySection() {
        VBox section = new VBox(ReceptionStyles.SPACING_SM);
        section.setPadding(new Insets(16));
        section.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");

        // Total row
        HBox totalRow = new HBox(ReceptionStyles.SPACING_MD);
        totalRow.setAlignment(Pos.CENTER_LEFT);

        VBox totalBox = new VBox(2);
        Label totalTitleLabel = new Label("TOTAL");
        totalTitleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        totalLabel = new Label("€0");
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #198754;");
        guestSummaryLabel = new Label("1 adult • GP");
        guestSummaryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
        totalBox.getChildren().addAll(totalTitleLabel, totalLabel, guestSummaryLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Pay now checkbox
        payNowCheckbox = new CheckBox("Pay now");
        payNowCheckbox.setStyle("-fx-font-size: 12px;");
        payNowCheckbox.setSelected(true);
        payNowCheckbox.selectedProperty().addListener((obs, old, val) -> {
            paymentMethodBox.setVisible(val);
            paymentMethodBox.setManaged(val);
        });

        totalRow.getChildren().addAll(totalBox, spacer, payNowCheckbox);

        // Payment method row
        paymentMethodBox = new HBox(ReceptionStyles.SPACING_SM);
        paymentMethodBox.setPadding(new Insets(12, 0, 0, 0));

        cardButton = new ToggleButton("💳 Card");
        cardButton.setPrefWidth(100);
        cardButton.setPadding(new Insets(10, 16, 10, 16));
        cardButton.setSelected(true);
        cardButton.setOnAction(e -> {
            cardButton.setSelected(true);
            cashButton.setSelected(false);
            updatePaymentButtonStyles();
        });

        cashButton = new ToggleButton("💵 Cash");
        cashButton.setPrefWidth(100);
        cashButton.setPadding(new Insets(10, 16, 10, 16));
        cashButton.setOnAction(e -> {
            cashButton.setSelected(true);
            cardButton.setSelected(false);
            updatePaymentButtonStyles();
        });

        updatePaymentButtonStyles();

        paymentMethodBox.getChildren().addAll(cardButton, cashButton);

        section.getChildren().addAll(totalRow, paymentMethodBox);

        return section;
    }

    private void updatePaymentButtonStyles() {
        if (cardButton.isSelected()) {
            cardButton.setStyle("-fx-background-color: #cfe2ff; -fx-text-fill: #0d6efd; -fx-background-radius: 8; -fx-border-color: #0d6efd; -fx-border-radius: 8;");
            cashButton.setStyle("-fx-background-color: white; -fx-text-fill: #6c757d; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");
        } else {
            cashButton.setStyle("-fx-background-color: #d1e7dd; -fx-text-fill: #198754; -fx-background-radius: 8; -fx-border-color: #198754; -fx-border-radius: 8;");
            cardButton.setStyle("-fx-background-color: white; -fx-text-fill: #6c757d; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");
        }
    }

    private void updateTotal() {
        // Guard against being called before UI is fully built
        if (totalLabel == null || guestSummaryLabel == null) {
            return;
        }

        int basePrice = getTeachingPrice();
        double total = 0;

        // Count guests by type
        int adults = 0, teens = 0, children = 0, infants = 0;

        for (GuestEntry guest : guests) {
            total += basePrice * guest.getMultiplier();
            switch (guest.type) {
                case "adult": adults++; break;
                case "teen": teens++; break;
                case "child": children++; break;
                case "infant": infants++; break;
            }
        }

        totalLabel.setText("€" + (int) total);

        // Build guest summary
        List<String> parts = new ArrayList<>();
        if (adults > 0) parts.add(adults + " adult" + (adults > 1 ? "s" : ""));
        if (teens > 0) parts.add(teens + " teen" + (teens > 1 ? "s" : ""));
        if (children > 0) parts.add(children + " child" + (children > 1 ? "ren" : ""));
        if (infants > 0) parts.add(infants + " infant" + (infants > 1 ? "s" : ""));

        guestSummaryLabel.setText(String.join(", ", parts) + " • " + getTeachingShort());
    }

    private void validateForm() {
        // Guard against being called before UI is fully built
        if (bookerNameField == null) {
            return;
        }
        boolean hasBooker = bookerNameField.getText() != null && !bookerNameField.getText().trim().isEmpty();
        boolean hasGuests = !guests.isEmpty();
        canProceed.set(hasBooker && hasGuests);
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
            LocalDate teachingDate = datePicker.getValue();
            boolean payNow = payNowCheckbox.isSelected();
            boolean isCash = cashButton.isSelected();
            int basePrice = getTeachingPrice();

            // Create a Document for each guest with shared teaching_group_id
            boolean isFirstGuest = true;
            for (GuestEntry guest : guests) {
                double guestPrice = basePrice * guest.getMultiplier();

                // Create Person
                Person person = updateStore.insertEntity(Person.class);
                if (!guest.firstName.isEmpty() || !guest.lastName.isEmpty()) {
                    person.setFirstName(guest.firstName.isEmpty() ? "Guest" : guest.firstName);
                    person.setLastName(guest.lastName.isEmpty() ? ("of " + bookerNameField.getText().trim()) : guest.lastName);
                } else if (isFirstGuest) {
                    // First guest gets booker info
                    String[] nameParts = bookerNameField.getText().trim().split("\\s+", 2);
                    person.setFirstName(nameParts[0]);
                    if (nameParts.length > 1) {
                        person.setLastName(nameParts[1]);
                    }
                    if (!bookerEmailField.getText().trim().isEmpty()) {
                        person.setEmail(bookerEmailField.getText().trim());
                    }
                    if (!bookerPhoneField.getText().trim().isEmpty()) {
                        person.setPhone(bookerPhoneField.getText().trim());
                    }
                } else {
                    // Other guests without names
                    String typeLabel = getGuestTypeLabel(guest.type).split(" ")[0];
                    person.setFirstName(typeLabel);
                    person.setLastName("Guest");
                }
                person.setOrganization(organizationId);

                // Create Document
                Document document = updateStore.insertEntity(Document.class);
                document.setPerson(person);
                document.setConfirmed(true);
                document.setPriceNet((int) guestPrice);
                // Store teaching_group_id and guest_type in custom fields if available
                // document.setFieldValue("teaching_group_id", teachingGroupId);
                // document.setFieldValue("guest_type_code", guest.type);

                // Create DocumentLine for the teaching
                DocumentLine dl = updateStore.insertEntity(DocumentLine.class);
                dl.setDocument(document);
                dl.setStartDate(teachingDate);
                dl.setEndDate(teachingDate);
                // TODO: Link to teaching item based on selectedTeachingIndex

                // Create MoneyTransfer if paying now and price > 0
                if (payNow && guestPrice > 0) {
                    MoneyTransfer mt = updateStore.insertEntity(MoneyTransfer.class);
                    mt.setDocument(document);
                    mt.setAmount((int) guestPrice);
                    mt.setMethod(isCash ? 0 : 1); // 0=cash, 1=card
                    mt.setDate(LocalDateTime.now());
                }

                isFirstGuest = false;
            }

            // Submit all changes
            updateStore.submitChanges()
                    .onFailure(error -> {
                        Console.log("Error booking teachings: " + error.getMessage());
                        dialogCallback.closeDialog();
                    })
                    .onSuccess(result -> {
                        Console.log("Teachings booked successfully for " + guests.size() + " attendees");
                        if (onSuccessCallback != null) {
                            onSuccessCallback.run();
                        }
                        dialogCallback.closeDialog();
                    });

        } catch (Exception e) {
            Console.log("Error booking teachings: " + e.getMessage());
            dialogCallback.closeDialog();
        }
    }

    @Override
    public String getPrimaryButtonText() {
        if (payNowCheckbox != null && payNowCheckbox.isSelected()) {
            return "Book & Pay " + totalLabel.getText();
        }
        return "Book " + guests.size() + " attendee" + (guests.size() > 1 ? "s" : "");
    }
}
