package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.RecurringEventSchedule;

public final class LettersSlideController {

    private final BookEventActivity bookEventActivity;
    private final TransitionPane transitionPane = new TransitionPane();
    private final ScrollPane scrollPane = ControlUtil.createVerticalScrollPane(transitionPane);
    private final StepABlankSlide stepABlankSlide;
    private final StepBLoadingSlide stepBLoadingSlide;
    private final StepCBookEventSlide stepCBookEventSlide;
    private final StepDThankYouSlide stepDThankYouSlide;

    public LettersSlideController(BookEventActivity bookEventActivity) {
        this.bookEventActivity = bookEventActivity;
        stepABlankSlide     = new StepABlankSlide(bookEventActivity);
        stepBLoadingSlide   = new StepBLoadingSlide(bookEventActivity);
        stepCBookEventSlide = new StepCBookEventSlide(bookEventActivity);
        stepDThankYouSlide  = new StepDThankYouSlide(bookEventActivity);
        transitionPane.setCircleAnimation(true);
        displaySlide(stepABlankSlide);
    }

    public Region getContainer() {
        return scrollPane;
    }

    public void onEventChanged(Event event) {
        transitionPane.replaceContentNoAnimation(stepABlankSlide.get());
        displaySlide(stepBLoadingSlide);
        stepCBookEventSlide.onEventChanged(event);
    }

    public ReadOnlyObjectProperty<Font> mediumFontProperty() {
        return stepCBookEventSlide.mediumFontProperty();
    }

    /**
     * In this method, we update the UI according to the event
     */
    public void onWorkingBookingLoaded() {
        stepCBookEventSlide.onWorkingBookingLoaded();
    }


    private void displaySlide(StepSlide slide) {
        UiScheduler.runInUiThread((() -> {
            slide.mainVbox.minHeightProperty().bind(scrollPane.heightProperty());
            transitionPane.transitToContent(slide.get());
        }));
    }

    public void displayBookSlide() {
        if (!transitionPane.isTransiting())
            displaySlide(stepCBookEventSlide);
        else {
            transitionPane.transitingProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    transitionPane.transitingProperty().removeListener(this);
                    displayBookSlide();
                }
            });
        }
    }

    public void displayThankYouSlide() {
        displaySlide(stepDThankYouSlide);
        bookEventActivity.onReachingEndSlide();
    }

    public void displayCheckoutSlide() {
        stepCBookEventSlide.displayCheckoutSlide();
    }

    public void displayErrorMessage(String message) {
        stepCBookEventSlide.displayErrorMessage(message);
        bookEventActivity.onReachingEndSlide();
    }

    public void displayPaymentSlide(WebPaymentForm webPaymentForm) {
        stepCBookEventSlide.displayPaymentSlide(webPaymentForm);
    }

    public void displayCancellationSlide() {
        stepCBookEventSlide.displayCancellationSlide();
        bookEventActivity.onReachingEndSlide();
    }

    public RecurringEventSchedule getRecurringEventSchedule() {
        return stepCBookEventSlide.getRecurringEventSchedule();
    }

    public HtmlText bindI18nEventExpression(HtmlText text, String eventExpression) {
        return stepCBookEventSlide.bindI18nEventExpression(text, eventExpression);
    }

}
