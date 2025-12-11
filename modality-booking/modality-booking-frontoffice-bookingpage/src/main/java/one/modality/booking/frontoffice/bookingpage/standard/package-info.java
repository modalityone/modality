/**
 * Standard Booking Form Implementation.
 *
 * <p>This package provides the {@link StandardBookingForm} which handles the
 * complete booking workflow including:</p>
 * <ul>
 *   <li>State management ({@link BookingFormState})</li>
 *   <li>Household member loading ({@link HouseholdMemberLoader})</li>
 *   <li>Navigation between steps</li>
 *   <li>Payment processing</li>
 * </ul>
 *
 * <h2>Creating a Custom Booking Form</h2>
 * <ol>
 *   <li>Create custom step pages for your form-specific options (e.g., teaching selection, accommodation)</li>
 *   <li>Implement {@link StandardBookingFormCallbacks} to provide custom summary updates</li>
 *   <li>Use {@link StandardBookingFormBuilder} to compose the form with your custom steps + standard steps</li>
 * </ol>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public class MyBookingForm implements StandardBookingFormCallbacks {
 *
 *     private final StandardBookingForm form;
 *     private MyOptionsSection optionsSection;  // Your custom section
 *
 *     public MyBookingForm(HasWorkingBookingProperties activity, EventBookingFormSettings settings) {
 *         // Create custom step
 *         optionsSection = new MyOptionsSection();
 *         BookingFormPage optionsPage = new CompositeBookingFormPage(
 *             "Options",
 *             new ThemedBookingFormSection(optionsSection, BookingFormColorScheme.WISDOM_BLUE));
 *
 *         // Build form
 *         this.form = new StandardBookingFormBuilder(activity, settings)
 *             .withColorScheme(BookingFormColorScheme.WISDOM_BLUE)
 *             .addCustomStep(optionsPage)
 *             .withCallbacks(this)
 *             .build();
 *     }
 *
 *     @Override
 *     public void updateSummary(DefaultSummarySection section, BookingFormState state) {
 *         // Add price lines based on selections in your custom step
 *         section.addPriceLine("Selected Package", null, optionsSection.getPrice());
 *     }
 *
 *     public StandardBookingForm getForm() {
 *         return form;
 *     }
 * }
 * }</pre>
 *
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link StandardBookingForm} - The main form that orchestrates the booking flow</li>
 *   <li>{@link StandardBookingFormBuilder} - Builder for creating forms with custom + standard steps</li>
 *   <li>{@link StandardBookingFormCallbacks} - Interface for form-specific callbacks (summary updates)</li>
 *   <li>{@link BookingFormState} - Holds transient state (logged-in user, selected member, etc.)</li>
 *   <li>{@link HouseholdMemberLoader} - Utility to load household members from database</li>
 *   <li>{@link SummaryUpdater} - Functional interface for updating the summary section</li>
 * </ul>
 *
 * @see StandardBookingForm
 * @see StandardBookingFormBuilder
 * @see StandardBookingFormCallbacks
 */
package one.modality.booking.frontoffice.bookingpage.standard;
