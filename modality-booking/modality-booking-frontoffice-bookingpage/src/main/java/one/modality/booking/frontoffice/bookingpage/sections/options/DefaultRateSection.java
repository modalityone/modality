package one.modality.booking.frontoffice.bookingpage.sections.options;
import one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.kit.util.properties.FXProperties;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.BookingPageUIBuilder;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.policy.service.PolicyAggregate;

import java.util.List;
import java.util.function.Consumer;

import static one.modality.booking.frontoffice.bookingpage.BookingPageCssSelectors.*;

/**
 * Default rate selection section for booking forms.
 * Allows users to choose between Standard Rate and Member Rate.
 *
 * <p>This is the default implementation of {@link HasRateTypeSection} that can be used
 * as-is or extended for organization-specific customizations.</p>
 *
 * @author Claude
 */
public class DefaultRateSection implements BookingFormSection, HasRateTypeSection {

    // UI Components
    private final VBox container = new VBox(16);
    private VBox standardRateCard;
    private VBox memberRateCard;

    // State
    private final ObjectProperty<RateType> selectedRateType = new SimpleObjectProperty<>(RateType.STANDARD);
    private final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);
    private final ObjectProperty<BookingFormColorScheme> colorSchemeProperty = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);

    // Working booking reference
    private WorkingBookingProperties workingBookingProperties;

    // Rates
    private int standardPrice = 0;
    private int memberPrice = 0;

    // Callbacks
    private Consumer<RateType> onRateTypeChanged;

    public DefaultRateSection() {
        buildUI();
    }

    private void buildUI() {
        // Section header
        StyledSectionHeader header = new StyledSectionHeader(
                BookingPageI18nKeys.YourPricingTier,
                StyledSectionHeader.ICON_TAG
        );

        // Rate cards container (FlowPane for responsive wrapping on mobile)
        FlowPane cardsContainer = new FlowPane();
        cardsContainer.setHgap(16);
        cardsContainer.setVgap(12);
        cardsContainer.setAlignment(Pos.TOP_LEFT);

        // Standard Rate card
        standardRateCard = createRateCard(
                BookingPageI18nKeys.StandardRate,
                RateType.STANDARD
        );

        // Member Rate card
        memberRateCard = createRateCard(
                BookingPageI18nKeys.MemberRate,
                RateType.MEMBER
        );

        cardsContainer.getChildren().addAll(standardRateCard, memberRateCard);

        container.getChildren().addAll(header, cardsContainer);
        container.getStyleClass().add("default-rate-section");
        container.setMinWidth(0);
    }

    private VBox createRateCard(Object titleKey, RateType rateType) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setMinWidth(160);
        card.setPrefWidth(180);
        card.setMaxWidth(220);
        card.getStyleClass().addAll(bookingpage_card, "default-rate-card");
        card.setCursor(Cursor.HAND);

        // Track selection state for this card
        SimpleBooleanProperty isSelected = new SimpleBooleanProperty(selectedRateType.get() == rateType);
        selectedRateType.addListener((obs, old, newVal) -> isSelected.set(newVal == rateType));

        // Checkmark indicator (top-right) - uses CSS-themed helper
        StackPane checkmark = BookingPageUIBuilder.createCheckmarkBadgeCss(20);
        checkmark.setVisible(isSelected.get());
        isSelected.addListener((obs, old, val) -> checkmark.setVisible(val));

        // Rate type label
        Label titleLabel = I18nControls.newLabel(titleKey);
        titleLabel.getStyleClass().addAll(bookingpage_text_base, bookingpage_font_semibold, bookingpage_text_dark);

        // Price label (per class)
        Label priceLabel = new Label();
        priceLabel.getStyleClass().addAll(bookingpage_text_sm, bookingpage_text_muted);

        // Store reference for price updates
        if (rateType == RateType.STANDARD) {
            priceLabel.setUserData("standardPrice");
        } else {
            priceLabel.setUserData("memberPrice");
        }

        // Card content with checkmark overlay
        BorderPane cardLayout = new BorderPane();
        VBox contentBox = new VBox(4);
        contentBox.getChildren().addAll(titleLabel, priceLabel);
        cardLayout.setCenter(contentBox);

        StackPane cardWithCheckmark = new StackPane(cardLayout, checkmark);
        StackPane.setAlignment(checkmark, Pos.TOP_RIGHT);
        StackPane.setMargin(checkmark, new Insets(-4, -4, 0, 0));

        card.getChildren().add(cardWithCheckmark);

        // Update card styling based on selection
        FXProperties.runNowAndOnPropertyChange(selected -> {
            if (selected) {
                if (!card.getStyleClass().contains(BookingPageCssSelectors.selected)) {
                    card.getStyleClass().add(BookingPageCssSelectors.selected);
                }
            } else {
                card.getStyleClass().remove(BookingPageCssSelectors.selected);
            }
        }, isSelected);

        // Click handler
        card.setOnMouseClicked(e -> {
            selectedRateType.set(rateType);
            if (onRateTypeChanged != null) {
                onRateTypeChanged.accept(rateType);
            }
            e.consume();
        });

        // Hover/selected effects are handled via CSS (.bookingpage-card:hover, .bookingpage-card.selected)
        // using color scheme variables (--booking-form-hover-border, --booking-form-primary)

        // Store price label reference for updates
        card.setUserData(priceLabel);

        return card;
    }

    private void loadData() {
        if (workingBookingProperties == null) return;

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();

        if (policyAggregate == null) return;

        // Try to get rate from first teaching scheduled item (more accurate for GP classes)
        Rate rate = null;
        List<ScheduledItem> teachingItems = policyAggregate.filterTeachingScheduledItems();
        if (!teachingItems.isEmpty()) {
            rate = policyAggregate.getScheduledItemDailyRate(teachingItems.get(0));
        }

        // Fallback to generic daily rate if no scheduled item rate found
        if (rate == null) {
            rate = policyAggregate.getDailyRate();
        }

        if (rate != null) {
            // Standard price from getPrice()
            if (rate.getPrice() != null) {
                standardPrice = rate.getPrice();
            }

            // Member price from getFacilityFeePrice() (facility fee = member price in context)
            if (rate.getFacilityFeePrice() != null) {
                memberPrice = rate.getFacilityFeePrice();
            } else {
                // Fallback to standard price if no member price defined
                memberPrice = standardPrice;
            }
        }

        // Update price labels
        updatePriceLabels();

        // Notify listener of initial selection
        if (onRateTypeChanged != null) {
            onRateTypeChanged.accept(selectedRateType.get());
        }
    }

    private void updatePriceLabels() {
        // Get the localized "per class" text
        String perClassText = I18n.getI18nText(BookingPageI18nKeys.PerClass);

        // Update standard rate card price
        if (standardRateCard != null && standardRateCard.getUserData() instanceof Label priceLabel) {
            priceLabel.setText(formatPrice(standardPrice) + " " + perClassText);
        }

        // Update member rate card price
        if (memberRateCard != null && memberRateCard.getUserData() instanceof Label priceLabel) {
            priceLabel.setText(formatPrice(memberPrice) + " " + perClassText);
        }
    }

    private String formatPrice(int priceInCents) {
        Event event = getEvent();
        return EventPriceFormatter.formatWithCurrency(priceInCents, event);
    }

    private Event getEvent() {
        if (workingBookingProperties != null && workingBookingProperties.getWorkingBooking() != null) {
            return workingBookingProperties.getWorkingBooking().getEvent();
        }
        return null;
    }

    // === BookingFormSection Implementation ===

    @Override
    public Object getTitleI18nKey() {
        return BookingPageI18nKeys.YourPricingTier;
    }

    @Override
    public Node getView() {
        return container;
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        this.workingBookingProperties = workingBookingProperties;
        loadData();
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return validProperty;
    }

    // === HasRateTypeSection Implementation ===

    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorSchemeProperty;
    }

    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        colorSchemeProperty.set(scheme);
    }

    @Override
    public RateType getSelectedRateType() {
        return selectedRateType.get();
    }

    @Override
    public void setOnPackageSelected(Consumer<one.modality.base.shared.entities.BookablePeriod> handler) {
        // Not used for general program class - individual date selection instead
    }

    @Override
    public void setOnRateTypeChanged(Consumer<RateType> handler) {
        this.onRateTypeChanged = handler;
    }
}
