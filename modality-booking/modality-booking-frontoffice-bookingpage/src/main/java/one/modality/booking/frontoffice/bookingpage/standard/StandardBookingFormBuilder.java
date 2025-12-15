package one.modality.booking.frontoffice.bookingpage.standard;

import one.modality.booking.client.workingbooking.HasWorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingFormPage;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.event.frontoffice.activities.book.event.EventBookingFormSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Builder for creating standard booking forms with a flexible number of custom steps
 * followed by common checkout steps (Your Information → Confirmation).
 *
 * <p>Usage pattern:</p>
 * <pre>
 * // Simple form with 1 custom step
 * StandardBookingFormBuilder builder = new StandardBookingFormBuilder(activity, settings)
 *     .withColorScheme(BookingFormColorScheme.WISDOM_BLUE)
 *     .addCustomStep(optionsPage)
 *     .build();
 *
 * // Complex form with 5 custom steps
 * StandardBookingFormBuilder builder = new StandardBookingFormBuilder(activity, settings)
 *     .withColorScheme(BookingFormColorScheme.JOY_AMBER)
 *     .addCustomStep(dateSelectionPage)
 *     .addCustomStep(packageSelectionPage)
 *     .addCustomStep(roomSelectionPage)
 *     .addCustomStep(mealsPage)
 *     .addCustomStep(extrasPage)
 *     .build();
 * </pre>
 *
 * <p>The resulting form will have:</p>
 * <ul>
 *   <li>Steps 1-N: Custom steps (provided via addCustomStep)</li>
 *   <li>Step N+1: Your Information (login/registration)</li>
 *   <li>Step N+2: Member Selection (who is booking for)</li>
 *   <li>Step N+3: Summary (review booking)</li>
 *   <li>Step N+4: Pending Bookings (basket)</li>
 *   <li>Step N+5: Payment</li>
 *   <li>Step N+6: Confirmation</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public class StandardBookingFormBuilder {

    private final HasWorkingBookingProperties activity;
    private final EventBookingFormSettings settings;

    // Custom steps (form-specific, any number)
    private final List<BookingFormPage> customSteps = new ArrayList<>();

    // Common step overrides (optional - defaults will be used if not set)
    private Supplier<BookingFormPage> yourInformationPageSupplier;
    private Supplier<BookingFormPage> memberSelectionPageSupplier;
    private Supplier<BookingFormPage> summaryPageSupplier;
    private Supplier<BookingFormPage> pendingBookingsPageSupplier;
    private Supplier<BookingFormPage> paymentPageSupplier;
    private Supplier<BookingFormPage> confirmationPageSupplier;

    // Theme
    private BookingFormColorScheme colorScheme = BookingFormColorScheme.DEFAULT;

    // Header options
    private boolean showUserBadge = false; // Hide user badge in header by default

    // Callbacks for inter-step communication
    private StandardBookingFormCallbacks callbacks;

    /**
     * Creates a new builder for a standard booking form.
     *
     * @param activity The activity providing WorkingBookingProperties
     * @param settings The event booking form settings
     */
    public StandardBookingFormBuilder(HasWorkingBookingProperties activity, EventBookingFormSettings settings) {
        this.activity = activity;
        this.settings = settings;
    }

    // === Custom Steps ===

    /**
     * Adds a custom step to the form. Custom steps appear before the common checkout steps.
     * Steps are displayed in the order they are added.
     *
     * @param page The custom step page
     * @return This builder for chaining
     */
    public StandardBookingFormBuilder addCustomStep(BookingFormPage page) {
        customSteps.add(page);
        return this;
    }

    // === Theme ===

    /**
     * Sets the color scheme for the form.
     *
     * @param colorScheme The color scheme to apply
     * @return This builder for chaining
     */
    public StandardBookingFormBuilder withColorScheme(BookingFormColorScheme colorScheme) {
        this.colorScheme = colorScheme;
        return this;
    }

    /**
     * Sets whether to show the user badge in the step progress header.
     * When true, shows a badge with the logged-in user's initials and name.
     * Default is false (badge hidden).
     *
     * @param show true to show the badge, false to hide it
     * @return This builder for chaining
     */
    public StandardBookingFormBuilder withShowUserBadge(boolean show) {
        this.showUserBadge = show;
        return this;
    }

    // === Common Step Overrides ===

    /**
     * Sets a custom summary page supplier to use instead of the default summary page.
     * This allows forms to provide a completely custom summary page with their own sections.
     *
     * @param supplier The supplier that creates the custom summary page
     * @return This builder for chaining
     */
    public StandardBookingFormBuilder withSummaryPageSupplier(Supplier<BookingFormPage> supplier) {
        this.summaryPageSupplier = supplier;
        return this;
    }

    // === Other Common Step Overrides ===

    // === Callbacks ===

    /**
     * Sets the callbacks for inter-step communication.
     *
     * @param callbacks The callbacks implementation
     * @return This builder for chaining
     */
    public StandardBookingFormBuilder withCallbacks(StandardBookingFormCallbacks callbacks) {
        this.callbacks = callbacks;
        return this;
    }

    // === Build ===

    /**
     * Builds the booking form with all configured steps.
     *
     * @return A new StandardBookingForm instance
     */
    public StandardBookingForm build() {
        if (customSteps.isEmpty()) {
            throw new IllegalStateException("At least one custom step must be added before building");
        }

        // Skip flags for common steps
        boolean skipMemberSelection = false;
        boolean skipPendingBookings = false;
        return new StandardBookingForm(
            activity,
            settings,
            colorScheme,
            showUserBadge,
            customSteps,
            yourInformationPageSupplier,
            memberSelectionPageSupplier,
                skipMemberSelection,
            summaryPageSupplier,
            pendingBookingsPageSupplier,
                skipPendingBookings,
            paymentPageSupplier,
            confirmationPageSupplier,
            callbacks
        );
    }
}
