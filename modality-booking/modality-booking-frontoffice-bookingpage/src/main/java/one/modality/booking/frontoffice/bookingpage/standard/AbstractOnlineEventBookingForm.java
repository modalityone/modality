package one.modality.booking.frontoffice.bookingpage.standard;

import dev.webfx.platform.async.Future;
import javafx.beans.binding.Bindings;
import one.modality.booking.client.workingbooking.HasWorkingBookingProperties;
import one.modality.booking.frontoffice.bookingform.BookingFormEntryPoint;
import one.modality.booking.frontoffice.bookingpage.BookingFormButton;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.CompositeBookingFormPage;
import one.modality.booking.frontoffice.bookingpage.sections.audio.DefaultAudioRecordingSection;
import one.modality.booking.frontoffice.bookingpage.sections.audio.HasAudioRecordingSection;
import one.modality.booking.frontoffice.bookingpage.sections.options.HasRateTypeSection;
import one.modality.booking.frontoffice.bookingpage.sections.prerequisite.HasPrerequisiteSection;
import one.modality.booking.frontoffice.bookingpage.sections.summary.DefaultEventHeaderSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.event.frontoffice.activities.book.event.EventBookingFormSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for online event booking forms.
 *
 * <p>An "Online Event" booking typically includes:</p>
 * <ul>
 *   <li>Event header section</li>
 *   <li>Optional prerequisite/confirmation section</li>
 *   <li>Rate/pricing section</li>
 *   <li>Audio recording selection section</li>
 * </ul>
 *
 * <p>This abstract class provides:</p>
 * <ul>
 *   <li>Single "Options" page combining all custom sections</li>
 *   <li>Integration with StandardBookingForm for checkout flow</li>
 *   <li>CSS-based theming via color scheme</li>
 *   <li>Communication between rate selection and audio recording</li>
 * </ul>
 *
 * <p>Subclasses must implement:</p>
 * <ul>
 *   <li>{@link #getColorScheme()} - returns the color theme for the form</li>
 *   <li>{@link #createRateTypeSection()} - creates the rate/pricing section</li>
 *   <li>{@link #getOptionsPageTitleKey()} - returns i18n key for options page</li>
 * </ul>
 *
 * @author Bruno Salmon
 * @see StandardBookingForm
 * @see StandardBookingFormCallbacks
 */
public abstract class AbstractOnlineEventBookingForm implements StandardBookingFormCallbacks {

    // === Core Dependencies ===

    /** The StandardBookingForm instance that handles the checkout flow. */
    protected final StandardBookingForm form;

    /** The event booking form settings. */
    protected final EventBookingFormSettings settings;

    /** The entry point (NEW_BOOKING, MODIFY_BOOKING, PAY_BOOKING). */
    protected final BookingFormEntryPoint entryPoint;

    // === Section Instances ===

    /** Event header section showing event name, dates, location. */
    protected DefaultEventHeaderSection eventHeaderSection;

    /** Optional prerequisite/confirmation section. */
    protected HasPrerequisiteSection prerequisiteSection;

    /** Rate/pricing selection section. */
    protected HasRateTypeSection rateTypeSection;

    /** Audio recording selection section. */
    protected HasAudioRecordingSection audioRecordingSection;

    // === Page ===

    /** The Options page combining all custom sections. */
    protected CompositeBookingFormPage optionsPage;

    // ========================================
    // Abstract Methods (Required)
    // ========================================

    /**
     * Returns the color scheme for this booking form.
     *
     * @return the color scheme to use for theming
     */
    protected abstract BookingFormColorScheme getColorScheme();

    /**
     * Creates the rate/pricing section.
     * The section should implement {@link HasRateTypeSection}.
     *
     * @return the rate type section
     */
    protected abstract HasRateTypeSection createRateTypeSection();

    /**
     * Returns the i18n key for the Options page title.
     *
     * @return the i18n key object
     */
    protected abstract Object getOptionsPageTitleKey();

    // ========================================
    // Override Points (Optional)
    // ========================================

    /**
     * Creates the prerequisite/confirmation section.
     * Return null if no prerequisite is needed.
     *
     * <p>Default returns null (no prerequisite section).</p>
     *
     * @return the prerequisite section, or null
     */
    protected HasPrerequisiteSection createPrerequisiteSection() {
        return null;
    }

    /**
     * Creates the audio recording section.
     * Default creates a {@link DefaultAudioRecordingSection}.
     *
     * @return the audio recording section
     */
    protected HasAudioRecordingSection createAudioRecordingSection() {
        return new DefaultAudioRecordingSection();
    }

    /**
     * Configures the StandardBookingFormBuilder.
     * Override to customize the builder with additional options.
     *
     * @param builder the builder to configure
     */
    protected void configureBuilder(StandardBookingFormBuilder builder) {
        // Default: no additional configuration
    }

    /**
     * Sets up communication between sections after they're created.
     * Default links package selection to audio recording section.
     *
     * <p>Override to add custom communication logic.</p>
     */
    protected void setupSectionCommunication() {
        if (rateTypeSection != null && audioRecordingSection != null) {
            rateTypeSection.setOnPackageSelected(period ->
                audioRecordingSection.setSelectedProgramme(period));
        }
    }

    // ========================================
    // Constructor
    // ========================================

    /**
     * Creates a new online event booking form.
     *
     * @param activity   the activity providing WorkingBookingProperties
     * @param settings   the event booking form settings
     * @param entryPoint the entry point (NEW_BOOKING, MODIFY_BOOKING, PAY_BOOKING)
     */
    protected AbstractOnlineEventBookingForm(
            HasWorkingBookingProperties activity,
            EventBookingFormSettings settings,
            BookingFormEntryPoint entryPoint
    ) {
        this.settings = settings;
        this.entryPoint = entryPoint;

        // Create custom options step
        createCustomStep();

        // Build the form using StandardBookingFormBuilder
        StandardBookingFormBuilder builder = new StandardBookingFormBuilder(activity, settings)
            .withColorScheme(getColorScheme())
            .withEntryPoint(entryPoint)
            .addCustomStep(optionsPage)
            .withCallbacks(this);

        // Let subclass configure additional builder options
        configureBuilder(builder);

        this.form = builder.build();

        // Set up communication between sections
        setupSectionCommunication();
    }

    // ========================================
    // Page Creation
    // ========================================

    /**
     * Creates the custom Options step with all sections.
     */
    private void createCustomStep() {
        // Event Header - shows event name, dates, location
        eventHeaderSection = new DefaultEventHeaderSection();

        // Optional prerequisite section
        prerequisiteSection = createPrerequisiteSection();

        // Rate/pricing section (required)
        rateTypeSection = createRateTypeSection();

        // Audio recording section
        audioRecordingSection = createAudioRecordingSection();

        // Build options page with non-null sections
        List<BookingFormSection> sections = new ArrayList<>();
        sections.add(eventHeaderSection);
        if (prerequisiteSection != null) {
            sections.add(prerequisiteSection);
        }
        sections.add(rateTypeSection);
        if (audioRecordingSection != null) {
            sections.add(audioRecordingSection);
        }

        optionsPage = new CompositeBookingFormPage(
                getOptionsPageTitleKey(),
                sections.toArray(new BookingFormSection[0]))
                .setStep(true)
                .setHeaderVisible(true);

        // Set up navigation button
        optionsPage.setButtons(
            BookingFormButton.async(BookingPageI18nKeys.Continue,
                button -> navigateFromOptionsAsync(),
                "btn-primary booking-form-btn-primary",
                Bindings.not(optionsPage.validProperty())));
    }

    /**
     * Handles navigation from the Options step.
     */
    protected Future<?> navigateFromOptionsAsync() {
        form.continueFromCustomSteps();
        return Future.succeededFuture();
    }

    // ========================================
    // Public Accessors
    // ========================================

    /**
     * Returns the built StandardBookingForm.
     */
    public StandardBookingForm getForm() {
        return form;
    }

    /**
     * Returns the prerequisite section, if any.
     */
    public HasPrerequisiteSection getPrerequisiteSection() {
        return prerequisiteSection;
    }

    /**
     * Returns the rate type section.
     */
    public HasRateTypeSection getRateTypeSection() {
        return rateTypeSection;
    }

    /**
     * Returns the audio recording section, if any.
     */
    public HasAudioRecordingSection getAudioRecordingSection() {
        return audioRecordingSection;
    }
}
