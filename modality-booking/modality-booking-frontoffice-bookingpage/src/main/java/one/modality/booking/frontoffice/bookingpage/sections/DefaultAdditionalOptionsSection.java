package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.platform.console.Console;
import javafx.beans.property.*;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of the additional options section.
 * Displays additional services like parking, shuttle, and accessibility options.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Assisted listening checkbox (typically free)</li>
 *   <li>Parking checkbox with price per day</li>
 *   <li>Parking type selection (standard/handicap)</li>
 *   <li>Shuttle checkboxes (outbound/return)</li>
 * </ul>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-additional-options-section} - section container</li>
 *   <li>{@code .bookingpage-option-checkbox} - option checkbox card</li>
 *   <li>{@code .bookingpage-option-checkbox.selected} - selected state</li>
 *   <li>{@code .bookingpage-parking-type} - parking type option</li>
 *   <li>{@code .bookingpage-parking-type.selected} - selected parking type</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasAdditionalOptionsSection
 */
public class DefaultAdditionalOptionsSection implements HasAdditionalOptionsSection {

    // === COLOR SCHEME ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // === OPTIONS SELECTION ===
    protected final BooleanProperty assistedListening = new SimpleBooleanProperty(false);
    protected final BooleanProperty needsParking = new SimpleBooleanProperty(false);
    protected final ObjectProperty<ParkingType> parkingType = new SimpleObjectProperty<>(ParkingType.STANDARD);
    protected final BooleanProperty shuttleOutbound = new SimpleBooleanProperty(false);
    protected final BooleanProperty shuttleReturn = new SimpleBooleanProperty(false);

    // === PRICING ===
    protected int parkingPricePerDay = 500; // Default $5
    protected int shuttlePrice = 2500; // Default $25
    protected int daysCount = 1;

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected HBox parkingTypeSection;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

    public DefaultAdditionalOptionsSection() {
        buildUI();
        setupBindings();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_LEFT);
        container.setSpacing(12);
        container.getStyleClass().add("bookingpage-additional-options-section");

        // Section header
        HBox sectionHeader = new StyledSectionHeader(BookingPageI18nKeys.AdditionalOptions, StyledSectionHeader.ICON_PLUS_CIRCLE);

        // Assisted listening option
        VBox assistedListeningCard = createOptionCard(
            BookingPageI18nKeys.AssistedListeningDevice,
            BookingPageI18nKeys.HearingAssistanceAvailable,
            assistedListening,
            BookingPageI18nKeys.Free,
            null
        );

        // Parking option with nested type selection
        VBox parkingCard = createParkingCard();

        // Shuttle options
        VBox shuttleCard = createShuttleCard();

        container.getChildren().addAll(sectionHeader, assistedListeningCard, parkingCard, shuttleCard);
        VBox.setMargin(sectionHeader, new Insets(0, 0, 8, 0));
    }

    protected VBox createOptionCard(Object titleKey, Object subtitleKey, BooleanProperty selectedProperty,
                                    Object priceKey, String priceText) {
        VBox card = new VBox(0);
        card.getStyleClass().add("bookingpage-option-checkbox");

        // Apply border styling in Java per project conventions
        BookingFormColorScheme scheme = colorScheme.get();
        if (scheme == null) scheme = BookingFormColorScheme.DEFAULT;
        final BookingFormColorScheme finalScheme = scheme;

        // Initial style
        updateOptionCardStyle(card, selectedProperty.get(), finalScheme);

        HBox mainRow = new HBox(12);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(16));
        mainRow.setCursor(Cursor.HAND);

        // Checkbox indicator
        StackPane checkbox = BookingPageUIBuilder.createCheckboxIndicator(selectedProperty, colorScheme);

        // Text content
        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = I18nControls.newLabel(titleKey);
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #212529;");

        Label subtitle = I18nControls.newLabel(subtitleKey);
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        textContent.getChildren().addAll(title, subtitle);

        // Price
        Label price;
        if (priceText != null) {
            price = new Label(priceText);
        } else {
            price = I18nControls.newLabel(priceKey);
        }
        price.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #212529;");

        mainRow.getChildren().addAll(checkbox, textContent, price);
        card.getChildren().add(mainRow);

        // Selection handling - update styling and CSS class
        selectedProperty.addListener((obs, old, newVal) -> {
            updateOptionCardStyle(card, newVal, finalScheme);
            if (newVal) {
                if (!card.getStyleClass().contains("selected")) {
                    card.getStyleClass().add("selected");
                }
            } else {
                card.getStyleClass().remove("selected");
            }
        });

        mainRow.setOnMouseClicked(e -> selectedProperty.set(!selectedProperty.get()));

        return card;
    }

    /**
     * Updates the option card style based on selection state.
     */
    private void updateOptionCardStyle(VBox card, boolean selected, BookingFormColorScheme scheme) {
        if (selected) {
            card.setStyle("-fx-border-color: " + toHex(scheme.getPrimary()) + "; -fx-border-width: 2; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: " + toHex(scheme.getSelectedBg()) + ";");
        } else {
            card.setStyle("-fx-border-color: #dee2e6; -fx-border-width: 2; " +
                "-fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: white;");
        }
    }

    protected VBox createParkingCard() {
        VBox card = new VBox(0);
        card.getStyleClass().add("bookingpage-option-checkbox");

        // Main parking checkbox row
        HBox mainRow = new HBox(12);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(16));
        mainRow.setCursor(Cursor.HAND);

        StackPane checkbox = BookingPageUIBuilder.createCheckboxIndicator(needsParking, colorScheme);

        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = I18nControls.newLabel(BookingPageI18nKeys.ParkingPass);
        title.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-medium");

        Label subtitle = I18nControls.newLabel(BookingPageI18nKeys.LimitedParkingAvailable);
        subtitle.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        textContent.getChildren().addAll(title, subtitle);

        Label price = new Label(formatPrice(parkingPricePerDay) + "/day");
        price.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold");

        mainRow.getChildren().addAll(checkbox, textContent, price);

        // Parking type selection (hidden when parking not selected)
        parkingTypeSection = buildParkingTypeSection();
        parkingTypeSection.setVisible(false);
        parkingTypeSection.setManaged(false);

        card.getChildren().addAll(mainRow, parkingTypeSection);

        // Selection handling
        if (needsParking.get()) {
            card.getStyleClass().add("selected");
        }
        needsParking.addListener((obs, old, newVal) -> {
            if (newVal) {
                card.getStyleClass().add("selected");
            } else {
                card.getStyleClass().remove("selected");
            }
        });

        mainRow.setOnMouseClicked(e -> needsParking.set(!needsParking.get()));

        return card;
    }

    protected HBox buildParkingTypeSection() {
        HBox section = new HBox(12);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setPadding(new Insets(0, 16, 16, 48)); // Indented under checkbox

        Label label = I18nControls.newLabel(BookingPageI18nKeys.SelectParkingType);
        label.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        // Standard option
        HBox standardOption = createParkingTypeOption(BookingPageI18nKeys.Standard, ParkingType.STANDARD);

        // Handicap option
        HBox handicapOption = createParkingTypeOption(BookingPageI18nKeys.Handicap, ParkingType.HANDICAP);

        section.getChildren().addAll(label, standardOption, handicapOption);
        return section;
    }

    protected HBox createParkingTypeOption(Object labelKey, ParkingType type) {
        HBox option = new HBox(6);
        option.setAlignment(Pos.CENTER_LEFT);
        option.setPadding(new Insets(6, 12, 6, 12));
        option.setCursor(Cursor.HAND);
        option.getStyleClass().add("bookingpage-parking-type");

        Label label = I18nControls.newLabel(labelKey);
        label.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-font-medium");

        option.getChildren().add(label);

        // Selection handling
        if (parkingType.get() == type) {
            option.getStyleClass().add("selected");
        }
        parkingType.addListener((obs, old, newVal) -> {
            if (newVal == type) {
                option.getStyleClass().add("selected");
            } else {
                option.getStyleClass().remove("selected");
            }
        });

        option.setOnMouseClicked(e -> parkingType.set(type));

        return option;
    }

    protected VBox createShuttleCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-border-color: #dee2e6; -fx-border-width: 2; " +
            "-fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: white;");
        card.getStyleClass().add("bookingpage-shuttle-section");

        // Header with plane icon
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Plane SVG icon
        SVGPath planeIcon = new SVGPath();
        planeIcon.setContent(BookingPageUIBuilder.ICON_PLANE);
        planeIcon.setStroke(Color.web("#3b82f6"));
        planeIcon.setStrokeWidth(2);
        planeIcon.setFill(Color.TRANSPARENT);
        planeIcon.setScaleX(0.85);
        planeIcon.setScaleY(0.85);

        Label header = I18nControls.newLabel(BookingPageI18nKeys.AirportShuttle);
        header.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #212529;");

        headerRow.getChildren().addAll(planeIcon, header);

        // Shuttle options container
        VBox shuttleOptionsContainer = new VBox(8);

        // Outbound shuttle
        HBox outboundCard = createShuttleOptionRow(
            BookingPageI18nKeys.ShuttleToVenue,
            shuttleOutbound,
            formatPrice(shuttlePrice)
        );

        // Return shuttle
        HBox returnCard = createShuttleOptionRow(
            BookingPageI18nKeys.ShuttleFromVenue,
            shuttleReturn,
            formatPrice(shuttlePrice)
        );

        shuttleOptionsContainer.getChildren().addAll(outboundCard, returnCard);

        card.getChildren().addAll(headerRow, shuttleOptionsContainer);
        return card;
    }

    protected HBox createShuttleOptionRow(Object labelKey, BooleanProperty selectedProperty, String priceText) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setCursor(Cursor.HAND);
        row.getStyleClass().add("bookingpage-option-checkbox");

        // Apply border styling in Java
        BookingFormColorScheme scheme = colorScheme.get();
        if (scheme == null) scheme = BookingFormColorScheme.DEFAULT;
        final BookingFormColorScheme finalScheme = scheme;

        updateShuttleRowStyle(row, selectedProperty.get(), finalScheme);

        StackPane checkbox = BookingPageUIBuilder.createCheckboxIndicator(selectedProperty, colorScheme);

        // Label with flex grow
        Label label = I18nControls.newLabel(labelKey);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #212529;");
        HBox.setHgrow(label, Priority.ALWAYS);

        // Price label
        Label priceLabel = new Label(priceText);
        priceLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #212529;");

        row.getChildren().addAll(checkbox, label, priceLabel);

        // Selection handling - update styling
        selectedProperty.addListener((obs, old, newVal) -> {
            updateShuttleRowStyle(row, newVal, finalScheme);
            if (newVal) {
                if (!row.getStyleClass().contains("selected")) {
                    row.getStyleClass().add("selected");
                }
            } else {
                row.getStyleClass().remove("selected");
            }
        });

        row.setOnMouseClicked(e -> selectedProperty.set(!selectedProperty.get()));

        return row;
    }

    /**
     * Updates the shuttle row style based on selection state.
     */
    private void updateShuttleRowStyle(HBox row, boolean selected, BookingFormColorScheme scheme) {
        if (selected) {
            row.setStyle("-fx-border-color: " + toHex(scheme.getPrimary()) + "; -fx-border-width: 1; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: " + toHex(scheme.getSelectedBg()) + ";");
        } else {
            row.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: #f9fafb;");
        }
    }

    protected void setupBindings() {
        // Show/hide parking type section based on parking selection
        needsParking.addListener((obs, old, newVal) -> {
            parkingTypeSection.setVisible(newVal);
            parkingTypeSection.setManaged(newVal);
        });
    }

    protected String formatPrice(int priceInCents) {
        return "$" + (priceInCents / 100);
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.AdditionalOptions;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
    }

    // ========================================
    // HasAdditionalOptionsSection INTERFACE
    // ========================================

    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public void setParkingPricePerDay(int price) {
        this.parkingPricePerDay = price;
    }

    @Override
    public void setShuttlePrice(int price) {
        this.shuttlePrice = price;
    }

    @Override
    public void setDaysCount(int count) {
        this.daysCount = count;
    }

    @Override
    public BooleanProperty assistedListeningProperty() {
        return assistedListening;
    }

    @Override
    public BooleanProperty needsParkingProperty() {
        return needsParking;
    }

    @Override
    public ObjectProperty<ParkingType> parkingTypeProperty() {
        return parkingType;
    }

    @Override
    public BooleanProperty shuttleOutboundProperty() {
        return shuttleOutbound;
    }

    @Override
    public BooleanProperty shuttleReturnProperty() {
        return shuttleReturn;
    }

    @Override
    public int getTotalParkingCost() {
        if (needsParking.get()) {
            return parkingPricePerDay * daysCount;
        }
        return 0;
    }

    @Override
    public int getTotalShuttleCost() {
        int total = 0;
        if (shuttleOutbound.get()) {
            total += shuttlePrice;
        }
        if (shuttleReturn.get()) {
            total += shuttlePrice;
        }
        return total;
    }

    // ========================================
    // DATA POPULATION FROM POLICY AGGREGATE
    // ========================================

    /**
     * Populates additional options prices from PolicyAggregate data.
     * Looks for PARKING and TRANSPORT family items and extracts their rates.
     *
     * @param policyAggregate the policy data containing scheduledItems and rates
     */
    public void populateFromPolicyAggregate(PolicyAggregate policyAggregate) {
        if (policyAggregate == null) {
            Console.log("DefaultAdditionalOptionsSection: PolicyAggregate is null, using default prices");
            return;
        }

        // Get PARKING items
        List<ScheduledItem> parkingItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.PARKING);
        Console.log("DefaultAdditionalOptionsSection: Found " + parkingItems.size() + " parking scheduled items");

        // Extract parking price from first item
        if (!parkingItems.isEmpty()) {
            Map<Item, List<ScheduledItem>> parkingItemMap = parkingItems.stream()
                .filter(si -> si.getItem() != null)
                .collect(Collectors.groupingBy(ScheduledItem::getItem));

            for (Item item : parkingItemMap.keySet()) {
                int dailyRate = policyAggregate.filterDailyRatesStreamOfSiteAndItem(null, item)
                    .findFirst()
                    .map(Rate::getPrice)
                    .orElse(0);

                if (dailyRate > 0) {
                    setParkingPricePerDay(dailyRate);
                    Console.log("DefaultAdditionalOptionsSection: Set parking price to " + dailyRate);
                    break;
                }
            }
        }

        // Get TRANSPORT (shuttle) items
        List<ScheduledItem> transportItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.TRANSPORT);
        Console.log("DefaultAdditionalOptionsSection: Found " + transportItems.size() + " transport scheduled items");

        // Extract shuttle price from first item
        if (!transportItems.isEmpty()) {
            Map<Item, List<ScheduledItem>> transportItemMap = transportItems.stream()
                .filter(si -> si.getItem() != null)
                .collect(Collectors.groupingBy(ScheduledItem::getItem));

            for (Item item : transportItemMap.keySet()) {
                int rate = policyAggregate.filterDailyRatesStreamOfSiteAndItem(null, item)
                    .findFirst()
                    .map(Rate::getPrice)
                    .orElse(0);

                if (rate > 0) {
                    setShuttlePrice(rate);
                    Console.log("DefaultAdditionalOptionsSection: Set shuttle price to " + rate);
                    break;
                }
            }
        }

        // Rebuild UI to reflect new prices
        rebuildUI();
    }

    /**
     * Rebuilds the UI to reflect updated prices.
     */
    protected void rebuildUI() {
        container.getChildren().clear();
        buildUI();
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Converts a JavaFX Color to a CSS hex string.
     * @param color the Color to convert
     * @return hex string like "#RRGGBB"
     */
    protected static String toHex(Color color) {
        if (color == null) return "#000000";
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
}
