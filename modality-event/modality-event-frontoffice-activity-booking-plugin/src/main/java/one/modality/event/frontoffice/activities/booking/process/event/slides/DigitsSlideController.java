package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.scene.layout.Region;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.frontoffice.activities.booking.fx.FXPersonToBook;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.RecurringEventSchedule;

final class DigitsSlideController {

    private final BookEventActivity bookEventActivity;
    private final TransitionPane transitionPane = new TransitionPane();
    private final Step1BookDatesSlide step1BookDatesSlide;
    private final Step2CheckoutSlide step2CheckoutSlide;
    private final Step3PaymentSlide step3PaymentSlide;
    private final Step4CancellationSlide step4CancellationSlide;
    private final Step5ErrorSlide step5ErrorSlide;
    private StepSlide displayedSlide;

    public DigitsSlideController(BookEventActivity bookEventActivity) {
        this.bookEventActivity = bookEventActivity;
        step1BookDatesSlide    = new Step1BookDatesSlide(bookEventActivity);
        step2CheckoutSlide     = new Step2CheckoutSlide(bookEventActivity);
        step3PaymentSlide      = new Step3PaymentSlide(bookEventActivity);
        step4CancellationSlide = new Step4CancellationSlide(bookEventActivity);
        step5ErrorSlide        = new Step5ErrorSlide(bookEventActivity);
    }

    Region getContainer() {
        return transitionPane;
    }

    void onWorkingBookingLoaded() {
        step1BookDatesSlide.onWorkingBookingLoaded();

        // TODO: avoid rebuilding the whole UI for these remaining slides
        step3PaymentSlide.reset();
        step5ErrorSlide.reset();

        if (displayedSlide != step2CheckoutSlide) {
            transitionPane.replaceContentNoAnimation(step1BookDatesSlide.get());
            displayedSlide = step1BookDatesSlide;
            // Sub-routing node binding (displaying the possible sub-routing account node in the appropriate place in step3)
            step2CheckoutSlide.accountMountNodeProperty().bind(bookEventActivity.mountNodeProperty());
        } else {
            displayCheckoutSlide();
        }
    }

    RecurringEventSchedule getRecurringEventSchedule() {
        return step1BookDatesSlide.getRecurringEventSchedule();
    }

    private void displaySlide(StepSlide slide) {
        displayedSlide = slide;
        UiScheduler.runInUiThread((() -> transitionPane.transitToContent(slide.get())));
    }

    void displayCheckoutSlide() {
        if (displayedSlide == step1BookDatesSlide) {
            step2CheckoutSlide.setStep1PersonToBookWasShown(FXPersonToBook.getPersonToBook() != null);
        }
        step2CheckoutSlide.reset(); // ensures the summary is updated
        displaySlide(step2CheckoutSlide);
    }

    void displayPaymentSlide(WebPaymentForm webPaymentForm) {
        step3PaymentSlide.setWebPaymentForm(webPaymentForm);
        displaySlide(step3PaymentSlide);
    }

    void displayCancellationSlide() {
        displaySlide(step4CancellationSlide);
    }

    void displayErrorMessage(String message) {
        step5ErrorSlide.setErrorMessage(message);
        displaySlide(step5ErrorSlide);
    }

}
