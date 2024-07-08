package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.carrousel.Carrousel;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Labeled;
import javafx.scene.text.Text;
import one.modality.event.client.event.fx.FXEvent;

public class SlideController {

    private Step1LoadingSlide step1LoadingSlide;
    private Step2EventDetailsSlide step2EventDetailsSlide;
    private Step3CheckoutSlide step3CheckoutSlide;
    private Step4PaymentSlide step4PaymentSlide;
    private Step6ThankYouSlide step6ThankYouSlide;
    private Step5ErrorSlide step5ErrorSlide;
    private final Carrousel carrousel;
    private final BooleanProperty eventDataLoaded = new SimpleBooleanProperty();

    public SlideController(Carrousel car) {
        carrousel = car;
    }

    public void initialise() {
        eventDataLoaded.setValue(false);
        step2EventDetailsSlide.reset();
        step3CheckoutSlide.reset();
        step4PaymentSlide.reset();
        step5ErrorSlide.reset();
        step6ThankYouSlide.reset();

        // Rebuilding the UI and showing the loading slide while the event is loading
        FXProperties.runNowAndOnPropertiesChange(() -> UiScheduler.runInUiThread(() -> {
            if (eventDataLoaded.get()) {
                step2EventDetailsSlide.buildUi();
                step3CheckoutSlide.buildUi();
                step4PaymentSlide.buildUi();
                step5ErrorSlide.buildUi();
                step6ThankYouSlide.buildUi();
                carrousel.displaySlide(step2EventDetailsSlide.get());
            } else { // Here the data are not loaded
                carrousel.displaySlide(step1LoadingSlide.get());
            }
        }), eventDataLoaded);
    }

    void setStep1LoadingSlide(Step1LoadingSlide step1LoadingSlide) {
        this.step1LoadingSlide = step1LoadingSlide;
    }

    void setStep2EventDetailsSlide(Step2EventDetailsSlide step2EventDetailsSlide) {
        this.step2EventDetailsSlide = step2EventDetailsSlide;
    }

    Step3CheckoutSlide getStep3CheckoutSlide() {
        return step3CheckoutSlide;
    }

    void setStep3CheckoutSlide(Step3CheckoutSlide step3CheckoutSlide) {
        this.step3CheckoutSlide = step3CheckoutSlide;
    }

    void setStep6ThankYouSlide(Step6ThankYouSlide step4ThankYouSlide) {
        this.step6ThankYouSlide = step4ThankYouSlide;
    }

    public void goToThankYouSlide() {
        carrousel.displaySlide(step6ThankYouSlide.get());
    }

    void setStep5ErrorSlide(Step5ErrorSlide step5ErrorSlide) {
        this.step5ErrorSlide = step5ErrorSlide;
    }

    Step4PaymentSlide getStep4PaymentSlide() {
        return this.step4PaymentSlide;
    }

    void setStep4PaymentSlide(Step4PaymentSlide step4PaymentSlide) {
        this.step4PaymentSlide = step4PaymentSlide;
    }

    public void displayErrorMessage(String message) {
        step5ErrorSlide.setErrorMessage(message);
        Platform.runLater(() ->         carrousel.displaySlide(step5ErrorSlide.get()));
    }

    public BooleanProperty eventDataLoadedProperty() {
        return eventDataLoaded;
    }

    public void setEventDataLoaded(boolean eventDataLoaded) {
        this.eventDataLoaded.set(eventDataLoaded);
    }

    public void displayNextSlide() {
        carrousel.moveForward();
    }

    public RecurringEventSchedule getRecurringEventSchedule() {
        return step2EventDetailsSlide.getRecurringEventSchedule();
    }

    public void bindI18nEventExpression(Property<String> textProperty, String eventExpression) {
        I18n.bindI18nTextProperty(textProperty, new I18nSubKey("expression: " + eventExpression, FXEvent.eventProperty()), eventDataLoadedProperty());
    }

    public <T extends Text> T bindI18nEventExpression(T text, String eventExpression) {
        bindI18nEventExpression(text.textProperty(), eventExpression);
        return text;
    }

    public <L extends Labeled> L bindI18nEventExpression(L text, String eventExpression) {
        bindI18nEventExpression(text.textProperty(), eventExpression);
        return text;
    }

    public HtmlText bindI18nEventExpression(HtmlText text, String eventExpression) {
        bindI18nEventExpression(text.textProperty(), eventExpression);
        return text;
    }

}
