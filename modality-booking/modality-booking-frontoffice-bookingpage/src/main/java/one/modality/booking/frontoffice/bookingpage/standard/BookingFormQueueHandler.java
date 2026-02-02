package one.modality.booking.frontoffice.bookingpage.standard;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.util.dialog.DialogCallback;
import dev.webfx.extras.util.dialog.DialogUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.visibility.Visibility;
import dev.webfx.platform.visibility.VisibilityListener;
import dev.webfx.platform.visibility.VisibilityState;
import dev.webfx.platform.windowlocation.WindowLocation;
import dev.webfx.stack.authn.AuthenticationService;
import dev.webfx.stack.authn.InitiateAccountCreationCredentials;
import one.modality.base.client.error.ErrorReporter;
import one.modality.base.client.mainframe.fx.FXMainFrameDialogArea;
import one.modality.base.shared.entities.Event;
import one.modality.booking.client.workingbooking.EventQueueFinalResultNotification;
import one.modality.booking.client.workingbooking.EventQueueProgressNotification;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingpage.BookingFormButton;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.CompositeBookingFormPage;
import one.modality.booking.frontoffice.bookingpage.sections.queue.DefaultUnifiedQueueSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.booking.frontoffice.bookingpage.util.SoldOutErrorParser;
import one.modality.ecommerce.document.service.DocumentChangesRejectedReason;
import one.modality.ecommerce.document.service.DocumentService;
import one.modality.ecommerce.document.service.SubmitDocumentChangesResult;

/**
 * Handles registration queue processing for booking forms.
 * Extracted from StandardBookingForm to improve maintainability.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Managing the unified queue page (countdown + processing states)</li>
 *   <li>Handling ENQUEUED status from booking submission</li>
 *   <li>Processing final queue results (APPROVED, SOLD_OUT)</li>
 *   <li>Tab visibility handling to recover from missed push notifications</li>
 *   <li>Leave queue functionality</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public class BookingFormQueueHandler {

    /**
     * Callback interface for queue handler to communicate with the form.
     */
    public interface QueueFormCallback {
        /** Get the current event */
        Event getEvent();

        /** Get the color scheme for styling */
        BookingFormColorScheme getColorScheme();

        /** Get the working booking properties */
        WorkingBookingProperties getWorkingBookingProperties();

        /** Navigate to a special page */
        void navigateToSpecialPage(CompositeBookingFormPage page);

        /** Navigate to pending bookings page */
        void navigateToPendingBookings();

        /** Navigate to summary page */
        void navigateToSummary();

        /** Handle sold-out accommodation error */
        void handleAccommodationSoldOut(SoldOutErrorParser.SoldOutInfo soldOutInfo);

        /** Populate pending bookings for new user */
        void populatePendingBookingsForNewUser(String name, String email);

        /** Load all bookings for event asynchronously */
        Future<?> loadAllBookingsForEventAsync();

        /** Check if registration is open based on policy */
        boolean isRegistrationOpen();

        /** Get seconds until registration opens */
        int getSecondsToRegistrationOpening();

        /** Set registration confirmed open flag */
        void setRegistrationConfirmedOpen(boolean confirmed);

        /** Show the offline/maintenance page */
        void showOfflinePage();

        /** Show "already booked" error dialog */
        void showAlreadyBookedErrorDialog();
    }

    private final QueueFormCallback callback;

    // Queue page components (lazy-initialized)
    private DefaultUnifiedQueueSection unifiedQueueSection;
    private CompositeBookingFormPage unifiedQueuePage;

    // Queue state
    private Object currentQueueToken; // Token for the current enqueued booking (used to leave queue)
    private VisibilityListener queueVisibilityListener; // For fetching missed queue results when tab becomes active

    // Stored context for handling queue results after tab reactivation
    private boolean storedIsNewUser;
    private String storedNewUserName;
    private String storedNewUserEmail;
    private boolean storedWantsAccountCreation;

    /**
     * Creates a new queue handler.
     *
     * @param callback The callback interface for communicating with the form
     */
    public BookingFormQueueHandler(QueueFormCallback callback) {
        this.callback = callback;
    }

    /**
     * Handles ENQUEUED status from submission by showing the unified queue page.
     * The page shows a countdown if registration is not yet open, then transitions
     * to progress display when the server starts processing.
     *
     * @param result The submit result containing queue token
     * @param isNewUser Whether this is a new user (guest checkout)
     * @param newUserName Name for new users
     * @param newUserEmail Email for new users
     * @param wantsAccountCreation Whether user wants to create an account
     */
    public void handleEnqueued(SubmitDocumentChangesResult result, boolean isNewUser, String newUserName, String newUserEmail, boolean wantsAccountCreation) {
        Object queueToken = result.queueToken();
        this.currentQueueToken = queueToken; // Store for use when leaving queue

        // Store context for potential deferred result handling (if tab was inactive)
        this.storedIsNewUser = isNewUser;
        this.storedNewUserName = newUserName;
        this.storedNewUserEmail = newUserEmail;
        this.storedWantsAccountCreation = wantsAccountCreation;

        Event event = callback.getEvent();

        // Navigate to unified queue page
        navigateToUnifiedQueuePage();

        // Determine initial state based on whether registration is open
        if (unifiedQueueSection != null) {
            // Set up callback for when countdown completes
            unifiedQueueSection.setOnCountdownComplete(() -> {
                // Countdown reached zero - transition to processing state
                unifiedQueueSection.transitionToProcessing();
            });

            if (callback.isRegistrationOpen()) {
                // Registration already open - show processing state directly
                unifiedQueueSection.setState(DefaultUnifiedQueueSection.QueueState.QUEUE_PROCESSING);
                unifiedQueueSection.startStatusMessageRotation();
            } else {
                // Registration not yet open - show countdown
                int secondsToOpening = callback.getSecondsToRegistrationOpening();
                unifiedQueueSection.setState(DefaultUnifiedQueueSection.QueueState.PRE_COUNTDOWN);
                unifiedQueueSection.startCountdown(secondsToOpening);
            }
        }

        // Set up queue notification to track progress
        if (event != null) {
            EventQueueProgressNotification eventQueueProgressNotification = EventQueueProgressNotification.getOrCreate(event);

            // Update progress when queue status changes
            FXProperties.runOnPropertyChange(progress -> {
                if (progress != null && unifiedQueueSection != null) {
                    UiScheduler.runInUiThread(() -> {
                        // Only transition to processing state if registration is now open
                        // (countdown has reached zero or event opening time has passed)
                        if (unifiedQueueSection.getState() == DefaultUnifiedQueueSection.QueueState.PRE_COUNTDOWN) {
                            if (callback.isRegistrationOpen()) {
                                unifiedQueueSection.transitionToProcessing();
                            }
                            // If not open yet, stay in countdown state - don't update progress
                            return;
                        }
                        unifiedQueueSection.updateProgress(progress.processedRequests(), progress.totalRequests());
                    });
                }
            }, eventQueueProgressNotification.progressProperty());
        }

        // Register handler for final result (push notification from server)
        EventQueueFinalResultNotification.setEnqueuedBookingFinalResultHandler(queueToken, finalResult -> {
            UiScheduler.runInUiThread(() -> {
                // Stop timers and status rotation
                if (unifiedQueueSection != null) {
                    unifiedQueueSection.stopCountdown();
                    unifiedQueueSection.stopStatusMessageRotation();
                }

                handleEnqueuedFinalResult(finalResult, isNewUser, newUserName, newUserEmail, wantsAccountCreation);
            });
        });

        // Register visibility listener to fetch results if tab was inactive
        registerQueueVisibilityListener();
    }

    /**
     * Handles the final result after queue processing completes.
     * Routes to appropriate page based on final status (APPROVED or SOLD_OUT).
     */
    private void handleEnqueuedFinalResult(SubmitDocumentChangesResult finalResult, boolean isNewUser, String newUserName, String newUserEmail, boolean wantsAccountCreation) {
        removeQueueVisibilityListener();

        // Queue processing completed - registration is now confirmed open
        callback.setRegistrationConfirmedOpen(true);
        currentQueueToken = null; // Clear the token as queue processing is complete

        switch (finalResult.status()) {
            case APPROVED -> {
                // Send account creation email if user opted to create an account
                if (wantsAccountCreation && newUserEmail != null) {
                    InitiateAccountCreationCredentials credentials = new InitiateAccountCreationCredentials(
                        newUserEmail,
                        WindowLocation.getOrigin(),
                        null,
                        I18n.getLanguage(),
                        false,
                        null
                    );
                    AuthenticationService.authenticate(credentials);
                }

                if (isNewUser) {
                    callback.populatePendingBookingsForNewUser(newUserName, newUserEmail);
                    callback.navigateToPendingBookings();
                } else {
                    // Load bookings then navigate
                    callback.loadAllBookingsForEventAsync()
                        .onSuccess(ignored -> UiScheduler.runInUiThread(callback::navigateToPendingBookings));
                }
            }
            case REJECTED -> {
                DocumentChangesRejectedReason reason = finalResult.rejectedReason();
                if (reason == null) {
                    showQueueErrorDialog("Booking rejected without reason");
                    callback.navigateToSummary();
                } else {
                    switch (reason) {
                        case SOLD_OUT -> {
                            callback.handleAccommodationSoldOut(new SoldOutErrorParser.SoldOutInfo(
                                finalResult.soldOutSitePrimaryKey(),
                                finalResult.soldOutItemPrimaryKey(),
                                null
                            ));
                        }
                        case EVENT_ON_HOLD -> {
                            callback.showOfflinePage();
                        }
                        case TECHNICAL_ERROR -> {
                            showQueueErrorDialog(finalResult.errorMessage());
                            callback.navigateToSummary();
                        }
                        case ALREADY_BOOKED -> {
                            callback.showAlreadyBookedErrorDialog();
                            callback.navigateToSummary();
                        }
                    }
                }
            }
            default -> {
                // Unexpected status - show error and report it
                showQueueErrorDialog("Unexpected queue result status: " + finalResult.status());
                callback.navigateToSummary();
            }
        }
    }

    /**
     * Shows an error dialog when queue processing fails.
     * Reports the error to the database and displays a user-friendly message.
     *
     * @param errorMessage The error message from the server
     */
    private void showQueueErrorDialog(String errorMessage) {
        String message = errorMessage != null ? errorMessage : "Unknown error";

        // Get event context for error reporting
        String eventName = null;
        Event event = callback.getEvent();
        if (event != null) {
            eventName = event.getName();
        }

        // Build error report message
        StringBuilder reportMessage = new StringBuilder();
        reportMessage.append("[BookingFormQueueHandler] Queue processing failed: ").append(message);
        if (eventName != null) {
            reportMessage.append(" | Event: ").append(eventName);
        }

        // Report error to database
        ErrorReporter.reportError(reportMessage.toString());

        // Show error dialog to user
        UiScheduler.runInUiThread(() -> {
            DialogContent errorDialog = DialogContent.createErrorDialogWithTechnicalDetails(
                I18n.getI18nText(BookingPageI18nKeys.ServerErrorTitle),
                I18n.getI18nText(BookingPageI18nKeys.ServerErrorHeader),
                I18n.getI18nText(BookingPageI18nKeys.QueueErrorMessage),
                message,  // Technical details - the actual server error
                null,     // No error code
                null      // No timestamp
            );

            DialogCallback dialogCallback = DialogUtil.showModalNodeInGoldLayout(
                errorDialog.build(),
                FXMainFrameDialogArea.getDialogArea()
            );
            errorDialog.setDialogCallback(dialogCallback);
            errorDialog.getPrimaryButton().setOnAction(e -> dialogCallback.closeDialog());
        });
    }

    /**
     * Creates or returns the unified queue page (lazy initialization).
     * This page handles both the pre-countdown waiting state and the queue processing state.
     */
    private CompositeBookingFormPage getOrCreateUnifiedQueuePage() {
        if (unifiedQueuePage == null) {
            unifiedQueueSection = new DefaultUnifiedQueueSection();
            unifiedQueueSection.setColorScheme(callback.getColorScheme());
            unifiedQueueSection.setWorkingBookingProperties(callback.getWorkingBookingProperties());

            unifiedQueuePage = new CompositeBookingFormPage(
                "RegistrationQueue", // i18n key
                unifiedQueueSection
            );
            unifiedQueuePage.setStep(false)
                .setHeaderVisible(true) // Show step navigation header
                .setShowingOwnSubmitButton(true)
                .setCanGoBack(false); // Can't go back during queue - use "Leave Queue" button

            // Create "Leave Queue and Edit Booking" button
            BookingFormButton leaveQueueButton = new BookingFormButton(
                "LeaveQueueAndEditBooking", // i18n key
                e -> handleLeaveQueue(),
                "registration-queue-action-button"
            );

            unifiedQueuePage.setButtons(leaveQueueButton);

            // Set up callback for when user leaves queue
            unifiedQueueSection.setOnLeaveQueueAndEdit(this::handleLeaveQueue);
        }
        return unifiedQueuePage;
    }

    /**
     * Handles when user clicks "Leave Queue and Edit Booking".
     * Stops countdown/processing, cancels the enqueued booking on the server, and returns to summary page.
     */
    private void handleLeaveQueue() {
        if (unifiedQueueSection != null) {
            unifiedQueueSection.stopCountdown();
            unifiedQueueSection.stopStatusMessageRotation();
        }
        // Cancel the enqueued booking on the server
        if (currentQueueToken != null) {
            DocumentService.leaveEventQueue(currentQueueToken);
            currentQueueToken = null; // Clear the token
        }
        removeQueueVisibilityListener();
        callback.navigateToSummary();
    }

    /**
     * Registers a visibility listener to fetch queue results when tab becomes visible.
     * Called when booking is enqueued.
     */
    private void registerQueueVisibilityListener() {
        removeQueueVisibilityListener(); // Remove any existing listener first

        queueVisibilityListener = visibilityState -> {
            if (visibilityState == VisibilityState.VISIBLE && currentQueueToken != null) {
                // Small delay to let any pending push notifications arrive first
                UiScheduler.scheduleDelay(150, this::fetchQueueResultIfStillWaiting);
            }
        };
        Visibility.addVisibilityListener(queueVisibilityListener);
    }

    /**
     * Removes the visibility listener. Called when queue processing completes or user leaves queue.
     */
    private void removeQueueVisibilityListener() {
        if (queueVisibilityListener != null) {
            Visibility.removeVisibilityListener(queueVisibilityListener);
            queueVisibilityListener = null;
        }
    }

    /**
     * Fetches queue result from server if still waiting for a result.
     * Called when tab becomes visible to recover from missed push notifications.
     */
    private void fetchQueueResultIfStillWaiting() {
        Object token = currentQueueToken; // Capture current value
        if (token == null) {
            return; // Already processed or left queue
        }

        Console.log("Tab became visible - fetching queue result as fallback");
        DocumentService.fetchEventQueueResult(token)
            .onSuccess(result -> {
                if (result != null && token.equals(currentQueueToken)) {
                    // Remove push handler to prevent duplicate processing
                    EventQueueFinalResultNotification.removeEnqueuedBookingFinalResultHandler(token);
                    UiScheduler.runInUiThread(() ->
                        handleEnqueuedFinalResult(result, storedIsNewUser, storedNewUserName, storedNewUserEmail, storedWantsAccountCreation)
                    );
                }
                // If the result is null, still processing - do nothing, wait for push
            })
            .onFailure(error -> {
                Console.log("Fallback queue fetch failed: " + error.getMessage());
                // Note: if there is a technical issue (ex: bus closed), we don't show an error dialog here because this
                // is just a double-verification (when the tab becomes visible) in addition to the push notification
                // (which remains the main process). There is no point in making the user worry about a technical issue
                // happening during this double-verification.
            });
    }

    /**
     * Navigates to the unified queue page.
     * Call this when booking is enqueued.
     */
    public void navigateToUnifiedQueuePage() {
        CompositeBookingFormPage page = getOrCreateUnifiedQueuePage();

        // Refresh working booking properties in case they changed
        unifiedQueueSection.setWorkingBookingProperties(callback.getWorkingBookingProperties());

        callback.navigateToSpecialPage(page);
    }

    /**
     * Gets the current queue token (for external use if needed).
     */
    public Object getCurrentQueueToken() {
        return currentQueueToken;
    }

    /**
     * Checks if currently waiting in queue.
     */
    public boolean isInQueue() {
        return currentQueueToken != null;
    }
}
