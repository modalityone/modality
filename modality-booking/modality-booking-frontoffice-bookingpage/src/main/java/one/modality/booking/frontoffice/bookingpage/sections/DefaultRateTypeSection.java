package one.modality.booking.frontoffice.bookingpage.sections;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import one.modality.base.shared.entities.BookablePeriod;
import one.modality.base.shared.entities.Rate;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.components.StyledSectionHeader;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.document.service.PolicyAggregate;

import java.util.function.Consumer;

/**
 * Default implementation of the rate type/pricing section.
 * Displays programme info and standard rate in a styled card.
 *
 * <p>Uses CSS for styling - colors come from CSS variables that can be
 * overridden by theme classes (e.g., .theme-wisdom-blue) on a parent container.</p>
 *
 * <p>CSS classes used:</p>
 * <ul>
 *   <li>{@code .booking-form-rate-type-card} - card container</li>
 *   <li>{@code .booking-form-rate-type-badge} - rate type badge</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see HasRateTypeSection
 */
public class DefaultRateTypeSection implements HasRateTypeSection {

    protected final VBox container = new VBox(20);
    protected final SimpleBooleanProperty validProperty = new SimpleBooleanProperty(true);

    // Kept for API compatibility - theming is now CSS-based
    protected final ObjectProperty<BookingFormColorScheme> colorScheme = new SimpleObjectProperty<>(BookingFormColorScheme.DEFAULT);
    protected final RateType selectedRateType = RateType.STANDARD;

    protected WorkingBookingProperties workingBookingProperties;
    protected Rate currentRate;
    protected StyledSectionHeader header;
    protected Consumer<RateType> onRateTypeChanged;

    // Card UI components
    protected VBox card;
    protected Label rateBadge;
    protected Label priceLabel;
    protected Consumer<BookablePeriod> onPackageSelected;

    public DefaultRateTypeSection() {
        buildUI();
    }

    protected void buildUI() {
        // Section header
        header = new StyledSectionHeader(
                BookingPageI18nKeys.YourPricingTier,
                StyledSectionHeader.ICON_TAG
        );
        header.colorSchemeProperty().bind(colorScheme);

        // Simple card - styled via CSS
        card = new VBox(12);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("booking-form-rate-type-card");

        // Rate badge - styled via CSS
        rateBadge = new Label("Standard Rate");
        rateBadge.setPadding(new Insets(4, 10, 4, 10));
        rateBadge.getStyleClass().add("booking-form-rate-type-badge");

        card.getChildren().addAll(rateBadge, priceLabel);

        container.getChildren().addAll(header, card);
        container.getStyleClass().add("booking-form-rate-type-section");
        container.setMinWidth(0); // Allow shrinking for responsive design
        card.setMinWidth(0); // Allow card to shrink
    }

    /**
     * Applies styling to the card and badge components.
     * This method is called during buildUI and when color scheme changes.
     *
     * <p>In this class, styling is handled via CSS classes, so this method
     * is a no-op. Subclasses that need custom Java-based styling can override
     * this method to apply their own styling logic.</p>
     *
     * @deprecated Prefer using CSS classes for styling. This method exists
     * for backwards compatibility with subclasses.
     */
    @Deprecated
    protected void applyCardStyles() {
        // No-op: styling is handled via CSS classes in this implementation.
        // Subclasses can override to apply custom Java-based styling if needed.
    }

    protected void loadData() {
        if (workingBookingProperties == null) {
            return;
        }

        WorkingBooking workingBooking = workingBookingProperties.getWorkingBooking();
        PolicyAggregate policyAggregate = workingBooking.getPolicyAggregate();

        if (policyAggregate == null) {
            return;
        }

        currentRate = policyAggregate.getDailyRate();

        if (onRateTypeChanged != null) {
            onRateTypeChanged.accept(selectedRateType);
        }
    }


    // === BookingFormSection interface ===

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

    // === HasRateTypeSection interface ===

    /**
     * @deprecated Color scheme is now handled via CSS classes on parent container.
     * Use theme classes like "theme-wisdom-blue" on a parent element instead.
     */
    @Deprecated
    @Override
    public ObjectProperty<BookingFormColorScheme> colorSchemeProperty() {
        return colorScheme;
    }

    /**
     * @deprecated Use CSS theme classes instead.
     */
    @Deprecated
    @Override
    public void setColorScheme(BookingFormColorScheme scheme) {
        this.colorScheme.set(scheme);
    }

    @Override
    public RateType getSelectedRateType() {
        return selectedRateType;
    }

    @Override
    public Rate getCurrentRate() {
        return currentRate;
    }

    @Override
    public void setOnPackageSelected(Consumer<BookablePeriod> handler) {
        this.onPackageSelected = handler;
    }

    @Override
    public void setOnRateTypeChanged(Consumer<RateType> handler) {
        this.onRateTypeChanged = handler;
    }
}
