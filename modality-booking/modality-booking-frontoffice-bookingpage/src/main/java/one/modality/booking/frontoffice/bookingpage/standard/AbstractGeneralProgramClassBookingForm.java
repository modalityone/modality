package one.modality.booking.frontoffice.bookingpage.standard;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import javafx.beans.binding.Bindings;
import one.modality.base.shared.entities.Person;
import one.modality.booking.client.workingbooking.FXPersonToBook;
import one.modality.booking.client.workingbooking.HasWorkingBookingProperties;
import one.modality.booking.client.workingbooking.WorkingBooking;
import one.modality.booking.frontoffice.bookingform.BookingFormEntryPoint;
import one.modality.booking.frontoffice.bookingpage.*;
import one.modality.booking.frontoffice.bookingpage.sections.dates.HasClassDateSelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.existing.HasExistingBookingSection;
import one.modality.booking.frontoffice.bookingpage.sections.member.HasMemberSelectionSection;
import one.modality.booking.frontoffice.bookingpage.sections.options.HasRateTypeSection;
import one.modality.booking.frontoffice.bookingpage.sections.summary.DefaultEventHeaderSection;
import one.modality.booking.frontoffice.bookingpage.sections.summary.HasSummarySection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.event.frontoffice.activities.book.event.EventBookingFormSettings;

/**
 * Abstract base class for General Program class booking forms.
 *
 * <p>A "General Program" booking is a series of weekly classes where users can select
 * individual class dates. This contrasts with single-period residential events.</p>
 *
 * <p>This abstract class provides:</p>
 * <ul>
 *   <li>Rate selection page (Standard vs Member pricing)</li>
 *   <li>Class date selection page (grid of selectable dates)</li>
 *   <li>Existing booking check page (auto-skips if not applicable)</li>
 *   <li>Custom summary section with class-specific pricing</li>
 *   <li>Full-term discount when all classes are selected</li>
 *   <li>Integration with StandardBookingForm for checkout flow</li>
 * </ul>
 *
 * <p>Subclasses must implement:</p>
 * <ul>
 *   <li>{@link #getColorScheme()} - returns the color theme for the form</li>
 *   <li>{@link #createRateSection()} - creates the rate selection section</li>
 *   <li>{@link #createDateSelectionSection()} - creates the date selection section</li>
 *   <li>{@link #createSummarySection(HasClassDateSelectionSection)} - creates the summary section</li>
 *   <li>{@link #createExistingBookingSection()} - creates the existing booking section</li>
 *   <li>{@link #getSelectClassesPageTitleKey()} - returns i18n key for select classes page</li>
 *   <li>{@link #getExistingBookingPageTitleKey()} - returns i18n key for existing booking page</li>
 * </ul>
 *
 * @author Claude
 * @see StandardBookingForm
 * @see StandardBookingFormCallbacks
 */
public abstract class AbstractGeneralProgramClassBookingForm implements StandardBookingFormCallbacks {

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

    /** Rate selection section (Standard vs Member). */
    protected HasRateTypeSection rateSection;

    /** Date selection section (grid of class dates). */
    protected HasClassDateSelectionSection dateSelectionSection;

    /** Existing booking section (for modification flow). */
    protected HasExistingBookingSection existingBookingSection;

    /** Summary section with class-specific pricing. */
    protected HasSummarySection summarySection;

    // === Pages ===

    /** The Select Classes page combining rate and date selection. */
    protected CompositeBookingFormPage selectClassesPage;

    /** The Existing Booking page (auto-skips if not applicable). */
    protected CompositeBookingFormPage existingBookingPage;

    // === State ===

    /** Whether the user is modifying their own booking vs booking for another person. */
    protected boolean modifyingOwnBooking = true;

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
     * Creates the rate selection section.
     * The section should implement {@link HasRateTypeSection}.
     *
     * @return the rate selection section
     */
    protected abstract HasRateTypeSection createRateSection();

    /**
     * Creates the class date selection section.
     * The section should implement {@link HasClassDateSelectionSection}.
     *
     * @return the date selection section
     */
    protected abstract HasClassDateSelectionSection createDateSelectionSection();

    /**
     * Creates the summary section with class-specific pricing.
     * The section should implement {@link HasSummarySection}.
     *
     * @param dateSelectionSection the date selection section (for price calculation)
     * @return the summary section
     */
    protected abstract HasSummarySection createSummarySection(HasClassDateSelectionSection dateSelectionSection);

    /**
     * Creates the existing booking section.
     * The section should implement {@link HasExistingBookingSection}.
     *
     * @return the existing booking section
     */
    protected abstract HasExistingBookingSection createExistingBookingSection();

    /**
     * Returns the i18n key for the Select Classes page title.
     *
     * @return the i18n key object
     */
    protected abstract Object getSelectClassesPageTitleKey();

    /**
     * Returns the i18n key for the Existing Booking page title.
     *
     * @return the i18n key object
     */
    protected abstract Object getExistingBookingPageTitleKey();

    // ========================================
    // Override Points (Optional)
    // ========================================

    /**
     * Configures the StandardBookingFormBuilder. Override to customize the builder.
     *
     * <p>Default configuration for General Program class booking forms:</p>
     * <ul>
     *   <li>User badge shown (withShowUserBadge(true))</li>
     *   <li>Card payment only (withCardPaymentOnly(true))</li>
     *   <li>Navigation not clickable (withNavigationClickable(false))</li>
     * </ul>
     *
     * @param builder the builder to configure
     */
    protected void configureBuilder(StandardBookingFormBuilder builder) {
        builder.withShowUserBadge(true)
               .withCardPaymentOnly(true)
               .withNavigationClickable(false);
    }

    /**
     * Called when the rate type changes in the rate section.
     * Override to add custom behavior.
     *
     * @param rateType the new rate type
     */
    protected void onRateSectionRateChanged(HasRateTypeSection.RateType rateType) {
        // Default: update date selection section with new rate
        if (dateSelectionSection != null) {
            dateSelectionSection.setRateType(rateType);
        }
    }

    /**
     * Called when an existing booking is selected for modification.
     * Override to add custom behavior.
     *
     * @param docAggregate the selected booking's DocumentAggregate
     */
    protected void onExistingBookingDocumentSelected(DocumentAggregate docAggregate) {
        // Recreate WorkingBooking with the selected booking data
        Console.log("Recreating WorkingBooking with selected booking's DocumentAggregate");
        WorkingBooking newWorkingBooking = new WorkingBooking(
            form.getWorkingBookingProperties().getPolicyAggregate(),
            docAggregate,
            null  // no payOrderDocumentId
        );
        // Mark that member was explicitly selected - this skips the member selection page
        newWorkingBooking.setMemberExplicitlySelected(true);
        form.getWorkingBookingProperties().setWorkingBooking(newWorkingBooking);

        // Hide rate section - rate cannot be changed when modifying existing booking
        if (rateSection != null) {
            rateSection.getView().setVisible(false);
            rateSection.getView().setManaged(false);
        }
    }

    /**
     * Called when a member is selected for a new booking.
     * Override to add custom behavior.
     *
     * @param memberInfo the selected member's info
     */
    protected void onExistingBookingMemberSelected(HasMemberSelectionSection.MemberInfo memberInfo) {
        if (memberInfo == null) return;

        // Use the name/email stored in MemberInfo
        String fullName = memberInfo.getName();
        String email = memberInfo.getEmail();
        Person person = memberInfo.getPersonEntity();

        // Parse fullName into firstName/lastName
        String firstName = "";
        String lastName = "";
        if (fullName != null && !fullName.isEmpty()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : "";
        }

        Console.log("Creating fresh WorkingBooking for new booking - MemberInfo details:");
        Console.log("  fullName: " + fullName);
        Console.log("  firstName (parsed): " + firstName);
        Console.log("  lastName (parsed): " + lastName);
        Console.log("  email: " + email);

        WorkingBooking newWorkingBooking = new WorkingBooking(
            form.getWorkingBookingProperties().getPolicyAggregate(),
            null,   // null = brand new booking
            null    // no payOrderDocumentId
        );
        // Mark that member was explicitly selected
        newWorkingBooking.setMemberExplicitlySelected(true);

        // Set the person on the fresh document
        newWorkingBooking.getDocument().setPerson(person);

        // Copy personal details to document
        newWorkingBooking.getDocument().setFirstName(firstName);
        newWorkingBooking.getDocument().setLastName(lastName);
        newWorkingBooking.getDocument().setEmail(email);

        Console.log("  Document firstName after set: " + newWorkingBooking.getDocument().getFirstName());
        Console.log("  Document lastName after set: " + newWorkingBooking.getDocument().getLastName());

        // Update the form with the new WorkingBooking FIRST
        form.getWorkingBookingProperties().setWorkingBooking(newWorkingBooking);

        // Set FXPersonToBook so submission uses correct person
        FXPersonToBook.setPersonToBook(person);
        Console.log("  Set FXPersonToBook to: " + fullName);

        // Set the attendee name directly on the summary section
        if (summarySection != null) {
            summarySection.setAttendeeName(fullName);
            summarySection.setAttendeeEmail(email);
            Console.log("  Set attendee name directly on summary section: " + fullName);
        }

        // Show rate section - user needs to select rate for new booking
        if (rateSection != null) {
            rateSection.getView().setVisible(true);
            rateSection.getView().setManaged(true);
        }
    }

    // ========================================
    // Constructor
    // ========================================

    /**
     * Creates a new General Program class booking form.
     *
     * @param activity   the activity providing WorkingBookingProperties
     * @param settings   the event booking form settings
     * @param entryPoint the entry point (NEW_BOOKING, MODIFY_BOOKING, PAY_BOOKING)
     */
    protected AbstractGeneralProgramClassBookingForm(
            HasWorkingBookingProperties activity,
            EventBookingFormSettings settings,
            BookingFormEntryPoint entryPoint
    ) {
        this.settings = settings;
        this.entryPoint = entryPoint;

        // Build the form using the builder
        StandardBookingFormBuilder builder = new StandardBookingFormBuilder(activity, settings)
            .withColorScheme(getColorScheme())
            .withEntryPoint(entryPoint)
            .withCallbacks(this);

        // Let subclass configure additional builder options
        configureBuilder(builder);

        // Skip custom steps when entry point is for paying a booking
        if (entryPoint != BookingFormEntryPoint.PAY_BOOKING) {
            // Create custom sections and steps
            createSelectClassesStep();
            createExistingBookingStep();

            // Create summary section that shows class-specific pricing
            summarySection = createSummarySection(dateSelectionSection);

            builder
                // Add existing booking check page first (auto-skips if not applicable)
                .addCustomStep(existingBookingPage)
                // Add date selection page
                .addCustomStep(selectClassesPage)
                // Use custom summary page
                .withSummaryPageSupplier(() -> createSummaryPage());
        }

        this.form = builder.build();
    }

    // ========================================
    // Page Creation Methods
    // ========================================

    /**
     * Creates the Select Classes page with rate and date selection.
     */
    protected void createSelectClassesStep() {
        // Event Header
        eventHeaderSection = new DefaultEventHeaderSection();

        // Rate Selection
        rateSection = createRateSection();

        // Date Selection
        dateSelectionSection = createDateSelectionSection();

        // Link rate selection to date selection
        rateSection.setOnRateTypeChanged(this::onRateSectionRateChanged);

        // Combine into Select Classes page
        selectClassesPage = new CompositeBookingFormPage(
                getSelectClassesPageTitleKey(),
                eventHeaderSection,
                rateSection,
                dateSelectionSection)
                .setStep(true)
                .setHeaderVisible(true);

        // Set up navigation button
        selectClassesPage.setButtons(
            BookingFormButton.async(BookingPageI18nKeys.Continue,
                button -> navigateFromSelectClassesAsync(),
                "btn-primary booking-form-btn-primary",
                Bindings.not(selectClassesPage.validProperty())));
    }

    /**
     * Creates the Existing Booking Check step.
     */
    protected void createExistingBookingStep() {
        existingBookingSection = createExistingBookingSection();

        // Handle selection type changes
        existingBookingSection.setOnSelectionTypeChanged(selectionType -> {
            modifyingOwnBooking = (selectionType == HasExistingBookingSection.SelectionType.MODIFY_EXISTING_BOOKING);
        });

        // Handle existing booking selection
        existingBookingSection.setOnDocumentAggregateSelected(this::onExistingBookingDocumentSelected);

        // Handle member selection for new booking
        existingBookingSection.setOnMemberSelected(this::onExistingBookingMemberSelected);

        // Handle continue button
        existingBookingSection.setOnContinuePressed(() -> form.navigateToNextPage());

        // Combine into Existing Booking page
        existingBookingPage = new CompositeBookingFormPage(
                getExistingBookingPageTitleKey(),
                existingBookingSection)
                .setStep(true)
                .setHeaderVisible(false)  // Don't show step header for this page
                .setButtons();  // Empty - section has its own Continue button
    }

    /**
     * Creates the summary page using the summary section.
     */
    protected BookingFormPage createSummaryPage() {
        return new CompositeBookingFormPage(BookingPageI18nKeys.Summary, summarySection)
            .setStep(true);
    }

    /**
     * Handles navigation from the Select Classes step.
     */
    protected Future<?> navigateFromSelectClassesAsync() {
        return form.continueFromCustomSteps();
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
     * Returns the date selection section.
     */
    public HasClassDateSelectionSection getDateSelectionSection() {
        return dateSelectionSection;
    }

    /**
     * Returns the existing booking section.
     */
    public HasExistingBookingSection getExistingBookingSection() {
        return existingBookingSection;
    }

    /**
     * Returns whether the user is modifying their own booking.
     */
    public boolean isModifyingOwnBooking() {
        return modifyingOwnBooking;
    }
}
