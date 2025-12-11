/**
 * Booking Form Framework for Modality Front-Office Applications.
 *
 * <h2>Architecture Overview</h2>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────┐
 * │                    StandardBookingForm                       │
 * │  (Orchestrates the complete booking flow)                   │
 * ├─────────────────────────────────────────────────────────────┤
 * │  Custom Steps          │  Standard Steps                    │
 * │  ─────────────         │  ──────────────                    │
 * │  [Options Page]   →    │  [Your Info] → [Member] → [Summary]│
 * │  [Date Selection] →    │       ↓            ↓          ↓    │
 * │  [Accommodation]  →    │  [Pending] → [Payment] → [Confirm] │
 * └─────────────────────────────────────────────────────────────┘
 * </pre>
 *
 * <h2>Quick Start</h2>
 * <pre>{@code
 * // 1. Implement callbacks for your custom logic
 * public class MyForm implements StandardBookingFormCallbacks {
 *     @Override
 *     public void updateSummary(DefaultSummarySection section, BookingFormState state) {
 *         section.addPriceLine("My Item", null, 100.0);
 *     }
 * }
 *
 * // 2. Build the form with custom + default steps
 * StandardBookingForm form = new StandardBookingFormBuilder(activity, settings)
 *     .withColorScheme(BookingFormColorScheme.WISDOM_BLUE)
 *     .addCustomStep(myOptionsPage)  // Your custom step(s)
 *     .withCallbacks(this)
 *     .build();                      // Auto-adds standard steps
 * }</pre>
 *
 * <h2>Package Structure</h2>
 * <ul>
 *   <li>{@code standard/} - StandardBookingForm and related classes for building complete booking forms</li>
 *   <li>{@code navigation/} - Navigation headers and button bars (step progress, back/continue)</li>
 *   <li>{@code sections/} - Section interfaces (Has*Section) and default implementations (Default*Section)</li>
 *   <li>{@code theme/} - Color schemes and theming support</li>
 *   <li>{@code pages/} - Specific page implementations (teaching, payment, etc.)</li>
 *   <li>{@code cart/} - Shopping cart functionality for multiple bookings</li>
 * </ul>
 *
 * <h2>Core Interfaces</h2>
 * <ul>
 *   <li>{@link BookingFormPage} - A single page in the booking form</li>
 *   <li>{@link BookingFormSection} - A section within a page</li>
 *   <li>{@link BookingFormButton} - Button definition for navigation</li>
 *   <li>{@link BookingFormHeader} - Header component (step progress)</li>
 *   <li>{@link BookingFormNavigation} - Navigation component (back/continue buttons)</li>
 * </ul>
 *
 * @see one.modality.booking.frontoffice.bookingpage.standard.StandardBookingForm
 * @see one.modality.booking.frontoffice.bookingpage.standard.StandardBookingFormBuilder
 * @see one.modality.booking.frontoffice.bookingpage.standard.StandardBookingFormCallbacks
 */
package one.modality.booking.frontoffice.bookingpage;
