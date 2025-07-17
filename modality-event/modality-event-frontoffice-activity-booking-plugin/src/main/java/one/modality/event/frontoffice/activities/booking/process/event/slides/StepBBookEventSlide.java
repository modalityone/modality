package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Arrays;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import one.modality.base.client.i18n.I18nEntities;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.frontoffice.utility.tyler.GeneralUtility;
import one.modality.base.frontoffice.utility.tyler.StyleUtility;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.ecommerce.frontoffice.bookingform.BookingForm;
import one.modality.event.frontoffice.activities.booking.process.event.EventBookingFormSettings;
import one.modality.event.frontoffice.eventheader.EventHeader;

/**
 * @author Bruno Salmon
 */
final class StepBBookEventSlide extends StepSlide {

    private static final double MAX_PAGE_WIDTH = FOPageUtil.MAX_PAGE_WIDTH;
    private static final double MIN_FONT_SIZE = 12;
    private static final double MAX_FONT_SIZE = 16;

    private final VBox headerPane = new VBox(); // For some reason, using MonoPane makes height grows when width grows
    private EventHeader eventHeader;
    private final ObjectProperty<Font> mediumFontProperty = new SimpleObjectProperty<>(Font.font(StyleUtility.MEDIUM_TEXT_SIZE));
    private final ObjectProperty<Font> subFontProperty = new SimpleObjectProperty<>(Font.font(StyleUtility.SUB_TEXT_SIZE));
    private boolean workingBookingLoaded;
    private final DigitsSlideController digitsSlideController;

    StepBBookEventSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
        digitsSlideController = new DigitsSlideController(bookEventActivity);
    }

    ReadOnlyObjectProperty<Font> mediumFontProperty() {
        return mediumFontProperty;
    }

    void onEventChanged(Event event) {
        digitsSlideController.onEventChanged(event);
        if (workingBookingLoaded)
            digitsSlideController.displayFirstSlide();
        workingBookingLoaded = false;
        BookingForm bookingForm = digitsSlideController.getBookingForm();
        if (bookingForm != null) {
            EventBookingFormSettings settings = (EventBookingFormSettings) bookingForm.getSettings();
            eventHeader = settings.eventHeader();
        } else {
            eventHeader = null;
        }
        if (eventHeader != null) {
            eventHeader.eventFontProperty().bind(mediumFontProperty);
            eventHeader.descriptionFontProperty().bind(subFontProperty);
            applyWidthConstraints(-1);
            eventHeader.loadAndSetEvent(event)
                .onFailure(ex -> displayErrorMessage(ex.getMessage()))
                .onSuccess(x -> UiScheduler.runInUiThread(this::onEventDescriptionLoaded));
        }
    }

    boolean autoLoadExistingBooking() {
        return digitsSlideController.autoLoadExistingBooking();
    }

    void onPrepareRevealEvent() {
        BookingForm bookingForm = digitsSlideController.getBookingForm();
        if (bookingForm != null) {
            EventBookingFormSettings settings = (EventBookingFormSettings) bookingForm.getSettings();
            headerPane.setBackground(settings.headerBackground());
        }
        if (eventHeader != null) {
            headerPane.getChildren().setAll(eventHeader.getView());
        } else
            headerPane.getChildren().clear();
    }

    void onWorkingBookingLoaded() {
        digitsSlideController.onWorkingBookingLoaded();
        workingBookingLoaded = true;
        checkLoaded();
    }

    void onEventDescriptionLoaded() {
        checkLoaded();
    }

    private void checkLoaded() {
        if (workingBookingLoaded && eventHeader != null && eventHeader.isEventLoaded()) {
            displayBookSlide(); // which is me!
        }
    }

    public void buildSlideUi() {
        headerPane.setAlignment(Pos.CENTER);
        headerPane.setMaxWidth(Double.MAX_VALUE);

        Region digitsTransitionPane = digitsSlideController.getContainer();
        mainVbox.setPadding(Insets.EMPTY);
        mainVbox.getChildren().setAll(headerPane, digitsTransitionPane);

        FXProperties.runNowAndOnDoublePropertyChange(this::applyWidthConstraints, mainVbox.widthProperty());
    }

    private void applyWidthConstraints(double width) {
        if (width < 0)
            width = mainVbox.getWidth();
        double maxPageWidth = Math.min(MAX_PAGE_WIDTH, 0.90 * width);
        double headerTopBottomPadding = maxPageWidth * 0.1;
        BookingForm bookingForm = digitsSlideController.getBookingForm();
        EventBookingFormSettings settings = bookingForm == null ? null : (EventBookingFormSettings) bookingForm.getSettings();
        if (settings != null) {
            double headerMaxTopBottomPadding = settings.headerMaxTopBottomPadding();
            if (headerMaxTopBottomPadding >= 0 && headerTopBottomPadding > headerMaxTopBottomPadding)
                headerTopBottomPadding = headerMaxTopBottomPadding;
        }
        headerPane.setPadding(new Insets(headerTopBottomPadding, 0, headerTopBottomPadding, 0));
        if (eventHeader != null)
            eventHeader.setMaxPageWidth(maxPageWidth);
        Region digitsTransitionPane = digitsSlideController.getContainer();
        digitsTransitionPane.setMaxWidth(maxPageWidth);
        // Extra space between the header (with padding) and the digitsTransitionPane (which contains the booking form)
        double extraSpace = settings == null ? 0 : settings.extraSpaceBetweenHeaderAndBookingForm();
        if (extraSpace > 0 && extraSpace < 1) { // indicates a percentage of the booking form width
            extraSpace *= maxPageWidth;
        }
        digitsTransitionPane.setPadding(extraSpace == 0 ? Insets.EMPTY : new Insets(extraSpace, 0, 0, 0));
        double fontFactor = GeneralUtility.computeFontFactor(maxPageWidth);
        mediumFontProperty.set(Font.font(Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, StyleUtility.MEDIUM_TEXT_SIZE * fontFactor))));
        subFontProperty.set(   Font.font(Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, StyleUtility.SUB_TEXT_SIZE    * fontFactor))));
    }

    @Override
    void displayErrorMessage(Object errorMessageI18nKey) {
        digitsSlideController.displayErrorMessage(errorMessageI18nKey);
    }

    @Override
    void displayPaymentSlide(WebPaymentForm webPaymentForm) {
        digitsSlideController.displayPaymentSlide(webPaymentForm);
    }

    @Override
    void displayPendingPaymentSlide() {
        digitsSlideController.displayPendingPaymentSlide();
    }

    @Override
    void displayFailedPaymentSlide() {
        digitsSlideController.displayFailedPaymentSlide();
    }

    @Override
    void displayCancellationSlide(CancelPaymentResult cancelPaymentResult) {
        digitsSlideController.displayCancellationSlide(cancelPaymentResult);
    }

    @Override
    public <L extends Labeled> L bindI18nEventExpression(L text, String eventExpression, Object... args) {
        bindI18nEventExpression(text.textProperty(), eventExpression, args);
        return text;
    }

    @Override
    public HtmlText bindI18nEventExpression(HtmlText text, String eventExpression, Object... args) {
        bindI18nEventExpression(text.textProperty(), eventExpression, args);
        return text;
    }

    private void bindI18nEventExpression(Property<String> textProperty, String eventExpression, Object... args) {
        I18nEntities.bindExpressionTextProperty(textProperty, FXEvent.lastNonNullEventProperty(), eventExpression, Arrays.add(Object[]::new, args, eventHeader.eventLoadedProperty()));
    }
}
