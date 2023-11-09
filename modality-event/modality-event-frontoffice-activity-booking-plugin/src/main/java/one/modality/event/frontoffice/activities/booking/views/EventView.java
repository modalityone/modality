package one.modality.event.frontoffice.activities.booking.views;

import dev.webfx.extras.panes.ScalePane;
import dev.webfx.kit.launcher.WebFxKitLauncher;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import one.modality.base.frontoffice.utility.GeneralUtility;
import one.modality.base.frontoffice.utility.StyleUtility;
import one.modality.base.frontoffice.utility.TextUtility;
import one.modality.base.shared.entities.Event;

public final class EventView {

    private Event event;

    private final ImageView eventImageView = new ImageView();
    private final ScalePane eventImageScalePane = new ScalePane(eventImageView);
    private final Label eventNameLabel = GeneralUtility.getMediumLabel(null, StyleUtility.MAIN_BLUE);
    private final Label eventDescriptionLabel = GeneralUtility.createLabel("eventDescription", Color.web(StyleUtility.VICTOR_BATTLE_BLACK), 10);
    private final Text eventDateText = TextUtility.getText(null, 10, StyleUtility.VICTOR_BATTLE_BLACK);
    private final Text eventCentreLocationText =
            TextUtility.weight(TextUtility.getText(null, 8, StyleUtility.ELEMENT_GRAY), FontWeight.THIN);
    private final Text eventCountryLocationText =
            TextUtility.weight(TextUtility.getText(null, 8, StyleUtility.ELEMENT_GRAY), FontWeight.MEDIUM);
    private final Node eventLocation = GeneralUtility.createVList(0, 0,
            eventCentreLocationText,
            eventCountryLocationText
            //distance < 0 ? new VBox() : TextUtility.getText(distance.toString(), 8, StyleUtility.ELEMENT_GRAY)
    );

    private final Button comingSoonButton = I18nControls.bindI18nProperties(
            GeneralUtility.createButton(Color.web(StyleUtility.IMPORTANT_RED), 4, null, 11),
            "comingSoon");

    private final Button bookButton = I18nControls.bindI18nProperties(
            GeneralUtility.createButton(Color.web(StyleUtility.MAIN_BLUE), 4, null, 11),
            "bookNow");
    private final Button closedButton = I18nControls.bindI18nProperties(
            GeneralUtility.createButton(Color.web(StyleUtility.IMPORTANT_RED), 4, null, 11),
            "closed");

    private final BorderPane buttonContainer = new BorderPane();

    private final VBox container = new VBox(10,
            eventImageScalePane,
            GeneralUtility.createSplitRow(eventNameLabel, eventDateText, 80, 0),
            GeneralUtility.createSplitRow(eventDescriptionLabel, new Text(""), 80, 0),
            GeneralUtility.createSplitRow(eventLocation, buttonContainer, 80, 0)
    );

    {
        eventImageView.setPreserveRatio(true);
        bookButton.setOnAction(e -> {
            String bookingFormUrl = (String) event.evaluate("bookingFormUrl");
            bookingFormUrl = bookingFormUrl.replace("{host}", "kadampabookings.org");
            WebFxKitLauncher.getApplication().getHostServices().showDocument(bookingFormUrl);
        });
        bookButton.setCursor(Cursor.HAND);
        container.setPadding(new Insets(40));
        container.setBackground(Background.fill(Color.web(StyleUtility.BACKGROUND_GRAY)));
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
        I18n.bindI18nProperties(eventCentreLocationText, new I18nSubKey("expression: '[At] ' + coalesce(i18n(venue), i18n(organization))", event));
        I18n.bindI18nProperties(eventCountryLocationText, new I18nSubKey("expression: coalesce(i18n(venue.country), i18n(organization.country))", event));
        eventDateText.setText(event.getStartDate().toString());
        buttonContainer.setCenter(event.isLive() ? bookButton : comingSoonButton);
    }

    public Node getView() {
        return container;
    }
}
