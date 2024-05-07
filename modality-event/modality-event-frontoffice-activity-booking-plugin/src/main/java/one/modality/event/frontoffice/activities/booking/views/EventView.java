package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.ScalePane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.EventState;
import one.modality.event.frontoffice.activities.booking.process.BookingStarter;

public final class EventView {

    private Event event;

    private final ImageView eventImageView = new ImageView();
    private final ScalePane eventImageScalePane = new ScalePane(eventImageView);
    private final Label eventNameLabel = GeneralUtility.createLabel(StyleUtility.MAIN_ORANGE_COLOR);
    private final Label eventDescriptionLabel = GeneralUtility.createLabel(Color.BLACK);
    private final Text eventDateText = TextUtility.createText(Color.BLACK);
    private final Text eventCentreLocationText = TextUtility.createText(StyleUtility.ELEMENT_GRAY_COLOR);
    private final Text eventCountryLocationText = TextUtility.createText(StyleUtility.ELEMENT_GRAY_COLOR);
    private final Node eventLocation = GeneralUtility.createVList(0, 0,
            eventCentreLocationText,
            eventCountryLocationText
    );
    private final Button comingSoonButton = GeneralUtility.createButton("comingSoon");
    private final Button bookButton = GeneralUtility.createButton("bookNow");
    private final Button closedButton = GeneralUtility.createButton("closed");
    private final BorderPane buttonContainer = new BorderPane();

    private final VBox container = new VBox(10,
            eventImageScalePane,
            GeneralUtility.createSplitRow(
                    new VBox(10,
                            eventNameLabel,
                            eventDescriptionLabel,
                            eventLocation),
                    centeredVBox(
                            eventDateText,
                            buttonContainer), 80, 0)
    );

    public EventView() {
        GeneralUtility.onNodeClickedWithoutScroll(e -> BookingStarter.startEventBooking(event), bookButton);
        container.setPadding(new Insets(40));
        container.setBackground(Background.fill(StyleUtility.BACKGROUND_GRAY_COLOR));

        FXProperties.runNowAndOnPropertiesChange(() ->
                        eventDescriptionLabel.setManaged(Strings.isNotEmpty(eventDescriptionLabel.getText()))
                , eventDescriptionLabel.textProperty());

        FXProperties.runOnPropertiesChange(() -> {
            double fontFactor = GeneralUtility.computeFontFactor(container.getWidth());
            GeneralUtility.setLabeledFont(eventNameLabel,        StyleUtility.TEXT_FAMILY, FontWeight.SEMI_BOLD,   fontFactor * StyleUtility.MEDIUM_TEXT_SIZE);
            GeneralUtility.setLabeledFont(eventDescriptionLabel, StyleUtility.TEXT_FAMILY, FontWeight.NORMAL, fontFactor * 10);
            GeneralUtility.setLabeledFont(comingSoonButton,      StyleUtility.TEXT_FAMILY, FontWeight.NORMAL, fontFactor * 11);
            GeneralUtility.setLabeledFont(bookButton,            StyleUtility.TEXT_FAMILY, FontWeight.NORMAL, fontFactor * 11);
            GeneralUtility.setLabeledFont(closedButton,          StyleUtility.TEXT_FAMILY, FontWeight.NORMAL, fontFactor * 11);
            TextUtility.setTextFont(eventDateText,               StyleUtility.TEXT_FAMILY, FontWeight.NORMAL, fontFactor * 10);
            TextUtility.setTextFont(eventCentreLocationText,     StyleUtility.TEXT_FAMILY, FontWeight.NORMAL, fontFactor * 8);
            TextUtility.setTextFont(eventCountryLocationText,    StyleUtility.TEXT_FAMILY, FontWeight.MEDIUM, fontFactor * 8);
            CornerRadii radii = new CornerRadii(4 * fontFactor);
            Background redBackground = new Background(new BackgroundFill(StyleUtility.IMPORTANT_RED_COLOR, radii, null));
            Background blueBackground = new Background(new BackgroundFill(StyleUtility.MAIN_BLUE_COLOR, radii, null));
            comingSoonButton.setBackground(redBackground);
            closedButton.setBackground(redBackground);
            bookButton.setBackground(blueBackground);
        }, container.widthProperty());
    }

    private static VBox centeredVBox(Node... children) {
        VBox vBox = new VBox(10, children);
        vBox.setAlignment(Pos.CENTER);
        return vBox;
    }

    public void setEvent(Event event) {
        this.event = event;
        String imageUrl = (String) event.evaluate("image.url");
        boolean hasImage = imageUrl != null;
        eventImageScalePane.setVisible(hasImage);
        eventImageScalePane.setManaged(hasImage);
        if (hasImage) {
            eventImageView.setImage(new Image(imageUrl, true));
        }
        I18nControls.bindI18nProperties(eventNameLabel, new I18nSubKey("expression: i18n(this)", event));
        I18nControls.bindI18nProperties(eventDescriptionLabel, new I18nSubKey("expression: i18n(shortDescriptionLabel)", event));
        I18n.bindI18nProperties(eventCentreLocationText, new I18nSubKey("expression: '[At] ' + coalesce(i18n(venue), i18n(organization))", event));
        I18n.bindI18nProperties(eventCountryLocationText, new I18nSubKey("expression: coalesce(i18n(venue.country), i18n(organization.country))", event));
        eventDateText.setText(Strings.toString(event.getStartDate()));
        buttonContainer.setCenter((event.isLive() ||event.getState()== EventState.OPEN) ? bookButton : comingSoonButton);
    }

    public Node getView() {
        return container;
    }
}
