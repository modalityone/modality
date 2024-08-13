package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.util.control.ControlUtil;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import one.modality.base.client.mainframe.dialogarea.fx.FXMainFrameTransiting;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.RecurringEventSchedule;

public final class LettersSlideController {

    private final BookEventActivity bookEventActivity;
    private final TransitionPane transitionPane = new TransitionPane();
    private final ScrollPane scrollPane = ControlUtil.createVerticalScrollPane(transitionPane);
    private final StepALoadingSlide stepALoadingSlide;
    private final StepBBookEventSlide stepBBookEventSlide;
    private final StepCThankYouSlide stepCThankYouSlide;
    private StepSlide displayedSlide;

    public LettersSlideController(BookEventActivity bookEventActivity) {
        this.bookEventActivity = bookEventActivity;
        stepALoadingSlide = new StepALoadingSlide(bookEventActivity);
        stepBBookEventSlide = new StepBBookEventSlide(bookEventActivity);
        stepCThankYouSlide = new StepCThankYouSlide(bookEventActivity);
        transitionPane.setCircleAnimation(true);
        displayLoadingSlide();
    }

    public Region getContainer() {
        return scrollPane;
    }

    public void onEventChanged(Event event) {
        if (displayedSlide != stepALoadingSlide) {
            displayLoadingSlide();
        }
        stepBBookEventSlide.onEventChanged(event);
    }

    public ReadOnlyObjectProperty<Font> mediumFontProperty() {
        return stepBBookEventSlide.mediumFontProperty();
    }

    /**
     * In this method, we update the UI according to the event
     */
    public void onWorkingBookingLoaded() {
        stepBBookEventSlide.onWorkingBookingLoaded();
    }


    private void displaySlide(StepSlide slide) {
        displayedSlide = slide;
        if (transitionPane.isTransiting()) {
            displaySlideOnTransitionComplete(slide, transitionPane.transitingProperty());
        } else if (FXMainFrameTransiting.isTransiting()) {
            displaySlideOnTransitionComplete(slide, FXMainFrameTransiting.transitingProperty());
        } else {
            UiScheduler.runInUiThread((() -> {
                slide.mainVbox.minHeightProperty().bind(scrollPane.heightProperty());
                transitionPane.transitToContent(slide.get());
            }));
        }
    }

    private void displaySlideOnTransitionComplete(StepSlide slide, ReadOnlyBooleanProperty transitingProperty) {
        transitingProperty.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                Console.log("transition changed (assuming finished)");
                transitingProperty.removeListener(this);
                displaySlide(slide);
            }
        });
    }

    private void displayLoadingSlide() {
        displaySlide(stepALoadingSlide);
    }

    public void displayBookSlide() {
        displaySlide(stepBBookEventSlide);
    }

    public void displayThankYouSlide() {
        displaySlide(stepCThankYouSlide);
        bookEventActivity.onReachingEndSlide();
    }

    public void displayCheckoutSlide() {
        stepBBookEventSlide.displayCheckoutSlide();
    }

    public void displayErrorMessage(String message) {
        stepBBookEventSlide.displayErrorMessage(message);
        bookEventActivity.onReachingEndSlide();
    }

    public void displayPaymentSlide(WebPaymentForm webPaymentForm) {
        stepBBookEventSlide.displayPaymentSlide(webPaymentForm);
    }

    public void displayCancellationSlide() {
        stepBBookEventSlide.displayCancellationSlide();
        bookEventActivity.onReachingEndSlide();
    }

    public RecurringEventSchedule getRecurringEventSchedule() {
        return stepBBookEventSlide.getRecurringEventSchedule();
    }

    public HtmlText bindI18nEventExpression(HtmlText text, String eventExpression) {
        return stepBBookEventSlide.bindI18nEventExpression(text, eventExpression);
    }

}
