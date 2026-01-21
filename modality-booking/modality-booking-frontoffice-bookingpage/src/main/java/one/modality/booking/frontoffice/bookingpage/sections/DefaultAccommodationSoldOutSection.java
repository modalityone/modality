package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ItemPolicy;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.standard.StandardBookingFormCallbacks;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Default implementation of the accommodation sold out recovery section.
 *
 * <p>This section is displayed when the user's selected accommodation becomes
 * unavailable (sold out) during booking submission. It provides a warm, non-alarming
 * interface for selecting an alternative accommodation.</p>
 *
 * <p>UI Structure (matching FestivalRegistrationV2.jsx mockup):</p>
 * <ul>
 *   <li>Header: Orange circle (80x80) with exchange arrows icon</li>
 *   <li>Title: "Choose Another Option" (24px, semibold)</li>
 *   <li>Subtitle: "Your first choice is no longer available"</li>
 *   <li>Explanation Box: Orange background (#FFF3E0) containing:
 *     <ul>
 *       <li>Main text: "Due to high demand, [item name] is now fully booked."</li>
 *       <li>Sold out indicator: X icon + "[Item Name] — SOLD OUT"</li>
 *       <li>Reassurance: "Your other choices...are saved"</li>
 *     </ul>
 *   </li>
 *   <li>Section Header: "Available Options" with home icon</li>
 *   <li>Alternatives: List of available accommodation cards</li>
 *   <li>Buttons: "Continue with New Selection" and "Cancel Registration"</li>
 * </ul>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-selectable-card} - accommodation card</li>
 * </ul>
 *
 * @author Claude Code
 * @see HasAccommodationSoldOutSection
 */
public class DefaultAccommodationSoldOutSection implements HasAccommodationSoldOutSection {

    // === SVG ICONS ===
    // Exchange/switch arrows icon matching mockup (diagonal arrows indicating change)
    private static final String ICON_EXCHANGE = "M16 3h5v5M4 20L21 3M21 16v5h-5M15 15l6 6M4 4l5 5";
    // Circle with X icon for sold out indicator
    private static final String ICON_CIRCLE_X = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2M15 9l-6 6M9 9l6 6";
    private static final String ICON_INFO = "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z";

    // === COLOR SCHEME ===
    private final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // === VALIDITY ===
    private final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(false);

    // === SELECTED OPTION ===
    private final ObjectProperty<HasAccommodationSelectionSection.AccommodationOption> selectedOptionProperty = new SimpleObjectProperty<>();

    // === OPTIONS LIST ===
    private final ObservableList<HasAccommodationSelectionSection.AccommodationOption> alternativeOptions = FXCollections.observableArrayList();

    // === ORIGINAL SELECTION INFO ===
    private String originalItemName = "";
    private int originalPrice = 0;
    private String eventName = "";
    private int numberOfNights = 0;  // For calculating total price from per-night rates

    // === UI COMPONENTS ===
    private final VBox container = new VBox();
    private VBox optionsContainer;
    private Label originalNameLabel;  // Shows "[Item Name] — SOLD OUT" in the sold out indicator
    private Label eventNameLabel;     // Shows "Due to high demand, [item] is now fully booked."
    private final Map<HasAccommodationSelectionSection.AccommodationOption, VBox> optionCardMap = new HashMap<>();
    private final Map<HasAccommodationSelectionSection.AccommodationOption, StackPane> checkmarkBadgeMap = new HashMap<>();

    // === RESPONSIVE DESIGN ===
    private ResponsiveDesign responsiveDesign;
    private boolean isMobileLayout = false;
    // Track header components for responsive layout switching
    private final java.util.List<CardHeaderComponents> cardHeaderList = new java.util.ArrayList<>();

    /** Holds references to card header components for responsive layout switching */
    private static class CardHeaderComponents {
        final VBox headerContainer;  // Container that holds either HBox or VBox layout
        final Label nameLabel;
        final VBox priceContainer;
        final StackPane checkmarkBadge;
        final boolean isAvailable;

        CardHeaderComponents(VBox headerContainer, Label nameLabel, VBox priceContainer, StackPane checkmarkBadge, boolean isAvailable) {
            this.headerContainer = headerContainer;
            this.nameLabel = nameLabel;
            this.priceContainer = priceContainer;
            this.checkmarkBadge = checkmarkBadge;
            this.isAvailable = isAvailable;
        }
    }

    // === CALLBACKS ===
    private Consumer<HasAccommodationSelectionSection.AccommodationOption> onOptionSelected;

    // === DATA ===
    private WorkingBookingProperties workingBookingProperties;

    // === ROOMMATE SECTION ===
    private VBox roommateContainer;
    private DefaultRoommateInfoSection roommateSection;
    private VBox body;  // Reference to add roommate section after options

    public DefaultAccommodationSoldOutSection() {
        buildUI();
        setupBindings();
    }

    private void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.setMaxWidth(720);

        // === COMBINED HEADER SECTION (warm cream background with rounded corners) ===
        // Merges the header title and "What happened" explanation into one cohesive box
        VBox header = createCombinedHeader();

        // === BODY SECTION ===
        body = new VBox(24);
        body.setPadding(new Insets(24));

        // Section title for alternatives using StyledSectionHeader with home icon
        HBox alternativesHeader = new StyledSectionHeader(BookingPageI18nKeys.AvailableOptions, StyledSectionHeader.ICON_HOME);

        // Options container for alternative accommodations
        optionsContainer = new VBox(12);
        optionsContainer.setAlignment(Pos.TOP_CENTER);
        optionsContainer.setFillWidth(true);

        // Roommate section (shown when selecting Double Room or Share Accommodation)
        roommateContainer = new VBox(16);
        roommateContainer.setVisible(false);
        roommateContainer.setManaged(false);
        roommateSection = new DefaultRoommateInfoSection();
        roommateContainer.getChildren().add(roommateSection.getView());

        body.getChildren().addAll(alternativesHeader, optionsContainer, roommateContainer);

        container.getChildren().addAll(header, body);

        // Setup responsive design for card layouts
        setupResponsiveDesign();
    }

    /**
     * Creates a combined header section matching the mockup design.
     * Features: orange theme, exchange arrows icon, explanation box with sold out indicator.
     */
    private VBox createCombinedHeader() {
        VBox header = new VBox(32);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(32, 24, 24, 24));

        // === TOP SECTION: Icon and title (centered) ===
        VBox topSection = createHeaderTopSection();

        // === EXPLANATION BOX (orange background with sold out indicator inside) ===
        VBox explanationBox = createExplanationBox();

        header.getChildren().addAll(topSection, explanationBox);

        return header;
    }

    /**
     * Creates the top section of the header with icon, title, and subtitle.
     */
    private VBox createHeaderTopSection() {
        VBox section = new VBox(8);
        section.setAlignment(Pos.CENTER);

        // Orange circle with exchange arrows icon (80x80 matching mockup)
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(80, 80);
        iconCircle.setMaxSize(80, 80);
        iconCircle.getStyleClass().add("bookingpage-soldout-icon-circle");

        SVGPath exchangeIcon = new SVGPath();
        exchangeIcon.setContent(ICON_EXCHANGE);
        exchangeIcon.getStyleClass().add("bookingpage-soldout-icon-circle-icon");
        exchangeIcon.setStrokeWidth(2);
        exchangeIcon.setScaleX(1.5);
        exchangeIcon.setScaleY(1.5);
        iconCircle.getChildren().add(exchangeIcon);

        // Title - "Choose Another Option"
        Label title = I18nControls.newLabel(BookingPageI18nKeys.ChooseAnotherOption);
        title.getStyleClass().addAll("bookingpage-text-2xl", "bookingpage-font-semibold", "bookingpage-text-dark");

        // Subtitle - "Your first choice is no longer available"
        Label subtitle = I18nControls.newLabel(BookingPageI18nKeys.FirstChoiceNoLongerAvailable);
        subtitle.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-muted");
        subtitle.setWrapText(true);

        section.getChildren().addAll(iconCircle, title, subtitle);
        VBox.setMargin(title, new Insets(12, 0, 4, 0));

        return section;
    }

    /**
     * Creates the explanation box with orange background containing the sold out indicator.
     */
    private VBox createExplanationBox() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.getStyleClass().add("bookingpage-soldout-explanation-box");

        // Main explanation text - "Due to high demand, [item name] is now fully booked."
        eventNameLabel = new Label();
        eventNameLabel.getStyleClass().addAll("bookingpage-text-base", "bookingpage-text-warning-dark");
        eventNameLabel.setWrapText(true);
        updateExplanationText();

        // Sold out indicator (inside the box with semi-transparent white background)
        HBox soldOutIndicator = createSoldOutIndicator();

        // Reassurance text - "Your other choices...are saved"
        Label reassuranceLabel = I18nControls.newLabel(BookingPageI18nKeys.OtherChoicesSaved);
        reassuranceLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-warning-dark");
        reassuranceLabel.setWrapText(true);

        box.getChildren().addAll(eventNameLabel, soldOutIndicator, reassuranceLabel);
        return box;
    }

    /**
     * Creates the sold out indicator row with X icon and item name.
     */
    private HBox createSoldOutIndicator() {
        HBox indicator = new HBox(8);
        indicator.setAlignment(Pos.CENTER_LEFT);
        indicator.setPadding(new Insets(10, 12, 10, 12));
        indicator.getStyleClass().add("bookingpage-soldout-indicator");

        // X icon (circle with X)
        SVGPath xIcon = new SVGPath();
        xIcon.setContent(ICON_CIRCLE_X);
        xIcon.getStyleClass().add("bookingpage-soldout-indicator-icon");
        xIcon.setStrokeWidth(2);
        xIcon.setScaleX(0.67);
        xIcon.setScaleY(0.67);

        // Wrap icon in StackPane for proper sizing
        StackPane iconWrapper = new StackPane(xIcon);
        iconWrapper.setMinSize(16, 16);
        iconWrapper.setMaxSize(16, 16);

        // Item name + " — SOLD OUT" text
        originalNameLabel = new Label();
        originalNameLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-font-medium", "bookingpage-text-warning-dark");
        updateSoldOutIndicatorText();

        indicator.getChildren().addAll(iconWrapper, originalNameLabel);
        return indicator;
    }

    /**
     * Updates the sold out indicator text with item name.
     */
    private void updateSoldOutIndicatorText() {
        if (originalNameLabel != null && originalItemName != null && !originalItemName.isEmpty()) {
            originalNameLabel.setText(originalItemName + " — SOLD OUT");
        }
    }

    private void setupBindings() {
        // Update validity and visual state when option is selected
        selectedOptionProperty.addListener((obs, oldOption, newOption) -> {
            // Update card styles
            if (oldOption != null) {
                VBox oldCard = optionCardMap.get(oldOption);
                if (oldCard != null) {
                    oldCard.getStyleClass().remove("selected");
                }
                StackPane oldCheckmark = checkmarkBadgeMap.get(oldOption);
                if (oldCheckmark != null) {
                    oldCheckmark.setVisible(false);
                }
            }
            if (newOption != null) {
                VBox newCard = optionCardMap.get(newOption);
                if (newCard != null) {
                    newCard.getStyleClass().add("selected");
                }
                StackPane newCheckmark = checkmarkBadgeMap.get(newOption);
                if (newCheckmark != null) {
                    newCheckmark.setVisible(true);
                }
            }

            // Configure roommate section based on selected option
            configureRoommateSection(newOption);

            // Update combined validity
            updateCombinedValidity();

            // Notify callback
            if (onOptionSelected != null && newOption != null) {
                onOptionSelected.accept(newOption);
            }
        });

        // Listen for roommate section validity changes
        roommateSection.validProperty().addListener((obs, oldVal, newVal) -> updateCombinedValidity());

        // Rebuild cards when list changes
        alternativeOptions.addListener((ListChangeListener<HasAccommodationSelectionSection.AccommodationOption>) change ->
            UiScheduler.runInUiThread(this::rebuildOptionCards));
    }

    /**
     * Configures the roommate section based on the selected accommodation option.
     * Shows roommate fields for Double Rooms and Share Accommodation.
     */
    private void configureRoommateSection(HasAccommodationSelectionSection.AccommodationOption option) {
        if (option == null || !option.isAvailable()) {
            hideRoommateSection();
            return;
        }

        Item item = option.getItemEntity();
        boolean isShareAccommodation = item != null && Boolean.TRUE.equals(item.isShare_mate());
        boolean isDayVisitor = option.isDayVisitor();

        if (isShareAccommodation) {
            // Share Accommodation: show single field for room booker
            roommateSection.reset();
            roommateSection.setIsRoomBooker(false);
            roommateSection.setVisible(true);
            roommateContainer.setVisible(true);
            roommateContainer.setManaged(true);
        } else if (!isDayVisitor && item != null) {
            // Check room capacity for multi-person rooms
            Integer capacity = item.getCapacity();
            if (capacity != null && capacity > 1) {
                // Double Room: show roommate fields
                roommateSection.reset();
                roommateSection.setRoomCapacity(capacity);
                // Get minOccupancy from ItemPolicy if available
                ItemPolicy itemPolicy = workingBookingProperties != null
                    ? workingBookingProperties.getPolicyAggregate().getItemPolicy(item)
                    : null;
                int minOccupancy = (itemPolicy != null && itemPolicy.getMinOccupancy() != null)
                    ? itemPolicy.getMinOccupancy() : capacity;
                roommateSection.setMinOccupancy(minOccupancy);
                roommateSection.setIsRoomBooker(true);
                roommateSection.setVisible(true);
                roommateContainer.setVisible(true);
                roommateContainer.setManaged(true);
            } else {
                // Single room: hide roommate section
                hideRoommateSection();
            }
        } else {
            // Day visitor: hide roommate section
            hideRoommateSection();
        }
    }

    private void hideRoommateSection() {
        roommateSection.setVisible(false);
        roommateContainer.setVisible(false);
        roommateContainer.setManaged(false);
    }

    /**
     * Updates the combined validity based on accommodation selection and roommate section.
     */
    private void updateCombinedValidity() {
        HasAccommodationSelectionSection.AccommodationOption selected = selectedOptionProperty.get();
        boolean accommodationValid = selected != null && selected.isAvailable();
        boolean roommateValid = !roommateSection.isVisible() || roommateSection.validProperty().get();
        validProperty.set(accommodationValid && roommateValid);
    }

    private void rebuildOptionCards() {
        optionsContainer.getChildren().clear();
        optionCardMap.clear();
        checkmarkBadgeMap.clear();
        cardHeaderList.clear();

        HasAccommodationSelectionSection.AccommodationOption currentlySelected = selectedOptionProperty.get();
        Object selectedItemId = currentlySelected != null ? currentlySelected.getItemId() : null;

        HasAccommodationSelectionSection.AccommodationOption matchingOption = null;

        // Separate options into accommodation and day visitor groups, sorted by Item.ord
        Comparator<HasAccommodationSelectionSection.AccommodationOption> byOrd = Comparator.comparing(
            option -> {
                Item item = option.getItemEntity();
                return item != null && item.getOrd() != null ? item.getOrd() : Integer.MAX_VALUE;
            });

        List<HasAccommodationSelectionSection.AccommodationOption> accommodationOpts = alternativeOptions.stream()
            .filter(opt -> !opt.isDayVisitor())
            .sorted(byOrd)
            .collect(Collectors.toList());

        List<HasAccommodationSelectionSection.AccommodationOption> dayVisitorOpts = alternativeOptions.stream()
            .filter(HasAccommodationSelectionSection.AccommodationOption::isDayVisitor)
            .sorted(byOrd)
            .collect(Collectors.toList());

        // Add accommodation cards first
        for (HasAccommodationSelectionSection.AccommodationOption option : accommodationOpts) {
            boolean isSelected = selectedItemId != null && selectedItemId.equals(option.getItemId());
            VBox card = createOptionCard(option, isSelected);
            optionsContainer.getChildren().add(card);
            optionCardMap.put(option, card);

            if (isSelected) {
                matchingOption = option;
            }
        }

        // Add separator if both groups exist
        if (!accommodationOpts.isEmpty() && !dayVisitorOpts.isEmpty()) {
            HBox separator = BookingPageUIBuilder.createNoAccommodationSeparator();
            VBox.setMargin(separator, new Insets(28, 0, 20, 0));
            optionsContainer.getChildren().add(separator);
        }

        // Add day visitor cards
        for (HasAccommodationSelectionSection.AccommodationOption option : dayVisitorOpts) {
            boolean isSelected = selectedItemId != null && selectedItemId.equals(option.getItemId());
            VBox card = createOptionCard(option, isSelected);
            optionsContainer.getChildren().add(card);
            optionCardMap.put(option, card);

            if (isSelected) {
                matchingOption = option;
            }
        }

        if (matchingOption != null && currentlySelected != matchingOption) {
            selectedOptionProperty.set(matchingOption);
        }

        // Reapply current layout after rebuilding cards
        applyCardLayout(isMobileLayout);
    }

    /**
     * Creates an accommodation option card using the same pattern as DefaultAccommodationSelectionSection.
     * Uses a responsive header that switches between horizontal and vertical layouts.
     */
    private VBox createOptionCard(HasAccommodationSelectionSection.AccommodationOption option, boolean isSelected) {
        boolean isSoldOut = option.getAvailability() == HasAccommodationSelectionSection.AvailabilityStatus.SOLD_OUT;
        boolean isAvailable = option.isAvailable();

        VBox card = new VBox(8);
        card.setMaxWidth(Double.MAX_VALUE);
        if (!isSoldOut) {
            card.setPadding(new Insets(20));
        }
        card.getStyleClass().add("bookingpage-selectable-card");

        if (isSoldOut) {
            card.getStyleClass().addAll("soldout", "disabled");
        } else if (isSelected) {
            card.getStyleClass().add("selected");
        }

        // Content container
        VBox contentBox = new VBox(8);
        if (isSoldOut) {
            contentBox.setPadding(new Insets(20));
        }

        // Header container - will hold either HBox (desktop) or VBox (mobile) layout
        VBox headerContainer = new VBox(8);

        // Room name
        Label nameLabel = new Label(option.getName());
        nameLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold");
        if (isSoldOut) {
            nameLabel.getStyleClass().add("bookingpage-text-muted-light");
        } else {
            nameLabel.getStyleClass().add("bookingpage-text-dark");
        }
        nameLabel.setWrapText(true);

        // Price container - shows per-night rate and total for duration
        int pricePerNight = option.getPricePerNight();
        VBox priceContainer = new VBox(2);

        if (!option.isDayVisitor()) {
            // Per-night price with "/night" label
            Label perNightLabel = new Label(formatPrice(pricePerNight) + "/night");
            perNightLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold");
            if (isSoldOut) {
                perNightLabel.getStyleClass().addAll("bookingpage-text-muted-light", "bookingpage-text-strikethrough");
            } else {
                perNightLabel.getStyleClass().add("bookingpage-text-dark");
            }
            priceContainer.getChildren().add(perNightLabel);

            // Total for duration (if we know the number of nights)
            int totalForDuration;
            if (option.hasPreCalculatedPrice()) {
                totalForDuration = option.getPreCalculatedTotalPrice();
            } else if (numberOfNights > 0) {
                totalForDuration = pricePerNight * numberOfNights;
            } else {
                totalForDuration = 0;
            }

            if (totalForDuration > 0 && numberOfNights > 0) {
                String nightsText = numberOfNights == 1 ? "night" : "nights";
                Label totalLabel = new Label(formatPrice(totalForDuration) + " for " + numberOfNights + " " + nightsText);
                totalLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");
                if (isSoldOut) {
                    totalLabel.getStyleClass().add("bookingpage-text-strikethrough");
                }
                priceContainer.getChildren().add(totalLabel);
            }

            // Per person/room indicator
            String pricingType = option.isPerPerson() ? "Per person" : "Per room";
            Label pricingTypeLabel = new Label(pricingType);
            pricingTypeLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");
            priceContainer.getChildren().add(pricingTypeLabel);
        } else {
            // Day visitor - just show "Free" or the price
            Label priceLabel = new Label(pricePerNight == 0 ? I18n.getI18nText(BookingPageI18nKeys.Free) : formatPrice(pricePerNight));
            priceLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold");
            if (isSoldOut) {
                priceLabel.getStyleClass().addAll("bookingpage-text-muted-light", "bookingpage-text-strikethrough");
            } else {
                priceLabel.getStyleClass().add("bookingpage-text-dark");
            }
            priceContainer.getChildren().add(priceLabel);
        }

        // Store checkmark badge reference (will be created later)
        StackPane checkmarkBadge = null;
        if (isAvailable) {
            checkmarkBadge = BookingPageUIBuilder.createCheckmarkBadgeCss(32);
            checkmarkBadge.setVisible(isSelected);
            checkmarkBadgeMap.put(option, checkmarkBadge);
        }

        // Track header components for responsive layout switching
        cardHeaderList.add(new CardHeaderComponents(headerContainer, nameLabel, priceContainer, checkmarkBadge, isAvailable));

        contentBox.getChildren().add(headerContainer);

        // === BADGES ROW: Constraint badge (minimum nights, etc.) ===
        if (option.hasConstraint()) {
            HBox badgesRow = new HBox(8);
            badgesRow.setAlignment(Pos.CENTER_LEFT);
            HBox constraintBadge = createConstraintBadge(option, isSoldOut);
            badgesRow.getChildren().add(constraintBadge);
            contentBox.getChildren().add(badgesRow);
        }

        // Description
        if (option.getDescription() != null && !option.getDescription().isEmpty()) {
            Label descLabel = new Label(option.getDescription());
            descLabel.getStyleClass().add("bookingpage-text-sm");
            if (isSoldOut) {
                descLabel.getStyleClass().add("bookingpage-text-muted-disabled");
            } else {
                descLabel.getStyleClass().add("bookingpage-text-muted");
            }
            descLabel.setWrapText(true);
            contentBox.getChildren().add(descLabel);
        }

        // === SHARING NOTE for "Per room" accommodations with capacity > 1 ===
        // Explains that one person books the room, roommates use "Share Accommodation"
        if (!option.isDayVisitor() && !option.isPerPerson()) {
            Item item = option.getItemEntity();
            Integer capacity = item != null ? item.getCapacity() : null;
            if (capacity != null && capacity > 1) {
                Label sharingNote = new Label("One person books the room, roommate(s) select 'Share Accommodation'");
                sharingNote.getStyleClass().add("bookingpage-text-sm");
                if (isSoldOut) {
                    sharingNote.getStyleClass().add("bookingpage-text-muted-disabled");
                } else {
                    sharingNote.getStyleClass().add("bookingpage-text-muted");
                }
                sharingNote.setWrapText(true);
                contentBox.getChildren().add(sharingNote);
            }
        }

        // Wrap content with checkmark badge or sold out ribbon
        if (isAvailable) {
            // Use the checkmark badge already created and stored in checkmarkBadgeMap
            StackPane existingCheckmarkBadge = checkmarkBadgeMap.get(option);

            StackPane wrapper = new StackPane(contentBox, existingCheckmarkBadge);
            StackPane.setAlignment(existingCheckmarkBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(existingCheckmarkBadge, new Insets(-8, -8, 0, 0));

            card.getChildren().add(wrapper);
        } else {
            // Add sold out ribbon
            StackPane wrapper = new StackPane(contentBox);
            wrapper.setMaxWidth(Double.MAX_VALUE);
            if (isSoldOut) {
                Node ribbon = createSoldOutRibbon();
                wrapper.getChildren().add(ribbon);
                StackPane.setAlignment(ribbon, Pos.TOP_RIGHT);

                Rectangle clip = new Rectangle();
                clip.setArcWidth(24);
                clip.setArcHeight(24);
                clip.widthProperty().bind(card.widthProperty());
                clip.heightProperty().bind(card.heightProperty());
                card.setClip(clip);
            }
            card.getChildren().add(wrapper);
        }

        // Click handler
        if (isAvailable) {
            card.setOnMouseClicked(e -> selectedOptionProperty.set(option));
        }

        return card;
    }

    private Node createSoldOutRibbon() {
        StackPane ribbon = new StackPane();
        ribbon.getStyleClass().add("bookingpage-soldout-ribbon");
        ribbon.setAlignment(Pos.CENTER);

        Label ribbonText = new Label("SOLD OUT");
        ribbonText.getStyleClass().add("bookingpage-soldout-ribbon-text");

        ribbon.setPadding(new Insets(4, 35, 4, 55));
        ribbon.getChildren().add(ribbonText);
        ribbon.setMaxWidth(Region.USE_PREF_SIZE);
        ribbon.setMaxHeight(Region.USE_PREF_SIZE);
        ribbon.setRotate(45);
        ribbon.setTranslateX(30);
        ribbon.setTranslateY(18);

        return ribbon;
    }

    /**
     * Creates a constraint badge for the accommodation option (e.g., "Minimum 2 nights").
     * Copied from DefaultAccommodationSelectionSection to maintain consistency.
     */
    private HBox createConstraintBadge(HasAccommodationSelectionSection.AccommodationOption option, boolean isSoldOut) {
        HBox badge = new HBox(5);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setMaxWidth(Region.USE_PREF_SIZE);
        badge.getStyleClass().add("bookingpage-badge-constraint");

        // Apply disabled class for sold out state - CSS handles colors
        if (isSoldOut) {
            badge.getStyleClass().add("disabled");
        }

        // Info icon (circle with "i" - using SVG path) - CSS handles colors
        SVGPath infoIcon = new SVGPath();
        infoIcon.setContent(ICON_INFO);
        infoIcon.setScaleX(0.5);
        infoIcon.setScaleY(0.5);
        infoIcon.getStyleClass().add("bookingpage-badge-constraint-icon");

        // Text
        String constraintText = option.getConstraintLabel();
        if (constraintText == null) {
            constraintText = option.getConstraintType() == HasAccommodationSelectionSection.ConstraintType.FULL_EVENT_ONLY
                ? I18n.getI18nText(BookingPageI18nKeys.FullFestivalOnly)
                : I18n.getI18nText(BookingPageI18nKeys.MinNights, option.getMinNights());
        }
        Label textLabel = new Label(constraintText);
        textLabel.getStyleClass().add("bookingpage-badge-constraint-text");

        badge.getChildren().addAll(infoIcon, textLabel);
        return badge;
    }

    private void updateExplanationText() {
        if (eventNameLabel != null) {
            // Use the new i18n key: "Due to high demand, {0} is now fully booked."
            String text = I18n.getI18nText(BookingPageI18nKeys.DueToHighDemandSoldOut, originalItemName);
            eventNameLabel.setText(text);
        }
    }

    private String formatPrice(int priceInCents) {
        return "$" + (priceInCents / 100);
    }

    // ========================================
    // RESPONSIVE DESIGN
    // ========================================

    /**
     * Sets up responsive design to switch card header layouts based on container width.
     */
    private void setupResponsiveDesign() {
        responsiveDesign = new ResponsiveDesign(container);

        // Desktop layout (width >= 450): horizontal name + price layout
        responsiveDesign.addResponsiveLayout(
            width -> width >= 450,
            () -> {
                isMobileLayout = false;
                applyCardLayout(false);
            }
        );

        // Mobile layout (width < 450): vertical stacked layout
        responsiveDesign.addResponsiveLayout(
            width -> width < 450,
            () -> {
                isMobileLayout = true;
                applyCardLayout(true);
            }
        );

        responsiveDesign.start();
    }

    /**
     * Applies the card header layout (horizontal or vertical) to all option cards.
     *
     * @param mobile true for vertical mobile layout, false for horizontal desktop layout
     */
    private void applyCardLayout(boolean mobile) {
        for (CardHeaderComponents header : cardHeaderList) {
            header.headerContainer.getChildren().clear();

            if (mobile) {
                // Mobile: stack name and price vertically
                header.nameLabel.setAlignment(Pos.CENTER_LEFT);
                header.priceContainer.setAlignment(Pos.CENTER_LEFT);

                header.headerContainer.setSpacing(8);
                header.headerContainer.getChildren().addAll(header.nameLabel, header.priceContainer);
            } else {
                // Desktop: name and price side by side with spacer
                header.nameLabel.setAlignment(Pos.CENTER_LEFT);
                header.priceContainer.setAlignment(Pos.TOP_RIGHT);

                HBox hbox = new HBox();
                hbox.setAlignment(Pos.TOP_LEFT);
                // Reserve space for checkmark badge on available cards
                if (header.isAvailable) {
                    hbox.setPadding(new Insets(0, 40, 0, 0));
                }

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                HBox.setHgrow(header.nameLabel, Priority.SOMETIMES);

                hbox.getChildren().addAll(header.nameLabel, spacer, header.priceContainer);
                header.headerContainer.getChildren().add(hbox);
            }
        }
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.ChooseAnotherOption;
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
        return validProperty;
    }

    // ========================================
    // HasAccommodationSoldOutSection INTERFACE
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
    public void setOriginalSelection(String itemName, int price) {
        this.originalItemName = itemName != null ? itemName : "";
        this.originalPrice = price;
        updateSoldOutIndicatorText();
        updateExplanationText();
    }

    @Override
    public void setEventName(String eventName) {
        this.eventName = eventName != null ? eventName : "";
        updateExplanationText();
    }

    @Override
    public void setNumberOfNights(int nights) {
        this.numberOfNights = nights;
    }

    @Override
    public void setAlternativeOptions(List<HasAccommodationSelectionSection.AccommodationOption> options) {
        alternativeOptions.setAll(options);
    }

    @Override
    public void clearOptions() {
        alternativeOptions.clear();
    }

    @Override
    public ObjectProperty<HasAccommodationSelectionSection.AccommodationOption> selectedOptionProperty() {
        return selectedOptionProperty;
    }

    @Override
    public void setOnOptionSelected(Consumer<HasAccommodationSelectionSection.AccommodationOption> callback) {
        this.onOptionSelected = callback;
    }

    @Override
    public String getValidationMessage() {
        return I18n.getI18nText(BookingPageI18nKeys.PleaseSelectAccommodation);
    }

    @Override
    public StandardBookingFormCallbacks.SoldOutRecoveryRoommateInfo getRoommateInfo() {
        if (roommateSection == null || !roommateSection.isVisible()) {
            return null;
        }

        boolean isRoomBooker = roommateSection.isRoomBooker();
        if (isRoomBooker) {
            List<String> names = roommateSection.getAllRoommateNames();
            return new StandardBookingFormCallbacks.SoldOutRecoveryRoommateInfo(true, names, null);
        } else {
            String ownerName = roommateSection.getRoommateName();
            return new StandardBookingFormCallbacks.SoldOutRecoveryRoommateInfo(false, null, ownerName);
        }
    }
}
