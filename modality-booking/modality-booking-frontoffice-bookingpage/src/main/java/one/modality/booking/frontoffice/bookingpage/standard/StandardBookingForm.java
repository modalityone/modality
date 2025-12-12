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
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.markers.HasPersonalDetails;
import one.modality.booking.client.workingbooking.*;
import one.modality.booking.frontoffice.bookingpage.*;
import one.modality.booking.frontoffice.bookingpage.navigation.ButtonNavigation;
import one.modality.booking.frontoffice.bookingpage.navigation.ResponsiveStepProgressHeader;
import one.modality.booking.frontoffice.bookingpage.sections.*;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.booking.frontoffice.bookingpage.theme.ThemedBookingFormSection;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXModalityUserPrincipal;
import one.modality.crm.shared.services.authn.fx.FXUserPerson;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.PolicyAggregate;
import one.modality.ecommerce.payment.PaymentAllocation;
import one.modality.ecommerce.payment.PaymentFormType;
import one.modality.ecommerce.payment.PaymentService;
import one.modality.ecommerce.payment.client.ClientPaymentUtil;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.frontoffice.activities.book.event.EventBookingFormSettings;
import one.modality.event.frontoffice.activities.book.fx.FXGuestToBook;

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
    private final BookingFormPage[] pages;
    private final StandardBookingFormCallbacks callbacks;

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
    private DefaultPaymentSection defaultPaymentSection;
    private DefaultConfirmationSection defaultConfirmationSection;

    // Stored new user info for payment/confirmation flow (persists after submission clears state)
    private String storedNewUserName;
    private String storedNewUserEmail;

    /**
     * Package-private constructor - use {@link StandardBookingFormBuilder} to create instances.
     */
    StandardBookingForm(
            HasWorkingBookingProperties activity,
            EventBookingFormSettings settings,
            BookingFormColorScheme colorScheme,
            List<BookingFormPage> customSteps,
            Supplier<BookingFormPage> yourInformationPageSupplier,
            Supplier<BookingFormPage> memberSelectionPageSupplier,
            boolean skipMemberSelection,
            Supplier<BookingFormPage> summaryPageSupplier,
            Supplier<BookingFormPage> pendingBookingsPageSupplier,
            boolean skipPendingBookings,
            Supplier<BookingFormPage> paymentPageSupplier,
            Supplier<BookingFormPage> confirmationPageSupplier,
            StandardBookingFormCallbacks callbacks) {

        super(activity, settings);
        this.colorScheme = colorScheme;
        this.callbacks = callbacks;
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
                new ThemedBookingFormSection(defaultYourInformationSection, colorScheme))
            .setStep(false)
            .setShowingOwnSubmitButton(true);
    }

    protected BookingFormPage createDefaultMemberSelectionPage() {
        defaultMemberSelectionSection = new DefaultMemberSelectionSection();
        defaultMemberSelectionSection.setColorScheme(colorScheme);
        return new CompositeBookingFormPage(BookingPageI18nKeys.MemberSelection,
                new ThemedBookingFormSection(defaultMemberSelectionSection, colorScheme))
            .setStep(true)
            .setShowingOwnSubmitButton(true);
    }

    protected BookingFormPage createDefaultSummaryPage() {
        defaultSummarySection = new DefaultSummarySection();
        defaultSummarySection.setColorScheme(colorScheme);
        return new CompositeBookingFormPage(BookingPageI18nKeys.Summary,
                new ThemedBookingFormSection(defaultSummarySection, colorScheme))
            .setStep(true);
    }

    protected BookingFormPage createDefaultPendingBookingsPage() {
        defaultPendingBookingsSection = new DefaultPendingBookingsSection();
        defaultPendingBookingsSection.setColorScheme(colorScheme);
        return new CompositeBookingFormPage(BookingPageI18nKeys.PendingBookings,
                new ThemedBookingFormSection(defaultPendingBookingsSection, colorScheme))
            .setStep(true);
    }

    protected BookingFormPage createDefaultPaymentPage() {
        defaultPaymentSection = new DefaultPaymentSection();
        defaultPaymentSection.setColorScheme(colorScheme);
        return new CompositeBookingFormPage(BookingPageI18nKeys.Payment,
                new ThemedBookingFormSection(defaultPaymentSection, colorScheme))
            .setStep(true);
    }

    protected BookingFormPage createDefaultConfirmationPage() {
        defaultConfirmationSection = new DefaultConfirmationSection();
        defaultConfirmationSection.setColorScheme(colorScheme);
        return new CompositeBookingFormPage(BookingPageI18nKeys.Confirmation,
                new ThemedBookingFormSection(defaultConfirmationSection, colorScheme))
            .setStep(true)
            .setButtons() // No navigation buttons on confirmation page
            .setShowingOwnSubmitButton(true); // Prevents default activity-level submit button from showing
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

        // Pending Bookings page: Register Another Person + Proceed to Payment
        if (pendingBookingsPage instanceof CompositeBookingFormPage compositePending) {
            compositePending.setButtons(
                new BookingFormButton(BookingPageI18nKeys.RegisterAnotherPerson,
                    e -> handleRegisterAnotherPerson(),
                    "btn-secondary booking-form-btn-secondary"),
                new BookingFormButton(BookingPageI18nKeys.ProceedToPaymentArrow,
                    e -> handleProceedToPayment(),
                    "btn-primary booking-form-btn-primary")
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
        // Go back to Member Selection if available, otherwise Your Information
        if (memberSelectionPage != null) {
            // Allow member reselection when going back
            state.setAllowMemberReselection(true);
            navigateToMemberSelection();
        } else {
            navigateToYourInformation();
        }
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

            // Create booking item
            HasPendingBookingsSection.BookingItem bookingItem = new HasPendingBookingsSection.BookingItem(
                doc,
                personName,
                personEmail,
                eventName,
                totalPrice  // Convert cents to currency
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

    private void handleLoginSuccess(Person person) {
        // Update state
        state.setLoggedInPerson(person);

        // Notify callbacks (optional hook)
        if (callbacks != null) {
            callbacks.onAfterLogin();
        }

        // Load household members and navigate
        Event event = getEvent();
        if (defaultMemberSelectionSection != null) {
            HouseholdMemberLoader.loadMembersAsync(person, defaultMemberSelectionSection, event)
                .onSuccess(v -> UiScheduler.runInUiThread(this::navigateToMemberSelection));
        } else {
            // Skip member selection if section is not configured
            navigateToMemberSelection();
        }
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
                    // Check wrapped sections (ThemedBookingFormSection delegates)
                    if (section instanceof ThemedBookingFormSection) {
                        BookingFormSection delegate = ((ThemedBookingFormSection) section).getDelegate();
                        if (delegate instanceof ResettableSection) {
                            ((ResettableSection) delegate).reset();
                        }
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

    private Future<Void> handlePaymentSubmit(HasPaymentSection.PaymentResult sectionResult) {

        // Converting into paymentAllocations (required for InitiatePaymentArgument)
        PaymentAllocation[] paymentAllocations = sectionResult.getAllocations().entrySet().stream()
            .map(entry -> new PaymentAllocation(Entities.getPrimaryKey(entry.getKey()), entry.getValue()))
            .toArray(PaymentAllocation[]::new);

        return PaymentService.initiatePayment(
                ClientPaymentUtil.createInitiatePaymentArgument(
                    sectionResult.getAmount(),
                    paymentAllocations,
                    PaymentFormType.REDIRECTED, // We were using EMBEDDED so far, now we try REDIRECTED
                    "/payment-return/:moneyTransferId",
                    "/payment-cancel/:moneyTransferId")
            )
            .inUiThread()
            .onFailure(paymentResult -> {
                // TODO: show an error message to the user
                Console.log(paymentResult);
            })
            .compose(paymentResult -> {
                HasPersonalDetails buyerDetails = FXUserPerson.getUserPerson();
                if (buyerDetails == null)
                    buyerDetails = FXGuestToBook.getGuestToBook();
                WebPaymentForm webPaymentForm = new WebPaymentForm(paymentResult, buyerDetails);
                // If it's a redirected payment form, we just navigate to it
                if (webPaymentForm.isRedirectedPaymentForm()) {
                    webPaymentForm.navigateToRedirectedPaymentForm();
                    return Future.succeededFuture();
                } else {
                    return Future.failedFuture("Embedded payment form is not yet implemented in " + getClass().getSimpleName());
                }
            });
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

        for (HasPendingBookingsSection.BookingItem booking : defaultPendingBookingsSection.getBookings()) {
            defaultPaymentSection.addBookingItem(new HasPaymentSection.PaymentBookingItem(
                booking.getDocument(),
                booking.getPersonName(),
                booking.getEventName(),
                booking.getTotalAmount()));
            total += booking.getTotalAmount();
        }

        defaultPaymentSection.setTotalAmount(total);
        defaultPaymentSection.setDepositAmount(total / 10);
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
            total
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
        if (!customStepPages.isEmpty()) {
            navigateTo(customStepPages.get(0));
        }
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
     * Handles checking if user is already logged in.
     */
    public void continueFromCustomSteps() {
        Person userPerson = FXUserPerson.getUserPerson();

        if (userPerson != null) {
            // User already logged in - load members and go to member selection
            handleLoginSuccess(userPerson);
        } else {
            // Not logged in - go to Your Information
            navigateToYourInformation();
        }
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
