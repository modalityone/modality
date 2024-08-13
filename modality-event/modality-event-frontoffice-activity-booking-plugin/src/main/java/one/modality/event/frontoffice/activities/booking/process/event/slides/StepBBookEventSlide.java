package one.modality.event.frontoffice.activities.booking.process.event.slides;

import dev.webfx.extras.panes.ScaleMode;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.shared.entities.Event;
import one.modality.ecommerce.payment.client.WebPaymentForm;
import one.modality.event.client.event.fx.FXEvent;
import one.modality.event.frontoffice.activities.booking.process.event.BookEventActivity;
import one.modality.event.frontoffice.activities.booking.process.event.RecurringEventSchedule;

final class StepBBookEventSlide extends StepSlide {

    private static final double MAX_PAGE_WIDTH = 1200;
    private static final double MIN_FONT_SIZE = 12;
    private static final double MAX_FONT_SIZE = 16;

    private final CloudImageService cloudImageService = new ClientImageService();
    private final ImageView imageView = new ImageView();
    private final BooleanProperty eventDescriptionLoadedProperty = new SimpleBooleanProperty();
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
        event.onExpressionLoaded("name, shortDescription, description, venue.(name, label, address), organization.country")
                .onFailure(ex -> displayErrorMessage(ex.getMessage()))
                .onSuccess(x -> UiScheduler.runInUiThread(this::onEventDescriptionLoaded));

        imageView.setImage(null);
        Object imageTag = event.getId().getPrimaryKey();
        String pictureId = String.valueOf(imageTag);
        cloudImageService.exists(pictureId)
                .onFailure(Console::log)
                .onSuccess(exists -> Platform.runLater(() -> {
                    Console.log("exists: " + exists);
                    if (exists) {
                        //First, we need to get the zoom factor of the screen
                        double zoomFactor = Screen.getPrimary().getOutputScaleX();
                        String url = cloudImageService.url(String.valueOf(imageTag), (int) (imageView.getFitWidth() * zoomFactor), -1);
                        Image imageToDisplay = new Image(url, true);
                        imageView.setImage(imageToDisplay);
                    }
                }));
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
        Label eventLabel = bindI18nEventExpression(new Label(),"i18n(this)");
        eventLabel.setTextAlignment(TextAlignment.CENTER);
        eventLabel.getStyleClass().add("event-title");
        eventLabel.fontProperty().bind(mediumFontProperty);

        Label venueAddress = bindI18nEventExpression(new Label(), "venue.address");
        venueAddress.setTextAlignment(TextAlignment.CENTER);
        venueAddress.getStyleClass().add("event-title");
        venueAddress.setGraphicTextGap(5);
        venueAddress.setGraphic(SvgIcons.createPinpointSVGPath());
        venueAddress.fontProperty().bind(subFontProperty);

        eventShortDescriptionHtmlText.fontProperty().bind(subFontProperty);
        eventShortDescriptionHtmlText.getStyleClass().add("event-title");
        eventShortDescriptionHtmlText.setFocusTraversable(false);

        ScalePane imageScalePane = new ScalePane(ScaleMode.BEST_FIT, imageView);
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
        orangePane.setBackground(Background.fill(StyleUtility.MAIN_ORANGE_COLOR));
        orangePane.setMaxWidth(Double.MAX_VALUE);

        Region digitsTransitionPane = digitsSlideController.getContainer();

        mainVbox.setPadding(Insets.EMPTY);
        mainVbox.getChildren().setAll(orangePane, digitsTransitionPane);

        FXProperties.runOnPropertiesChange(() -> {
            double width = mainVbox.getWidth();
            double maxPageWidth = Math.min(MAX_PAGE_WIDTH, 0.90 * width);
            double orangeVerticalGap = maxPageWidth * 0.1;
            orangePane.setPadding(new Insets(orangeVerticalGap, 0, orangeVerticalGap, 0));
            gridPane.setMaxWidth(maxPageWidth);
            VBox.setMargin(eventLabel, new Insets(0,0,5 * maxPageWidth / MAX_PAGE_WIDTH,0));
            VBox.setMargin(eventShortDescriptionHtmlText, new Insets(20 * maxPageWidth / MAX_PAGE_WIDTH, 0, 0, 0));
            eventShortDescriptionHtmlText.setMaxWidth(maxPageWidth * 0.6);
            digitsTransitionPane.setMaxWidth(maxPageWidth);
            digitsTransitionPane.setPadding(new Insets(maxPageWidth * 0.03, 0, 0, 0));
            double fontFactor = GeneralUtility.computeFontFactor(maxPageWidth);
            mediumFontProperty.set(Font.font(Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, StyleUtility.MEDIUM_TEXT_SIZE * fontFactor))));
            subFontProperty.set(   Font.font(Math.max(MIN_FONT_SIZE, Math.min(MAX_FONT_SIZE, StyleUtility.SUB_TEXT_SIZE    * fontFactor))));
        }, mainVbox.widthProperty());
    }

    void displayCheckoutSlide() {
        digitsSlideController.displayCheckoutSlide();
    }

    void displayErrorMessage(String message) {
        digitsSlideController.displayErrorMessage(message);
    }

    void displayPaymentSlide(WebPaymentForm webPaymentForm) {
        digitsSlideController.displayPaymentSlide(webPaymentForm);
    }

    void displayCancellationSlide() {
        digitsSlideController.displayCancellationSlide();
    }

    RecurringEventSchedule getRecurringEventSchedule() {
        return digitsSlideController.getRecurringEventSchedule();
    }

    void bindI18nEventExpression(Property<String> textProperty, String eventExpression) {
        I18n.bindI18nTextProperty(textProperty, new I18nSubKey("expression: " + eventExpression,
                FXEvent.lastNonNullEventProperty()), FXEvent.lastNonNullEventProperty(), eventDescriptionLoadedProperty);
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
