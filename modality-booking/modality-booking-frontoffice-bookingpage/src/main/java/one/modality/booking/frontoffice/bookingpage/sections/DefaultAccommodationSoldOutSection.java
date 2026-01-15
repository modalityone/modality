package one.modality.booking.frontoffice.bookingpage.sections;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Default implementation of the accommodation sold out recovery section.
 *
 * <p>This section is displayed when the user's selected accommodation becomes
 * unavailable (sold out) during booking submission. It provides a warm, non-alarming
 * interface for selecting an alternative accommodation.</p>
 *
 * <p>UI Structure:</p>
 * <ul>
 *   <li>Header: Warm cream background with refresh icon and title</li>
 *   <li>What Happened: Explanation box with original selection showing "SOLD OUT"</li>
 *   <li>Alternatives: List of available accommodation cards (reuses existing card pattern)</li>
 *   <li>Buttons: "Continue with New Selection" and "Cancel Registration"</li>
 * </ul>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .bookingpage-selectable-card} - accommodation card</li>
 *   <li>{@code .bookingpage-info-box-warning} - warning info box</li>
 *   <li>{@code .bookingpage-text-warning} - warning text color</li>
 * </ul>
 *
 * @author Claude Code
 * @see HasAccommodationSoldOutSection
 */
public class DefaultAccommodationSoldOutSection implements HasAccommodationSoldOutSection {

    // === SVG ICONS ===
    private static final String ICON_REFRESH = "M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8M3 3v5h5M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16M16 21h5v-5";
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
    private Label originalNameLabel;
    private Label originalPriceLabel;
    private Label eventNameLabel;
    private final Map<HasAccommodationSelectionSection.AccommodationOption, VBox> optionCardMap = new HashMap<>();
    private final Map<HasAccommodationSelectionSection.AccommodationOption, StackPane> checkmarkBadgeMap = new HashMap<>();

    // === CALLBACKS ===
    private Consumer<HasAccommodationSelectionSection.AccommodationOption> onOptionSelected;

    // === DATA ===
    private WorkingBookingProperties workingBookingProperties;

    public DefaultAccommodationSoldOutSection() {
        buildUI();
        setupBindings();
    }

    private void buildUI() {
        container.setAlignment(Pos.TOP_CENTER);
        container.setSpacing(0);
        container.setMaxWidth(720);

        // === HEADER SECTION (warm cream background) ===
        VBox header = createHeader();

        // === BODY SECTION ===
        VBox body = new VBox(24);
        body.setPadding(new Insets(24));

        // What happened section
        VBox whatHappened = createWhatHappenedSection();

        // Section title for alternatives
        VBox alternativesHeader = new VBox(4);
        Label alternativesTitle = I18nControls.newLabel(BookingPageI18nKeys.ChooseNewAccommodation);
        alternativesTitle.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold", "bookingpage-text-dark");
        alternativesHeader.getChildren().add(alternativesTitle);

        // Options container for alternative accommodations
        optionsContainer = new VBox(12);
        optionsContainer.setAlignment(Pos.TOP_CENTER);
        optionsContainer.setFillWidth(true);

        body.getChildren().addAll(whatHappened, alternativesHeader, optionsContainer);

        container.getChildren().addAll(header, body);
    }

    private VBox createHeader() {
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20, 24, 20, 24));
        // Warm cream background color (#FFF8E7) - matching JSX mockup
        header.setStyle("-fx-background-color: #FFF8E7; -fx-border-color: #F0E4CC; -fx-border-width: 0 0 1 0;");

        // Refresh icon in amber circle (smaller)
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(48, 48);
        iconCircle.setMaxSize(48, 48);
        iconCircle.setStyle("-fx-background-color: white; -fx-background-radius: 24; -fx-border-color: #E5A545; -fx-border-width: 2; -fx-border-radius: 24;");

        SVGPath refreshIcon = new SVGPath();
        refreshIcon.setContent(ICON_REFRESH);
        refreshIcon.setFill(Color.TRANSPARENT);
        refreshIcon.setStroke(Color.web("#E5A545"));
        refreshIcon.setStrokeWidth(2);
        refreshIcon.setScaleX(1.0);
        refreshIcon.setScaleY(1.0);
        iconCircle.getChildren().add(refreshIcon);

        // Title (smaller)
        Label title = I18nControls.newLabel(BookingPageI18nKeys.AccommodationUpdateNeeded);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #5D4E37;");

        // Subtitle (smaller)
        Label subtitle = I18nControls.newLabel(BookingPageI18nKeys.AccommodationNoLongerAvailable);
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B5D4D;");
        subtitle.setWrapText(true);

        header.getChildren().addAll(iconCircle, title, subtitle);
        VBox.setMargin(title, new Insets(8, 0, 2, 0));

        return header;
    }

    private VBox createWhatHappenedSection() {
        VBox section = new VBox(12);
        section.setPadding(new Insets(16));
        section.setStyle("-fx-background-color: #F7F6F3; -fx-background-radius: 12; -fx-border-color: #E8E6E1; -fx-border-radius: 12; -fx-border-width: 1;");

        // What happened header with icon
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        SVGPath infoIcon = new SVGPath();
        infoIcon.setContent(ICON_INFO);
        infoIcon.setScaleX(0.75);
        infoIcon.setScaleY(0.75);
        infoIcon.setFill(Color.web("#5D4E37"));

        Label headerLabel = I18nControls.newLabel(BookingPageI18nKeys.WhatHappened);
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #5D4E37;");

        headerRow.getChildren().addAll(infoIcon, headerLabel);

        // Explanation text
        eventNameLabel = new Label();
        eventNameLabel.setWrapText(true);
        eventNameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B5D4D;");
        updateExplanationText();

        // Original selection box (with SOLD OUT badge)
        HBox originalBox = createOriginalSelectionBox();

        section.getChildren().addAll(headerRow, eventNameLabel, originalBox);

        return section;
    }

    private HBox createOriginalSelectionBox() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12, 16, 12, 16));
        // Light red/pink background for sold out - danger styling
        box.setStyle("-fx-background-color: #FEF2F2; -fx-background-radius: 8; -fx-border-color: #FECACA; -fx-border-radius: 8; -fx-border-width: 1;");

        // SOLD OUT badge
        Label badge = new Label("SOLD OUT");
        badge.setPadding(new Insets(4, 10, 4, 10));
        badge.setStyle("-fx-background-color: #DC2626; -fx-background-radius: 6; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: 600;");

        // Original item name
        originalNameLabel = new Label(originalItemName);
        originalNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #991B1B;");
        HBox.setHgrow(originalNameLabel, Priority.ALWAYS);

        // Original price (strikethrough)
        originalPriceLabel = new Label(formatPrice(originalPrice));
        originalPriceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #991B1B; -fx-strikethrough: true;");

        box.getChildren().addAll(badge, originalNameLabel, originalPriceLabel);

        return box;
    }

    private void setupBindings() {
        // Update validity and visual state when option is selected
        selectedOptionProperty.addListener((obs, oldOption, newOption) -> {
            boolean isValid = newOption != null && newOption.isAvailable();
            validProperty.set(isValid);

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

            // Notify callback
            if (onOptionSelected != null && newOption != null) {
                onOptionSelected.accept(newOption);
            }
        });

        // Rebuild cards when list changes
        alternativeOptions.addListener((ListChangeListener<HasAccommodationSelectionSection.AccommodationOption>) change ->
            UiScheduler.runInUiThread(this::rebuildOptionCards));
    }

    private void rebuildOptionCards() {
        optionsContainer.getChildren().clear();
        optionCardMap.clear();
        checkmarkBadgeMap.clear();

        HasAccommodationSelectionSection.AccommodationOption currentlySelected = selectedOptionProperty.get();
        Object selectedItemId = currentlySelected != null ? currentlySelected.getItemId() : null;

        HasAccommodationSelectionSection.AccommodationOption matchingOption = null;

        for (HasAccommodationSelectionSection.AccommodationOption option : alternativeOptions) {
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

    /**
     * Creates an accommodation option card using the same pattern as DefaultAccommodationSelectionSection.
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

        // Header row: Room name + Total Price
        BorderPane headerRow = new BorderPane();
        if (isAvailable) {
            headerRow.setPadding(new Insets(0, 40, 0, 0));
        }

        // Room name
        Label nameLabel = new Label(option.getName());
        nameLabel.getStyleClass().addAll("bookingpage-text-lg", "bookingpage-font-semibold");
        if (isSoldOut) {
            nameLabel.getStyleClass().add("bookingpage-text-muted-light");
        } else {
            nameLabel.getStyleClass().add("bookingpage-text-dark");
        }
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(200);

        // Price container - shows per-night rate and total for duration
        int pricePerNight = option.getPricePerNight();
        VBox priceContainer = new VBox(2);
        priceContainer.setAlignment(Pos.TOP_RIGHT);

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

        headerRow.setLeft(nameLabel);
        headerRow.setRight(priceContainer);
        BorderPane.setAlignment(nameLabel, Pos.TOP_LEFT);
        BorderPane.setAlignment(priceContainer, Pos.TOP_RIGHT);
        BorderPane.setMargin(priceContainer, new Insets(0, 0, 0, 12));

        contentBox.getChildren().add(headerRow);

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

        // Wrap content with checkmark badge or sold out ribbon
        if (isAvailable) {
            StackPane checkmarkBadge = BookingPageUIBuilder.createCheckmarkBadgeCss(32);
            checkmarkBadge.setVisible(isSelected);
            checkmarkBadgeMap.put(option, checkmarkBadge);

            StackPane wrapper = new StackPane(contentBox, checkmarkBadge);
            StackPane.setAlignment(checkmarkBadge, Pos.TOP_RIGHT);
            StackPane.setMargin(checkmarkBadge, new Insets(-8, -8, 0, 0));

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
        ribbonText.setStyle("-fx-font-size: 9px;");

        ribbon.setPadding(new Insets(4, 35, 4, 55));
        ribbon.getChildren().add(ribbonText);
        ribbon.setMaxWidth(Region.USE_PREF_SIZE);
        ribbon.setMaxHeight(Region.USE_PREF_SIZE);
        ribbon.setStyle("-fx-background-color: #78716c;");
        ribbon.setRotate(45);
        ribbon.setTranslateX(30);
        ribbon.setTranslateY(18);

        return ribbon;
    }

    private void updateExplanationText() {
        if (eventNameLabel != null) {
            String text = I18n.getI18nText(BookingPageI18nKeys.WhatHappenedSoldOut, eventName, originalItemName);
            eventNameLabel.setText(text);
        }
    }

    private String formatPrice(int priceInCents) {
        return "$" + (priceInCents / 100);
    }

    // ========================================
    // BookingFormSection INTERFACE
    // ========================================

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.AccommodationUpdateNeeded;
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
        if (originalNameLabel != null) {
            originalNameLabel.setText(this.originalItemName);
        }
        if (originalPriceLabel != null) {
            originalPriceLabel.setText(formatPrice(this.originalPrice));
        }
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
}
