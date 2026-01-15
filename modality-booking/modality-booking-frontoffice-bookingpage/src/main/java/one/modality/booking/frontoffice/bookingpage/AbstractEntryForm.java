package one.modality.booking.frontoffice.bookingpage;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import one.modality.booking.client.workingbooking.HasWorkingBookingProperties;
import one.modality.booking.frontoffice.bookingform.BookingForm;
import one.modality.booking.frontoffice.bookingform.BookingFormActivityCallback;
import one.modality.booking.frontoffice.bookingform.BookingFormEntryPoint;
import one.modality.booking.frontoffice.bookingform.BookingFormSettings;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.event.frontoffice.activities.book.event.EventBookingFormSettings;

/**
 * Abstract base class for entry forms that present an initial choice before navigating
 * to the appropriate booking form (e.g., In-Person vs Online registration).
 *
 * <p>This class provides:</p>
 * <ul>
 *   <li>Standard MAX_WIDTH (800px) for consistent form centering</li>
 *   <li>Color scheme support with CSS theme class application</li>
 *   <li>StackPane wrapper with TOP_CENTER alignment</li>
 *   <li>Form swapping via {@link BookingFormActivityCallback#swapToForm}</li>
 * </ul>
 *
 * <p>Subclasses should:</p>
 * <ol>
 *   <li>Call {@link #setColorScheme} to set the theme</li>
 *   <li>Add UI content to {@link #getContainer()}</li>
 *   <li>Use {@link #swapToForm} to navigate to the selected form</li>
 * </ol>
 *
 * @author Bruno Salmon
 * @see BookingForm
 * @see BookingFormActivityCallback#swapToForm
 */
public abstract class AbstractEntryForm implements BookingForm {

    /** Standard max width for form centering (matches MultiPageBookingForm). */
    protected static final double MAX_WIDTH = 800;

    // === DEPENDENCIES ===
    protected final HasWorkingBookingProperties activity;
    protected final EventBookingFormSettings settings;
    protected final BookingFormEntryPoint entryPoint;

    // === STATE ===
    private BookingFormActivityCallback activityCallback;
    private BookingFormColorScheme colorScheme = BookingFormColorScheme.DEFAULT;

    // === UI ===
    private final VBox container = new VBox();
    private Node cachedView;

    /**
     * Creates a new entry form.
     *
     * @param activity The activity providing working booking properties
     * @param settings The form settings
     * @param entryPoint The entry point (NEW_BOOKING, MODIFY_BOOKING, etc.)
     */
    protected AbstractEntryForm(HasWorkingBookingProperties activity, EventBookingFormSettings settings, BookingFormEntryPoint entryPoint) {
        this.activity = activity;
        this.settings = settings;
        this.entryPoint = entryPoint;
    }

    // ========================================
    // Configuration
    // ========================================

    /**
     * Sets the color scheme for this form.
     * Should be called before {@link #buildUi()}.
     *
     * @param colorScheme The color scheme to apply
     */
    public void setColorScheme(BookingFormColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    /**
     * Returns the current color scheme.
     */
    public BookingFormColorScheme getColorScheme() {
        return colorScheme;
    }

    /**
     * Returns the container VBox for subclasses to add content.
     */
    protected VBox getContainer() {
        return container;
    }

    /**
     * Returns the activity for subclasses.
     */
    protected HasWorkingBookingProperties getActivity() {
        return activity;
    }

    /**
     * Returns the entry point for subclasses.
     */
    protected BookingFormEntryPoint getEntryPoint() {
        return entryPoint;
    }

    // ========================================
    // Form Swapping
    // ========================================

    /**
     * Swaps to a new booking form by replacing this entry form's UI with the new form's UI.
     * This handles the swap directly without requiring activity-level support.
     *
     * @param newForm The form to swap to
     */
    protected void swapToForm(BookingForm newForm) {
        // Pass through the activity callback to the new form
        if (activityCallback != null) {
            newForm.setActivityCallback(activityCallback);
        }

        // Build the new form's UI
        Node newFormUi = newForm.buildUi();

        // Replace our UI content with the new form's UI
        if (cachedView instanceof StackPane) {
            ((StackPane) cachedView).getChildren().setAll(newFormUi);
        }

        // Initialize the new form with working booking data
        newForm.onWorkingBookingLoaded();
    }

    // ========================================
    // BookingForm Interface Implementation
    // ========================================

    @Override
    public BookingFormSettings getSettings() {
        return settings;
    }

    @Override
    public Node buildUi() {
        if (cachedView == null) {
            // Initialize UI content (subclasses add to container)
            initializeContent();

            // Set max width for the content container to match other forms
            container.setMaxWidth(MAX_WIDTH);

            // Wrap in StackPane with TOP_CENTER alignment to match other forms
            StackPane wrapper = new StackPane(container);
            StackPane.setAlignment(container, Pos.TOP_CENTER);

            // Apply CSS theme class for color scheme
            if (colorScheme != null) {
                String themeClass = "theme-" + colorScheme.getId();
                wrapper.getStyleClass().add(themeClass);
            }

            cachedView = wrapper;
        }
        return cachedView;
    }

    /**
     * Called once during {@link #buildUi()} to initialize the UI content.
     * Subclasses should add their UI components to {@link #getContainer()}.
     */
    protected abstract void initializeContent();

    @Override
    public void setActivityCallback(BookingFormActivityCallback activityCallback) {
        this.activityCallback = activityCallback;
    }

    @Override
    public BookingFormActivityCallback getActivityCallback() {
        return activityCallback;
    }
}
