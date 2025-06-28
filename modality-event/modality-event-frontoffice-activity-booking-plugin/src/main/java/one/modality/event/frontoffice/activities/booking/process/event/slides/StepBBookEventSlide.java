package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.platform.util.Arrays;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import javafx.beans.property.*;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import one.modality.base.client.brand.Brand;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.frontoffice.utility.page.FOPageUtil;
import one.modality.base.frontoffice.utility.tyler.GeneralUtility;
import one.modality.base.frontoffice.utility.tyler.StyleUtility;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.payment.CancelPaymentResult;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.booking.BookableDatesUi;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;

/**
 * @author Bruno Salmon
 */
final class StepBBookEventSlide extends StepSlide {

    private static final double MAX_PAGE_WIDTH = FOPageUtil.MAX_PAGE_WIDTH;
    private static final double MIN_FONT_SIZE = 12;
    private static final double MAX_FONT_SIZE = 16;

    private final BooleanProperty eventDescriptionLoadedProperty = new SimpleBooleanProperty();
    private final MonoPane eventImageContainer = new MonoPane();
    private final HtmlText eventShortDescriptionHtmlText = bindI18nEventExpression(new HtmlText(), "'<center>' + shortDescription + '</center>'");
    private final ObjectProperty<Font> mediumFontProperty = new SimpleObjectProperty<>(Font.font(StyleUtility.MEDIUM_TEXT_SIZE));
    private final ObjectProperty<Font> subFontProperty = new SimpleObjectProperty<>(Font.font(StyleUtility.SUB_TEXT_SIZE));
    private boolean workingBookingLoaded;
    private final DigitsSlideController digitsSlideController;

    public StepBBookEventSlide(BookEventActivity bookEventActivity) {
        super(bookEventActivity);
        digitsSlideController = new DigitsSlideController(bookEventActivity);
    }

    public ReadOnlyObjectProperty<Font> mediumFontProperty() {
        return mediumFontProperty;
    }

    public void onEventChanged(Event event) {
        if (workingBookingLoaded)
            digitsSlideController.displayFirstSlide();
        workingBookingLoaded = false;
        eventDescriptionLoadedProperty.set(false);
        event.onExpressionLoaded("name, label, shortDescription, description, venue.(name, label, address), organization.country")
            .onFailure(ex -> displayErrorMessage(ex.getMessage()))
            .onSuccess(x -> UiScheduler.runInUiThread(this::onEventDescriptionLoaded));

        eventImageContainer.setContent(null);
        String cloudImagePath =  ModalityCloudinary.eventImagePath(event);
        ModalityCloudinary.loadImage(cloudImagePath, eventImageContainer, -1, -1, null);
    }

    void onWorkingBookingLoaded() {
        digitsSlideController.onWorkingBookingLoaded();
        workingBookingLoaded = true;
        checkLoaded();
    }

    void onEventDescriptionLoaded() {
        eventDescriptionLoadedProperty.set(true);
        checkLoaded();
    }

    private void checkLoaded() {
        if (workingBookingLoaded && eventDescriptionLoadedProperty.get()) {
            displayBookSlide(); // which is me!
        }
    }

    public void buildSlideUi() {
        Label eventLabel = bindI18nEventExpression(new Label(), "i18n(this)");
        eventLabel.setTextAlignment(TextAlignment.CENTER);
        eventLabel.getStyleClass().add("event-title");
        eventLabel.fontProperty().bind(mediumFontProperty);

        Label venueAddress = bindI18nEventExpression(new Label(), "coalesce(venue.address, i18n(venue))");
        venueAddress.setTextAlignment(TextAlignment.CENTER);
        venueAddress.getStyleClass().add("event-title");
        venueAddress.setGraphicTextGap(5);
        venueAddress.setGraphic(SvgIcons.createPinpointSVGPath());
        venueAddress.fontProperty().bind(subFontProperty);

        eventShortDescriptionHtmlText.fontProperty().bind(subFontProperty);
        eventShortDescriptionHtmlText.getStyleClass().add("event-title");
        eventShortDescriptionHtmlText.setFocusTraversable(false);

        ScalePane imageScalePane = new ScalePane(ScaleMode.BEST_FIT, eventImageContainer);
        imageScalePane.setCanGrow(false);

        VBox eventShortTextBox = new VBox(
            eventLabel,
            venueAddress,
            eventShortDescriptionHtmlText
        );
        eventShortTextBox.setMinWidth(Region.USE_PREF_SIZE);
        ScalePane eventShortTextScalePane = new ScalePane(ScaleMode.BEST_FIT, eventShortTextBox);

        eventShortTextBox.setAlignment(Pos.CENTER);
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(33);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(66);
        gridPane.getColumnConstraints().addAll(c1, c2);
        gridPane.add(imageScalePane, 0, 0);
        gridPane.add(eventShortTextScalePane, 1, 0);
        GridPane.setHalignment(imageScalePane, HPos.LEFT);
        GridPane.setValignment(eventShortTextScalePane, VPos.CENTER);

        VBox orangePane = new VBox(gridPane); // For any reason, using MonoPane makes height grows when width grows
        orangePane.setAlignment(Pos.CENTER);
        orangePane.setBackground(Background.fill(Brand.getBrandMainColor()));
        orangePane.setMaxWidth(Double.MAX_VALUE);

        Region digitsTransitionPane = digitsSlideController.getContainer();

        mainVbox.setPadding(Insets.EMPTY);
        mainVbox.getChildren().setAll(orangePane, digitsTransitionPane);

        FXProperties.runOnDoublePropertyChange(width -> {
            double maxPageWidth = Math.min(MAX_PAGE_WIDTH, 0.90 * width);
            double orangeVerticalGap = maxPageWidth * 0.1;
            orangePane.setPadding(new Insets(orangeVerticalGap, 0, orangeVerticalGap, 0));
            gridPane.setMaxWidth(maxPageWidth);
            VBox.setMargin(eventLabel, new Insets(0, 0, 5 * maxPageWidth / MAX_PAGE_WIDTH, 0));
            VBox.setMargin(eventShortDescriptionHtmlText, new Insets(20 * maxPageWidth / MAX_PAGE_WIDTH, 0, 0, 0));
            eventShortDescriptionHtmlText.setMaxWidth(maxPageWidth * 0.6);
            digitsTransitionPane.setMaxWidth(maxPageWidth);
            digitsTransitionPane.setPadding(new Insets(maxPageWidth * 0.03, 0, 0, 0));
            double fontFactor = GeneralUtility.computeFontFactor(maxPageWidth);
            mediumFontProperty.set(Font.font(Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, StyleUtility.MEDIUM_TEXT_SIZE * fontFactor))));
            subFontProperty.set(   Font.font(Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, StyleUtility.SUB_TEXT_SIZE    * fontFactor))));
        }, mainVbox.widthProperty());
    }

    @Override
    protected void displayCheckoutSlide() {
        digitsSlideController.displayCheckoutSlide();
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
    BookableDatesUi getBookableDatesUi() {
        return digitsSlideController.getBookableDateUi();
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
        Object[] additionalArgs = { FXEvent.lastNonNullEventProperty(), eventDescriptionLoadedProperty };
        Object[] allArgs = Arrays.concat(Object[]::new, args, additionalArgs);
        I18n.bindI18nTextProperty(textProperty, new I18nSubKey("expression: " + eventExpression,
            FXEvent.lastNonNullEventProperty()), allArgs);
    }
}
