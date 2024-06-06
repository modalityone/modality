package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.carrousel.Carrousel;
import dev.webfx.extras.webtext.HtmlText;
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
    private Step4ThankYouSlide step4ThankYouSlide;
    private Step5ErrorSlide step5ErrorSlide;
    private Carrousel carrousel;
    private BooleanProperty eventDataLoaded = new SimpleBooleanProperty();
    private BooleanProperty registrationDataLoaded = new SimpleBooleanProperty();

    public SlideController(Carrousel car) {
        carrousel = car;
    }

    public void initialise() {
        eventDataLoaded.addListener((observable, oldValue, newValue) -> {
            if(newValue&&registrationDataLoaded.getValue()) {
                Platform.runLater(() -> {
                    step2EventDetailsSlide.buildUi();
                    step3CheckoutSlide.buildUi();
                    step4ThankYouSlide.buildUi();
                    carrousel.displaySlide(1,false);
                });
            }
            else{ //Here the data are not loaded
                Platform.runLater(() -> {
                    carrousel.displaySlide(0,false);
                });
            }
        });
        registrationDataLoaded.addListener((observable, oldValue, newValue) -> {
            if(newValue&&eventDataLoaded.getValue()) {
                Platform.runLater(() -> {
                    step2EventDetailsSlide.buildUi();
                    step3CheckoutSlide.buildUi();
                    step4ThankYouSlide.buildUi();
                    carrousel.displaySlide(1,false);
                });
            }
            else{ //Here the data are not loaded

                Platform.runLater(() -> {
                    carrousel.displaySlide(0,false);
                });
            }
        });
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

    public Step4ThankYouSlide getStep4ThankYouSlide() {
        return step4ThankYouSlide;
    }

    public void setStep4ThankYouSlide(Step4ThankYouSlide step4ThankYouSlide) {
        this.step4ThankYouSlide = step4ThankYouSlide;
    }

    public Step5ErrorSlide getStep5ErrorSlide() {
        return step5ErrorSlide;
    }

    public void setStep5ErrorSlide(Step5ErrorSlide step5ErrorSlide) {
        this.step5ErrorSlide = step5ErrorSlide;
    }

    public void displayErrorMessage(String message) {
        step5ErrorSlide.setErrorMessage(message);
        Platform.runLater(() -> {
            carrousel.displaySlide(4);
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
