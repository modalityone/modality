package one.modality.booking.frontoffice.bookingpage.standard;

import dev.webfx.extras.panes.GrowingPane;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.Entities;
import javafx.scene.Node;
import one.modality.base.shared.entities.Event;
import one.modality.booking.client.workingbooking.WorkingBookingProperties;
import one.modality.booking.frontoffice.bookingform.GatewayPaymentForm;
import one.modality.booking.frontoffice.bookingpage.BookingFormSection;
import one.modality.booking.frontoffice.bookingpage.BookingPageI18nKeys;
import one.modality.booking.frontoffice.bookingpage.CompositeBookingFormPage;
import one.modality.booking.frontoffice.bookingpage.sections.payment.HasPaymentSection;
import one.modality.booking.frontoffice.bookingpage.sections.payment.PaymentCanceledSection;
import one.modality.booking.frontoffice.bookingpage.sections.payment.PaymentRefusedSection;
import one.modality.booking.frontoffice.bookingpage.theme.BookingFormColorScheme;
import one.modality.ecommerce.payment.PaymentAllocation;
import one.modality.ecommerce.payment.PaymentFailureReason;
import one.modality.ecommerce.payment.PaymentFormType;
import one.modality.ecommerce.payment.PaymentStatus;
import one.modality.ecommerce.payment.client.ClientPaymentUtil;
import one.modality.event.frontoffice.activities.book.event.slides.ProvidedGatewayPaymentForm;

/**
 * Handles payment processing for booking forms.
 * Extracted from StandardBookingForm to improve maintainability.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Initiating payment (redirected or embedded)</li>
 *   <li>Displaying embedded payment forms</li>
 *   <li>Handling payment success, failure, and cancellation</li>
 *   <li>Retry logic for failed payments</li>
 * </ul>
 *
 * @author Bruno Salmon
 */
public class BookingFormPaymentHandler {

    /**
     * Callback interface for payment handler to communicate with the form.
     */
    public interface PaymentFormCallback {
        /** Navigate to a special page (e.g., payment form, error page) */
        void navigateToSpecialPage(CompositeBookingFormPage page);

        /** Navigate to the confirmation page */
        void navigateToConfirmation();

        /** Update the confirmation section with payment result */
        void updateConfirmationSection(StandardBookingFormCallbacks.PaymentResult result);

        /** Cancel the booking and exit the form */
        void cancelBookingAndExit();

        /** Get the current event */
        Event getEvent();

        /** Get the color scheme for styling sections */
        BookingFormColorScheme getColorScheme();

        /** Get the callbacks for form-specific hooks */
        StandardBookingFormCallbacks getCallbacks();
    }

    private final PaymentFormCallback callback;

    // Payment retry parameters (stored when payment is initiated, used for retry after failure)
    private int lastPaymentAmount;
    private PaymentAllocation[] lastPaymentAllocations;

    /**
     * Creates a new payment handler.
     *
     * @param callback The callback interface for communicating with the form
     */
    public BookingFormPaymentHandler(PaymentFormCallback callback) {
        this.callback = callback;
    }

    /**
     * Handles payment submission from the payment section.
     *
     * @param sectionResult The payment result from the section containing amount and allocations
     * @return Future that completes when payment is initiated
     */
    public Future<Void> handlePaymentSubmit(HasPaymentSection.PaymentResult sectionResult) {
        // Converting into paymentAllocations (required for InitiatePaymentArgument)
        PaymentAllocation[] paymentAllocations = sectionResult.getAllocations().entrySet().stream()
            .map(entry -> new PaymentAllocation(Entities.getPrimaryKey(entry.getKey()), entry.getValue()))
            .toArray(PaymentAllocation[]::new);

        return handlePaymentSubmit(sectionResult.getAmount(), paymentAllocations);
    }

    /**
     * Handles payment submission with explicit amount and allocations.
     *
     * @param amount The payment amount
     * @param paymentAllocations The payment allocations per document
     * @return Future that completes when payment is initiated
     */
    public Future<Void> handlePaymentSubmit(int amount, PaymentAllocation[] paymentAllocations) {
        // Store parameters for potential retry after payment failure
        this.lastPaymentAmount = amount;
        this.lastPaymentAllocations = paymentAllocations;

        Event event = callback.getEvent();
        PaymentFormType preferredFormType =
            // Using embedded payment form for STTP (type 48) and US Festival (type 38)
            Entities.samePrimaryKey(event.getType(), 48) // STTP
            || Entities.samePrimaryKey(event.getType(), 38) // US Festival
                ? PaymentFormType.EMBEDDED
                : PaymentFormType.REDIRECTED;

        return ClientPaymentUtil.initiateRedirectedPaymentAndRedirectToGatewayPaymentPage(amount, paymentAllocations, preferredFormType)
            .onSuccess(webPaymentForm -> {
                // If it's a redirected payment form, we just navigate to it
                if (webPaymentForm.isRedirectedPaymentForm()) {
                    webPaymentForm.navigateToRedirectedPaymentForm();
                } else {
                    // Creating and displaying the gateway payment form
                    GatewayPaymentForm gatewayPaymentForm = new ProvidedGatewayPaymentForm(webPaymentForm, event, Console::log, Console::log, result -> {
                        PaymentStatus paymentStatus = result.paymentStatus();
                        if (paymentStatus.isSuccessful()) {
                            handleEmbeddedPaymentSuccess(amount);
                        } else if (paymentStatus == PaymentStatus.CANCELED) {
                            handleEmbeddedPaymentCanceled(amount);
                        } else if (paymentStatus.isFailed()) {
                            handleEmbeddedPaymentFailed(amount, result.failureReason());
                        }
                    });
                    // Display the embedded payment form
                    displayEmbeddedPaymentForm(gatewayPaymentForm, amount);
                }
            })
            .mapEmpty();
    }

    /**
     * Displays the embedded gateway payment form using CompositeBookingFormPage.
     * Follows the same pattern as PaymentPage.displayGatewayPaymentForm().
     *
     * @param gatewayPaymentForm The payment form to display (contains Pay/Cancel buttons)
     * @param amount The payment amount for success handling
     */
    private void displayEmbeddedPaymentForm(GatewayPaymentForm gatewayPaymentForm, int amount) {
        // Wrap in GrowingPane like PaymentPage does (maintains size when unloaded)
        GrowingPane growingPane = new GrowingPane(gatewayPaymentForm.getView());

        // Create wrapper section for the payment form
        BookingFormSection paymentFormSection = new BookingFormSection() {
            @Override
            public Object getTitleI18nKey() { return BookingPageI18nKeys.Payment; }

            @Override
            public Node getView() { return growingPane; }

            @Override
            public void setWorkingBookingProperties(WorkingBookingProperties props) { }
        };

        // Create page using CompositeBookingFormPage API
        CompositeBookingFormPage embeddedPaymentPage = new CompositeBookingFormPage(
            BookingPageI18nKeys.Payment,
            paymentFormSection
        );

        // Configure page:
        // - setStep(false): Stay on the same header navigation step
        // - setShowingOwnSubmitButton(true): ProvidedGatewayPaymentForm has its own Pay/Cancel buttons
        // - setCanGoBack(false): Prevent back navigation during payment (like PaymentPage)
        embeddedPaymentPage
            .setStep(false)
            .setShowingOwnSubmitButton(true)
            .setCanGoBack(false);

        // Handle cancel: show payment canceled page with retry option
        gatewayPaymentForm.setCancelPaymentResultHandler(ar -> {
            growingPane.setContent(null); // Unload payment form
            handleEmbeddedPaymentCanceled(amount); // Show canceled page with retry option
        });

        // Display the page
        callback.navigateToSpecialPage(embeddedPaymentPage);
    }

    /**
     * Handles successful embedded payment.
     */
    private void handleEmbeddedPaymentSuccess(int amount) {
        // Convert to callbacks result type
        StandardBookingFormCallbacks.PaymentResult result = new StandardBookingFormCallbacks.PaymentResult(amount);

        // Update confirmation section
        callback.updateConfirmationSection(result);

        // Notify callbacks (optional hook)
        StandardBookingFormCallbacks callbacks = callback.getCallbacks();
        if (callbacks != null) {
            callbacks.onAfterPayment();
        }

        // Navigate to confirmation
        callback.navigateToConfirmation();
    }

    /**
     * Handles embedded payment failure (card declined, error).
     * Displays the PaymentRefusedSection with "Decline Reason" card and "Try Again" option.
     */
    private void handleEmbeddedPaymentFailed(int amount, PaymentFailureReason failureReason) {
        // Create PaymentRefusedSection matching JSX mockup
        PaymentRefusedSection section = new PaymentRefusedSection();
        section.setColorScheme(callback.getColorScheme());
        section.setAmount(amount);
        section.setFailureReason(failureReason);
        section.setOnRetryPayment(() -> handlePaymentSubmit(lastPaymentAmount, lastPaymentAllocations));
        section.setOnCancelBooking(callback::cancelBookingAndExit);

        // Set event info
        Event event = callback.getEvent();
        if (event != null) {
            section.setEventName(event.getName());
            section.setEventDates(event.getStartDate(), event.getEndDate());
        }

        // Display section using CompositeBookingFormPage
        CompositeBookingFormPage failedPaymentPage = new CompositeBookingFormPage(
            BookingPageI18nKeys.Payment,
            section
        );
        failedPaymentPage
            .setStep(false)
            .setShowingOwnSubmitButton(true)
            .setCanGoBack(false);

        callback.navigateToSpecialPage(failedPaymentPage);
    }

    /**
     * Handles embedded payment cancellation (user clicked cancel).
     * Displays the PaymentCanceledSection with "Try Again" option.
     */
    private void handleEmbeddedPaymentCanceled(int amount) {
        // Create PaymentCanceledSection matching JSX mockup
        PaymentCanceledSection section = new PaymentCanceledSection();
        section.setColorScheme(callback.getColorScheme());
        section.setAmount(amount);
        section.setOnRetryPayment(() -> handlePaymentSubmit(lastPaymentAmount, lastPaymentAllocations));

        // Display section using CompositeBookingFormPage
        CompositeBookingFormPage canceledPaymentPage = new CompositeBookingFormPage(
            BookingPageI18nKeys.Payment,
            section
        );
        canceledPaymentPage
            .setStep(false)
            .setShowingOwnSubmitButton(true)
            .setCanGoBack(false);

        callback.navigateToSpecialPage(canceledPaymentPage);
    }

    /**
     * Gets the last payment amount (for retry scenarios).
     */
    public int getLastPaymentAmount() {
        return lastPaymentAmount;
    }

    /**
     * Gets the last payment allocations (for retry scenarios).
     */
    public PaymentAllocation[] getLastPaymentAllocations() {
        return lastPaymentAllocations;
    }
}
