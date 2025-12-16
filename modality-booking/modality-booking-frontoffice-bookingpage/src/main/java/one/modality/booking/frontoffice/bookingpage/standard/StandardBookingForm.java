package one.modality.booking.frontoffice.bookingpage.standard;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authn.InitiateAccountCreationCredentials;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.*;
import one.modality.booking.client.workingbooking.*;
import one.modality.booking.frontoffice.bookingform.BookingFormEntryPoint;
import one.modality.booking.frontoffice.bookingpage.*;
import one.modality.booking.frontoffice.bookingpage.navigation.ButtonNavigation;
import one.modality.booking.frontoffice.bookingpage.navigation.ResponsiveStepProgressHeader;
import one.modality.booking.frontoffice.bookingpage.sections.*;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.ecommerce.payment.PaymentAllocation;
import one.modality.ecommerce.payment.client.ClientPaymentUtil;
import one.modality.event.frontoffice.activities.book.event.EventBookingFormSettings;
import one.modality.event.frontoffice.activities.book.fx.FXGuestToBook;
import one.modality.event.frontoffice.activities.book.fx.FXResumePayment;

import java.util.*;
import java.util.function.Supplier;

/**
 * A standard booking form with a flexible number of custom steps followed by
 * common checkout steps (Your Information → Confirmation).
 *
 * <p>This form manages:</p>
 * <ul>
 *   <li>State via {@link BookingFormState} (logged-in person, selected member, etc.)</li>
 *   <li>Household member loading via {@link HouseholdMemberLoader}</li>
 *   <li>Navigation between all steps</li>
 *   <li>Generic callback wiring between sections</li>
 * </ul>
 *
 * <p>Forms only need to implement {@link StandardBookingFormCallbacks#updateSummary}
 * to add their custom price lines.</p>
 *
 * <p>Step layout:</p>
 * <pre>
 * [Custom Step 1] → [Custom Step 2] → ... → [Your Info] → [Member] → [Summary] → [Pending] → [Payment] → [Confirmation]
 *      Step 1           Step 2        ...     Step N+1      Step N+2    Step N+3    Step N+4    Step N+5     Step N+6
 * </pre>
 *
 * @author Bruno Salmon
 * @see StandardBookingFormBuilder
 * @see BookingFormState
 * @see HouseholdMemberLoader
 */
public class StandardBookingForm extends MultiPageBookingForm {

    // Configuration
    private final BookingFormColorScheme colorScheme;
    private final boolean showUserBadge;
    private final BookingFormPage[] pages;
    private final StandardBookingFormCallbacks callbacks;
    private final boolean cardPaymentOnly;
    private final BookingFormEntryPoint entryPoint;

    // State management
    private final BookingFormState state;

    // Page references for navigation
    private final List<BookingFormPage> customStepPages;
    private BookingFormPage yourInformationPage;
    private BookingFormPage memberSelectionPage;
    private BookingFormPage summaryPage;
    private BookingFormPage pendingBookingsPage;
    private BookingFormPage paymentPage;
    private BookingFormPage confirmationPage;

    // Default section references (only set when using default pages)
    private DefaultYourInformationSection defaultYourInformationSection;
    private DefaultMemberSelectionSection defaultMemberSelectionSection;
    private DefaultSummarySection defaultSummarySection;
    private DefaultPendingBookingsSection defaultPendingBookingsSection;
    private DefaultTermsSection defaultTermsSection;
    private DefaultPaymentSection defaultPaymentSection;
    private DefaultConfirmationSection defaultConfirmationSection;

    // Stored new user info for payment/confirmation flow (persists after submission clears state)
    private String storedNewUserName;
    private String storedNewUserEmail;

    // Dynamic button text for pending bookings page (changes based on total amount)
    private final StringProperty pendingBookingsButtonText = new SimpleStringProperty();

    /**
     * Package-private constructor - use {@link StandardBookingFormBuilder} to create instances.
     */
    StandardBookingForm(
            HasWorkingBookingProperties activity,
            EventBookingFormSettings settings,
            BookingFormColorScheme colorScheme,
            boolean showUserBadge,
            List<BookingFormPage> customSteps,
            Supplier<BookingFormPage> yourInformationPageSupplier,
            Supplier<BookingFormPage> memberSelectionPageSupplier,
            boolean skipMemberSelection,
            Supplier<BookingFormPage> summaryPageSupplier,
            Supplier<BookingFormPage> pendingBookingsPageSupplier,
            boolean skipPendingBookings,
            Supplier<BookingFormPage> paymentPageSupplier,
            Supplier<BookingFormPage> confirmationPageSupplier,
            StandardBookingFormCallbacks callbacks,
            boolean cardPaymentOnly,
            BookingFormEntryPoint entryPoint) {

        super(activity, settings);
        this.colorScheme = colorScheme;
        this.showUserBadge = showUserBadge;
        this.callbacks = callbacks;
        this.cardPaymentOnly = cardPaymentOnly;
        this.entryPoint = entryPoint;
        this.customStepPages = new ArrayList<>(customSteps);

        // Initialize state management
        WorkingBookingProperties workingBookingProperties = activity.getWorkingBookingProperties();
        this.state = new BookingFormState(workingBookingProperties);

        // Build the pages array
        this.pages = buildPages(
            customSteps,
            yourInformationPageSupplier,
            memberSelectionPageSupplier,
            skipMemberSelection,
            summaryPageSupplier,
            pendingBookingsPageSupplier,
            skipPendingBookings,
            paymentPageSupplier,
            confirmationPageSupplier
        );

        // Set up theme
        setupTheme();

        // Wire up internal callbacks
        wireUpInternalCallbacks();

        // Set up navigation buttons for each page
        setupPageButtons();
    }

    private BookingFormPage[] buildPages(
            List<BookingFormPage> customSteps,
            Supplier<BookingFormPage> yourInformationPageSupplier,
            Supplier<BookingFormPage> memberSelectionPageSupplier,
            boolean skipMemberSelection,
            Supplier<BookingFormPage> summaryPageSupplier,
            Supplier<BookingFormPage> pendingBookingsPageSupplier,
            boolean skipPendingBookings,
            Supplier<BookingFormPage> paymentPageSupplier,
            Supplier<BookingFormPage> confirmationPageSupplier) {

        // 1. Add all custom steps
        List<BookingFormPage> allPages = new ArrayList<>(customSteps);

        // 2. Your Information page (always present)
        // Index tracking for common pages (set during page array construction)
        yourInformationPage = yourInformationPageSupplier != null
            ? yourInformationPageSupplier.get()
            : createDefaultYourInformationPage();
        allPages.add(yourInformationPage);

        // 3. Member Selection page (optional)
        if (!skipMemberSelection) {
            // -1 if skipped
            memberSelectionPage = memberSelectionPageSupplier != null
                ? memberSelectionPageSupplier.get()
                : createDefaultMemberSelectionPage();
            allPages.add(memberSelectionPage);
        }

        // 4. Summary page (always present)
        summaryPage = summaryPageSupplier != null
            ? summaryPageSupplier.get()
            : createDefaultSummaryPage();
        allPages.add(summaryPage);

        // Extract DefaultSummarySection from custom summary page if present
        // This allows updateSummaryWithAttendee() to work with custom summary pages
        if (summaryPageSupplier != null && defaultSummarySection == null && summaryPage instanceof CompositeBookingFormPage compositeSummary) {
            for (BookingFormSection section : compositeSummary.getSections()) {
                if (section instanceof DefaultSummarySection dss) {
                    defaultSummarySection = dss;
                    defaultSummarySection.setColorScheme(colorScheme);
                    break;
                }
            }
        }

        // 5. Pending Bookings page (optional)
        if (!skipPendingBookings) {
            // -1 if skipped
            pendingBookingsPage = pendingBookingsPageSupplier != null
                ? pendingBookingsPageSupplier.get()
                : createDefaultPendingBookingsPage();
            allPages.add(pendingBookingsPage);
        }

        // 6. Payment page (always present)
        paymentPage = paymentPageSupplier != null
            ? paymentPageSupplier.get()
            : createDefaultPaymentPage();
        allPages.add(paymentPage);

        // 7. Confirmation page (always present)
        confirmationPage = confirmationPageSupplier != null
            ? confirmationPageSupplier.get()
            : createDefaultConfirmationPage();
        allPages.add(confirmationPage);

        return allPages.toArray(new BookingFormPage[0]);
    }

    private void setupTheme() {
        // Set up responsive header with color scheme
        ResponsiveStepProgressHeader header = new ResponsiveStepProgressHeader();
        header.setColorScheme(colorScheme);
        header.setShowUserBadge(showUserBadge);
        setHeader(header);

        // Set navigation with color scheme
        ButtonNavigation navigation = new ButtonNavigation();
        navigation.setColorScheme(colorScheme);
        setNavigation(navigation);
    }

    @Override
    public javafx.scene.Node buildUi() {
        javafx.scene.Node node = super.buildUi();
        // Apply CSS theme class to the root container for CSS variable theming
        // Theme classes like "theme-wisdom-blue" override CSS variables
        if (node != null && colorScheme != null) {
            String themeClass = "theme-" + colorScheme.getId();
            node.getStyleClass().add(themeClass);
        }
        return node;
    }

    // === Default Page Factories ===

    protected BookingFormPage createDefaultYourInformationPage() {
        defaultYourInformationSection = new DefaultYourInformationSection();
        defaultYourInformationSection.setColorScheme(colorScheme);
        return new CompositeBookingFormPage(BookingPageI18nKeys.YourInformation,
                defaultYourInformationSection)
            .setStep(false)
            .setShowingOwnSubmitButton(true);
    }

    protected BookingFormPage createDefaultMemberSelectionPage() {
        defaultMemberSelectionSection = new DefaultMemberSelectionSection();
        defaultMemberSelectionSection.setColorScheme(colorScheme);
        return new CompositeBookingFormPage(BookingPageI18nKeys.MemberSelection,
                defaultMemberSelectionSection)
            .setStep(true)
            .setShowingOwnSubmitButton(true);
    }

    protected BookingFormPage createDefaultSummaryPage() {
        defaultSummarySection = new DefaultSummarySection();
        defaultSummarySection.setColorScheme(colorScheme);
        return new CompositeBookingFormPage(BookingPageI18nKeys.Summary,
                defaultSummarySection)
            .setStep(true);
    }

    protected BookingFormPage createDefaultPendingBookingsPage() {
        defaultPendingBookingsSection = new DefaultPendingBookingsSection();
        defaultPendingBookingsSection.setColorScheme(colorScheme);
        defaultTermsSection = new DefaultTermsSection();
        defaultTermsSection.setColorScheme(colorScheme);
        return new CompositeBookingFormPage(BookingPageI18nKeys.PendingBookings,
                defaultPendingBookingsSection,
                defaultTermsSection)
            .setStep(true);
    }

    protected BookingFormPage createDefaultPaymentPage() {
        defaultPaymentSection = new DefaultPaymentSection();
        defaultPaymentSection.setColorScheme(colorScheme);
        if (cardPaymentOnly) {
            defaultPaymentSection.setCardPaymentOnly(true);
        }
        return new CompositeBookingFormPage(BookingPageI18nKeys.Payment,
                defaultPaymentSection)
            .setStep(true);
    }

    protected BookingFormPage createDefaultConfirmationPage() {
        defaultConfirmationSection = new DefaultConfirmationSection();
        defaultConfirmationSection.setColorScheme(colorScheme);
        return new CompositeBookingFormPage(BookingPageI18nKeys.Confirmation,
                defaultConfirmationSection)
            .setStep(true)
            .setButtons() // No navigation buttons on confirmation page
            .setShowingOwnSubmitButton(true); // Prevents default activity-level submit button from showing
    }

    // === Entry Point Handling ===

    @Override
    public void onWorkingBookingLoaded() {
        if (entryPoint == BookingFormEntryPoint.RESUME_PAYMENT) {
            // Add 30px spacer at top of header for payment return flow
            BookingFormHeader headerRef = getHeader();
            if (headerRef != null && headerRef.getView() instanceof VBox) {
                VBox headerWrapper = (VBox) headerRef.getView();
                Region spacer = new Region();
                spacer.setMinHeight(30);
                spacer.setPrefHeight(30);
                spacer.setMaxHeight(30);
                headerWrapper.getChildren().add(0, spacer);
            }

            // Navigate directly to confirmation page (last step) when returning from payment gateway
            navigateToConfirmation();

            // Check the MoneyTransfer status and show appropriate content
            MoneyTransfer moneyTransfer = FXResumePayment.getMoneyTransfer();
            if (moneyTransfer != null) {
                handleResumePaymentContent(moneyTransfer);
            }
        } else {
            // Normal flow - starts at first applicable page
            super.onWorkingBookingLoaded();
        }
    }

    /**
     * Shows the appropriate content (Confirmation, Failed, or Pending) based on MoneyTransfer status.
     * The form stays at the last step (Confirmation) but the content changes.
     */
    private void handleResumePaymentContent(MoneyTransfer moneyTransfer) {
        boolean isSuccessful = moneyTransfer.isSuccessful();
        boolean isPending = moneyTransfer.isPending();

        Console.log("Resume payment - successful: " + isSuccessful + ", pending: " + isPending);

        if (isSuccessful && !isPending) {
            // Payment successful - show confirmation content
            updateConfirmationForResumedPayment(moneyTransfer);
        } else if (isPending) {
            // Payment pending - show pending payment content
            showPendingPaymentContent(moneyTransfer);
        } else {
            // Payment failed - show failed payment content
            showFailedPaymentContent(moneyTransfer);
        }
    }

    /**
     * Updates the confirmation section for a resumed successful payment.
     */
    private void updateConfirmationForResumedPayment(MoneyTransfer moneyTransfer) {
        if (defaultConfirmationSection == null) return;

        defaultConfirmationSection.clearConfirmedBookings();

        // Get booking info from the MoneyTransfer's document
        Document document = moneyTransfer.getDocument();
        if (document != null) {
            Person person = document.getPerson();
            String personName = person != null ? person.getFullName() : "";
            String personEmail = person != null ? person.getEmail() : "";
            Object bookingRefObj = document.getRef();
            String bookingRef = bookingRefObj != null ? bookingRefObj.toString() : "";

            defaultConfirmationSection.addConfirmedBooking(new HasConfirmationSection.ConfirmedBooking(
                personName,
                personEmail,
                bookingRef));

            // Set event info from document
            Event event = document.getEvent();
            if (event != null) {
                defaultConfirmationSection.setEventName(event.getName());
                defaultConfirmationSection.setEventDates(event.getStartDate(), event.getEndDate());
            }
        }

        // Set payment amounts from the money transfer (with null safety)
        Integer amountObj = moneyTransfer.getAmount();
        int amount = amountObj != null ? amountObj : 0;
        defaultConfirmationSection.setPaymentAmounts(amount, amount);
    }

    /**
     * Shows the failed payment content on the confirmation step.
     */
    private void showFailedPaymentContent(MoneyTransfer moneyTransfer) {
        DefaultFailedPaymentSection failedSection = new DefaultFailedPaymentSection();
        failedSection.setColorScheme(colorScheme);

        // Populate with data from MoneyTransfer
        Document document = moneyTransfer.getDocument();
        if (document != null) {
            Object bookingRefObj = document.getRef();
            failedSection.setBookingReference(bookingRefObj != null ? bookingRefObj.toString() : "");

            Person person = document.getPerson();
            if (person != null) {
                failedSection.setGuestName(person.getFullName());
            }

            Event event = document.getEvent();
            if (event != null) {
                failedSection.setEventName(event.getName());
                failedSection.setEventDates(event.getStartDate(), event.getEndDate());
            }
        }

        Integer amountObj = moneyTransfer.getAmount();
        int amount = amountObj != null ? amountObj : 0;
        failedSection.setAmountDue(amount);

        // Set error details (generic for now)
        failedSection.setErrorDetails(HasFailedPaymentSection.PaymentErrorType.CARD_DECLINED, null, null);

        // Set callbacks
        failedSection.setOnRetryPayment(() -> {
            // For now, we do only redirected payments, so we
            ClientPaymentUtil.initiateRedirectedPaymentAndRedirectToGatewayPaymentPage(FXResumePayment.getAmount(), FXResumePayment.getPaymentAllocations());
            /*// When embedded payments will be added, we will navigate back to the payment page
            navigateToPayment();*/
        });

        // Show the failed payment content in the transition pane
        getTransitionPane().transitToContent(failedSection.getView(), null);
    }

    /**
     * Shows the pending payment content on the confirmation step.
     */
    private void showPendingPaymentContent(MoneyTransfer moneyTransfer) {
        DefaultPendingPaymentSection pendingSection = new DefaultPendingPaymentSection();
        pendingSection.setColorScheme(colorScheme);

        // Populate with data from MoneyTransfer
        Document document = moneyTransfer.getDocument();
        if (document != null) {
            Object bookingRefObj = document.getRef();
            pendingSection.setBookingReference(bookingRefObj != null ? bookingRefObj.toString() : "");

            Person person = document.getPerson();
            if (person != null) {
                pendingSection.setGuestName(person.getFullName());
                pendingSection.setGuestEmail(person.getEmail());
            }

            Event event = document.getEvent();
            if (event != null) {
                pendingSection.setEventName(event.getName());
                pendingSection.setEventDates(event.getStartDate(), event.getEndDate());
            }
        }

        Integer amountObj = moneyTransfer.getAmount();
        int amount = amountObj != null ? amountObj : 0;
        pendingSection.setTotalAmount(amount);

        // Show the pending payment content in the transition pane
        getTransitionPane().transitToContent(pendingSection.getView(), null);
    }

    // === Internal Callback Wiring ===
    // This connects section events to internal handlers that manage state and navigation.

    private void wireUpInternalCallbacks() {
        // Your Information section
        if (defaultYourInformationSection != null) {
            defaultYourInformationSection.setOnLoginSuccess(this::handleLoginSuccess);
            defaultYourInformationSection.setOnNewUserContinue(this::handleNewUserContinue);
            defaultYourInformationSection.setOnBackPressed(this::navigateToLastCustomStep);
        }

        // Member Selection section
        if (defaultMemberSelectionSection != null) {
            defaultMemberSelectionSection.setOnMemberSelected(this::handleMemberSelected);
            defaultMemberSelectionSection.setOnContinuePressed(this::handleMemberSelectionContinue);
            defaultMemberSelectionSection.setOnBackPressed(this::handleMemberSelectionBack);
        }

        // Pending Bookings section
        if (defaultPendingBookingsSection != null) {
            defaultPendingBookingsSection.setOnRegisterAnotherPerson(this::handleRegisterAnotherPerson);
            defaultPendingBookingsSection.setOnProceedToPayment(this::handleProceedToPayment);
            defaultPendingBookingsSection.setOnBackPressed(this::navigateToSummary);
            // Update button text when bookings change (to switch between "Proceed to Payment" and "Confirm Booking")
            defaultPendingBookingsSection.getBookings().addListener((javafx.collections.ListChangeListener<HasPendingBookingsSection.BookingItem>) change -> {
                updatePendingBookingsButtonText();
            });
        }

        // Payment section
        if (defaultPaymentSection != null) {
            defaultPaymentSection.setOnBackPressed(this::navigateToPendingBookings);
            defaultPaymentSection.setOnPaymentSubmit(this::handlePaymentSubmit);
        }

        // Confirmation section
        if (defaultConfirmationSection != null) {
            defaultConfirmationSection.setOnMakeAnotherBooking(this::handleMakeAnotherBooking);
        }
    }

    // === Page Button Configuration ===
    // Sets up navigation buttons for each page using the CompositeBookingFormPage API.

    private void setupPageButtons() {
        // Summary page: Back + Submit Registration (submits booking before navigating)
        // Back: goes to Member Selection (or Your Information if no member selection)
        // Submit Registration: disabled when page not valid, submits booking then navigates to Pending Bookings
        if (summaryPage instanceof CompositeBookingFormPage compositeSummary) {
            compositeSummary.setButtons(
                new BookingFormButton(BookingPageI18nKeys.Back,
                    e -> handleSummaryBack(),
                    "btn-back booking-form-btn-back"),
                BookingFormButton.async(BookingPageI18nKeys.SubmitRegistration,
                    button -> handleSummaryContinueAsync(),
                    "btn-primary booking-form-btn-primary",
                    Bindings.not(compositeSummary.validProperty()))
            );
        }

        // Pending Bookings page: Register Another Person + Proceed to Payment (or Confirm Booking if price is zero)
        // Register Another Person is disabled when no available members to book
        // Proceed to Payment/Confirm Booking is disabled when terms are not accepted
        if (pendingBookingsPage instanceof CompositeBookingFormPage compositePending) {
            // Initialize button text based on total (will be updated when bookings change)
            updatePendingBookingsButtonText();

            compositePending.setButtons(
                new BookingFormButton(BookingPageI18nKeys.RegisterAnotherPerson,
                    e -> handleRegisterAnotherPerson(),
                    "btn-secondary booking-form-btn-secondary",
                    defaultMemberSelectionSection != null
                        ? Bindings.not(defaultMemberSelectionSection.hasAvailableMembersProperty())
                        : null),
                // Use dynamic button text that changes based on total amount
                new BookingFormButton(pendingBookingsButtonText,
                    e -> handleProceedToPaymentOrConfirm(),
                    "btn-primary booking-form-btn-primary",
                    defaultTermsSection != null
                        ? Bindings.not(defaultTermsSection.termsAcceptedProperty())
                        : null)
            );
        }

        // Payment page: Back + Pay Now
        // Pay Now: disabled by payment section's payButtonDisabledProperty
        if (paymentPage instanceof CompositeBookingFormPage compositePayment && defaultPaymentSection != null) {
            compositePayment.setButtons(
                new BookingFormButton(BookingPageI18nKeys.Back,
                    e -> navigateToPendingBookings(),
                    "btn-back booking-form-btn-back"),
                BookingFormButton.async(BookingPageI18nKeys.PayNow,
                    button -> handlePaymentSubmitAsync(),
                    "btn-primary booking-form-btn-primary",
                    defaultPaymentSection.payButtonDisabledProperty())
            );
        }

        // Confirmation page: No buttons - this is the final page
        // Users can navigate via the header or close the form
        if (confirmationPage instanceof CompositeBookingFormPage compositeConfirmation) {
            compositeConfirmation.setButtons(); // No buttons
        }
    }

    // === Summary Page Handlers ===

    private void handleSummaryBack() {
        // Allow member reselection when going back (if Member Selection is applicable)
        state.setAllowMemberReselection(true);
        // Use navigateToPreviousPage() which respects isApplicableToBooking()
        // For existing bookings: skips Member Selection and Your Information, goes to Select Classes
        // For new bookings: goes to Member Selection (if logged in) or Your Information
        navigateToPreviousPage();
    }

    private Future<?> handleSummaryContinueAsync() {
        Console.log("StandardBookingForm.handleSummaryContinueAsync() called");

        // Step 1: Set the person to book in FXPersonToBook before booking items
        HasMemberSelectionSection.MemberInfo member = state.getSelectedMember();
        if (member != null && member.getPersonEntity() != null) {
            FXPersonToBook.setPersonToBook(member.getPersonEntity());
            Console.log("FXPersonToBook set to: " + member.getName());
        }

        // Step 1b: For new users (guests OR creating account), set personal details on the document
        // and maintain guest session via FXGuestToBook (same pattern as Step1BookingFormAndSubmitSlide)
        HasYourInformationSection.NewUserData newUser = state.getPendingNewUserData();
        if (newUser != null) {
            WorkingBookingProperties props = state.getWorkingBookingProperties();
            if (props != null) {
                Document document = props.getWorkingBooking().getDocument();
                if (document != null) {
                    document.setFirstName(newUser.firstName);
                    document.setLastName(newUser.lastName);
                    document.setEmail(newUser.email);
                    // Set country from event organization (same as Step1BookingFormAndSubmitSlide)
                    Event event = props.getWorkingBooking().getEvent();
                    if (event != null && event.getOrganization() != null) {
                        document.setCountry(event.getOrganization().getCountry());
                    }
                    // Maintain guest session for payment/confirmation
                    FXGuestToBook.setGuestToBook(document);
                    Console.log("New user details set on document and FXGuestToBook: " +
                        newUser.firstName + " " + newUser.lastName + " (" + newUser.email + ")");
                }
            }
        }

        // Step 2: Book selected items into the WorkingBooking (default behavior)
        bookSelectedItemsInWorkingBooking();

        // Step 3: Let the form-specific code do any additional booking if needed
        // New users (both guests AND account creation) have no logged-in session
        final boolean isNewUser = newUser != null;
        final boolean wantsAccountCreation = isNewUser && newUser.createAccount;
        // Save new user info before submission (it gets cleared in submitBookingAsync)
        final String newUserName = isNewUser ? newUser.firstName + " " + newUser.lastName : null;
        final String newUserEmail = isNewUser ? newUser.email : null;

        return callbacks.onSubmitBooking()
            .compose(ignored -> submitBookingAsync())
            .compose(ignored -> {
                // For new users (guest or creating account), skip loading bookings
                // They're not logged in, so there's nothing to load
                if (isNewUser) {
                    return Future.succeededFuture();
                }
                return loadAllBookingsForEventAsync();
            })
            .onSuccess(ignored -> {
                // Send account creation email if user opted to create an account
                if (wantsAccountCreation && newUserEmail != null) {
                    Console.log("Sending account creation email to: " + newUserEmail);
                    InitiateAccountCreationCredentials credentials = new InitiateAccountCreationCredentials(
                        newUserEmail,
                        WindowLocation.getOrigin(),  // clientOrigin
                        null,                         // requestedPath (not needed)
                        I18n.getLanguage(),          // language
                        false,                        // verificationCodeOnly
                        null                          // context
                    );
                    AuthenticationService.authenticate(credentials)
                        .onFailure(error -> Console.log("Failed to send account creation email: " + error.getMessage()))
                        .onSuccess(v -> Console.log("Account creation email sent successfully"));
                }

                UiScheduler.runInUiThread(() -> {
                    if (isNewUser) {
                        // Both guests and account creation users go to pending bookings
                        // (populated from WorkingBooking, not from database)
                        Console.log("New user submission successful, navigating to Pending Bookings");
                        populatePendingBookingsForNewUser(newUserName, newUserEmail);
                        navigateToPendingBookings();
                    } else {
                        Console.log("Logged-in user submission successful, navigating to Pending Bookings");
                        navigateToPendingBookings();
                    }
                });
            })
            .onFailure(error -> {
                Console.log("ERROR: " + error.getMessage());
                // Stay on current page - spinner will be hidden automatically
            });
    }

    /**
     * Books selected items into the WorkingBooking before submission.
     * <p>
     * The WorkingBooking tracks all booking state. This method only books the whole event
     * as a fallback if nothing has been booked yet. Forms with custom options (like period
     * selection) should book items when the user makes selections, not at submission time.
     */
    private void bookSelectedItemsInWorkingBooking() {
        Console.log("bookSelectedItemsInWorkingBooking() called");

        WorkingBookingProperties props = state.getWorkingBookingProperties();
        if (props == null) {
            Console.log("ERROR: workingBookingProperties is null");
            return;
        }

        WorkingBooking workingBooking = props.getWorkingBooking();
        Console.log("WorkingBooking obtained, hasChanges: " + workingBooking.hasChanges());

        // Only book the whole event if nothing has been booked yet
        // This is the default fallback for simple forms without custom option selection
        if (workingBooking.hasNoChanges()) {
            Console.log("No items booked yet, booking whole event as default...");
            workingBooking.bookWholeEvent();
            Console.log("Whole event booked");
        } else {
            Console.log("Items already booked in WorkingBooking, using existing selections");
        }

        Console.log("bookSelectedItemsInWorkingBooking() completed");
    }

    /**
     * Submit booking changes to database, returning a Future.
     *
     * @return Future that completes when submission is done (or immediately if no changes)
     */
    private Future<Void> submitBookingAsync() {
        WorkingBookingProperties props = state.getWorkingBookingProperties();
        if (props == null) {
            Console.log("ERROR: workingBookingProperties is null in submitBookingAsync");
            return Future.failedFuture("workingBookingProperties is null");
        }

        WorkingBooking workingBooking = props.getWorkingBooking();
        Console.log("WorkingBooking hasNoChanges: " + workingBooking.hasNoChanges());

        // Check if there are changes to submit
        if (workingBooking.hasNoChanges()) {
            Console.log("No booking changes to submit");
            return Future.succeededFuture();
        }

        Console.log("Generating history comment and submitting to database...");

        // Generate history comment for the booking
        WorkingBookingHistoryHelper historyHelper = new WorkingBookingHistoryHelper(workingBooking);
        String historyComment = historyHelper.generateHistoryComment();
        Console.log("History comment: " + historyComment);

        // Submit changes to the database
        return workingBooking.submitChanges(historyComment)
            .map(submitResult -> {
                Console.log("Booking submitted successfully. Reference: " + submitResult.getDocumentRef());

                // Store the booking reference
                props.setBookingReference(submitResult.getDocumentRef());

                // Reset reselection flag - booking is now confirmed
                state.setAllowMemberReselection(false);

                // Clear pending new user data - booking is now associated with a Person in DB
                state.setPendingNewUserData(null);

                return null;
            });
    }

    /**
     * Load all bookings for event asynchronously, returning a Future.
     *
     * @return Future that completes when bookings are loaded
     */
    private Future<Void> loadAllBookingsForEventAsync() {
        Promise<Void> promise = Promise.promise();
        loadAllBookingsForEvent(promise::complete);
        return promise.future();
    }

    /**
     * Load all bookings for this event from members of the same frontendAccount.
     *
     * @param onComplete Callback to run when loading is complete
     */
    private void loadAllBookingsForEvent(Runnable onComplete) {
        Console.log("loadAllBookingsForEvent() called");

        WorkingBookingProperties props = state.getWorkingBookingProperties();
        if (props == null) {
            Console.log("ERROR: workingBookingProperties is null");
            if (onComplete != null) onComplete.run();
            return;
        }

        Event event = props.getWorkingBooking().getEvent();
        if (event == null) {
            Console.log("ERROR: event is null");
            if (onComplete != null) onComplete.run();
            return;
        }

        ModalityUserPrincipal principal = FXModalityUserPrincipal.modalityUserPrincipalProperty().get();
        if (principal == null) {
            Console.log("User not logged in - skipping booking load");
            if (onComplete != null) onComplete.run();
            return;
        }

        Object eventId = event.getPrimaryKey();
        Object accountId = principal.getUserAccountId();

        Console.log("Querying bookings for event=" + eventId + ", account=" + accountId);

        EntityStore entityStore = EntityStore.create(DataSourceModelService.getDefaultDataSourceModel());

        // Query all documents (bookings) for this event from account members
        entityStore.<Document>executeQuery(
            "select id,ref,person.(id,firstName,lastName,email)," +
            "price_net,price_deposit,price_minDeposit " +
            "from Document " +
            "where event=? and person.frontendAccount=? and !cancelled " +
            "order by person.firstName",
            eventId, accountId
        )
        .onFailure(error -> {
            Console.log("Error loading bookings for event: " + error.getMessage());
            UiScheduler.runInUiThread(() -> {
                if (onComplete != null) onComplete.run();
            });
        })
        .onSuccess(documents -> {
            Console.log("Found " + documents.size() + " bookings for event");
            loadDocumentLinesForDocuments(documents, entityStore, eventId, accountId, onComplete);
        });
    }

    /**
     * Load document lines for the given documents.
     */
    private void loadDocumentLinesForDocuments(
            EntityList<Document> documents,
            EntityStore entityStore,
            Object eventId,
            Object accountId,
            Runnable onComplete) {

        Console.log("loadDocumentLinesForDocuments() called for " + documents.size() + " documents");

        entityStore.<DocumentLine>executeQuery(
            "select id,document,item.(id,name,family.(code,name)),price_net " +
            "from DocumentLine " +
            "where document.event=? and document.person.frontendAccount=? and !cancelled " +
            "order by document,item.ord",
            eventId, accountId
        )
        .onFailure(error -> {
            Console.log("Error loading document lines: " + error.getMessage());
            loadPaymentStatusForDocuments(documents, Collections.emptyList(), entityStore, eventId, accountId, onComplete);
        })
        .onSuccess(documentLines -> {
            Console.log("Found " + documentLines.size() + " document lines");
            loadPaymentStatusForDocuments(documents, documentLines, entityStore, eventId, accountId, onComplete);
        });
    }

    /**
     * Load payment status for the given documents and update UI sections.
     */
    private void loadPaymentStatusForDocuments(
            EntityList<Document> documents,
            List<DocumentLine> documentLines,
            EntityStore entityStore,
            Object eventId,
            Object accountId,
            Runnable onComplete) {

        Console.log("loadPaymentStatusForDocuments() called for " + documents.size() + " documents");

        entityStore.<MoneyTransfer>executeQuery(
            "select id,document,amount,pending,successful " +
            "from MoneyTransfer " +
            "where document.event=? and document.person.frontendAccount=? " +
            "order by document,date desc",
            eventId, accountId
        )
        .onFailure(error -> {
            Console.log("Error loading payment status: " + error.getMessage());
            UiScheduler.runInUiThread(() -> {
                populateBookingsFromDocuments(documents, documentLines, Collections.emptyList());
                if (onComplete != null) onComplete.run();
            });
        })
        .onSuccess(payments -> {
            Console.log("Found " + payments.size() + " payment records");
            UiScheduler.runInUiThread(() -> {
                populateBookingsFromDocuments(documents, documentLines, payments);
                if (onComplete != null) onComplete.run();
            });
        });
    }

    /**
     * Populate PendingBookingsSection from database documents.
     */
    private void populateBookingsFromDocuments(
            EntityList<Document> documents,
            List<DocumentLine> documentLines,
            List<MoneyTransfer> payments) {

        Console.log("populateBookingsFromDocuments() called with " + documents.size() + " documents and " + documentLines.size() + " lines");

        if (defaultPendingBookingsSection == null) {
            Console.log("defaultPendingBookingsSection is null - skipping population");
            return;
        }

        // Build map of document ID -> total paid amount
        Map<Object, Integer> paidAmounts = new HashMap<>();
        for (MoneyTransfer mt : payments) {
            if (mt.isSuccessful() && !mt.isPending()) {
                Object docId = mt.getDocument() != null ? mt.getDocument().getId() : null;
                if (docId != null) {
                    paidAmounts.merge(docId, mt.getAmount(), Integer::sum);
                }
            }
        }

        // Build map of document ID -> list of document lines
        Map<Object, List<DocumentLine>> linesByDocument = new HashMap<>();
        for (DocumentLine line : documentLines) {
            Document lineDoc = line.getDocument();
            if (lineDoc != null) {
                Object docId = lineDoc.getId();
                linesByDocument.computeIfAbsent(docId, k -> new ArrayList<>()).add(line);
            }
        }

        // Clear existing bookings
        defaultPendingBookingsSection.clearBookings();

        // Get event name for booking items
        String eventName = "";
        Event event = getEvent();
        if (event != null) {
            eventName = event.getName() != null ? event.getName() : "";
        }

        // Add each document as a booking
        for (Document doc : documents) {
            Person person = doc.getPerson();
            String personName = getPersonFullName(person);
            String personEmail = person != null ? person.getEmail() : "";

            // Get document lines for this booking
            List<DocumentLine> lines = linesByDocument.get(doc.getId());

            // First, try to use doc.getPriceNet() but if it's 0 or null, calculate from lines
            Integer priceNetObj = doc.getPriceNet();
            int totalPrice = priceNetObj != null ? priceNetObj : 0;

            // If totalPrice is 0, calculate from document lines
            if (totalPrice == 0 && lines != null) {
                for (DocumentLine line : lines) {
                    Integer linePriceObj = line.getPriceNet();
                    if (linePriceObj != null) {
                        totalPrice += linePriceObj;
                    }
                }
                Console.log("Calculated total from lines: " + totalPrice);
            }

            int paidAmount = paidAmounts.getOrDefault(doc.getId(), 0);
            int balance = totalPrice - paidAmount;

            Console.log("Processing booking for " + personName + ": docPriceNet=" + priceNetObj + ", calculatedTotal=" + totalPrice + ", paid=" + paidAmount);

            // Get minDeposit from document (calculated by API/price algorithm)
            Integer priceMinDepositObj = doc.getPriceMinDeposit();
            int minDeposit = priceMinDepositObj != null ? priceMinDepositObj : totalPrice; // Default to full amount if not set

            // Create booking item
            HasPendingBookingsSection.BookingItem bookingItem = new HasPendingBookingsSection.BookingItem(
                doc,
                personName,
                personEmail,
                eventName,
                totalPrice,
                minDeposit
            );

            // Mark as paid if balance is zero or negative
            bookingItem.setPaid(balance <= 0);
            bookingItem.setPaidAmount(paidAmount);
            Object refObj = doc.getRef();
            bookingItem.setBookingReference(refObj != null ? refObj.toString() : null);

            // Add line items from DocumentLines
            if (lines != null) {
                for (DocumentLine line : lines) {
                    Item item = line.getItem();
                    if (item != null) {
                        ItemFamily family = item.getFamily();
                        // Skip items where family has summaryHidden = true (e.g., rounding)
                        if (family != null && Boolean.TRUE.equals(family.isSummaryHidden())) {
                            continue;
                        }
                        String familyCode = family != null ? family.getCode() : "";
                        // Use family name for display, fall back to item name if no family
                        String familyName = (family != null && family.getName() != null) ? family.getName() : null;
                        String displayName = familyName != null ? familyName : (item.getName() != null ? item.getName() : "Item");
                        Integer linePriceObj = line.getPriceNet();
                        int linePrice = linePriceObj != null ? linePriceObj : 0;

                        Console.log("  Line item: " + displayName + " = " + linePrice);
                        bookingItem.addLineItem(displayName, familyCode, linePrice);
                    }
                }
            } else {
                Console.log("  No document lines found for document ID: " + doc.getId());
            }

            defaultPendingBookingsSection.addBooking(bookingItem);
        }

        // Update alreadyBookedPersonIds in member selection section
        // This ensures the "Register Another Person" button is correctly enabled/disabled
        if (defaultMemberSelectionSection != null) {
            Set<Object> bookedPersonIds = new HashSet<>();
            for (Document doc : documents) {
                Person person = doc.getPerson();
                if (person != null) {
                    // MUST use getPrimaryKey() to match how MemberInfo stores person IDs
                    bookedPersonIds.add(person.getPrimaryKey());
                }
            }
            defaultMemberSelectionSection.setAlreadyBookedPersonIds(bookedPersonIds);
        }

        Console.log("Added " + documents.size() + " bookings to pending bookings section");
    }

    /**
     * Get full name from Person entity.
     */
    private String getPersonFullName(Person person) {
        if (person == null) return "";
        String firstName = person.getFirstName() != null ? person.getFirstName() : "";
        String lastName = person.getLastName() != null ? person.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    // === Payment Page Handlers ===

    private Future<?> handlePaymentSubmitAsync() {
        if (defaultPaymentSection == null) {
            return Future.failedFuture("Payment section not available");
        }
        return defaultPaymentSection.submitPaymentAsync();
    }

    // === Internal Event Handlers ===
    // These handle the generic booking flow logic.

    /**
     * Handles login success asynchronously, returning a Future that completes when
     * household members are loaded and navigation is ready.
     */
    private Future<?> handleLoginSuccessAsync(Person person) {
        // Update state
        state.setLoggedInPerson(person);

        // Notify callbacks (optional hook)
        if (callbacks != null) {
            callbacks.onAfterLogin();
        }

        // Load household members and navigate
        Event event = getEvent();
        if (defaultMemberSelectionSection != null) {
            return HouseholdMemberLoader.loadMembersAsync(person, defaultMemberSelectionSection, event)
                .onSuccess(v -> UiScheduler.runInUiThread(this::navigateToMemberSelection));
        } else {
            // Skip member selection if section is not configured
            navigateToMemberSelection();
            return Future.succeededFuture();
        }
    }

    /**
     * Handles login success synchronously (for internal use from Your Information section).
     * Calls the async version but doesn't return the Future.
     */
    private void handleLoginSuccess(Person person) {
        handleLoginSuccessAsync(person);
    }

    private void handleNewUserContinue(HasYourInformationSection.NewUserData newUserData) {
        // Store pending new user data in state
        state.setPendingNewUserData(newUserData);

        // For new users (both guests and those creating accounts), skip member selection
        // and go directly to summary. The account doesn't exist yet, so there are no
        // household members to select. Account creation email will be sent after booking.
        updateSummaryWithAttendee();
        navigateToSummary();
    }

    private void handleMemberSelected(HasMemberSelectionSection.MemberInfo member) {
        // Update state
        state.setSelectedMember(member);

        // Notify callbacks (optional hook)
        if (callbacks != null) {
            callbacks.onAfterMemberSelected();
        }
    }

    private void handleMemberSelectionContinue() {
        // Update summary with attendee info
        updateSummaryWithAttendee();

        // Navigate to summary
        navigateToSummary();
    }

    private void handleMemberSelectionBack() {
        // If user is logged in or has pending new user data, go back to custom steps
        if (state.isLoggedIn() || state.hasPendingNewUser()) {
            navigateToLastCustomStep();
        } else {
            navigateToYourInformation();
        }
    }

    private void handleRegisterAnotherPerson() {
        // Prepare state for new booking (creates fresh WorkingBooking document)
        state.prepareForNewBooking();

        // Reset summary section for fresh booking
        if (defaultSummarySection != null) {
            defaultSummarySection.clearPriceLines();
            defaultSummarySection.clearAdditionalOptions();
        }

        // Reset sections in custom pages (audio recording, prerequisite, etc.)
        resetCustomPageSections();

        // Notify callbacks for any additional custom reset logic
        if (callbacks != null) {
            callbacks.onPrepareNewBooking();
        }

        // Navigate back to first custom step
        navigateToFirstCustomStep();
    }

    /**
     * Resets all resettable sections in custom pages.
     * Finds sections implementing ResettableSection and calls reset() on them.
     */
    private void resetCustomPageSections() {
        for (BookingFormPage page : customStepPages) {
            if (page instanceof CompositeBookingFormPage compositePage) {
                for (BookingFormSection section : compositePage.getSections()) {
                    // Reset sections implementing ResettableSection
                    if (section instanceof ResettableSection) {
                        ((ResettableSection) section).reset();
                    }
                }
            }
        }
    }

    private void handleProceedToPayment() {
        // Update payment section with totals from pending bookings
        updatePaymentFromPendingBookings();

        // Navigate to payment
        navigateToPayment();
    }

    /**
     * Handles the "Proceed to Payment" or "Confirm Booking" button on the pending bookings page.
     * If the total is zero, skips payment and goes directly to confirmation.
     * Otherwise, proceeds to the payment page.
     */
    private void handleProceedToPaymentOrConfirm() {
        int total = defaultPendingBookingsSection != null ? defaultPendingBookingsSection.getTotalAmount() : 0;

        if (total <= 0) {
            // Zero price - skip payment and go directly to confirmation
            updateConfirmationForZeroPrice();
            navigateToConfirmation();
        } else {
            // Normal flow - proceed to payment
            handleProceedToPayment();
        }
    }

    /**
     * Updates the button text on the pending bookings page based on whether payment is required.
     * Called when bookings change or on initialization.
     */
    private void updatePendingBookingsButtonText() {
        int total = defaultPendingBookingsSection != null ? defaultPendingBookingsSection.getTotalAmount() : 0;

        if (total <= 0) {
            // Zero price - show "Confirm Booking" button
            I18n.bindI18nTextProperty(pendingBookingsButtonText, BookingPageI18nKeys.ConfirmBookingArrow);
        } else {
            // Payment required - show "Proceed to Payment" button
            I18n.bindI18nTextProperty(pendingBookingsButtonText, BookingPageI18nKeys.ProceedToPaymentArrow);
        }
    }

    /**
     * Updates the confirmation section for a zero-price booking (no payment required).
     */
    private void updateConfirmationForZeroPrice() {
        if (defaultConfirmationSection == null) return;

        defaultConfirmationSection.clearConfirmedBookings();

        // Add confirmed bookings from pending bookings section
        if (defaultPendingBookingsSection != null) {
            for (HasPendingBookingsSection.BookingItem booking : defaultPendingBookingsSection.getBookings()) {
                defaultConfirmationSection.addConfirmedBooking(new HasConfirmationSection.ConfirmedBooking(
                    booking.getPersonName(),
                    booking.getPersonEmail(),
                    booking.getBookingReference()));
            }
        }

        // Set event info
        Event event = getEvent();
        if (event != null) {
            defaultConfirmationSection.setEventName(event.getName());
            defaultConfirmationSection.setEventDates(event.getStartDate(), event.getEndDate());
        }

        // Set payment amounts (both zero for free booking)
        defaultConfirmationSection.setPaymentAmounts(0, 0);
    }

    private Future<Void> handlePaymentSubmit(HasPaymentSection.PaymentResult sectionResult) {

        // Converting into paymentAllocations (required for InitiatePaymentArgument)
        PaymentAllocation[] paymentAllocations = sectionResult.getAllocations().entrySet().stream()
            .map(entry -> new PaymentAllocation(Entities.getPrimaryKey(entry.getKey()), entry.getValue()))
            .toArray(PaymentAllocation[]::new);

        return ClientPaymentUtil.initiateRedirectedPaymentAndRedirectToGatewayPaymentPage(sectionResult.getAmount(), paymentAllocations);
    }

    // TODO: once embedded payment is implemented, call back this method on payment success
    private void handleEmbeddedPaymentSuccess(HasPaymentSection.PaymentResult sectionResult) {
        // Convert to callbacks result type
        StandardBookingFormCallbacks.PaymentResult result = new StandardBookingFormCallbacks.PaymentResult(
            sectionResult.getAmount()
        );

        // Update confirmation section
        updateConfirmationSection(result);

        // Notify callbacks (optional hook)
        if (callbacks != null) {
            callbacks.onAfterPayment();
        }

        // Navigate to confirmation
        navigateToConfirmation();
    }

    private void handleMakeAnotherBooking() {
        // Reset state
        state.reset();

        // Clear stored new user info
        storedNewUserName = null;
        storedNewUserEmail = null;

        // Clear sections
        if (defaultMemberSelectionSection != null) {
            defaultMemberSelectionSection.clearMembers();
            defaultMemberSelectionSection.clearAlreadyBooked();
            defaultMemberSelectionSection.clearSelection();
        }
        if (defaultPendingBookingsSection != null) {
            defaultPendingBookingsSection.clearBookings();
        }

        // Navigate to first custom step
        navigateToFirstCustomStep();
    }

    // === Summary and Payment Updates ===

    private void updateSummaryWithAttendee() {
        if (defaultSummarySection == null) return;

        // Set attendee info from state
        HasMemberSelectionSection.MemberInfo member = state.getSelectedMember();
        HasYourInformationSection.NewUserData newUser = state.getPendingNewUserData();

        if (member != null) {
            defaultSummarySection.setAttendeeName(member.getName());
            defaultSummarySection.setAttendeeEmail(member.getEmail());
        } else if (newUser != null) {
            defaultSummarySection.setAttendeeName(newUser.firstName + " " + newUser.lastName);
            defaultSummarySection.setAttendeeEmail(newUser.email);
        } else {
            // Fallback for modifications: get from WorkingBooking document
            WorkingBookingProperties props = state.getWorkingBookingProperties();
            WorkingBooking wb = props != null ? props.getWorkingBooking() : null;
            if (wb != null) {
                Document doc = wb.getDocument();
                if (doc != null) {
                    // Document has direct firstName/lastName/email via EntityHasPersonalDetailsCopy
                    String firstName = doc.getFirstName();
                    String lastName = doc.getLastName();
                    StringBuilder name = new StringBuilder();
                    if (firstName != null) name.append(firstName);
                    if (lastName != null) {
                        if (name.length() > 0) name.append(" ");
                        name.append(lastName);
                    }
                    if (name.length() > 0) {
                        defaultSummarySection.setAttendeeName(name.toString());
                    }
                    if (doc.getEmail() != null) {
                        defaultSummarySection.setAttendeeEmail(doc.getEmail());
                    }
                }
            }
        }

        // Set event info
        Event event = getEvent();
        if (event != null) {
            defaultSummarySection.setEventName(event.getName());
            defaultSummarySection.setEventDates(event.getStartDate(), event.getEndDate());
        }

        // Clear and update price lines
        defaultSummarySection.clearPriceLines();
        defaultSummarySection.clearAdditionalOptions();

        // Add default price lines from WorkingBooking document lines
        addDefaultSummaryPriceLines();

        // Allow callbacks to add additional form-specific price lines
        if (callbacks != null) {
            callbacks.updateSummary();
        }
    }

    /**
     * Adds default price lines from WorkingBooking.
     * Groups attendances by item family and calculates prices using date-specific daily rates.
     */
    private void addDefaultSummaryPriceLines() {
        WorkingBookingProperties props = state.getWorkingBookingProperties();
        if (props == null) return;

        WorkingBooking workingBooking = props.getWorkingBooking();
        DocumentAggregate documentAggregate = workingBooking.getLastestDocumentAggregate();
        if (documentAggregate == null) return;

        // Get policy aggregate to access daily rates
        PolicyAggregate policyAggregate = documentAggregate.getPolicyAggregate();
        if (policyAggregate == null) return;

        // Get all attendances
        List<Attendance> attendances = documentAggregate.getAttendances();
        if (attendances == null || attendances.isEmpty()) return;

        // Get all daily rates
        List<Rate> dailyRates = policyAggregate.getDailyRates();

        // Group attendances by item family and calculate prices using date-specific rates
        Map<ItemFamily, Integer> pricesByFamily = new LinkedHashMap<>();
        for (Attendance attendance : attendances) {
            DocumentLine line = attendance.getDocumentLine();
            if (line == null) continue;

            Item item = line.getItem();
            if (item == null) continue;

            ItemFamily family = item.getFamily();
            if (family == null) continue;

            // Find the rate applicable to this attendance's date
            java.time.LocalDate attendanceDate = attendance.getDate();
            Rate applicableRate = findRateForDate(attendanceDate, item, dailyRates);

            if (applicableRate != null && applicableRate.getPrice() != null) {
                pricesByFamily.merge(family, applicableRate.getPrice(), Integer::sum);
            }
        }

        // Add a price line for each item family
        for (Map.Entry<ItemFamily, Integer> entry : pricesByFamily.entrySet()) {
            ItemFamily family = entry.getKey();
            int priceInCents = entry.getValue();
            String displayName = family.getName() != null ? family.getName() : "Item";
            defaultSummarySection.addPriceLine(displayName, null, priceInCents);
        }
    }

    /**
     * Finds the daily rate applicable to a specific date for an item.
     */
    private Rate findRateForDate(java.time.LocalDate date, Item item, List<Rate> dailyRates) {
        if (date == null || dailyRates == null) return null;

        // Filter rates for this item
        for (Rate rate : dailyRates) {
            if (rate.getItem() == null || !rate.getItem().equals(item)) continue;

            java.time.LocalDate rateStart = rate.getStartDate();
            java.time.LocalDate rateEnd = rate.getEndDate();

            // Check if rate's date range includes the attendance date
            if (rateStart != null && rateEnd != null) {
                if (!date.isBefore(rateStart) && !date.isAfter(rateEnd)) {
                    return rate;
                }
            } else if (rateStart != null && rateStart.equals(date)) {
                return rate;
            }
        }

        // Fallback: return first rate for this item (if no date match)
        for (Rate rate : dailyRates) {
            if (rate.getItem() != null && rate.getItem().equals(item)) {
                return rate;
            }
        }

        return null;
    }

    private void updatePaymentFromPendingBookings() {
        if (defaultPaymentSection == null || defaultPendingBookingsSection == null) return;

        defaultPaymentSection.clearBookingItems();
        int total = 0;
        int totalMinDeposit = 0;

        for (HasPendingBookingsSection.BookingItem booking : defaultPendingBookingsSection.getBookings()) {
            defaultPaymentSection.addBookingItem(new HasPaymentSection.PaymentBookingItem(
                booking.getDocument(),
                booking.getPersonName(),
                booking.getEventName(),
                booking.getTotalAmount()));
            total += booking.getTotalAmount();
            totalMinDeposit += booking.getMinDeposit();
        }

        defaultPaymentSection.setTotalAmount(total);
        // Use the calculated minDeposit from the booking items (comes from API/database)
        defaultPaymentSection.setDepositAmount(totalMinDeposit);
    }

    /**
     * Populates the Pending Bookings section with data from the just-submitted booking for new users.
     * This is used when the user is a guest or creating an account (not logged in, so can't load from DB).
     */
    private void populatePendingBookingsForNewUser(String userName, String userEmail) {
        // Store new user info for confirmation section (state is cleared after submission)
        this.storedNewUserName = userName;
        this.storedNewUserEmail = userEmail;

        if (defaultPendingBookingsSection == null) return;

        defaultPendingBookingsSection.clearBookings();

        // Get booking info from WorkingBookingProperties
        WorkingBookingProperties props = state.getWorkingBookingProperties();
        if (props == null) {
            Console.log("ERROR: WorkingBookingProperties is null in populatePendingBookingsForNewUser");
            return;
        }

        int total = props.getTotal();
        int minDeposit = props.getMinDeposit();
        Event event = props.getWorkingBooking().getEvent();
        String eventName = event != null ? event.getName() : "";

        // Get booking reference
        Object bookingRefObj = props.getBookingReference();
        String bookingRef = bookingRefObj != null ? String.valueOf(bookingRefObj) : null;

        // Create booking item for new user
        HasPendingBookingsSection.BookingItem bookingItem = new HasPendingBookingsSection.BookingItem(
            props.getWorkingBooking().getDocument(),
            userName != null ? userName : "Guest",
            userEmail != null ? userEmail : "",
            eventName,
            total,
            minDeposit
        );
        bookingItem.setBookingReference(bookingRef);

        // Add line items from WorkingBooking document lines (if available)
        WorkingBooking workingBooking = props.getWorkingBooking();
        if (workingBooking != null && workingBooking.getDocument() != null) {
            // For now, add a single line item with the total
            // TODO: Could iterate through actual document lines for more detail
            bookingItem.addLineItem(new HasPendingBookingsSection.BookingLineItem(eventName, total, false));
        }

        defaultPendingBookingsSection.addBooking(bookingItem);

        Console.log("Pending bookings populated for new user: " + userName + ", total: " + total);
    }

    private void updateConfirmationSection(StandardBookingFormCallbacks.PaymentResult result) {
        if (defaultConfirmationSection == null) return;

        defaultConfirmationSection.clearConfirmedBookings();

        // Add confirmed bookings - either from pending bookings or from new user data
        boolean hasBookingsFromPendingSection = defaultPendingBookingsSection != null
            && !defaultPendingBookingsSection.getBookings().isEmpty();

        if (hasBookingsFromPendingSection) {
            // Logged-in users: get booking info from pending bookings section
            for (HasPendingBookingsSection.BookingItem booking : defaultPendingBookingsSection.getBookings()) {
                defaultConfirmationSection.addConfirmedBooking(new HasConfirmationSection.ConfirmedBooking(
                    booking.getPersonName(),
                    booking.getPersonEmail(),
                    booking.getBookingReference()));
            }
        } else {
            // New users (guest or account creation): use stored info from payment flow
            // (state.getPendingNewUserData() is cleared after submission)
            WorkingBookingProperties props = state.getWorkingBookingProperties();
            Object bookingRefObj = props != null ? props.getBookingReference() : null;
            String bookingRef = bookingRefObj != null ? String.valueOf(bookingRefObj) : null;

            if (storedNewUserName != null) {
                defaultConfirmationSection.addConfirmedBooking(new HasConfirmationSection.ConfirmedBooking(
                    storedNewUserName,
                    storedNewUserEmail,
                    bookingRef));
            }
        }

        // Set event info
        Event event = getEvent();
        if (event != null) {
            defaultConfirmationSection.setEventName(event.getName());
            defaultConfirmationSection.setEventDates(event.getStartDate(), event.getEndDate());
        }

        // Set payment amounts
        int totalAmount;
        if (hasBookingsFromPendingSection) {
            totalAmount = defaultPendingBookingsSection.getTotalAmount();
        } else {
            // Get total from WorkingBookingProperties for new users
            WorkingBookingProperties props = state.getWorkingBookingProperties();
            totalAmount = props != null ? props.getTotal() : 0;
        }
        defaultConfirmationSection.setPaymentAmounts(totalAmount, result.getAmount());
    }

    // === Helper Methods ===

    private Event getEvent() {
        WorkingBookingProperties props = state.getWorkingBookingProperties();
        if (props != null && props.getWorkingBooking() != null) {
            return props.getWorkingBooking().getEvent();
        }
        return null;
    }

    // === Navigation Methods ===

    public void navigateTo(BookingFormPage page) {
        for (int i = 0; i < pages.length; i++) {
            if (pages[i] == page) {
                navigateToPage(i);
                return;
            }
        }
    }

    public void navigateToLastCustomStep() {
        if (!customStepPages.isEmpty()) {
            navigateTo(customStepPages.get(customStepPages.size() - 1));
        }
    }

    public void navigateToFirstCustomStep() {
        // Find the first applicable custom step page
        // This is important when the first page may not be applicable (e.g., ExistingBookingSection
        // is only applicable for existing bookings, not new ones)
        WorkingBooking workingBooking = getWorkingBooking();
        for (BookingFormPage page : customStepPages) {
            if (page.isApplicableToBooking(workingBooking)) {
                navigateTo(page);
                return;
            }
        }
        // If no custom step is applicable, continue to the standard pages
        navigateToNextPage();
    }

    public void navigateToYourInformation() {
        navigateTo(yourInformationPage);
    }

    public void navigateToMemberSelection() {
        if (memberSelectionPage != null) {
            navigateTo(memberSelectionPage);
        }
    }

    public void navigateToSummary() {
        updateSummaryWithAttendee();
        navigateTo(summaryPage);
    }

    public void navigateToPendingBookings() {
        if (pendingBookingsPage != null) {
            navigateTo(pendingBookingsPage);
        } else {
            navigateTo(paymentPage);
        }
    }

    public void navigateToPayment() {
        navigateTo(paymentPage);
    }

    public void navigateToConfirmation() {
        navigateTo(confirmationPage);
    }

    // === Public API for Custom Step Navigation ===

    /**
     * Called from custom steps to continue to the next standard step.
     * Uses navigateToNextPage() which respects isApplicableToBooking() to skip
     * non-applicable pages (e.g., Your Information and Member Selection for existing bookings).
     *
     * @return Future that completes when the navigation is ready (including any async loading)
     */
    public Future<?> continueFromCustomSteps() {
        Person userPerson = FXUserPerson.getUserPerson();

        if (userPerson != null) {
            // User already logged in - update state and pre-load members (in case member selection is applicable)
            state.setLoggedInPerson(userPerson);
            if (callbacks != null) {
                callbacks.onAfterLogin();
            }
            Event event = getEvent();
            if (defaultMemberSelectionSection != null) {
                // Pre-load household members, then navigate to next applicable page
                return HouseholdMemberLoader.loadMembersAsync(userPerson, defaultMemberSelectionSection, event)
                    .onSuccess(v -> UiScheduler.runInUiThread(this::navigateToNextPage));
            }
        }
        // Not logged in, or no member selection section - just navigate to next applicable page
        navigateToNextPage();
        return Future.succeededFuture();
    }

    // === Accessors ===

    @Override
    public BookingFormPage[] getPages() {
        return pages;
    }

    public BookingFormColorScheme getColorScheme() {
        return colorScheme;
    }

    public StandardBookingFormCallbacks getCallbacks() {
        return callbacks;
    }

    /**
     * Returns the booking form state for direct access if needed.
     */
    public BookingFormState getState() {
        return state;
    }
}
