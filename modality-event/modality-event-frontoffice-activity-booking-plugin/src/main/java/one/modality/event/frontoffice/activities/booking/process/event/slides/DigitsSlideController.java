package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.platform.service.MultipleServiceProviders;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.collection.Collections;
import javafx.scene.layout.Region;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.client.workingbooking.FXPersonToBook;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.booking.BookableDatesUi;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.BookingForm;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.BookingFormProvider;
import one.modality.event.frontoffice.activities.booking.process.event.bookingform.BookingFormSettings;

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
    private final Step1BookingFormSlide step1BookingFormSlide;
    private final Step2CheckoutSlide step2CheckoutSlide;
    private final Step3PaymentSlide step3PaymentSlide;
    private final Step4PendingPaymentSlide step4PendingPaymentSlide;
    private final Step5FailedPaymentSlide step5FailedPaymentSlide;
    private final Step6CancellationSlide step6CancellationSlide;
    private final Step7ErrorSlide step7ErrorSlide;
    private BookingForm bookingForm;
    private StepSlide displayedSlide;

    public DigitsSlideController(BookEventActivity bookEventActivity) {
        this.bookEventActivity   = bookEventActivity;

        step1BookingFormSlide    = new Step1BookingFormSlide(bookEventActivity);
        step2CheckoutSlide       = new Step2CheckoutSlide(bookEventActivity);
        step3PaymentSlide        = new Step3PaymentSlide(bookEventActivity);
        step4PendingPaymentSlide = new Step4PendingPaymentSlide(bookEventActivity);
        step5FailedPaymentSlide  = new Step5FailedPaymentSlide(bookEventActivity);
        step6CancellationSlide   = new Step6CancellationSlide(bookEventActivity);
        step7ErrorSlide          = new Step7ErrorSlide(bookEventActivity);
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
            step7ErrorSlide.setErrorMessage("Error: Unmanaged type of event");
            displaySlide(step7ErrorSlide);
        } else
            bookingForm = bookingFormProvider.createBookingForm(event, bookEventActivity);
    }

    public BookingForm getBookingForm() {
        return bookingForm;
    }

    void onWorkingBookingLoaded() {
        // TODO: avoid rebuilding the whole UI for these remaining slides
        step3PaymentSlide.reset();
        step7ErrorSlide.reset();

        if (bookingForm != null) {
            bookingForm.onWorkingBookingLoaded();
            step1BookingFormSlide.setBookingForm(bookingForm);
            BookingFormSettings bookingFormSettings = bookingForm.getSettings();
            step2CheckoutSlide.setBookAsGuestAllowed(bookingFormSettings.isBookAsAGuestAllowed());
            step2CheckoutSlide.setPartialEventAllowed(bookingFormSettings.isPartialEventAllowed());

            if (displayedSlide != step2CheckoutSlide) {
                displayFirstSlide();
            } else {
                displayCheckoutSlide();
            }
            // Sub-routing node binding (displaying the possible sub-routing account node in the appropriate place in step2)
            step2CheckoutSlide.accountMountNodeProperty().bind(bookEventActivity.mountNodeProperty());
        }
    }

    void displayFirstSlide() {
        displaySlide(!step1BookingFormSlide.isEmpty() ? step1BookingFormSlide : step2CheckoutSlide);
    }

    BookableDatesUi getBookableDateUi() {
        if (bookingForm == null)
            return null;
        return bookingForm.getBookableDatesUi();
    }

    private void displaySlide(StepSlide slide) {
        boolean animate = slide != step1BookingFormSlide || displayedSlide == step5FailedPaymentSlide || displayedSlide == step6CancellationSlide;
        displayedSlide = slide;
        UiScheduler.runInUiThread((() -> {
            if (animate)
                stepSlidetransitionPane.transitToContent(slide.get());
            else
                stepSlidetransitionPane.replaceContentNoAnimation(slide.get());
        }));
    }

    void displayCheckoutSlide() {
        if (displayedSlide == step1BookingFormSlide) {
            step2CheckoutSlide.setStep1PersonToBookWasShown(FXPersonToBook.getPersonToBook() != null);
        }
        step2CheckoutSlide.reset(); // ensures the summary is updated
        displaySlide(step2CheckoutSlide);
    }

    void displayPaymentSlide(WebPaymentForm webPaymentForm) {
        step3PaymentSlide.setWebPaymentForm(webPaymentForm);
        displaySlide(step3PaymentSlide);
    }

    void displayPendingPaymentSlide() {
        displaySlide(step4PendingPaymentSlide);
    }

    void displayFailedPaymentSlide() {
        displaySlide(step5FailedPaymentSlide);
    }

    void displayCancellationSlide(CancelPaymentResult cancelPaymentResult) {
        step6CancellationSlide.setCancelPaymentResult(cancelPaymentResult);
        displaySlide(step6CancellationSlide);
    }

    void displayErrorMessage(Object errorMessageI18nKey) {
        step7ErrorSlide.setErrorMessage(errorMessageI18nKey);
        displaySlide(step7ErrorSlide);
    }

}
