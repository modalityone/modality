package one.modality.event.frontoffice.medias;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Objects;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.Event;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public final class EventHeader {

    private static final double STRAIGHT_MOBILE_LAYOUT_UNDER_WIDTH = 400; // mainly to reduce responsive computation on low-end devices
    private static final double IMAGE_HEIGHT = 240;

    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>(); // The event loaded from the event id
    private final ObjectProperty<Object> languageProperty = new SimpleObjectProperty<>();

    private final MonoPane responsiveHeader = new MonoPane();
    private final Label expirationLabel = Bootstrap.strong(new Label()); // TODO: put bold in CSS

    public EventHeader(boolean video) {
        MonoPane eventImageContainer = new MonoPane();

        Label eventLabel = Bootstrap.strong(new Label());
        Controls.setupTextWrapping(eventLabel, true, false);

        HtmlText eventDescriptionHTMLText = new HtmlText();
        eventDescriptionHTMLText.managedProperty().bind(eventDescriptionHTMLText.textProperty().isNotEmpty());
        eventDescriptionHTMLText.setMaxHeight(60);

        Controls.setupTextWrapping(expirationLabel, true, false);
        Layouts.bindManagedToVisibleProperty(expirationLabel);

        VBox titleVBox = new VBox(
            eventLabel,
            eventDescriptionHTMLText,
            expirationLabel);
        VBox.setMargin(eventDescriptionHTMLText, new Insets(12, 0, 0, 0));
        VBox.setMargin(expirationLabel, new Insets(30, 0, 0, 0));

        Layouts.setMinMaxHeightToPref(titleVBox); // No need to compute min/max height as different to pref (layout computation optimization)
        HBox.setHgrow(titleVBox, Priority.ALWAYS); // Necessary for the web version TODO: should work without, so needs investigation and bug fix

        FXProperties.runOnPropertiesChange(() -> {
            Event event = eventProperty.get();
            if (event == null)
                return;
            Object language = languageProperty.get();
            if (language == null)
                language = I18n.getLanguage();

            eventLabel.setText(MediaUtil.translate(event, language));
            eventDescriptionHTMLText.setText(Objects.coalesce(MediaUtil.translate(event.getShortDescriptionLabel(), language), event.getShortDescription()));

            // Loading the event image in the header
            String eventCloudImagePath = ModalityCloudinary.eventCoverImagePath(event, language);
            ModalityCloudinary.loadImage(eventCloudImagePath, eventImageContainer, -1, IMAGE_HEIGHT, video ? SvgIcons::createVideoIconPath : SvgIcons::createAudioCoverPath)
                .onFailure(error -> {
                    // If we can't find the picture of the cover for the selected language, we display the default image
                    ModalityCloudinary.loadImage(ModalityCloudinary.eventCoverImagePath(event, null), eventImageContainer, -1, IMAGE_HEIGHT, video ? SvgIcons::createVideoIconPath : SvgIcons::createAudioCoverPath);
                });
            // Updating the expiration date in the header
            LocalDateTime expirationDate = video ? event.getVodExpirationDate() : event.getAudioExpirationDate();
            if (expirationDate == null) {
                expirationLabel.setVisible(false);
            } else {
                expirationLabel.setVisible(true);
                LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();
                boolean available = nowInEventTimezone.isBefore(expirationDate);
                FXProperties.runNowAndOnPropertyChange(eventTimeSelected -> {
                    LocalDateTime userTimezoneExpirationDate = eventTimeSelected ? expirationDate : TimeZoneSwitch.convertEventLocalDateTimeToUserLocalDateTime(expirationDate);
                    I18nControls.bindI18nProperties(expirationLabel,
                        available ? MediasI18nKeys.AvailableUntil1 : MediasI18nKeys.ExpiredSince1,
                        LocalizedTime.formatLocalDateTimeProperty(userTimezoneExpirationDate, FrontOfficeTimeFormats.MEDIA_EXPIRATION_DATE_TIME_FORMAT));
                }, TimeZoneSwitch.eventLocalTimeSelectedProperty());
            }
        }, eventProperty, languageProperty);

        new ResponsiveDesign(responsiveHeader)
            // 1. Horizontal layout (for desktops) - as far as TitleVBox is not higher than the image
            .addResponsiveLayout(/* applicability test: */ width -> {
                    // Note that we take the opportunity of this responsive test (called each time the width changes) to
                    // set the font of eventLabel and eventDescriptionHTMLText in dependence on that width
                    double spacing = width * 0.05;
                    HBox.setMargin(titleVBox, new Insets(0, 0, 0, spacing));
                    double titleVBoxWidth = width - eventImageContainer.getWidth() - spacing;
                    // Here we resize the font according to the size of the window
                    double fontSizeFactor = Double.max(0.75, Double.min(1, titleVBoxWidth * 0.0042));
                    // In JavaFX, the CSS has priority on font; that's why we do a setStyle after. In web, the font has priority on CSS
                    eventLabel.setFont(Font.font(fontSizeFactor * 30));
                    eventLabel.setStyle("-fx-font-size: " + fontSizeFactor * 30);
                    eventDescriptionHTMLText.setFont(Font.font(fontSizeFactor * 18));
                    // Now we actually evaluate the responsive test based on the font size factor
                    return width > STRAIGHT_MOBILE_LAYOUT_UNDER_WIDTH && fontSizeFactor > 0.75; // if superior to 0.75 threshold => this desktop layout
                }, /* apply method: */ () -> { // for the desktop layout
                    responsiveHeader.setContent(new HBox(eventImageContainer, titleVBox));
                }
                , /* test dependencies: */ eventImageContainer.widthProperty())
            // 2. Vertical layout (for mobiles) - when TitleVBox is too high (always applicable if 1. is not)
            .addResponsiveLayout(/* apply method: */ () -> {
                VBox vBox = new VBox(10, eventImageContainer, titleVBox);
                Layouts.setMinMaxHeightToPref(vBox); // No need to compute min/max height as different to pref (layout computation optimization)
                vBox.setAlignment(Pos.CENTER);
                VBox.setMargin(titleVBox, new Insets(5, 10, 5, 10)); // Same as cell padding => vertically aligned with cell content
                responsiveHeader.setContent(vBox);
            }).start();
    }

    public MonoPane getView() {
        return responsiveHeader;
    }

    public Event getEvent() {
        return eventProperty.get();
    }

    public ObjectProperty<Event> eventProperty() {
        return eventProperty;
    }

    public ObjectProperty<Object> languageProperty() {
        return languageProperty;
    }

}
