package one.modality.event.frontoffice.activities.booking.process.event;

import dev.webfx.extras.carousel.Carousel;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import one.modality.base.shared.entities.Event;
import one.modality.event.client.event.fx.FXEvent;

final class SlideController {

    private final Carousel carousel = new Carousel();
    private final Step1LoadingSlide step1LoadingSlide;
    private final Step2EventDetailsSlide step2EventDetailsSlide;
    private final Step3CheckoutSlide step3CheckoutSlide;
    private final Step4PaymentSlide step4PaymentSlide;
    private final Step5ThankYouSlide step5ThankYouSlide;
    private final Step6CancellationSlide step6CancellationSlide;
    private final Step7ErrorSlide step7ErrorSlide;
    private final BookEventData bookEventData;
    private final BooleanProperty eventDataLoaded = new SimpleBooleanProperty();

    public SlideController(BookEventData bookEventData, Property<Node> mountNodeProperty) {
        this.bookEventData = bookEventData;
        step1LoadingSlide = new Step1LoadingSlide(this);
        step2EventDetailsSlide = new Step2EventDetailsSlide(this);
        step3CheckoutSlide = new Step3CheckoutSlide(this);
        step4PaymentSlide = new Step4PaymentSlide(this);
        step5ThankYouSlide = new Step5ThankYouSlide(this);
        step6CancellationSlide = new Step6CancellationSlide(this);
        step7ErrorSlide = new Step7ErrorSlide(this);
        initialise();

        carousel.setSlideSuppliers(
                step1LoadingSlide,
                step2EventDetailsSlide,
                step3CheckoutSlide,
                step4PaymentSlide,
                step5ThankYouSlide,
                step6CancellationSlide,
                step7ErrorSlide);
        carousel.setLoop(false);
        carousel.setShowingDots(false);

        step1LoadingSlide.buildUi();
        //the step2, 3 and 7 slide needs the data to be loaded from the database before we're able to build the UI, so the call is made elsewhere
        //the step3 slide needs the data to be loaded from the database before we're able to build the UI, so the call is made elsewhere
        step4PaymentSlide.buildUi();
        step7ErrorSlide.buildUi();

        // Sub-routing node binding (displaying the possible sub-routing account node in the appropriate place in step3)
        step3CheckoutSlide.accountMountNodeProperty().bind(mountNodeProperty);
    }

    void initialise() {
        eventDataLoaded.setValue(false);
        step2EventDetailsSlide.reset();
        step3CheckoutSlide.reset();
        step4PaymentSlide.reset();
        step5ThankYouSlide.reset();
        step7ErrorSlide.reset();

        // Rebuilding the UI and showing the loading slide while the event is loading
        FXProperties.runNowAndOnPropertiesChange(() -> UiScheduler.runInUiThread(() -> {
            if (eventDataLoaded.get()) {
                step2EventDetailsSlide.buildUi();
                step3CheckoutSlide.buildUi();
                step4PaymentSlide.buildUi();
                step5ThankYouSlide.buildUi();
                step6CancellationSlide.buildUi();
                step7ErrorSlide.buildUi();
                carousel.displaySlide(step2EventDetailsSlide.get());
            } else { // Here the data are not loaded
                carousel.displaySlide(step1LoadingSlide.get());
            }
        }), eventDataLoaded);
    }

    BookEventData getBookEventData() {
        return bookEventData;
    }

    Region getContainer() {
        return carousel.getContainer();
    }

    /**
     * In this method, we update the UI according to the event
     * @param e the event that has been selected
     */
    void loadEventDetails(Event e) {
        step2EventDetailsSlide.reset();
        step3CheckoutSlide.reset();
        step4PaymentSlide.reset();
        step5ThankYouSlide.reset();
        step7ErrorSlide.reset();

        step2EventDetailsSlide.loadData(e);
    }


    Step3CheckoutSlide getStep3CheckoutSlide() {
        return step3CheckoutSlide;
    }

    void goToThankYouSlide() {
        carousel.displaySlide(step5ThankYouSlide.get());
    }

    Step4PaymentSlide getStep4PaymentSlide() {
        return this.step4PaymentSlide;
    }

    void displayErrorMessage(String message) {
        step7ErrorSlide.setErrorMessage(message);
        Platform.runLater(() -> carousel.displaySlide(step7ErrorSlide.get()));
    }

    void displayCancellationMessage() {
        Platform.runLater(() -> carousel.displaySlide(step6CancellationSlide.get()));
    }

    BooleanProperty eventDataLoadedProperty() {
        return eventDataLoaded;
    }

    void setEventDataLoaded(boolean eventDataLoaded) {
        this.eventDataLoaded.set(eventDataLoaded);
    }

    void displayNextSlide() {
        carousel.moveForward();
    }

    RecurringEventSchedule getRecurringEventSchedule() {
        return step2EventDetailsSlide.getRecurringEventSchedule();
    }

    void bindI18nEventExpression(Property<String> textProperty, String eventExpression) {
        I18n.bindI18nTextProperty(textProperty, new I18nSubKey("expression: " + eventExpression, FXEvent.eventProperty()), eventDataLoadedProperty());
    }

    <T extends Text> T bindI18nEventExpression(T text, String eventExpression) {
        bindI18nEventExpression(text.textProperty(), eventExpression);
        return text;
    }

    <L extends Labeled> L bindI18nEventExpression(L text, String eventExpression) {
        bindI18nEventExpression(text.textProperty(), eventExpression);
        return text;
    }

    HtmlText bindI18nEventExpression(HtmlText text, String eventExpression) {
        bindI18nEventExpression(text.textProperty(), eventExpression);
        return text;
    }

}
