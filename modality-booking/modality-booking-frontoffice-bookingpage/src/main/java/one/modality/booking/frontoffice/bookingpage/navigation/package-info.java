/**
 * Navigation Components for Booking Forms.
 *
 * <p>Provides header and button navigation for multi-page booking forms:</p>
 * <ul>
 *   <li>{@link ResponsiveStepProgressHeader} - Step progress indicator that adapts to screen size</li>
 *   <li>{@link StepProgressHeader} - Basic step progress indicator</li>
 *   <li>{@link ButtonNavigation} - Back/Continue button bar with theming support</li>
 *   <li>{@link StandardBookingFormNavigation} - Standard navigation with step tabs</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>Navigation components are typically configured automatically by {@link one.modality.booking.frontoffice.bookingpage.standard.StandardBookingForm}.
 * Custom forms can set their own navigation using:</p>
 * <pre>{@code
 * // In your form setup
 * ResponsiveStepProgressHeader header = new ResponsiveStepProgressHeader();
 * header.setColorScheme(BookingFormColorScheme.WISDOM_BLUE);
 * setHeader(header);
 *
 * ButtonNavigation navigation = new ButtonNavigation();
 * navigation.setColorScheme(BookingFormColorScheme.WISDOM_BLUE);
 * setNavigation(navigation);
 * }</pre>
 *
 * <h2>Color Scheme Support</h2>
 * <p>All navigation components support theming via {@link one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme}.
 * The color scheme affects:</p>
 * <ul>
 *   <li>Step indicator colors (active, completed, pending)</li>
 *   <li>Button colors (primary, secondary, hover states)</li>
 *   <li>Progress bar colors</li>
 * </ul>
 *
 * @see ResponsiveStepProgressHeader
 * @see ButtonNavigation
 * @see one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme
 */
package one.modality.booking.frontoffice.bookingpage.navigation;
