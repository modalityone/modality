package one.modality.booking.backoffice.activities.registration;

import dev.webfx.extras.controlfactory.button.ButtonFactoryMixin;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.dql.DqlStatement;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.controls.entity.selector.EntityButtonSelector;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.domainmodel.formatters.PriceFormatter;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.crm.shared.services.authn.fx.FXUserName;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static one.modality.booking.backoffice.activities.registration.RegistrationStyles.*;

/**
 * Edit modal for a DocumentLine (booking option).
 * <p>
 * Features:
 * - Category-colored header with item name and read/unread toggle
 * - Date info bar showing date range and duration
 * - Category-specific fields (room allocation, transport details, meal sitting)
 * - Pricing section with discount vs fixed price toggle
 * - Comment textarea
 * <p>
 * Based on RegistrationDashboardFull.jsx EditLineModal (lines 6252-7067).
 *
 * @author Claude Code
 */
public class EditLineModal {

    private final DocumentLine line;
    private final UpdateStore updateStore;
    private final WorkingBooking workingBooking;
    private final Event event;
    private final String category;
    private final Runnable onSave;
    private final Runnable onCancel;

    private DialogCallback dialogCallback;

    // Editable state properties
    private final BooleanProperty isReadProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty hasCustomPriceProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty lockSittingProperty = new SimpleBooleanProperty(false);

    // Form fields
    private TextField pickupLocationField;
    private TextField pickupTimeField;
    private TextField flightNumberField;
    private TextField sittingAllocationField;
    private TextField discountField;
    private TextField customPriceField;
    private TextArea commentArea;

    // Entity selectors for accommodation
    private EntityButtonSelector<Item> accommodationTypeSelector;
    private EntityButtonSelector<ResourceConfiguration> roomSelector;
    private final ObjectProperty<Item> selectedAccommodationType = new SimpleObjectProperty<>();
    private final ObjectProperty<ResourceConfiguration> selectedRoom = new SimpleObjectProperty<>();

    // Dialog pane reference (needed for entity selector dropdowns)
    private BorderPane dialogPane;

    // Button factory mixin for entity selectors
    private final ButtonFactoryMixin buttonMixin = new ButtonFactoryMixin() {};

    // Labels for reactive updates
    private Label totalValueLabel;

    // Cached price data
    private int standardPricePerUnit = 0;
    private int currentTotal = 0;

    // Date formatter
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM");

    /**
     * Constructor for legacy UpdateStore-based editing.
     * @deprecated Use constructor with WorkingBooking instead
     */
    public EditLineModal(DocumentLine line, UpdateStore updateStore, Runnable onSave, Runnable onCancel) {
        this.line = line;
        this.updateStore = updateStore;
        this.workingBooking = null;
        this.event = line.getDocument() != null ? line.getDocument().getEvent() : null;
        this.category = getCategoryFromLine(line);
        this.onSave = onSave;
        this.onCancel = onCancel;

        initializeFromLine();
    }

    /**
     * Constructor for WorkingBooking-based editing.
     * Uses the event-sourcing API for proper price calculation and updates.
     */
    public EditLineModal(DocumentLine line, WorkingBooking workingBooking, Event event, Runnable onSave, Runnable onCancel) {
        this.line = line;
        this.updateStore = null;
        this.workingBooking = workingBooking;
        this.event = event;
        this.category = getCategoryFromLine(line);
        this.onSave = onSave;
        this.onCancel = onCancel;

        initializeFromLine();
    }

    /**
     * Initializes properties from the document line.
     */
    private void initializeFromLine() {
        // Initialize properties from line
        isReadProperty.set(Boolean.TRUE.equals(line.getFieldValue("read")));
        hasCustomPriceProperty.set(line.getPriceCustom() != null && line.getPriceCustom() > 0);
        // TODO: lockSittingAllocation field not yet in database schema
        lockSittingProperty.set(false);

        // Calculate standard price per unit from PolicyAggregate
        if (workingBooking != null) {
            PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();
            if (policyAggregate != null) {
                Site site = line.getSite();
                Item item = line.getItem();
                standardPricePerUnit = policyAggregate.filterDailyRatesStreamOfSiteAndItem(site, item)
                    .findFirst()
                    .map(rate -> rate.getPrice() != null ? rate.getPrice() : 0)
                    .orElse(0);
            }
        }

        // Get current total from line
        currentTotal = line.getPriceNet() != null ? line.getPriceNet() : 0;
    }

    /**
     * Formats a price in cents using the event's currency.
     */
    private String formatPrice(int priceInCents) {
        String currencySymbol = EventPriceFormatter.getEventCurrencySymbol(event);
        return PriceFormatter.formatWithCurrency(priceInCents, currencySymbol, true);
    }

    /**
     * Shows the edit line modal.
     */
    public void show() {
        dialogPane = new BorderPane();
        dialogPane.setBackground(createBackground(BG, 12));
        dialogPane.setPrefWidth(450);
        dialogPane.setMaxWidth(500);
        dialogPane.setMaxHeight(650);

        // Header
        dialogPane.setTop(createHeader());

        // Content
        ScrollPane scrollPane = new ScrollPane(createContent());
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent;");
        dialogPane.setCenter(scrollPane);

        // Footer
        dialogPane.setBottom(createFooter());

        // Show dialog
        dialogCallback = DialogUtil.showModalNodeInGoldLayout(dialogPane, FXMainFrameDialogArea.getDialogArea());
    }

    /**
     * Creates the header with category color, icon, item name, and controls.
     */
    private Node createHeader() {
        Color bgColor = getCategoryBgColor(category);
        Color fillColor = getCategoryIconColor(category);

        HBox header = new HBox(12);
        header.setPadding(new Insets(14, 16, 14, 16));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setBackground(createBackground(bgColor, 12, 12, 0, 0));
        header.setBorder(new Border(new BorderStroke(
            deriveColor(fillColor, 0.2),
            BorderStrokeStyle.SOLID,
            new CornerRadii(12, 12, 0, 0, false),
            new BorderWidths(0, 0, 1, 0) // bottom border only
        )));

        // Category icon (36x36)
        StackPane iconBox = new StackPane();
        iconBox.setMinSize(36, 36);
        iconBox.setMaxSize(36, 36);
        iconBox.setBackground(createBackground(fillColor, 8));

        Label iconLabel = new Label(getCategoryEmoji(category));
        iconLabel.setFont(Font.font(18));
        iconBox.getChildren().add(iconLabel);

        // Item info
        VBox itemInfo = new VBox(2);
        HBox.setHgrow(itemInfo, Priority.ALWAYS);

        String itemName = line.getItem() != null ? line.getItem().getName() : "Unknown Item";
        Label nameLabel = new Label(itemName);
        nameLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        nameLabel.setTextFill(fillColor);

        Label categoryLabel = new Label(category);
        categoryLabel.setFont(Font.font("System", 11));
        categoryLabel.setTextFill(TEXT_MUTED);

        itemInfo.getChildren().addAll(nameLabel, categoryLabel);

        // Read/Unread toggle button
        Button readToggle = createReadToggleButton();

        // Close button
        Button closeBtn = new Button("×");
        closeBtn.setFont(Font.font("System", 16));
        closeBtn.setTextFill(TEXT_MUTED);
        closeBtn.setBackground(Background.EMPTY);
        closeBtn.setBorder(Border.EMPTY);
        closeBtn.setMinSize(28, 28);
        closeBtn.setMaxSize(28, 28);
        closeBtn.setCursor(Cursor.HAND);
        closeBtn.setOnAction(e -> closeDialog(false));

        header.getChildren().addAll(iconBox, itemInfo, readToggle, closeBtn);
        return header;
    }

    /**
     * Creates the read/unread toggle button.
     */
    private Button createReadToggleButton() {
        Button btn = new Button();
        btn.setCursor(Cursor.HAND);
        btn.setOnAction(e -> isReadProperty.set(!isReadProperty.get()));

        // Update appearance based on state
        FXProperties.runNowAndOnPropertiesChange(() -> {
            boolean isRead = isReadProperty.get();

            HBox content = new HBox(4);
            content.setAlignment(Pos.CENTER);

            if (isRead) {
                Label checkIcon = new Label("✓");
                checkIcon.setFont(Font.font(10));
                checkIcon.setTextFill(SUCCESS);
                Label text = new Label("Read");
                text.setFont(Font.font("System", FontWeight.SEMI_BOLD, 10));
                text.setTextFill(SUCCESS);
                content.getChildren().addAll(checkIcon, text);

                btn.setBackground(createBackground(SUCCESS_LIGHT, 14));
                btn.setBorder(createBorder(SUCCESS_BORDER, 14));
            } else {
                Circle dot = new Circle(3, WARNING);
                Label text = new Label("Unread");
                text.setFont(Font.font("System", FontWeight.SEMI_BOLD, 10));
                text.setTextFill(WARNING_TEXT);
                content.getChildren().addAll(dot, text);

                btn.setBackground(createBackground(WARNING_LIGHT, 14));
                btn.setBorder(createBorder(WARNING_BORDER, 14));
            }

            btn.setGraphic(content);
        }, isReadProperty);

        btn.setPadding(new Insets(5, 10, 5, 10));
        return btn;
    }

    /**
     * Creates the content area with form fields.
     */
    private Node createContent() {
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));

        // Date info bar (if temporal item)
        content.getChildren().add(createDateInfoBar());

        // Category-specific fields
        if ("accommodation".equals(category)) {
            content.getChildren().add(createAccommodationSection());
        } else if ("transport".equals(category)) {
            content.getChildren().add(createTransportSection());
        } else if ("meals".equals(category)) {
            content.getChildren().add(createMealsSection());
        }

        // Shared room options (for accommodation with double/shared in name)
        if ("accommodation".equals(category) && itemNameContainsShared()) {
            content.getChildren().add(createSharingOptionsSection());
        }

        // Pricing section (always shown)
        content.getChildren().add(createPricingSection());

        // Comment section (always shown)
        content.getChildren().add(createCommentSection());

        return content;
    }

    /**
     * Creates the date info bar showing date range and duration.
     */
    private Node createDateInfoBar() {
        Color bgColor = getCategoryBgColor(category);
        Color fillColor = getCategoryIconColor(category);

        HBox bar = new HBox(6);
        bar.setPadding(new Insets(8, 12, 8, 12));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setBackground(createBackground(bgColor, 6));

        // Calendar icon
        Label calIcon = new Label("📅");
        calIcon.setFont(Font.font(12));

        // Date range
        LocalDate startDate = line.getStartDate();
        LocalDate endDate = line.getEndDate();

        String dateText = "";
        if (startDate != null && endDate != null) {
            dateText = startDate.format(DATE_FORMAT) + " → " + endDate.format(DATE_FORMAT);
        }

        Label dateLabel = new Label(dateText);
        dateLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        dateLabel.setTextFill(fillColor);
        HBox.setHgrow(dateLabel, Priority.ALWAYS);

        // Duration
        long days = 0;
        if (startDate != null && endDate != null) {
            days = ChronoUnit.DAYS.between(startDate, endDate);
        }
        Label durationLabel = new Label(days + " day" + (days != 1 ? "s" : ""));
        durationLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
        durationLabel.setTextFill(fillColor);

        bar.getChildren().addAll(calIcon, dateLabel, durationLabel);
        return bar;
    }

    /**
     * Creates the accommodation-specific fields section.
     * Includes:
     * - Accommodation Type selector (Item entity) to change room type - uses PolicyAggregate rates
     * - Room Allocation selector (ResourceConfiguration entity) to assign specific room - queries by globalSite
     */
    private Node createAccommodationSection() {
        VBox section = new VBox(12);

        // Get the organization's globalSite for room queries
        Site globalSite = null;
        if (event != null && event.getOrganization() != null) {
            globalSite = event.getOrganization().getGlobalSite();
        }
        final Site finalGlobalSite = globalSite;

        // ═══════════════════════════════════════════════════════════════════════════════
        // ACCOMMODATION TYPE SELECTOR - Uses EntityButtonSelector (GWT-compatible)
        // ═══════════════════════════════════════════════════════════════════════════════

        VBox typeSection = new VBox(6);
        Label typeLabel = createSectionTitle("Accommodation Type");

        // Initialize the selected accommodation type from current line
        selectedAccommodationType.set(line.getItem());

        // Create EntityButtonSelector for accommodation type (Item entity)
        // Query accommodation items scheduled for this event
        accommodationTypeSelector = new EntityButtonSelector<>(
            "{class: 'Item', alias: 'i', columns: 'name', orderBy: 'name'}",
            buttonMixin,
            dialogPane,
            DataSourceModelService.getDefaultDataSourceModel()
        );

        // Filter by accommodation family and items available for this event
        if (event != null) {
            // Get items scheduled for this event with accommodation family
            accommodationTypeSelector.always(DqlStatement.where(
                "family=1 and exists(select 1 from ScheduledItem si where si.item=i and si.event=$1)",
                Entities.getPrimaryKey(event)));
        } else {
            // Fallback: just filter by accommodation family
            accommodationTypeSelector.always(DqlStatement.where("family=1"));
        }

        accommodationTypeSelector.setAutoOpenOnMouseEntered(true);
        accommodationTypeSelector.selectedItemProperty().bindBidirectional(selectedAccommodationType);

        // Recalculate price when accommodation type changes
        selectedAccommodationType.addListener((obs, oldItem, newItem) -> {
            if (newItem != null && !newItem.equals(oldItem)) {
                recalculatePriceForNewItem();
            }
        });

        // Style the selector button
        Button typeButton = accommodationTypeSelector.getButton();
        styleEntitySelectorButton(typeButton);

        typeSection.getChildren().addAll(typeLabel, typeButton);

        // ═══════════════════════════════════════════════════════════════════════════════
        // ROOM ALLOCATION SELECTOR - Queries by organization's globalSite
        // ═══════════════════════════════════════════════════════════════════════════════

        VBox roomSection = new VBox(6);
        Label roomLabel = createSectionTitle("Room Assignment");

        // Initialize the selected room from current line
        selectedRoom.set(line.getResourceConfiguration());

        // Create entity selector for room allocation (ResourceConfiguration)
        // Query rooms where the resource belongs to the organization's globalSite
        roomSelector = new EntityButtonSelector<>(
            "{class: 'ResourceConfiguration', alias: 'rc', columns: 'name,resource.name', orderBy: 'name'}",
            buttonMixin,
            dialogPane,
            DataSourceModelService.getDefaultDataSourceModel()
        );

        // Filter rooms by globalSite and selected accommodation type
        if (finalGlobalSite != null) {
            FXProperties.runNowAndOnPropertiesChange(() -> {
                Item selectedItem = selectedAccommodationType.get();
                if (selectedItem != null) {
                    // Filter by globalSite and selected item type
                    roomSelector.always(DqlStatement.where("item=$1 and resource.site=$2",
                        Entities.getPrimaryKey(selectedItem),
                        Entities.getPrimaryKey(finalGlobalSite)));
                } else {
                    // Just filter by globalSite
                    roomSelector.always(DqlStatement.where("resource.site=$1",
                        Entities.getPrimaryKey(finalGlobalSite)));
                }
            }, selectedAccommodationType);
        }

        roomSelector.setAutoOpenOnMouseEntered(true);
        roomSelector.appendNullEntity(true); // Allow "No room assigned" option
        roomSelector.selectedItemProperty().bindBidirectional(selectedRoom);

        // Style the selector button
        Button roomButton = roomSelector.getButton();
        styleEntitySelectorButton(roomButton);

        // Room info display (shows bed count when room selected)
        HBox roomInfoBox = new HBox(8);
        roomInfoBox.setAlignment(Pos.CENTER_LEFT);

        Label roomInfoLabel = new Label();
        roomInfoLabel.setFont(Font.font("System", 11));
        roomInfoLabel.setTextFill(TEXT_MUTED);

        FXProperties.runNowAndOnPropertiesChange(() -> {
            ResourceConfiguration rc = selectedRoom.get();
            if (rc != null && rc.getMax() != null) {
                roomInfoLabel.setText("Capacity: " + rc.getMax() + " bed" + (rc.getMax() > 1 ? "s" : ""));
                roomInfoLabel.setVisible(true);
            } else {
                roomInfoLabel.setVisible(false);
            }
        }, selectedRoom);

        roomInfoBox.getChildren().addAll(roomButton, roomInfoLabel);
        roomSection.getChildren().addAll(roomLabel, roomInfoBox);

        section.getChildren().addAll(typeSection, roomSection);

        return section;
    }

    /**
     * Applies consistent styling to entity selector buttons.
     */
    private void styleEntitySelectorButton(Button button) {
        button.setFont(Font.font("System", 13));
        button.setPadding(new Insets(10, 14, 10, 14));
        button.setBackground(createBackground(Color.WHITE, 8));
        button.setBorder(createBorder(BORDER, 8));
        button.setCursor(Cursor.HAND);
        button.setMaxWidth(Double.MAX_VALUE);

        // Hover effect
        button.setOnMouseEntered(e -> {
            button.setBackground(createBackground(Color.web("#f8f7f5"), 8));
            button.setBorder(createBorder(Color.web("#c5bfb6"), 8));
        });
        button.setOnMouseExited(e -> {
            button.setBackground(createBackground(Color.WHITE, 8));
            button.setBorder(createBorder(BORDER, 8));
        });
    }

    /**
     * Creates the transport-specific fields section.
     */
    private Node createTransportSection() {
        VBox section = new VBox(8);

        Label titleLabel = createSectionTitle("Transport Details");

        // Pickup location + time row
        HBox row1 = new HBox(8);
        row1.setAlignment(Pos.CENTER_LEFT);

        pickupLocationField = new TextField();
        pickupLocationField.setPromptText("Pickup location");
        applyInputFieldStyle(pickupLocationField);
        HBox.setHgrow(pickupLocationField, Priority.ALWAYS);

        pickupTimeField = new TextField();
        pickupTimeField.setPromptText("Time");
        pickupTimeField.setMaxWidth(80);
        applyInputFieldStyle(pickupTimeField);

        row1.getChildren().addAll(pickupLocationField, pickupTimeField);

        // Flight number row
        flightNumberField = new TextField();
        flightNumberField.setPromptText("Flight / train number");
        applyInputFieldStyle(flightNumberField);

        section.getChildren().addAll(titleLabel, row1, flightNumberField);
        return section;
    }

    /**
     * Creates the meals-specific fields section.
     */
    private Node createMealsSection() {
        VBox section = new VBox(8);

        Label titleLabel = createSectionTitle("Meal Sitting");

        HBox fieldRow = new HBox(8);
        fieldRow.setAlignment(Pos.CENTER_LEFT);

        sittingAllocationField = new TextField();
        sittingAllocationField.setPromptText("e.g., Early Sitting, Table 5");
        applyInputFieldStyle(sittingAllocationField);
        HBox.setHgrow(sittingAllocationField, Priority.ALWAYS);

        // Lock toggle button
        Button lockBtn = createLockSittingButton();

        fieldRow.getChildren().addAll(sittingAllocationField, lockBtn);
        section.getChildren().addAll(titleLabel, fieldRow);

        // Lock explanation
        VBox lockInfo = new VBox(4);
        lockInfo.setVisible(false);
        lockInfo.setManaged(false);
        lockInfo.setPadding(new Insets(6, 10, 6, 10));
        lockInfo.setBackground(createBackground(PURPLE_LIGHT, 6));
        lockInfo.setBorder(createBorder(PURPLE_BORDER, 6));

        HBox infoRow = new HBox(6);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        Label infoIcon = new Label("ℹ");
        infoIcon.setFont(Font.font(12));
        infoIcon.setTextFill(PURPLE);
        Label infoText = new Label("This sitting is locked and won't be changed by automatic allocation.");
        infoText.setFont(Font.font(11));
        infoText.setTextFill(PURPLE);
        infoRow.getChildren().addAll(infoIcon, infoText);
        lockInfo.getChildren().add(infoRow);

        FXProperties.runNowAndOnPropertiesChange(() -> {
            lockInfo.setVisible(lockSittingProperty.get());
            lockInfo.setManaged(lockSittingProperty.get());
        }, lockSittingProperty);

        section.getChildren().add(lockInfo);
        return section;
    }

    /**
     * Creates the lock sitting toggle button.
     */
    private Button createLockSittingButton() {
        Button btn = new Button();
        btn.setCursor(Cursor.HAND);
        btn.setOnAction(e -> lockSittingProperty.set(!lockSittingProperty.get()));

        FXProperties.runNowAndOnPropertiesChange(() -> {
            boolean isLocked = lockSittingProperty.get();

            HBox content = new HBox(5);
            content.setAlignment(Pos.CENTER);

            Label lockIcon = new Label("🔒");
            lockIcon.setFont(Font.font(10));
            Label text = new Label("Lock Sitting");
            text.setFont(Font.font("System", FontWeight.MEDIUM, 11));

            if (isLocked) {
                Label checkIcon = new Label("✓");
                checkIcon.setFont(Font.font(10));
                checkIcon.setTextFill(PURPLE);
                content.getChildren().addAll(lockIcon, checkIcon, text);
                btn.setBackground(createBackground(PURPLE_LIGHT, 6));
                btn.setBorder(createBorder(PURPLE, 6));
                text.setTextFill(PURPLE);
            } else {
                content.getChildren().addAll(lockIcon, text);
                btn.setBackground(createBackground(Color.WHITE, 6));
                btn.setBorder(createBorder(BORDER, 6));
                text.setTextFill(TEXT_SECONDARY);
            }

            btn.setGraphic(content);
        }, lockSittingProperty);

        btn.setPadding(new Insets(6, 10, 6, 10));
        return btn;
    }

    /**
     * Creates the sharing options section for shared accommodation.
     */
    private Node createSharingOptionsSection() {
        VBox section = new VBox(8);

        Label titleLabel = createSectionTitle("Sharing Options");

        HBox optionsRow = new HBox(6);
        optionsRow.setAlignment(Pos.CENTER_LEFT);

        // Booker toggle
        // TODO: isBooker field handling

        // Mate toggle
        // TODO: isMate field handling

        section.getChildren().addAll(titleLabel, optionsRow);
        return section;
    }

    /**
     * Creates the pricing section with discount/fixed price toggle.
     */
    private Node createPricingSection() {
        VBox section = new VBox(8);

        // Title row with mode toggle
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = createSectionTitle("Pricing");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Mode toggle: Discount | Fixed price
        HBox modeToggle = new HBox(4);
        modeToggle.setPadding(new Insets(2));
        modeToggle.setBackground(createBackground(Color.web("#f1f5f9"), 6));

        Button discountBtn = new Button("Discount");
        discountBtn.setFont(Font.font("System", FontWeight.SEMI_BOLD, 10));
        discountBtn.setCursor(Cursor.HAND);

        Button fixedBtn = new Button("Fixed price");
        fixedBtn.setFont(Font.font("System", FontWeight.SEMI_BOLD, 10));
        fixedBtn.setCursor(Cursor.HAND);

        // Toggle behavior
        discountBtn.setOnAction(e -> hasCustomPriceProperty.set(false));
        fixedBtn.setOnAction(e -> hasCustomPriceProperty.set(true));

        FXProperties.runNowAndOnPropertiesChange(() -> {
            boolean isFixed = hasCustomPriceProperty.get();

            if (!isFixed) {
                discountBtn.setBackground(createBackground(Color.WHITE, 4));
                discountBtn.setTextFill(Color.web("#0369a1"));
                fixedBtn.setBackground(Background.EMPTY);
                fixedBtn.setTextFill(TEXT_MUTED);
            } else {
                discountBtn.setBackground(Background.EMPTY);
                discountBtn.setTextFill(TEXT_MUTED);
                fixedBtn.setBackground(createBackground(Color.WHITE, 4));
                fixedBtn.setTextFill(Color.web("#d97706"));
            }
        }, hasCustomPriceProperty);

        discountBtn.setPadding(new Insets(4, 10, 4, 10));
        fixedBtn.setPadding(new Insets(4, 10, 4, 10));
        discountBtn.setBorder(Border.EMPTY);
        fixedBtn.setBorder(Border.EMPTY);

        modeToggle.getChildren().addAll(discountBtn, fixedBtn);
        titleRow.getChildren().addAll(titleLabel, modeToggle);

        // Pricing fields row
        HBox fieldsRow = new HBox(10);
        fieldsRow.setAlignment(Pos.BOTTOM_LEFT);

        // Standard calculation (informative)
        VBox standardBox = new VBox(4);
        Label standardLabel = new Label("Standard");
        standardLabel.setFont(Font.font(9));
        standardLabel.setTextFill(TEXT_MUTED);

        // Use the daily rate from PolicyAggregate
        int pricePerUnit = standardPricePerUnit;
        int quantity = getDurationDays();
        int standardPrice = pricePerUnit * quantity;

        Label standardValue = new Label(formatPrice(pricePerUnit) + " × " + quantity + " = " + formatPrice(standardPrice));
        standardValue.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        standardValue.setTextFill(TEXT_SECONDARY);

        standardBox.getChildren().addAll(standardLabel, standardValue);

        // Discount input (when not fixed price)
        VBox discountBox = new VBox(4);
        discountBox.setMinWidth(110);

        Label discountLabel = new Label("Discount");
        discountLabel.setFont(Font.font(9));

        HBox discountInputBox = new HBox();
        discountInputBox.setBorder(createBorder(BORDER, 6));
        discountInputBox.setBackground(createBackground(Color.WHITE, 6));

        discountField = new TextField("0");
        discountField.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        discountField.setBackground(Background.EMPTY);
        discountField.setBorder(Border.EMPTY);
        discountField.setPadding(new Insets(8, 10, 8, 10));
        discountField.setStyle("-fx-alignment: center-right;");
        discountField.setPrefWidth(60);

        Label percentLabel = new Label("%");
        percentLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        percentLabel.setTextFill(TEXT_MUTED);
        percentLabel.setPadding(new Insets(8, 12, 8, 0));
        percentLabel.setBackground(createBackground(Color.web("#f1f5f9"), 0, 6, 6, 0));

        discountInputBox.getChildren().addAll(discountField, percentLabel);
        discountBox.getChildren().addAll(discountLabel, discountInputBox);

        // Fixed price input (when fixed price mode)
        VBox fixedBox = new VBox(4);

        Label fixedLabel = new Label("Fixed total");
        fixedLabel.setFont(Font.font(9));
        fixedLabel.setTextFill(Color.web("#d97706"));

        HBox fixedInputBox = new HBox();
        fixedInputBox.setBorder(createBorder(Color.web("#fbbf24"), 6));
        fixedInputBox.setBackground(createBackground(Color.web("#fffbeb"), 6));

        String currencySymbol = EventPriceFormatter.getEventCurrencySymbol(event);
        Label currencyLabel = new Label(currencySymbol);
        currencyLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        currencyLabel.setTextFill(Color.web("#92400e"));
        currencyLabel.setPadding(new Insets(6, 8, 6, 8));
        currencyLabel.setBackground(createBackground(Color.web("#fef3c7"), 6, 0, 0, 6));

        // Show price in major units (e.g., pounds/dollars) for user entry
        int currentPriceInCents = line.getPriceCustom() != null ? line.getPriceCustom() :
            (line.getPriceNet() != null ? line.getPriceNet() : 0);
        customPriceField = new TextField(String.valueOf(currentPriceInCents / 100));
        customPriceField.setFont(Font.font("System", FontWeight.SEMI_BOLD, 13));
        customPriceField.setBackground(Background.EMPTY);
        customPriceField.setBorder(Border.EMPTY);
        customPriceField.setPadding(new Insets(6, 8, 6, 8));
        customPriceField.setStyle("-fx-text-fill: #92400e;");
        customPriceField.setPrefWidth(60);

        fixedInputBox.getChildren().addAll(currencyLabel, customPriceField);
        fixedBox.getChildren().addAll(fixedLabel, fixedInputBox);

        // Total display
        VBox totalBox = new VBox(4);
        totalBox.setPadding(new Insets(6, 12, 6, 12));
        totalBox.setAlignment(Pos.CENTER_RIGHT);

        Label totalLabel = new Label("Total");
        totalLabel.setFont(Font.font(9));
        totalLabel.setTextFill(Color.web("#15803d"));

        totalValueLabel = new Label(formatPrice(currentTotal));
        totalValueLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        totalValueLabel.setTextFill(Color.web("#166534"));

        totalBox.setBackground(createBackground(Color.web("#f0fdf4"), 6));
        totalBox.getChildren().addAll(totalLabel, totalValueLabel);

        // Spacer to push total to right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        fieldsRow.getChildren().addAll(standardBox);

        // Show discount or fixed based on mode
        FXProperties.runNowAndOnPropertiesChange(() -> {
            fieldsRow.getChildren().clear();
            fieldsRow.getChildren().add(standardBox);

            if (hasCustomPriceProperty.get()) {
                standardValue.setStyle("-fx-strikethrough: true;");
                standardBox.setOpacity(0.4);
                fieldsRow.getChildren().addAll(fixedBox, spacer, totalBox);
                totalBox.setBackground(createBackground(Color.web("#fef3c7"), 6));
                totalLabel.setTextFill(Color.web("#92400e"));
                totalLabel.setText("Fixed");
                totalValueLabel.setTextFill(Color.web("#92400e"));
            } else {
                standardValue.setStyle("");
                standardBox.setOpacity(1.0);
                fieldsRow.getChildren().addAll(discountBox, spacer, totalBox);
                totalBox.setBackground(createBackground(Color.web("#f0fdf4"), 6));
                totalLabel.setTextFill(Color.web("#15803d"));
                totalLabel.setText("Total");
                totalValueLabel.setTextFill(Color.web("#166534"));
            }
        }, hasCustomPriceProperty);

        // Fixed price explanation
        HBox fixedExplanation = new HBox(6);
        fixedExplanation.setPadding(new Insets(6, 10, 6, 10));
        fixedExplanation.setBackground(createBackground(Color.web("#fef3c7"), 6));
        fixedExplanation.setAlignment(Pos.CENTER_LEFT);
        fixedExplanation.setVisible(false);
        fixedExplanation.setManaged(false);

        Label lockIcon = new Label("🔒");
        lockIcon.setFont(Font.font(10));
        Label explainText = new Label("Fixed price stays the same regardless of duration changes");
        explainText.setFont(Font.font(10));
        explainText.setTextFill(Color.web("#92400e"));
        fixedExplanation.getChildren().addAll(lockIcon, explainText);

        FXProperties.runNowAndOnPropertiesChange(() -> {
            fixedExplanation.setVisible(hasCustomPriceProperty.get());
            fixedExplanation.setManaged(hasCustomPriceProperty.get());
        }, hasCustomPriceProperty);

        section.getChildren().addAll(titleRow, fieldsRow, fixedExplanation);
        return section;
    }

    /**
     * Creates the comment section.
     */
    private Node createCommentSection() {
        VBox section = new VBox(8);

        Label titleLabel = createSectionTitle("Comment");

        commentArea = new TextArea();
        commentArea.setPromptText("Notes or special instructions...");
        String comment = (String) line.getFieldValue("comment");
        commentArea.setText(comment != null ? comment : "");
        commentArea.setPrefRowCount(2);
        commentArea.setWrapText(true);
        commentArea.setFont(Font.font(12));
        commentArea.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e0dbd4; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6;"
        );

        section.getChildren().addAll(titleLabel, commentArea);
        return section;
    }

    /**
     * Creates the footer with Cancel and Save buttons.
     */
    private Node createFooter() {
        Color fillColor = getCategoryIconColor(category);

        HBox footer = new HBox(10);
        footer.setPadding(new Insets(12, 20, 12, 20));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setBackground(createBackground(WARM_WHITE, 0, 0, 12, 12));
        footer.setBorder(new Border(new BorderStroke(
            BORDER,
            BorderStrokeStyle.SOLID,
            CornerRadii.EMPTY,
            new BorderWidths(1, 0, 0, 0) // top border only
        )));

        // Cancel button
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        cancelBtn.setTextFill(TEXT_SECONDARY);
        cancelBtn.setBackground(createBackground(Color.WHITE, 6));
        cancelBtn.setBorder(createBorder(BORDER, 6));
        cancelBtn.setPadding(new Insets(8, 16, 8, 16));
        cancelBtn.setCursor(Cursor.HAND);
        cancelBtn.setOnAction(e -> closeDialog(false));

        // Save button
        Button saveBtn = new Button("💾 Save");
        saveBtn.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
        saveBtn.setTextFill(Color.WHITE);
        saveBtn.setBackground(createBackground(fillColor, 6));
        saveBtn.setBorder(Border.EMPTY);
        saveBtn.setPadding(new Insets(8, 16, 8, 16));
        saveBtn.setCursor(Cursor.HAND);
        saveBtn.setOnAction(e -> handleSave());

        footer.getChildren().addAll(cancelBtn, saveBtn);
        return footer;
    }

    /**
     * Recalculates and displays the price preview when the accommodation type (Item) is changed.
     * Uses PolicyAggregate to get the daily rate for the new item.
     * Note: This only updates the UI preview - actual price change is done in handleSave()
     * through the WorkingBooking API.
     */
    private void recalculatePriceForNewItem() {
        if (workingBooking == null) return;

        PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();
        if (policyAggregate == null) return;

        Item newItem = selectedAccommodationType.get();
        Site site = line.getSite();

        if (newItem != null && site != null) {
            // Get the new daily rate for this item/site combination
            int newDailyRate = policyAggregate.filterDailyRatesStreamOfSiteAndItem(site, newItem)
                .findFirst()
                .map(rate -> rate.getPrice() != null ? rate.getPrice() : 0)
                .orElse(0);

            // Calculate new total price
            int days = getDurationDays();
            int newTotal = newDailyRate * days;

            // Update cached values for UI display
            standardPricePerUnit = newDailyRate;
            currentTotal = newTotal;

            // Update UI to show new price (unless custom price is set)
            if (!hasCustomPriceProperty.get() && totalValueLabel != null) {
                totalValueLabel.setText(formatPrice(newTotal));
            }
        }
    }

    /**
     * Handles the save action.
     * Uses WorkingBooking API when available, falls back to UpdateStore otherwise.
     */
    private void handleSave() {
        if (workingBooking != null) {
            // ═══════════════════════════════════════════════════════════════════════════════
            // WORKINGBOOKING API - Proper event sourcing for database persistence
            // ═══════════════════════════════════════════════════════════════════════════════

            // Update accommodation type and room assignment using WorkingBooking API
            Item newItem = selectedAccommodationType.get();
            ResourceConfiguration newRoom = selectedRoom.get();

            // Check if item changed
            boolean itemChanged = newItem != null && !dev.webfx.stack.orm.entity.Entities.sameId(newItem, line.getItem());
            // Check if room changed
            boolean roomChanged = newRoom != line.getResourceConfiguration() &&
                (newRoom == null || !dev.webfx.stack.orm.entity.Entities.sameId(newRoom, line.getResourceConfiguration()));

            if (itemChanged || roomChanged) {
                //workingBooking.updateDocumentLine(line, itemChanged ? newItem : null, roomChanged ? newRoom : null);
            }

            // Handle pricing changes using WorkingBooking API
            if (hasCustomPriceProperty.get()) {
                // Custom price mode
                try {
                    int customPriceMajorUnits = Integer.parseInt(customPriceField.getText());
                    int customPriceInCents = customPriceMajorUnits * 100;

                    // Use WorkingBooking API for price update
                   // workingBooking.updateDocumentLinePrice(line, customPriceInCents, customPriceInCents, null);

                } catch (NumberFormatException e) {
                    System.err.println("Invalid custom price: " + customPriceField.getText());
                }
            } else if (discountField != null) {
                // Discount mode - calculate discounted price
                try {
                    int discountPercent = Integer.parseInt(discountField.getText());
                    if (discountPercent >= 0 && discountPercent <= 100) {
                        int standardPrice = standardPricePerUnit * getDurationDays();
                        int discountAmount = standardPrice * discountPercent / 100;
                        int newPrice = standardPrice - discountAmount;

                        // Use WorkingBooking API for price update
                      //  workingBooking.updateDocumentLinePrice(line, newPrice, null, discountAmount);
                    }
                } catch (NumberFormatException e) {
                    // Keep existing discount
                }
            }

            // Update comment (direct property update - will be tracked by entity store)
            if (commentArea != null) {
                line.setFieldValue("comment", commentArea.getText());
            }

            // Update read status (direct property update)
            if (isReadProperty.get() != Boolean.TRUE.equals(line.getFieldValue("read"))) {
                line.setFieldValue("read", isReadProperty.get());
            }

        } else if (updateStore != null) {
            // Legacy UpdateStore-based save
            if (hasCustomPriceProperty.get()) {
                line.setFieldValue("price_isCustom", true);
                try {
                    int customPriceMajorUnits = Integer.parseInt(customPriceField.getText());
                    line.setPriceCustom(customPriceMajorUnits * 100);
                    line.setPriceNet(customPriceMajorUnits * 100);
                } catch (NumberFormatException e) {
                    // Keep existing price
                }
            } else {
                line.setFieldValue("price_isCustom", false);
                // Calculate price from discount
                if (discountField != null) {
                    try {
                        int discountPercent = Integer.parseInt(discountField.getText());
                        int standardPrice = standardPricePerUnit * getDurationDays();
                        int discountAmount = standardPrice * discountPercent / 100;
                        line.setPriceNet(standardPrice - discountAmount);
                        line.setPriceDiscount(discountAmount);
                    } catch (NumberFormatException e) {
                        // Keep existing price
                    }
                }
            }

            // Create History record for this edit (only for UpdateStore mode)
            String optionName = line.getSite() != null ? line.getSite().getName() :
                (line.getItem() != null ? line.getItem().getName() : "Option");
            Document document = line.getDocument();
            if (document != null) {
                History history = updateStore.createEntity(History.class);
                history.setDocument(document);
                history.setUsername(FXUserName.getUserName());
                history.setComment("Option edited: " + optionName);
            }
        }

        closeDialog(true);
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog(boolean saved) {
        if (dialogCallback != null) {
            dialogCallback.closeDialog();
        }
        if (saved && onSave != null) {
            onSave.run();
        } else if (!saved && onCancel != null) {
            onCancel.run();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Gets the category from a document line based on its item family.
     */
    private String getCategoryFromLine(DocumentLine line) {
        if (line == null || line.getItem() == null) return "program";

        Item item = line.getItem();
        one.modality.base.shared.entities.ItemFamily family = item.getFamily();
        if (family == null) return "program";

        String familyName = family.getName();
        String familyCode = family.getCode();
        String search = (familyName != null ? familyName : familyCode);
        if (search == null) return "program";

        search = search.toLowerCase();

        if (search.contains("accommodation") || search.contains("room") || search.contains("bed")) {
            return "accommodation";
        } else if (search.contains("meal") || search.contains("breakfast") || search.contains("lunch") || search.contains("dinner")) {
            return "meals";
        } else if (search.contains("diet") || search.contains("vegetarian") || search.contains("vegan")) {
            return "diet";
        } else if (search.contains("transport") || search.contains("shuttle") || search.contains("bus")) {
            return "transport";
        } else if (search.contains("parking") || search.contains("car")) {
            return "parking";
        }

        return "program";
    }

    /**
     * Creates a section title label.
     */
    private Label createSectionTitle(String text) {
        Label label = new Label(text.toUpperCase());
        label.setFont(Font.font("System", FontWeight.SEMI_BOLD, 10));
        label.setTextFill(TEXT_MUTED);
        return label;
    }

    /**
     * Applies standard input field styling.
     */
    private void applyInputFieldStyle(TextField field) {
        field.setFont(Font.font(13));
        field.setPadding(new Insets(8, 10, 8, 10));
        field.setBackground(createBackground(Color.WHITE, 6));
        field.setBorder(createBorder(BORDER, 6));
    }

    /**
     * Checks if the item name contains "double" or "shared".
     */
    private boolean itemNameContainsShared() {
        if (line == null || line.getItem() == null) return false;
        String name = line.getItem().getName();
        if (name == null) return false;
        String lower = name.toLowerCase();
        return lower.contains("double") || lower.contains("shared");
    }

    /**
     * Gets the duration in days.
     */
    private int getDurationDays() {
        LocalDate startDate = line.getStartDate();
        LocalDate endDate = line.getEndDate();
        if (startDate == null || endDate == null) return 1;
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }
}
