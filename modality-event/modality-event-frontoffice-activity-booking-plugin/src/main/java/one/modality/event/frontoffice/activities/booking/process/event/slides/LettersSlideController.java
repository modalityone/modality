package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.TransitionPane;
import dev.webfx.extras.panes.transitions.CircleTransition;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import one.modality.base.client.mainframe.fx.FXMainFrameTransiting;
import one.modality.base.frontoffice.mainframe.fx.FXShowFooter;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.booking.BookableDatesUi;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

/**
 * @author Bruno Salmon
 */
public final class LettersSlideController {

    private final BookEventActivity bookEventActivity;
    private final TransitionPane transitionPane = new TransitionPane();
    private final StepALoadingSlide stepALoadingSlide;
    private final StepBBookEventSlide stepBBookEventSlide;
    private final StepCThankYouSlide stepCThankYouSlide;
    private StepSlide displayedSlide;

    public LettersSlideController(BookEventActivity bookEventActivity) {
        this.bookEventActivity = bookEventActivity;
        stepALoadingSlide = new StepALoadingSlide(bookEventActivity);
        stepBBookEventSlide = new StepBBookEventSlide(bookEventActivity);
        stepCThankYouSlide = new StepCThankYouSlide(bookEventActivity);
        transitionPane.setTransition(new CircleTransition());
        transitionPane.setScrollToTop(true);
        displayLoadingSlide();
    }

    public Region getContainer() {
        return transitionPane;
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
                // Ensuring the slide is always at least as high as the transition pane. This will, for example, stretch
                // the loading slide so it fills the whole screen vertically.
                slide.mainVbox.minHeightProperty().bind(transitionPane.heightProperty());
                transitionPane.transitToContent(slide.get());
                // Hiding the footer for the loading slide
                FXShowFooter.setShowFooter(slide != stepALoadingSlide);
            }));
        }
    }

    private void displaySlideOnTransitionComplete(StepSlide slide, ReadOnlyBooleanProperty transitingProperty) {
        FXProperties.runOrUnregisterOnPropertyChange((thisListener, oldValue, newValue) -> {
            thisListener.unregister();
            displaySlide(slide);
        }, transitingProperty);
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

    public void displayErrorMessage(Object messageI18nKey) {
        stepBBookEventSlide.displayErrorMessage(messageI18nKey);
        bookEventActivity.onReachingEndSlide();
    }

    public void displayPaymentSlide(WebPaymentForm webPaymentForm) {
        stepBBookEventSlide.displayPaymentSlide(webPaymentForm);
    }

    public void displayPendingPaymentSlide() {
        stepBBookEventSlide.displayPendingPaymentSlide();
    }

    public void displayFailedPaymentSlide() {
        stepBBookEventSlide.displayFailedPaymentSlide();
    }

    public void displayCancellationSlide(CancelPaymentResult cancelPaymentResult) {
        stepBBookEventSlide.displayCancellationSlide(cancelPaymentResult);
        //bookEventActivity.onReachingEndSlide(); // Commented as this resets FXEvent() and prevents button to work in cancellation slide
    }

    public BookableDatesUi getBookableDatesUi() {
        return stepBBookEventSlide.getBookableDatesUi();
    }

    public <T extends Labeled> T bindI18nEventExpression(T text, String eventExpression, Object... args) {
        return stepBBookEventSlide.bindI18nEventExpression(text, eventExpression, args);
    }

    public HtmlText bindI18nEventExpression(HtmlText text, String eventExpression, Object... args) {
        return stepBBookEventSlide.bindI18nEventExpression(text, eventExpression, args);
    }

}
