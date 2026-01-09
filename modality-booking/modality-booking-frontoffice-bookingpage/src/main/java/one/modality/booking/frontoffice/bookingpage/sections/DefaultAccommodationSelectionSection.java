package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.orm.entity.Entities;
import javafx.beans.property.*;
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
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Default implementation of the accommodation selection section.
 * Displays accommodation/room options and allows selection.
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Selectable cards for each accommodation option</li>
 *   <li>Shows total price (teaching + accommodation for full festival)</li>
 *   <li>"Sold Out" corner ribbon for unavailable rooms</li>
 *   <li>Constraint badges ("Full Festival Only", "Minimum 3 nights")</li>
 *   <li>"LIMITED" badge for low availability</li>
 *   <li>Selection checkmark animation</li>
 * </ul>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-accommodation-section} - section container</li>
 *   <li>{@code .bookingpage-accommodation-card} - accommodation card</li>
 *   <li>{@code .bookingpage-accommodation-card.selected} - selected state</li>
 *   <li>{@code .bookingpage-accommodation-card.soldout} - sold out state</li>
 *   <li>{@code .bookingpage-badge-constraint} - constraint badge</li>
 *   <li>{@code .bookingpage-badge-limited} - limited availability badge</li>
 *   <li>{@code .bookingpage-ribbon-soldout} - sold out ribbon</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasAccommodationSelectionSection
 */
public class DefaultAccommodationSelectionSection implements HasAccommodationSelectionSection {

    // === COLOR SCHEME ===
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // === VALIDITY ===
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(false);

    // === SELECTED OPTION ===
    protected final ObjectProperty<AccommodationOption> selectedOptionProperty = new SimpleObjectProperty<>();

    // === OPTIONS LIST ===
    protected final ObservableList<AccommodationOption> accommodationOptions = FXCollections.observableArrayList();

    // === PRICING INFO ===
    protected int fullEventTeachingPrice = 0;
    protected int fullEventNights = 0;
    protected int fullEventMealsPrice = 0;

    // === UI COMPONENTS ===
    protected final VBox container = new VBox();
    protected VBox optionsContainer;  // Changed from FlowPane to VBox for full-width cards
    protected HBox priceIncludesInfoBox;
    protected final Map<AccommodationOption, VBox> optionCardMap = new HashMap<>();
    protected final Map<AccommodationOption, StackPane> checkmarkBadgeMap = new HashMap<>();

    // === CALLBACKS ===
    protected Consumer<AccommodationOption> onOptionSelected;
    protected Runnable onContinuePressed;
    protected Runnable onBackPressed;

    // === DATA ===
    protected WorkingBookingProperties workingBookingProperties;

    public DefaultAccommodationSelectionSection() {
        buildUI();
        setupBindings();
    }

    protected void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.getStyleClass().add("bookingpage-accommodation-section");

        // Section header with icon
        HBox sectionHeader = new StyledSectionHeader(BookingPageI18nKeys.AccommodationOptions, StyledSectionHeader.ICON_HOME);

        // Info box explaining what price includes (per JSX mockup)
        // Note: Price shown is teachings + accommodation only (meals are selected in the next step)
        priceIncludesInfoBox = BookingPageUIBuilder.createPriceIncludesInfoBox(
            "All teachings and accommodation for the full event.",
            "You can adjust dates and options like meals in the next step"
        );

        // Options container - VBox for full-width cards (changed from FlowPane)
        optionsContainer = new VBox(12);  // 12px gap between cards per JSX
        optionsContainer.setAlignment(Pos.TOP_CENTER);
        optionsContainer.setFillWidth(true);  // Ensure cards take full width

        container.getChildren().addAll(sectionHeader, priceIncludesInfoBox, optionsContainer);
        VBox.setMargin(sectionHeader, new Insets(0, 0, 16, 0));
        VBox.setMargin(priceIncludesInfoBox, new Insets(0, 0, 20, 0));  // marginBottom: 20px per JSX
    }

    protected void setupBindings() {
        // Update validity and visual state when option is selected
        selectedOptionProperty.addListener((obs, oldOption, newOption) -> {
            boolean isValid = newOption != null && newOption.isAvailable();
            validProperty.set(isValid);

            // Update card styles via CSS classes and checkmark visibility
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

            // Notify callback
            if (onOptionSelected != null && newOption != null) {
                onOptionSelected.accept(newOption);
            }
        });

        // Rebuild cards when list changes
        accommodationOptions.addListener((ListChangeListener<AccommodationOption>) change ->
            UiScheduler.runInUiThread(this::rebuildOptionCards));
    }

    protected void rebuildOptionCards() {
        optionsContainer.getChildren().clear();
        optionCardMap.clear();
        checkmarkBadgeMap.clear();

        AccommodationOption currentlySelected = selectedOptionProperty.get();
        Object selectedItemId = currentlySelected != null ? currentlySelected.getItemId() : null;

        AccommodationOption matchingOption = null;

        for (AccommodationOption option : accommodationOptions) {
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
    }

    protected VBox createOptionCard(AccommodationOption option, boolean isSelected) {
        boolean isSoldOut = option.getAvailability() == AvailabilityStatus.SOLD_OUT;
        boolean isAvailable = option.isAvailable();

        VBox card = new VBox(8);
        card.setMaxWidth(Double.MAX_VALUE);  // Full width cards
        card.setPadding(new Insets(20));
        card.getStyleClass().add("bookingpage-selectable-card");

        // Apply CSS classes for different states (styling handled in CSS)
        if (isSoldOut) {
            card.getStyleClass().addAll("soldout", "disabled");
        } else if (isSelected) {
            card.getStyleClass().add("selected");
        }

        // Content container
        VBox contentBox = new VBox(8);

        // === HEADER ROW: Room name (left) + Total Price (right) ===
        // Using BorderPane for proper space-between layout like JSX
        BorderPane headerRow = new BorderPane();
        // Always add right padding for available cards to avoid price/checkmark overlap
        // (checkmark can appear at any time when user selects the card)
        if (isAvailable) {
            headerRow.setPadding(new Insets(0, 40, 0, 0));
        }

        // Room name (left side) - 16px, semibold
        Label nameLabel = new Label(option.getName());
        nameLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold");
        if (isSoldOut) {
            nameLabel.getStyleClass().add("bookingpage-text-muted-light");
        } else {
            nameLabel.getStyleClass().add("bookingpage-text-dark");
        }
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(200); // Prevent name from pushing price off

        // Total price (right side) with per person/room indicator
        int totalPrice = calculateTotalPrice(option);

        // Price container (VBox with price and per person/room text)
        VBox priceContainer = new VBox(2);
        priceContainer.setAlignment(Pos.TOP_RIGHT);

        Label priceLabel = new Label(formatPrice(totalPrice));
        priceLabel.getStyleClass().addAll("bookingpage-text-2xl", "bookingpage-font-bold");
        if (isSoldOut) {
            priceLabel.getStyleClass().addAll("bookingpage-text-muted-light", "bookingpage-text-strikethrough");
        } else {
            priceLabel.getStyleClass().add("bookingpage-text-dark");
        }

        // Per person / Per room indicator (skip for Day Visitor and Share Accommodation)
        if (!option.isDayVisitor()) {
            String pricingType = option.isPerPerson() ? "Per person" : "Per room";
            Label pricingTypeLabel = new Label(pricingType);
            pricingTypeLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");
            priceContainer.getChildren().addAll(priceLabel, pricingTypeLabel);
        } else {
            priceContainer.getChildren().add(priceLabel);
        }

        headerRow.setLeft(nameLabel);
        headerRow.setRight(priceContainer);
        BorderPane.setAlignment(nameLabel, Pos.TOP_LEFT);
        BorderPane.setAlignment(priceContainer, Pos.TOP_RIGHT);
        BorderPane.setMargin(priceContainer, new Insets(0, 0, 0, 12)); // marginLeft like JSX

        contentBox.getChildren().add(headerRow);

        // === BADGES ROW: Constraint badge only (LIMITED badge removed per user request) ===
        if (option.hasConstraint()) {
            HBox badgesRow = new HBox(8);
            badgesRow.setAlignment(Pos.CENTER_LEFT);
            HBox constraintBadge = createConstraintBadge(option, isSoldOut);
            badgesRow.getChildren().add(constraintBadge);
            contentBox.getChildren().add(badgesRow);
        }

        // === DESCRIPTION (at bottom, 13px muted) ===
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

        // === SHARING NOTE for "Per room" accommodations ===
        // Explains that one person books the room, roommates use "Share Accommodation"
        if (!option.isDayVisitor() && !option.isPerPerson()) {
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

        // Wrap content with checkmark badge or sold out ribbon
        if (isAvailable) {
            // Create checkmark badge (CSS-themed, 32px like JSX)
            StackPane checkmarkBadge = BookingPageUIBuilder.createCheckmarkBadgeCss(32);
            checkmarkBadge.setVisible(isSelected);
            checkmarkBadgeMap.put(option, checkmarkBadge);

            // StackPane wrapper to position checkmark in top-right corner
            StackPane wrapper = new StackPane(contentBox, checkmarkBadge);
            StackPane.setAlignment(checkmarkBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(checkmarkBadge, new Insets(-8, -8, 0, 0));

            card.getChildren().add(wrapper);
        } else {
            // Add sold out ribbon
            StackPane wrapper = new StackPane(contentBox);
            if (isSoldOut) {
                Node ribbon = createSoldOutRibbon();
                wrapper.getChildren().add(ribbon);
                StackPane.setAlignment(ribbon, Pos.TOP_RIGHT);

                // Apply rounded rectangle clip to card to contain ribbon within borders
                // Match border-radius of 12px
                Rectangle clip = new Rectangle();
                clip.setArcWidth(24); // 2x border-radius for arc
                clip.setArcHeight(24);
                clip.widthProperty().bind(card.widthProperty());
                clip.heightProperty().bind(card.heightProperty());
                card.setClip(clip);
            }
            card.getChildren().add(wrapper);
        }

        // Click handler (only if available)
        if (isAvailable) {
            card.setOnMouseClicked(e -> handleOptionClick(option));
        }

        return card;
    }

    protected HBox createPriceRow(int totalPrice, int pricePerNight) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        // Total price
        Label totalLabel = new Label(formatPrice(totalPrice));
        totalLabel.getStyleClass().addAll("bookingpage-text-xl", "bookingpage-font-bold", "bookingpage-text-primary");

        // "total" text
        Label totalTextLabel = I18nControls.newLabel(BookingPageI18nKeys.Total);
        totalTextLabel.getStyleClass().addAll("bookingpage-text-sm", "bookingpage-text-muted");

        row.getChildren().addAll(totalLabel, totalTextLabel);

        // Per night breakdown (if applicable)
        if (pricePerNight > 0 && fullEventNights > 0) {
            Label breakdownLabel = new Label(" (" + formatPrice(pricePerNight) + "/night)");
            breakdownLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-text-muted");
            row.getChildren().add(breakdownLabel);
        }

        return row;
    }

    protected HBox createConstraintBadge(AccommodationOption option, boolean isSoldOut) {
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
        // Circle with 'i' icon path
        infoIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z");
        infoIcon.setScaleX(0.5);
        infoIcon.setScaleY(0.5);
        infoIcon.getStyleClass().add("bookingpage-badge-constraint-icon");

        // Text
        String constraintText = option.getConstraintLabel();
        if (constraintText == null) {
            constraintText = option.getConstraintType() == ConstraintType.FULL_EVENT_ONLY
                ? I18n.getI18nText(BookingPageI18nKeys.FullFestivalOnly)
                : I18n.getI18nText(BookingPageI18nKeys.MinNights, option.getMinNights());
        }
        Label textLabel = new Label(constraintText);
        textLabel.getStyleClass().add("bookingpage-badge-constraint-text");

        badge.getChildren().addAll(infoIcon, textLabel);
        return badge;
    }

    protected HBox createLimitedBadge() {
        HBox badge = new HBox(4);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(2, 6, 2, 6));
        badge.getStyleClass().add("bookingpage-badge-limited");

        Label icon = new Label("\u26A0"); // Warning sign
        icon.getStyleClass().addAll("bookingpage-text-xs");

        Label textLabel = I18nControls.newLabel(BookingPageI18nKeys.LimitedAvailability);
        textLabel.getStyleClass().addAll("bookingpage-text-xs", "bookingpage-font-medium");

        badge.getChildren().addAll(icon, textLabel);
        return badge;
    }

    protected Node createSoldOutRibbon() {
        // Create a diagonal corner ribbon in the top-right corner (per JSX mockup)
        // Style: warm gray background (#78716c), light text (#fafaf9), rotated 45deg
        StackPane ribbon = new StackPane();
        ribbon.getStyleClass().add("bookingpage-soldout-ribbon");

        // Text styling - CSS handles font and color
        Label ribbonText = new Label("SOLD OUT");
        ribbonText.getStyleClass().add("bookingpage-soldout-ribbon-text");

        ribbon.getChildren().add(ribbonText);
        ribbon.setMaxWidth(Region.USE_PREF_SIZE);
        ribbon.setMaxHeight(Region.USE_PREF_SIZE);
        ribbon.setOpacity(1.0);  // Ensure no transparency

        // Rotate for diagonal effect (45deg per JSX)
        // Using setRotate() rotates around the center of the node (unlike getTransforms().add(new Rotate()))
        ribbon.setRotate(45);

        // Position in top-right corner relative to TOP_RIGHT alignment
        ribbon.setTranslateX(25);
        ribbon.setTranslateY(5);

        return ribbon;
    }

    protected void handleOptionClick(AccommodationOption option) {
        if (!option.isAvailable()) {
            return;
        }
        selectedOptionProperty.set(option);
    }

    protected int calculateTotalPrice(AccommodationOption option) {
        // For "Day Visitor" option, no accommodation cost but include meals
        if (option.isDayVisitor()) {
            return fullEventTeachingPrice + fullEventMealsPrice;
        }
        // Total = teaching price + meals + (accommodation per night * nights)
        return fullEventTeachingPrice + fullEventMealsPrice + (option.getPricePerNight() * fullEventNights);
    }

    protected String formatPrice(int priceInCents) {
        // TODO: Use proper currency formatting from WorkingBookingProperties
        return "$" + (priceInCents / 100);
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.AccommodationOptions;
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
    // HasAccommodationSelectionSection INTERFACE
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
    public void setFullEventTeachingPrice(int price) {
        this.fullEventTeachingPrice = price;
        rebuildOptionCards();
    }

    @Override
    public void setFullEventNights(int nights) {
        this.fullEventNights = nights;
        rebuildOptionCards();
    }

    @Override
    public void setFullEventMealsPrice(int price) {
        this.fullEventMealsPrice = price;
        rebuildOptionCards();
    }

    @Override
    public void addAccommodationOption(AccommodationOption option) {
        accommodationOptions.add(option);
    }

    @Override
    public void clearOptions() {
        accommodationOptions.clear();
        selectedOptionProperty.set(null);
    }

    @Override
    public void setSelectedOption(Object itemId) {
        for (AccommodationOption option : accommodationOptions) {
            if (option.getItemId() != null && option.getItemId().equals(itemId)) {
                selectedOptionProperty.set(option);
                return;
            }
        }
    }

    @Override
    public ObjectProperty<AccommodationOption> selectedOptionProperty() {
        return selectedOptionProperty;
    }

    @Override
    public void setOnOptionSelected(Consumer<AccommodationOption> callback) {
        this.onOptionSelected = callback;
    }

    @Override
    public void setOnContinuePressed(Runnable callback) {
        this.onContinuePressed = callback;
    }

    @Override
    public void setOnBackPressed(Runnable callback) {
        this.onBackPressed = callback;
    }

    // ========================================
    // DATA POPULATION FROM POLICY AGGREGATE
    // ========================================

    /**
     * Populates accommodation options from PolicyAggregate data.
     * Checks availability using ScheduledItem.guestsAvailability and
     * constraints using ItemPolicy.minDay.
     *
     * @param policyAggregate the policy data containing scheduledItems and itemPolicies
     * @param limitedThreshold availability count below which to show as LIMITED (e.g., 5)
     */
    @Override
    public void populateFromPolicyAggregate(PolicyAggregate policyAggregate, int limitedThreshold) {
        clearOptions();

        if (policyAggregate == null) {
            return;
        }

        // Group accommodation ScheduledItems by Item
        List<ScheduledItem> accommodationItems = policyAggregate.filterAccommodationScheduledItems();
        Map<Item, List<ScheduledItem>> itemScheduledItems = accommodationItems.stream()
            .filter(si -> si.getItem() != null)
            .collect(Collectors.groupingBy(ScheduledItem::getItem));

        // Sort entries by Item.getOrd() to display in correct order
        List<Map.Entry<Item, List<ScheduledItem>>> sortedEntries = itemScheduledItems.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().getOrd() != null ? e.getKey().getOrd() : Integer.MAX_VALUE))
            .collect(Collectors.toList());

        for (Map.Entry<Item, List<ScheduledItem>> entry : sortedEntries) {
            Item item = entry.getKey();
            List<ScheduledItem> scheduledItems = entry.getValue();

            // Calculate minimum availability across all days
            int minAvailability = scheduledItems.stream()
                .mapToInt(si -> si.getGuestsAvailability() != null ? si.getGuestsAvailability() : 0)
                .min()
                .orElse(0);

            // Determine availability status
            AvailabilityStatus status;
            if (minAvailability <= 0) {
                status = AvailabilityStatus.SOLD_OUT;
            } else if (minAvailability <= limitedThreshold) {
                status = AvailabilityStatus.LIMITED;
            } else {
                status = AvailabilityStatus.AVAILABLE;
            }

            // Get constraint from ItemPolicy
            ItemPolicy itemPolicy = policyAggregate.getItemPolicy(item);
            ConstraintType constraintType = ConstraintType.NONE;
            String constraintLabel = null;
            int minNights = 0;

            if (itemPolicy != null && itemPolicy.getMinDay() != null && itemPolicy.getMinDay() > 0) {
                constraintType = ConstraintType.MIN_NIGHTS;
                minNights = itemPolicy.getMinDay();
                constraintLabel = I18n.getI18nText(BookingPageI18nKeys.MinNights, minNights);
            }

            // Get price and perPerson flag from rates (accommodation daily rate)
            // Try to find rate for this item - first try with null site, then try all rates
            Rate itemRate = policyAggregate.filterDailyRatesStreamOfSiteAndItem(null, item)
                .findFirst()
                .orElseGet(() -> {
                    // Fallback: search all daily rates for this item regardless of site
                    return policyAggregate.getDailyRatesStream()
                        .filter(r -> r.getItem() != null && r.getItem().getPrimaryKey() != null
                            && r.getItem().getPrimaryKey().equals(item.getPrimaryKey()))
                        .findFirst()
                        .orElse(null);
                });

            int pricePerNight = itemRate != null && itemRate.getPrice() != null ? itemRate.getPrice() : 0;
            // Per person = true (default), Per room = false
            boolean perPerson = itemRate == null || !Boolean.FALSE.equals(itemRate.isPerPerson());

            Console.log("DefaultAccommodationSection: Item '" + item.getName() + "' pricePerNight=" + pricePerNight + ", perPerson=" + perPerson);

            // Get item name and description
            String name = item.getName() != null ? item.getName() : "";
            // Note: Item doesn't have a direct description field, use empty string
            // In a production implementation, this could be fetched from a related entity
            String description = "";

            // Create AccommodationOption with perPerson info
            AccommodationOption option = new AccommodationOption(
                item.getPrimaryKey(),
                item,
                name,
                description,
                pricePerNight,
                status,
                constraintType,
                constraintLabel,
                minNights,
                false, // isDayVisitor - handled separately
                null,  // imageUrl
                perPerson  // per person or per room pricing
            );

            addAccommodationOption(option);
        }
    }
}
