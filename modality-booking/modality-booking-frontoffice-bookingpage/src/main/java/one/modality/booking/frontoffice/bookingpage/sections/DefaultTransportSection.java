package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.platform.console.Console;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ItemPolicy;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of the transport section combining Parking and Shuttle options.
 * Displays both parking choices and airport shuttle options in a single cohesive UI.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Section header with transport icon</li>
 *   <li>Parking options displayed as checkbox cards</li>
 *   <li>Shuttle options in a grouped container with outbound/return trips</li>
 *   <li>Shuttle trips enabled/disabled based on arrival/departure date matching</li>
 *   <li>Updates total prices when selections change</li>
 * </ul>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-transport-section} - section container</li>
 *   <li>{@code .bookingpage-checkbox-card} - parking option card</li>
 *   <li>{@code .bookingpage-shuttle-container} - grouped shuttle container</li>
 *   <li>{@code .bookingpage-shuttle-trip} - individual shuttle trip card</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasTransportSection
 */
public class DefaultTransportSection implements HasTransportSection {

    // === COLOR SCHEME ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // === DATE PROPERTIES (for shuttle availability) ===
    protected final ObjectProperty<LocalDate> arrivalDateProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<LocalDate> departureDateProperty = new SimpleObjectProperty<>();

    // === OPTIONS ===
    protected final List<ParkingOption> parkingOptions = new ArrayList<>();
    protected final List<ShuttleOption> shuttleOptions = new ArrayList<>();

    // === UNIFIED PARKING CARD STATE ===
    /** Main checkbox state - whether user wants parking */
    protected final BooleanProperty parkingEnabledProperty = new SimpleBooleanProperty(false);
    /** Currently selected parking type (e.g., Standard, Handicap) */
    protected final ObjectProperty<ParkingOption> selectedParkingTypeProperty = new SimpleObjectProperty<>();

    // === CONFIGURATION ===
    protected int daysCount = 1;
    /** Threshold for "Limited" availability status */
    protected static final int LIMITED_THRESHOLD = 5;

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected VBox parkingContainer;
    protected VBox shuttleGroupContainer;
    protected VBox shuttleTripsContainer;
    protected Label shuttleTotalPriceLabel;
    protected HBox shuttleHeaderPriceBox;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;
    protected PolicyAggregate policyAggregate;

    // === CALLBACKS ===
    protected Runnable onSelectionChangedCallback;

    public DefaultTransportSection() {
        // Set up date change listeners to update shuttle availability
        arrivalDateProperty.addListener((obs, old, newDate) -> updateShuttleAvailability());
        departureDateProperty.addListener((obs, old, newDate) -> updateShuttleAvailability());

        // Set up parking enabled/selection logic
        setupParkingSelectionLogic();

        buildUI();
    }

    /**
     * Sets up the parking selection logic:
     * - When parking enabled, auto-select default or first available option
     * - When parking disabled, clear selection and deselect all options
     * - When parking type changes, update individual option selected states
     */
    protected void setupParkingSelectionLogic() {
        parkingEnabledProperty.addListener((obs, wasEnabled, isEnabled) -> {
            if (!isEnabled) {
                // Parking disabled - clear selection
                selectedParkingTypeProperty.set(null);
                parkingOptions.forEach(opt -> opt.setSelected(false));
            } else if (isEnabled && selectedParkingTypeProperty.get() == null) {
                // Parking enabled with no selection - auto-select default or first available
                ParkingOption defaultOpt = getDefaultParkingOption();
                if (defaultOpt == null) {
                    defaultOpt = getFirstAvailableParkingOption();
                }
                if (defaultOpt != null) {
                    selectedParkingTypeProperty.set(defaultOpt);
                }
            }
            if (onSelectionChangedCallback != null) {
                onSelectionChangedCallback.run();
            }
        });

        selectedParkingTypeProperty.addListener((obs, oldOpt, newOpt) -> {
            // Update individual option selected states based on which one is selected
            parkingOptions.forEach(opt -> opt.setSelected(opt == newOpt));
            if (onSelectionChangedCallback != null) {
                onSelectionChangedCallback.run();
            }
        });
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_LEFT);
        container.setSpacing(16);
        container.getStyleClass().add("bookingpage-transport-section");

        // Section header
        HBox sectionHeader = new StyledSectionHeader(BookingPageI18nKeys.Transport, StyledSectionHeader.ICON_CAR);

        // Parking container
        parkingContainer = new VBox(12);
        parkingContainer.setAlignment(Pos.TOP_LEFT);
        buildParkingCards();

        // Shuttle grouped container
        shuttleGroupContainer = buildShuttleGroupContainer();

        container.getChildren().addAll(sectionHeader, parkingContainer, shuttleGroupContainer);
        VBox.setMargin(sectionHeader, new Insets(0, 0, 8, 0));

        // Update visibility based on available options
        updateVisibility();
    }

    // ========================================
    // PARKING UI - UNIFIED CARD WITH RADIO PILLS
    // ========================================

    /**
     * Builds the unified parking card with main checkbox and radio pill options.
     * Supports three variants:
     * 1. All available - main checkbox enabled, all radio pills selectable
     * 2. Partial sold out - main checkbox enabled, some radio pills disabled with "Sold Out" badge
     * 3. All sold out - entire card disabled with corner ribbon
     */
    protected void buildParkingCards() {
        parkingContainer.getChildren().clear();

        if (parkingOptions.isEmpty()) {
            return;
        }

        StackPane unifiedCard = createUnifiedParkingCard();
        parkingContainer.getChildren().add(unifiedCard);
    }

    /**
     * Creates the unified parking card containing:
     * - Main checkbox row (enable/disable parking)
     * - Sub-section with radio pills (parking type selection)
     * - Sold out ribbon if all options sold out
     */
    protected StackPane createUnifiedParkingCard() {
        boolean allSoldOut = areAllParkingOptionsSoldOut();

        // Use StackPane to allow ribbon overlay
        StackPane cardWrapper = new StackPane();

        VBox card = new VBox(0);
        card.getStyleClass().add("bookingpage-checkbox-card");

        if (allSoldOut) {
            card.getStyleClass().add("soldout");
        }

        // Main checkbox row
        HBox mainRow = createParkingMainRow(allSoldOut);
        card.getChildren().add(mainRow);

        // Sub-section with radio pills (only visible when parking enabled and not all sold out)
        if (!allSoldOut && parkingOptions.size() > 1) {
            VBox subsection = createParkingTypeSubsection();
            // Bind visibility to parkingEnabledProperty
            subsection.visibleProperty().bind(parkingEnabledProperty);
            subsection.managedProperty().bind(parkingEnabledProperty);
            card.getChildren().add(subsection);
        }

        // Update card style based on parking enabled state
        parkingEnabledProperty.addListener((obs, old, enabled) -> {
            if (enabled && !allSoldOut) {
                if (!card.getStyleClass().contains("selected")) {
                    card.getStyleClass().add("selected");
                }
            } else {
                card.getStyleClass().remove("selected");
            }
        });

        // Initial state
        if (parkingEnabledProperty.get() && !allSoldOut) {
            card.getStyleClass().add("selected");
        }

        cardWrapper.getChildren().add(card);

        // Add sold out ribbon if all options are sold out
        if (allSoldOut) {
            StackPane ribbon = BookingPageUIBuilder.createSoldOutRibbon();
            StackPane.setAlignment(ribbon, Pos.TOP_RIGHT);
            cardWrapper.getChildren().add(ribbon);
        }

        return cardWrapper;
    }

    /**
     * Creates the main row of the parking card with checkbox, icon, text, and price.
     */
    protected HBox createParkingMainRow(boolean allSoldOut) {
        HBox mainRow = new HBox(12);
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setPadding(new Insets(16));

        if (!allSoldOut) {
            mainRow.setCursor(Cursor.HAND);
        } else {
            mainRow.setCursor(Cursor.DEFAULT);
        }

        // Checkbox indicator - bound to parkingEnabled
        StackPane checkbox = BookingPageUIBuilder.createCheckboxIndicator(parkingEnabledProperty, colorScheme);
        if (allSoldOut) {
            checkbox.getStyleClass().add("disabled");
            checkbox.setOpacity(0.5);
        }

        // Text content
        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = new Label();
        title.textProperty().bind(I18n.i18nTextProperty(BookingPageI18nKeys.RegisterForParkingPass));
        title.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-medium");
        if (allSoldOut) {
            title.getStyleClass().add("bookingpage-text-muted");
        } else {
            title.getStyleClass().add("bookingpage-text-dark");
        }

        Label subtitle = new Label();
        if (allSoldOut) {
            subtitle.textProperty().bind(I18n.i18nTextProperty(BookingPageI18nKeys.ParkingSoldOutMessage));
            subtitle.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");
        } else {
            subtitle.textProperty().bind(I18n.i18nTextProperty(BookingPageI18nKeys.LimitedParkingAvailable));
            subtitle.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");
        }
        textContent.getChildren().addAll(title, subtitle);

        // Price label - show price from first option (they typically have same price)
        Label priceLabel = new Label(formatUnifiedParkingPrice());
        priceLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold");
        if (allSoldOut) {
            priceLabel.getStyleClass().addAll("bookingpage-text-muted", "bookingpage-strikethrough");
        } else {
            priceLabel.getStyleClass().add("bookingpage-text-dark");
        }

        mainRow.getChildren().addAll(checkbox, textContent, priceLabel);

        // Click handler (only if not all sold out)
        if (!allSoldOut) {
            mainRow.setOnMouseClicked(e -> {
                parkingEnabledProperty.set(!parkingEnabledProperty.get());
            });
        }

        return mainRow;
    }

    /**
     * Creates the sub-section containing parking type radio pills.
     */
    protected VBox createParkingTypeSubsection() {
        VBox subsection = new VBox(10);
        subsection.getStyleClass().add("bookingpage-parking-subsection");
        subsection.setPadding(new Insets(12, 16, 16, 48)); // Left padding to align under checkbox

        // "Select parking type:" label
        Label typeLabel = new Label();
        typeLabel.textProperty().bind(I18n.i18nTextProperty(BookingPageI18nKeys.SelectParkingType));
        typeLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-font-medium", "bookingpage-text-muted");

        // Radio pill container
        FlowPane pillsContainer = new FlowPane();
        pillsContainer.setHgap(10);
        pillsContainer.setVgap(10);

        for (ParkingOption option : parkingOptions) {
            HBox pill = createParkingTypeRadioPill(option);
            pillsContainer.getChildren().add(pill);
        }

        subsection.getChildren().addAll(typeLabel, pillsContainer);
        return subsection;
    }

    /**
     * Creates a radio pill button for a parking type option.
     * Shows sold out badge and strikethrough text if option is sold out.
     */
    protected HBox createParkingTypeRadioPill(ParkingOption option) {
        boolean soldOut = option.isSoldOut();

        HBox pill = new HBox(6);
        pill.setAlignment(Pos.CENTER_LEFT);
        pill.setPadding(new Insets(8, 16, 8, 16));
        pill.getStyleClass().add("bookingpage-radio-pill");

        if (soldOut) {
            pill.getStyleClass().add("disabled");
            pill.setCursor(Cursor.DEFAULT);
        } else {
            pill.setCursor(Cursor.HAND);
        }

        // Radio indicator (small circular)
        StackPane radio = createSmallRadioIndicator(option, soldOut);
        pill.getChildren().add(radio);

        // Label
        Label text = new Label(option.getName());
        text.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-medium");
        if (soldOut) {
            text.getStyleClass().addAll("bookingpage-text-muted", "bookingpage-strikethrough");
        } else {
            text.getStyleClass().add("bookingpage-text-dark");
        }
        pill.getChildren().add(text);

        // Add sold out badge if applicable
        if (soldOut) {
            Label badge = createInlineSoldOutBadge();
            pill.getChildren().add(badge);
        }

        // Update pill style based on selection
        Runnable updatePillStyle = () -> {
            ParkingOption selected = selectedParkingTypeProperty.get();
            boolean isSelected = (selected == option);
            if (isSelected && !soldOut) {
                if (!pill.getStyleClass().contains("selected")) {
                    pill.getStyleClass().add("selected");
                }
            } else {
                pill.getStyleClass().remove("selected");
            }
        };

        updatePillStyle.run();
        selectedParkingTypeProperty.addListener((obs, old, newOpt) -> updatePillStyle.run());

        // Click handler (only if not sold out)
        if (!soldOut) {
            pill.setOnMouseClicked(e -> {
                e.consume(); // Prevent event from bubbling to parent
                selectedParkingTypeProperty.set(option);
            });
        }

        return pill;
    }

    /**
     * Creates a small circular radio indicator for parking type pills.
     */
    protected StackPane createSmallRadioIndicator(ParkingOption option, boolean soldOut) {
        double size = 14;
        double dotSize = 6;

        // Outer circle
        Circle outer = new Circle(size / 2);
        outer.getStyleClass().add("bookingpage-radio-pill-outer");
        if (soldOut) {
            outer.getStyleClass().add("disabled");
        }

        // Inner dot
        Circle inner = new Circle(dotSize / 2);
        inner.setVisible(false);
        inner.getStyleClass().add("bookingpage-radio-pill-inner");

        StackPane container = new StackPane(outer, inner);
        container.setMinSize(size, size);
        container.setMaxSize(size, size);
        container.setAlignment(Pos.CENTER);

        // Update indicator based on selection
        Runnable updateIndicator = () -> {
            ParkingOption selected = selectedParkingTypeProperty.get();
            boolean isSelected = (selected == option);
            if (isSelected && !soldOut) {
                outer.getStyleClass().add("selected");
                inner.setVisible(true);
                inner.getStyleClass().add("selected");
            } else {
                outer.getStyleClass().remove("selected");
                inner.setVisible(false);
                inner.getStyleClass().remove("selected");
            }
        };

        updateIndicator.run();
        selectedParkingTypeProperty.addListener((obs, old, newOpt) -> updateIndicator.run());

        return container;
    }

    /**
     * Creates an inline "SOLD OUT" badge for radio pills.
     */
    protected Label createInlineSoldOutBadge() {
        Label badge = new Label("SOLD OUT");
        badge.getStyleClass().add("bookingpage-soldout-badge-inline");
        badge.setPadding(new Insets(2, 6, 2, 6));
        return badge;
    }

    /**
     * Formats the unified parking price for display.
     * Uses the price from the first option (assumes all options have same price).
     */
    protected String formatUnifiedParkingPrice() {
        if (parkingOptions.isEmpty()) {
            return "";
        }
        ParkingOption firstOption = parkingOptions.get(0);
        return formatParkingPrice(firstOption);
    }

    /**
     * Formats parking price for display.
     */
    protected String formatParkingPrice(ParkingOption option) {
        if (option.getPrice() == 0) {
            return "Free";
        }
        String priceStr = "$" + (option.getPrice() / 100);
        if (option.isPerDay()) {
            priceStr += "/day/vehicle";
        }
        return priceStr;
    }

    // ========================================
    // SHUTTLE UI
    // ========================================

    /**
     * Builds the grouped shuttle container with header and trip cards.
     */
    protected VBox buildShuttleGroupContainer() {
        VBox groupContainer = new VBox(0);
        groupContainer.getStyleClass().add("bookingpage-shuttle-container");
        groupContainer.setPadding(new Insets(0));

        // Header with icon, title, description, and price
        HBox header = createShuttleHeader();

        // Trips container
        shuttleTripsContainer = new VBox(10);
        shuttleTripsContainer.setPadding(new Insets(0, 16, 16, 16));
        shuttleTripsContainer.setAlignment(Pos.TOP_LEFT);

        // Build trip cards from options
        buildShuttleTripCards();

        groupContainer.getChildren().addAll(header, shuttleTripsContainer);

        return groupContainer;
    }

    /**
     * Creates the shuttle header section.
     */
    protected HBox createShuttleHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16));

        // Icon container with themed background
        StackPane iconContainer = createShuttleIconContainer();

        // Text content (title + description)
        VBox textContent = new VBox(4);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = new Label();
        title.textProperty().bind(I18n.i18nTextProperty(BookingPageI18nKeys.AirportShuttle));
        title.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold", "bookingpage-text-dark");

        Label description = new Label();
        description.textProperty().bind(I18n.i18nTextProperty(BookingPageI18nKeys.SelectYourJourney, getFormattedSingleTripPrice()));
        description.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");

        textContent.getChildren().addAll(title, description);

        // Price label (only visible when any shuttle selected)
        shuttleTotalPriceLabel = new Label();
        shuttleTotalPriceLabel.getStyleClass().addAll("bookingpage-price-medium", "bookingpage-text-primary");
        updateShuttleTotalPriceLabel();

        shuttleHeaderPriceBox = new HBox(shuttleTotalPriceLabel);
        shuttleHeaderPriceBox.setAlignment(Pos.CENTER_RIGHT);
        shuttleHeaderPriceBox.setVisible(false);
        shuttleHeaderPriceBox.setManaged(false);

        header.getChildren().addAll(iconContainer, textContent, shuttleHeaderPriceBox);

        return header;
    }

    /**
     * Creates the shuttle icon container with plane icon.
     */
    protected StackPane createShuttleIconContainer() {
        StackPane iconContainer = new StackPane();
        iconContainer.setMinSize(36, 36);
        iconContainer.setMaxSize(36, 36);
        iconContainer.getStyleClass().add("bookingpage-shuttle-icon-container");

        SVGPath icon = new SVGPath();
        icon.setContent(BookingPageUIBuilder.ICON_PLANE);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        icon.setStrokeWidth(2);
        icon.setFill(Color.TRANSPARENT);
        icon.getStyleClass().add("bookingpage-shuttle-icon");

        iconContainer.getChildren().add(icon);
        iconContainer.setAlignment(Pos.CENTER);

        return iconContainer;
    }

    /**
     * Builds shuttle trip cards from the current options list.
     * Cards are sorted chronologically by scheduled date.
     */
    protected void buildShuttleTripCards() {
        shuttleTripsContainer.getChildren().clear();

        if (shuttleOptions.isEmpty()) {
            return;
        }

        // Sort options by date chronologically (earliest first)
        List<ShuttleOption> sortedOptions = shuttleOptions.stream()
            .sorted((a, b) -> {
                LocalDate dateA = a.getScheduledDate();
                LocalDate dateB = b.getScheduledDate();
                if (dateA == null && dateB == null) return 0;
                if (dateA == null) return 1;  // null dates go to end
                if (dateB == null) return -1;
                return dateA.compareTo(dateB);
            })
            .collect(Collectors.toList());

        for (ShuttleOption option : sortedOptions) {
            HBox tripCard = createShuttleTripCard(option);
            shuttleTripsContainer.getChildren().add(tripCard);
        }
    }

    /**
     * Creates a trip card for a single shuttle option.
     */
    protected HBox createShuttleTripCard(ShuttleOption option) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.setCursor(Cursor.HAND);
        card.getStyleClass().add("bookingpage-shuttle-trip");

        // Checkbox indicator
        StackPane checkbox = BookingPageUIBuilder.createCheckboxIndicator(option.selectedProperty(), colorScheme);

        // Text content (trip name + date/time)
        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label title = new Label(option.getName());
        title.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-medium");

        Label dateTime = new Label(formatTripDateTime(option));
        dateTime.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");

        textContent.getChildren().addAll(title, dateTime);

        // Price label
        Label priceLabel = new Label(formatShuttlePrice(option.getPrice()));
        priceLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-font-semibold");

        card.getChildren().addAll(checkbox, textContent, priceLabel);

        // Selection handling
        option.selectedProperty().addListener((obs, old, newVal) -> {
            updateShuttleTripCardStyle(card, option);
            updateShuttleContainerStyle();
            updateShuttleTotalPriceLabel();
            if (onSelectionChangedCallback != null) {
                onSelectionChangedCallback.run();
            }
        });

        // Availability handling
        option.availableProperty().addListener((obs, old, newVal) -> {
            updateShuttleTripCardAvailability(card, option);
        });

        // Initial state
        updateShuttleTripCardStyle(card, option);
        updateShuttleTripCardAvailability(card, option);

        // Click to toggle (only if available)
        card.setOnMouseClicked(e -> {
            if (option.isAvailable()) {
                option.setSelected(!option.isSelected());
            }
        });

        return card;
    }

    /**
     * Updates shuttle trip card style based on selection state.
     */
    protected void updateShuttleTripCardStyle(HBox card, ShuttleOption option) {
        if (option.isSelected()) {
            if (!card.getStyleClass().contains("selected")) {
                card.getStyleClass().add("selected");
            }
        } else {
            card.getStyleClass().remove("selected");
        }
    }

    /**
     * Updates shuttle trip card availability based on date matching.
     */
    protected void updateShuttleTripCardAvailability(HBox card, ShuttleOption option) {
        if (option.isAvailable()) {
            card.getStyleClass().remove("disabled");
            card.setCursor(Cursor.HAND);
            card.setOpacity(1.0);
        } else {
            if (!card.getStyleClass().contains("disabled")) {
                card.getStyleClass().add("disabled");
            }
            card.setCursor(Cursor.DEFAULT);
            card.setOpacity(0.5);
            // Deselect if was selected but now unavailable
            if (option.isSelected()) {
                option.setSelected(false);
            }
        }
    }

    /**
     * Updates the shuttle grouped container style based on whether any shuttle is selected.
     */
    protected void updateShuttleContainerStyle() {
        boolean hasSelection = shuttleOptions.stream().anyMatch(ShuttleOption::isSelected);
        if (hasSelection) {
            if (!shuttleGroupContainer.getStyleClass().contains("has-selection")) {
                shuttleGroupContainer.getStyleClass().add("has-selection");
            }
            shuttleHeaderPriceBox.setVisible(true);
            shuttleHeaderPriceBox.setManaged(true);
        } else {
            shuttleGroupContainer.getStyleClass().remove("has-selection");
            shuttleHeaderPriceBox.setVisible(false);
            shuttleHeaderPriceBox.setManaged(false);
        }
    }

    /**
     * Updates the shuttle total price label in the header.
     */
    protected void updateShuttleTotalPriceLabel() {
        int total = getTotalShuttleCost();
        shuttleTotalPriceLabel.setText(formatShuttlePrice(total));
    }

    /**
     * Updates shuttle availability based on current arrival/departure dates.
     */
    protected void updateShuttleAvailability() {
        LocalDate arrivalDate = arrivalDateProperty.get();
        LocalDate departureDate = departureDateProperty.get();

        Console.log("DefaultTransportSection: Updating shuttle availability - arrival=" + arrivalDate + ", departure=" + departureDate);

        for (ShuttleOption option : shuttleOptions) {
            boolean available = false;

            if (option.isOutbound() && arrivalDate != null) {
                available = arrivalDate.equals(option.getScheduledDate());
                Console.log("DefaultTransportSection: Outbound '" + option.getName() +
                    "' scheduled=" + option.getScheduledDate() + " available=" + available);
            } else if (option.isReturn() && departureDate != null) {
                available = departureDate.equals(option.getScheduledDate());
                Console.log("DefaultTransportSection: Return '" + option.getName() +
                    "' scheduled=" + option.getScheduledDate() + " available=" + available);
            }

            option.setAvailable(available);
        }

        // Rebuild trip cards to reflect new availability
        buildShuttleTripCards();
        updateShuttleContainerStyle();
    }

    /**
     * Formats trip date and time for display.
     */
    protected String formatTripDateTime(ShuttleOption option) {
        LocalDate date = option.getScheduledDate();
        if (date == null) {
            return "";
        }

        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        int dayOfMonth = date.getDayOfMonth();
        String month = date.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

        String dateStr = dayOfWeek + " " + dayOfMonth + " " + month;

        String departureTime = option.getDescription();
        if (departureTime != null && !departureTime.isEmpty()) {
            return dateStr + ", " + departureTime;
        }

        return dateStr;
    }

    /**
     * Formats a shuttle price for display.
     */
    protected String formatShuttlePrice(int priceInCents) {
        if (priceInCents == 0) {
            return "Free";
        }
        return "$" + (priceInCents / 100);
    }

    /**
     * Gets the formatted price for a single trip.
     */
    protected String getFormattedSingleTripPrice() {
        if (shuttleOptions.isEmpty()) {
            return "$0";
        }
        int price = shuttleOptions.stream()
            .mapToInt(ShuttleOption::getPrice)
            .findFirst()
            .orElse(0);
        return formatShuttlePrice(price);
    }

    // ========================================
    // VISIBILITY
    // ========================================

    /**
     * Updates visibility of sub-containers based on available options.
     */
    protected void updateVisibility() {
        Layouts.setManagedAndVisibleProperties(parkingContainer, !parkingOptions.isEmpty());
        Layouts.setManagedAndVisibleProperties(shuttleGroupContainer, !shuttleOptions.isEmpty());
    }

    /**
     * Sets the visibility of the entire section.
     */
    public void setVisible(boolean visible) {
        Layouts.setManagedAndVisibleProperties(container, visible);
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.Transport;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
    }

    @Override
    public ObservableBooleanValue validProperty() {
        // Always valid - transport options are optional
        return new SimpleBooleanProperty(true);
    }

    // ========================================
    // HasTransportSection INTERFACE
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
    public ObjectProperty<LocalDate> arrivalDateProperty() {
        return arrivalDateProperty;
    }

    @Override
    public ObjectProperty<LocalDate> departureDateProperty() {
        return departureDateProperty;
    }

    @Override
    public void setDaysCount(int days) {
        this.daysCount = days;
    }

    @Override
    public int getDaysCount() {
        return daysCount;
    }

    @Override
    public List<ParkingOption> getParkingOptions() {
        return parkingOptions;
    }

    @Override
    public BooleanProperty parkingEnabledProperty() {
        return parkingEnabledProperty;
    }

    @Override
    public ObjectProperty<ParkingOption> selectedParkingTypeProperty() {
        return selectedParkingTypeProperty;
    }

    @Override
    public void clearParkingOptions() {
        parkingOptions.clear();
        parkingEnabledProperty.set(false);
        selectedParkingTypeProperty.set(null);
        if (parkingContainer != null) {
            parkingContainer.getChildren().clear();
        }
    }

    @Override
    public void addParkingOption(ParkingOption option) {
        parkingOptions.add(option);
    }

    @Override
    public List<ShuttleOption> getShuttleOptions() {
        return shuttleOptions;
    }

    @Override
    public void clearShuttleOptions() {
        shuttleOptions.clear();
        if (shuttleTripsContainer != null) {
            shuttleTripsContainer.getChildren().clear();
        }
    }

    @Override
    public void addShuttleOption(ShuttleOption option) {
        shuttleOptions.add(option);
    }

    @Override
    public void setOnSelectionChanged(Runnable callback) {
        this.onSelectionChangedCallback = callback;
    }

    @Override
    public void reset() {
        // Reset parking unified card state
        parkingEnabledProperty.set(false);
        selectedParkingTypeProperty.set(null);

        // Deselect all parking options
        for (ParkingOption option : parkingOptions) {
            option.setSelected(false);
        }
        // Deselect all shuttle options
        for (ShuttleOption option : shuttleOptions) {
            option.setSelected(false);
        }
        updateShuttleContainerStyle();
        updateShuttleTotalPriceLabel();
    }

    // ========================================
    // DATA POPULATION FROM POLICY AGGREGATE
    // ========================================

    @Override
    public void populateFromPolicyAggregate(PolicyAggregate policyAggregate) {
        if (policyAggregate == null) {
            Console.log("DefaultTransportSection: PolicyAggregate is null");
            return;
        }

        this.policyAggregate = policyAggregate;

        // Clear existing options
        clearAllOptions();

        // Load parking options
        loadParkingOptions(policyAggregate);

        // Load shuttle options
        loadShuttleOptions(policyAggregate);

        Console.log("DefaultTransportSection: Loaded " + parkingOptions.size() + " parking options and " +
            shuttleOptions.size() + " shuttle options");

        // Update shuttle availability based on current dates
        updateShuttleAvailability();

        // Rebuild UI
        rebuildUI();
    }

    /**
     * Loads parking options from PolicyAggregate.
     * Fetches availability from ScheduledItem.guestsAvailability() and ItemPolicy for default/sold out status.
     */
    protected void loadParkingOptions(PolicyAggregate policyAggregate) {
        List<ScheduledItem> parkingItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.PARKING);
        if (parkingItems == null || parkingItems.isEmpty()) {
            Console.log("DefaultTransportSection: No parking scheduled items found");
            return;
        }

        Console.log("DefaultTransportSection: Found " + parkingItems.size() + " parking scheduled items");

        // Group by Item
        Map<Item, List<ScheduledItem>> itemMap = parkingItems.stream()
            .filter(si -> si.getItem() != null)
            .collect(Collectors.groupingBy(ScheduledItem::getItem));

        // Sort entries by Item.ord (ascending) so Standard appears before Disabled
        List<Map.Entry<Item, List<ScheduledItem>>> sortedEntries = itemMap.entrySet().stream()
            .sorted((e1, e2) -> {
                Integer ord1 = e1.getKey().getOrd();
                Integer ord2 = e2.getKey().getOrd();
                // Null ord values go to the end
                if (ord1 == null && ord2 == null) return 0;
                if (ord1 == null) return 1;
                if (ord2 == null) return -1;
                return ord1.compareTo(ord2);
            })
            .collect(Collectors.toList());

        for (Map.Entry<Item, List<ScheduledItem>> entry : sortedEntries) {
            Item item = entry.getKey();
            List<ScheduledItem> scheduledItems = entry.getValue();

            // Get price from rate using the first scheduled item
            int price = 0;
            if (!scheduledItems.isEmpty()) {
                Rate rate = policyAggregate.getScheduledItemDailyRate(scheduledItems.get(0));
                if (rate != null) {
                    price = rate.getPrice();
                }
            }

            // Get ItemPolicy for default and sold out flags
            ItemPolicy itemPolicy = policyAggregate.getItemPolicy(item);
            boolean isDefault = itemPolicy != null && Boolean.TRUE.equals(itemPolicy.isDefault());
            boolean forceSoldOut = itemPolicy != null && Boolean.TRUE.equals(itemPolicy.isSoldOutForced());

            // Calculate minimum availability across all scheduled items
            int minAvailability = scheduledItems.stream()
                .mapToInt(si -> {
                    Integer avail = si.getGuestsAvailability();
                    return avail != null ? avail : Integer.MAX_VALUE;
                })
                .min()
                .orElse(Integer.MAX_VALUE);

            // Determine availability status
            ParkingAvailabilityStatus availabilityStatus;
            if (forceSoldOut || minAvailability <= 0) {
                availabilityStatus = ParkingAvailabilityStatus.SOLD_OUT;
            } else if (minAvailability <= LIMITED_THRESHOLD) {
                availabilityStatus = ParkingAvailabilityStatus.LIMITED;
            } else {
                availabilityStatus = ParkingAvailabilityStatus.AVAILABLE;
            }

            String name = item.getName() != null ? item.getName() : "Parking";
            String description = "";
            if (item.getLabel() != null && item.getLabel().getEn() != null) {
                description = item.getLabel().getEn();
            }

            ParkingOption option = new ParkingOption(
                item.getPrimaryKey(),
                item,
                name,
                description,
                price,
                true, // Parking is typically per-day
                scheduledItems,
                availabilityStatus,
                isDefault
            );

            addParkingOption(option);
            Console.log("DefaultTransportSection: Added parking option '" + name +
                "' price=" + price + " availability=" + availabilityStatus +
                " isDefault=" + isDefault + " minAvailability=" + minAvailability);
        }
    }

    /**
     * Loads shuttle/transport options from PolicyAggregate.
     */
    protected void loadShuttleOptions(PolicyAggregate policyAggregate) {
        List<ScheduledItem> transportItems = policyAggregate.filterScheduledItemsOfFamily(KnownItemFamily.TRANSPORT);
        if (transportItems == null || transportItems.isEmpty()) {
            Console.log("DefaultTransportSection: No transport scheduled items found");
            return;
        }

        Console.log("DefaultTransportSection: Found " + transportItems.size() + " transport scheduled items");

        // Group by Item
        Map<Item, List<ScheduledItem>> itemMap = transportItems.stream()
            .filter(si -> si.getItem() != null)
            .collect(Collectors.groupingBy(ScheduledItem::getItem));

        for (Map.Entry<Item, List<ScheduledItem>> entry : itemMap.entrySet()) {
            Item item = entry.getKey();
            List<ScheduledItem> scheduledItems = entry.getValue();

            // Get price from rate using the first scheduled item
            int price = 0;
            if (!scheduledItems.isEmpty()) {
                Rate rate = policyAggregate.getScheduledItemDailyRate(scheduledItems.get(0));
                if (rate != null) {
                    price = rate.getPrice();
                }
            }

            // Determine direction from item name
            ShuttleDirection direction = determineShuttleDirection(item);

            String name = item.getName() != null ? item.getName() : "Shuttle";

            // Get scheduled date and departure time from first scheduled item
            LocalDate scheduledDate = null;
            String departureTimeStr = "";

            for (ScheduledItem si : scheduledItems) {
                if (si.getDate() != null) {
                    scheduledDate = si.getDate();
                    // Get departure time from scheduled item
                    java.time.LocalTime startTime = si.getStartTime();
                    if (startTime != null) {
                        // Format time like "2:00 PM"
                        departureTimeStr = formatDepartureTime(startTime);
                    }
                    break;
                }
            }

            ShuttleOption option = new ShuttleOption(
                item.getPrimaryKey(),
                item,
                name,
                departureTimeStr,  // Use formatted time as description
                price,
                scheduledDate,
                direction,
                scheduledItems
            );

            addShuttleOption(option);
            Console.log("DefaultTransportSection: Added shuttle option '" + name +
                "' direction=" + direction + " date=" + scheduledDate +
                " time=" + departureTimeStr + " price=" + price);
        }
    }

    /**
     * Formats a LocalTime as a user-friendly departure time string.
     * @param time the departure time
     * @return formatted time like "2:00 PM" or "14:00"
     */
    protected String formatDepartureTime(java.time.LocalTime time) {
        if (time == null) {
            return "";
        }
        // Format as "h:mm a" (e.g., "2:00 PM")
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        return "departs " + time.format(formatter);
    }

    /**
     * Determines the shuttle direction based on item name.
     * Supports patterns like:
     * - "outbound", "arrival", "to venue" → OUTBOUND (to venue)
     * - "return", "departure", "from venue" → RETURN (from venue)
     * - "Airport => Venue" → OUTBOUND (arrow points to venue)
     * - "Venue => Airport" → RETURN (arrow points away from venue)
     */
    protected ShuttleDirection determineShuttleDirection(Item item) {
        String name = item.getName() != null ? item.getName().toLowerCase() : "";

        // Check explicit direction keywords first
        if (name.contains("outbound") || name.contains("arrival")) {
            return ShuttleDirection.OUTBOUND;
        } else if (name.contains("return") || name.contains("departure")) {
            return ShuttleDirection.RETURN;
        }

        // Check for arrow patterns like "Airport => Venue" or "Venue => Airport"
        // If the name contains "=>" or "→", check destination
        // Common airport keywords that suggest going TO airport (RETURN trip)
        String[] airportKeywords = {"airport", "jfk", "lga", "ewr", "newark", "laguardia", "heathrow", "gatwick"};

        if (name.contains("=>") || name.contains("→") || name.contains("->")) {
            // Split on arrow and check destination (right side)
            String[] parts = name.split("=>|→|->");
            if (parts.length >= 2) {
                String destination = parts[1].trim().toLowerCase();
                // If destination contains an airport keyword, it's RETURN (going to airport)
                for (String airport : airportKeywords) {
                    if (destination.contains(airport)) {
                        return ShuttleDirection.RETURN;
                    }
                }
                // Otherwise it's likely going to the venue (OUTBOUND)
                return ShuttleDirection.OUTBOUND;
            }
        }

        // Check for "to " or "from " patterns
        if (name.contains("to ") || name.contains("→")) {
            return ShuttleDirection.OUTBOUND;
        } else if (name.contains("from ") || name.contains("←")) {
            return ShuttleDirection.RETURN;
        }

        Console.log("DefaultTransportSection: Could not determine direction for '" + name + "', defaulting to OUTBOUND");
        return ShuttleDirection.OUTBOUND;
    }

    /**
     * Rebuilds the UI to reflect updated options.
     */
    protected void rebuildUI() {
        container.getChildren().clear();
        buildUI();
    }
}
