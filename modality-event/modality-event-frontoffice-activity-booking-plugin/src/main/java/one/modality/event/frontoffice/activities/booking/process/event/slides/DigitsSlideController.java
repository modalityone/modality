package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.scene.layout.Region;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.recurringevents.BookableDatesUi;
import one.modality.event.client.recurringevents.FXPersonToBook;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

final class DigitsSlideController {

    private final BookEventActivity bookEventActivity;
    private final TransitionPane transitionPane = new TransitionPane();
    private final Step1BookDatesSlide step1BookDatesSlide;
    private final Step2CheckoutSlide step2CheckoutSlide;
    private final Step3PaymentSlide step3PaymentSlide;
    private final Step4PendingPaymentSlide step4PendingPaymentSlide;
    private final Step5FailedPaymentSlide step5FailedPaymentSlide;
    private final Step6CancellationSlide step6CancellationSlide;
    private final Step7ErrorSlide step7ErrorSlide;
    private StepSlide displayedSlide;

    public DigitsSlideController(BookEventActivity bookEventActivity) {
        this.bookEventActivity   = bookEventActivity;
        step1BookDatesSlide      = new Step1BookDatesSlide(bookEventActivity);
        step2CheckoutSlide       = new Step2CheckoutSlide(bookEventActivity);
        step3PaymentSlide        = new Step3PaymentSlide(bookEventActivity);
        step4PendingPaymentSlide = new Step4PendingPaymentSlide(bookEventActivity);
        step5FailedPaymentSlide  = new Step5FailedPaymentSlide(bookEventActivity);
        step6CancellationSlide   = new Step6CancellationSlide(bookEventActivity);
        step7ErrorSlide          = new Step7ErrorSlide(bookEventActivity);
        transitionPane.setScrollToTop(true);
        // The following code is to solve a performance issue that happens on mobiles during the translation transition
        transitionPane.setUnmanagedDuringTransition(); // For more explanation, read the comment inside this method.
    }

    Region getContainer() {
        return transitionPane;
    }

    void onWorkingBookingLoaded() {
        step1BookDatesSlide.onWorkingBookingLoaded();

        // TODO: avoid rebuilding the whole UI for these remaining slides
        step3PaymentSlide.reset();
        step7ErrorSlide.reset();

        if (displayedSlide != step2CheckoutSlide) {
            displayFirstSlide();
            // Sub-routing node binding (displaying the possible sub-routing account node in the appropriate place in step3)
            step2CheckoutSlide.accountMountNodeProperty().bind(bookEventActivity.mountNodeProperty());
        } else {
            displayCheckoutSlide();
        }
    }

    void displayFirstSlide() {
        displaySlide(step1BookDatesSlide);
    }

    BookableDatesUi getBookableDateUi() {
        return step1BookDatesSlide.getBookableDatesUi();
    }

    private void displaySlide(StepSlide slide) {
        boolean animate = slide != step1BookDatesSlide || displayedSlide == step5FailedPaymentSlide || displayedSlide == step6CancellationSlide;
        displayedSlide = slide;
        UiScheduler.runInUiThread((() -> {
            if (animate)
                transitionPane.transitToContent(slide.get());
            else
                transitionPane.replaceContentNoAnimation(slide.get());
        }));
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

    void displayErrorMessage(String message) {
        step7ErrorSlide.setErrorMessage(message);
        displaySlide(step7ErrorSlide);
    }

}
