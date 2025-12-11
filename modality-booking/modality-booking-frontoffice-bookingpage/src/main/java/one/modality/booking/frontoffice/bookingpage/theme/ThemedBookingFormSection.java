package one.modality.booking.frontoffice.bookingpage.theme;

import javafx.beans.value.ObservableBooleanValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.*;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;

/**
 * Decorator that wraps a BookingFormSection with color scheme theming.
 * Applies consistent styling (borders, backgrounds, colors) based on the provided color scheme.
 *
 * <p>This follows the Decorator design pattern, allowing any BookingFormSection to be
 * themed without modifying the original section class.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * BookingFormSection packageSection = new PackageSection();
 * BookingFormSection themedSection = new ThemedBookingFormSection(packageSection, BookingFormColorScheme.WISDOM_BLUE);
 * </pre>
 *
 * @author Bruno Salmon
 */
public class ThemedBookingFormSection implements BookingFormSection {

    private final BookingFormSection delegate;
    private final BookingFormColorScheme colorScheme;
    private final VBox themedContainer;

    /**
     * Creates a themed wrapper around an existing BookingFormSection.
     *
     * @param section The section to wrap and theme
     * @param colorScheme The color scheme to apply
     */
    public ThemedBookingFormSection(BookingFormSection section, BookingFormColorScheme colorScheme) {
        this.delegate = section;
        this.colorScheme = colorScheme;
        this.themedContainer = new VBox();
        themedContainer.setPadding(new Insets(0, 0, 32, 0)); // 32px bottom margin between sections (per JSX mockup)
        themedContainer.getChildren().add(delegate.getView());
        applyTheme();
    }

    /**
     * Applies the color scheme styling to the section container.
     * Note: The background/border styling is now handled by StyledSectionHeader
     * which applies only to the header portion. The content area has a white
     * background with subtle border per the JSX mockup design.
     */
    private void applyTheme() {
        // Section container has transparent background, no border
        // The colored header styling is handled by StyledSectionHeader component

        // Set spacing between header and content
        themedContainer.setSpacing(0);

        // Apply transparent background with no border - using pure JavaFX API for cross-platform compatibility
        themedContainer.setBackground(Background.EMPTY);
        themedContainer.setBorder(Border.EMPTY);

        // Add CSS class for additional styling via CSS files
        themedContainer.getStyleClass().add("booking-form-themed-section");
    }

    /**
     * Returns the color scheme applied to this section.
     *
     * @return The current color scheme
     */
    public BookingFormColorScheme getColorScheme() {
        return colorScheme;
    }

    /**
     * Returns the wrapped (delegate) section.
     *
     * @return The original unwrapped section
     */
    public BookingFormSection getDelegate() {
        return delegate;
    }

    // === BookingFormSection interface delegation ===

    @Override
    public Object getTitleI18nKey() {
        return delegate.getTitleI18nKey();
    }

    @Override
    public Node getView() {
        return themedContainer;
    }

    @Override
    public void onTransitionFinished() {
        delegate.onTransitionFinished();
    }

    @Override
    public boolean isApplicableToBooking(WorkingBooking workingBooking) {
        return delegate.isApplicableToBooking(workingBooking);
    }

    @Override
    public void setWorkingBookingProperties(WorkingBookingProperties workingBookingProperties) {
        delegate.setWorkingBookingProperties(workingBookingProperties);
    }

    @Override
    public boolean isValid() {
        return delegate.isValid();
    }

    @Override
    public ObservableBooleanValue validProperty() {
        return delegate.validProperty();
    }
}
