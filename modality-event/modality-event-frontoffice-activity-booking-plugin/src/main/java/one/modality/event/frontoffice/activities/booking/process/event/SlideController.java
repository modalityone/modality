package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.carrousel.Carrousel;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.Unregisterable;
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
    private Carrousel carrousel;
    private BooleanProperty eventDataLoaded = new SimpleBooleanProperty();
    private BooleanProperty registrationDataLoaded = new SimpleBooleanProperty();

    public SlideController(Carrousel car) {
        carrousel = car;
    }

    public void initialise() {
        Unregisterable[] listeners = { null };
        listeners[0] = FXProperties.runOnPropertiesChange(() -> {
            if (eventDataLoaded.get() && registrationDataLoaded.get()) {
                Platform.runLater(() -> {
                    step2EventDetailsSlide.buildUi();
                    step3CheckoutSlide.buildUi();
                    step4PaymentSlide.buildUi();
                    step5ErrorSlide.buildUi();
                    step6ThankYouSlide.buildUi();
                    carrousel.displaySlide(1,false);
                });
             //   listeners[0].unregister();
            }
            else {//Here the data are not loaded
                    Platform.runLater(() -> {
                        carrousel.displaySlide(0,false);
                    });
                }
        }, eventDataLoaded, registrationDataLoaded);
    }

    public Step1LoadingSlide getStep1LoadingSlide() {
        return step1LoadingSlide;
    }
    public void setStep1LoadingSlide(Step1LoadingSlide step1LoadingSlide) {
        this.step1LoadingSlide = step1LoadingSlide;
    }

    public Step2EventDetailsSlide getStep2EventDetailsSlide() {
        return step2EventDetailsSlide;
    }

    public void setStep2EventDetailsSlide(Step2EventDetailsSlide step2EventDetailsSlide) {
        this.step2EventDetailsSlide = step2EventDetailsSlide;
    }

    public Step3CheckoutSlide getStep3CheckoutSlide() {
        return step3CheckoutSlide;
    }

    public void setStep3CheckoutSlide(Step3CheckoutSlide step3CheckoutSlide) {
        this.step3CheckoutSlide = step3CheckoutSlide;
    }

    public Step6ThankYouSlide getStep6ThankYouSlide() {
        return step6ThankYouSlide;
    }

    public void setStep6ThankYouSlide(Step6ThankYouSlide step4ThankYouSlide) {
        this.step6ThankYouSlide = step4ThankYouSlide;
    }

    public Step5ErrorSlide getStep5ErrorSlide() {
        return step5ErrorSlide;
    }

    public void setStep5ErrorSlide(Step5ErrorSlide step5ErrorSlide) {
        this.step5ErrorSlide = step5ErrorSlide;
    }

    public Step4PaymentSlide getStep4PaymentSlide() {
        return this.step4PaymentSlide;
    }
    public void setStep4PaymentSlide(Step4PaymentSlide step4PaymentSlide) {
        this.step4PaymentSlide = step4PaymentSlide;
    }

    public void displayErrorMessage(String message) {
        step5ErrorSlide.setErrorMessage(message,true);
        Platform.runLater(() -> {
            carrousel.displaySlide(5);
        });
    }

    public boolean isEventDataLoaded() {
        return eventDataLoaded.get();
    }

    public BooleanProperty eventDataLoadedProperty() {
        return eventDataLoaded;
    }

    public void setEventDataLoaded(boolean eventDataLoaded) {
        this.eventDataLoaded.set(eventDataLoaded);
    }

    public boolean isRegistrationDataLoaded() {
        return registrationDataLoaded.get();
    }

    public BooleanProperty registrationDataLoadedProperty() {
        return registrationDataLoaded;
    }

    public void setRegistrationDataLoaded(boolean registrationDataLoaded) {
        this.registrationDataLoaded.set(registrationDataLoaded);
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
