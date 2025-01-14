package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Numbers;
import dev.webfx.stack.orm.entity.Entities;
import javafx.scene.layout.Region;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.client.recurringevents.BookableDatesUi;
import one.modality.event.client.recurringevents.FXPersonToBook;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.slides.recurring_event.Step1BookDatesRecurringEventSlide;
import one.modality.event.frontoffice.activities.booking.process.event.slides.sttp.Step1BookSttpSlide;

final class DigitsSlideController {

    private final BookEventActivity bookEventActivity;
    private final TransitionPane transitionPane = new TransitionPane();
    private AbstractStep1Slide step1Slide;
    private Step2CheckoutSlide step2CheckoutSlide;
    private final Step3PaymentSlide step3PaymentSlide;
    private final Step4PendingPaymentSlide step4PendingPaymentSlide;
    private final Step5FailedPaymentSlide step5FailedPaymentSlide;
    private final Step6CancellationSlide step6CancellationSlide;
    private final Step7ErrorSlide step7ErrorSlide;
    private StepSlide displayedSlide;

    public DigitsSlideController(BookEventActivity bookEventActivity) {
        this.bookEventActivity   = bookEventActivity;

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
        // TODO: avoid rebuilding the whole UI for these remaining slides
        step3PaymentSlide.reset();
        step7ErrorSlide.reset();
        step2CheckoutSlide = new Step2CheckoutSlide(bookEventActivity);

        Event currentEvent = FXEvent.getEvent();
        int typeId = Numbers.toInteger(Entities.getPrimaryKey(currentEvent.getType()));
        if(typeId == KnownEventType.GP_CLASSES.getTypeId()) {
            //TODO: when we will have different Step1 for different type of event, implement an abstract class in this package, and the different step 1 will inherit this abstract class
            step1Slide = new Step1BookDatesRecurringEventSlide(bookEventActivity);
        } else if(typeId == KnownEventType.STTP.getTypeId()) {
            step1Slide =  new Step1BookSttpSlide(bookEventActivity);;
            step2CheckoutSlide.setBookAsGuestAllowed(false);
        }
        else {
            step7ErrorSlide.setErrorMessage("Error: Unmanaged type of event");
            displaySlide(step7ErrorSlide);
        }
        step1Slide.onWorkingBookingLoaded();

        if (displayedSlide != step2CheckoutSlide) {
            displayFirstSlide();
        } else {
            displayCheckoutSlide();
        }

        // Sub-routing node binding (displaying the possible sub-routing account node in the appropriate place in step2)
        step2CheckoutSlide.accountMountNodeProperty().bind(bookEventActivity.mountNodeProperty());
    }

    void displayFirstSlide() {
        displaySlide(step1Slide);
    }

    BookableDatesUi getBookableDateUi() {
        if(step1Slide==null) return null;
        return step1Slide.getBookableDatesUi();
    }

    private void displaySlide(StepSlide slide) {
        boolean animate = slide != step1Slide || displayedSlide == step5FailedPaymentSlide || displayedSlide == step6CancellationSlide;
        displayedSlide = slide;
        UiScheduler.runInUiThread((() -> {
            if (animate)
                transitionPane.transitToContent(slide.get());
            else
                transitionPane.replaceContentNoAnimation(slide.get());
        }));
    }

    void displayCheckoutSlide() {
        if (displayedSlide == step1Slide) {
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
