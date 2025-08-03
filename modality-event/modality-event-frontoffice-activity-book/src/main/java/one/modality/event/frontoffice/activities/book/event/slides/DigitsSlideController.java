package one.modality.event.frontoffice.activities.book.event.slides;

import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.platform.service.MultipleServiceProviders;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.collection.Collections;
import javafx.scene.layout.Region;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.frontoffice.bookingform.BookingForm;
import one.modality.ecommerce.frontoffice.bookingform.BookingFormProvider;
import one.modality.ecommerce.frontoffice.bookingform.GatewayPaymentForm;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.event.frontoffice.activities.book.event.BookEventActivity;

import java.util.List;
import java.util.ServiceLoader;

/**
 * @author Bruno Salmon
 */
final class DigitsSlideController {

    private static final List<BookingFormProvider> ALL_BOOKING_FORM_PROVIDERS_SORTED_BY_PRIORITY = MultipleServiceProviders.getProviders(BookingFormProvider.class, () -> ServiceLoader.load(BookingFormProvider.class));
    static {
        ALL_BOOKING_FORM_PROVIDERS_SORTED_BY_PRIORITY.sort((p1, p2) -> p2.getPriority() - p1.getPriority());
    }

    private final BookEventActivity bookEventActivity;
    private final TransitionPane stepSlidetransitionPane = new TransitionPane();
    private final Step1BookingFormAndSubmitSlide step1BookingFormAndSubmitSlide;
    private final Step2PaymentSlide step2PaymentSlide;
    private final Step3PendingPaymentSlide step3PendingPaymentSlide;
    private final Step4FailedPaymentSlide step4FailedPaymentSlide;
    private final Step5CancellationSlide step5CancellationSlide;
    private final Step6ErrorSlide step6ErrorSlide;
    private BookingForm bookingForm;
    private boolean bookingFormCreated;
    private StepSlide displayedSlide;

    public DigitsSlideController(BookEventActivity bookEventActivity) {
        this.bookEventActivity = bookEventActivity;

        step1BookingFormAndSubmitSlide = new Step1BookingFormAndSubmitSlide(bookEventActivity);
        step2PaymentSlide = new Step2PaymentSlide(bookEventActivity);
        step3PendingPaymentSlide = new Step3PendingPaymentSlide(bookEventActivity);
        step4FailedPaymentSlide = new Step4FailedPaymentSlide(bookEventActivity);
        step5CancellationSlide = new Step5CancellationSlide(bookEventActivity);
        step6ErrorSlide = new Step6ErrorSlide(bookEventActivity);
        stepSlidetransitionPane.setScrollToTop(true);
        // The following code is to solve a performance issue that happens on mobiles during the translation transition
        stepSlidetransitionPane.setUnmanagedDuringTransition(); // For more explanation, read the comment inside this method.
    }

    Region getContainer() {
        return stepSlidetransitionPane;
    }

    void onEventChanged(Event event) {
        // Searching for a booking form provider suitable for this event
        BookingFormProvider bookingFormProvider = Collections.findFirst(ALL_BOOKING_FORM_PROVIDERS_SORTED_BY_PRIORITY, provider -> provider.acceptEvent(event));
        if (bookingFormProvider == null) {
            bookingForm = null;
            step6ErrorSlide.setErrorMessage("Error: Unmanaged type of event");
            displaySlide(step6ErrorSlide);
        } else {
            bookingForm = bookingFormProvider.createBookingForm(event, bookEventActivity);
            bookingFormCreated = true;
        }
    }

    boolean autoLoadExistingBooking() {
        return bookingForm.getSettings().autoLoadExistingBooking();
    }

    public BookingForm getBookingForm() {
        return bookingForm;
    }

    void onWorkingBookingLoaded(Runnable onReadyToReveal) {
        // TODO: avoid rebuilding the whole UI for these remaining slides
        step2PaymentSlide.reset();
        step6ErrorSlide.reset();

        if (bookingForm == null)
            onReadyToReveal.run();
        else {
            if (bookingFormCreated) {
                step1BookingFormAndSubmitSlide.setBookingForm(bookingForm);
                // Sub-routing node binding (displaying the possible sub-routing account node in the appropriate place in step2)
                step1BookingFormAndSubmitSlide.accountMountNodeProperty().bind(bookEventActivity.mountNodeProperty());
                displayFirstSlide();
                bookingFormCreated = false;
            }
            bookEventActivity.getWorkingBooking().getEvent().onExpressionLoaded(bookingForm.getEventFieldsToLoad())
                .onSuccess(ignored -> UiScheduler.runInUiThread(() -> {
                    bookingForm.onWorkingBookingLoaded();
                    onReadyToReveal.run();
                }));
        }
    }

    void displayFirstSlide() {
        displaySlide(step1BookingFormAndSubmitSlide);
    }

    private void displaySlide(StepSlide slide) {
        boolean animate = slide != step1BookingFormAndSubmitSlide || displayedSlide == step4FailedPaymentSlide || displayedSlide == step5CancellationSlide;
        displayedSlide = slide;
        UiScheduler.runInUiThread((() -> {
            if (animate)
                stepSlidetransitionPane.transitToContent(slide.get());
            else
                stepSlidetransitionPane.replaceContentNoAnimation(slide.get());
        }));
    }

    void displayPaymentSlide(GatewayPaymentForm gatewayPaymentForm) {
        step2PaymentSlide.setWebPaymentForm(gatewayPaymentForm);
        displaySlide(step2PaymentSlide);
    }

    void displayPendingPaymentSlide() {
        displaySlide(step3PendingPaymentSlide);
    }

    void displayFailedPaymentSlide() {
        displaySlide(step4FailedPaymentSlide);
    }

    void displayCancellationSlide(CancelPaymentResult cancelPaymentResult) {
        step5CancellationSlide.setCancelPaymentResult(cancelPaymentResult);
        displaySlide(step5CancellationSlide);
    }

    void displayErrorMessage(Object errorMessageI18nKey) {
        step6ErrorSlide.setErrorMessage(errorMessageI18nKey);
        displaySlide(step6ErrorSlide);
    }

}
