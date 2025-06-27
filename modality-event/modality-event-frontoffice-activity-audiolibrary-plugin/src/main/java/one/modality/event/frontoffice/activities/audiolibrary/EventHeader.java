package one.modality.event.frontoffice.activities.audiolibrary;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.responsive.ResponsiveDesign;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.util.Objects;
import dev.webfx.stack.i18n.controls.I18nControls;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.client.time.FrontOfficeTimeFormats;
import one.modality.base.shared.entities.Event;
import one.modality.event.frontoffice.medias.MediaUtil;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
final class EventHeader {

    private static final double IMAGE_HEIGHT = 200;

    private final ObjectProperty<Event> eventProperty = new SimpleObjectProperty<>(); // The event loaded from the event id
    private final StringProperty pathItemCodeProperty = new SimpleStringProperty();
    private final StringProperty dateFormattedProperty = new SimpleStringProperty();

    private final Label audioExpirationText = Bootstrap.textSuccess(new Label());
    private final MonoPane responsiveHeader = new MonoPane();

    public EventHeader() {
        MonoPane imageMonoPane = new MonoPane();
        Label eventLabel = Bootstrap.strong(new Label());
        HtmlText eventDescriptionHTMLText = new HtmlText();

        FXProperties.runOnPropertiesChange(() -> {
            Event event = eventProperty.get();
            String itemCode = pathItemCodeProperty.get();
            if (event == null || itemCode == null)
                return;

            String languageAbr = itemCode.contains("-") ? itemCode.split("-")[1] : null;
            eventLabel.setText(MediaUtil.translate(event, languageAbr));
            eventDescriptionHTMLText.setText(Objects.coalesce(MediaUtil.translate(event.getShortDescriptionLabel(), languageAbr), event.getShortDescription()));

            String lang = extractLang(pathItemCodeProperty.get());
            String cloudImagePath = ModalityCloudinary.eventCoverImagePath(event, lang);
            ModalityCloudinary.loadImage(cloudImagePath, imageMonoPane, -1, IMAGE_HEIGHT, SvgIcons::createAudioCoverPath);
            LocalDateTime audioExpirationDate = event.getAudioExpirationDate();
            if (audioExpirationDate != null) {
                dateFormattedProperty.bind(LocalizedTime.formatLocalDateProperty(audioExpirationDate, FrontOfficeTimeFormats.AUDIO_PLAYLIST_DATE_FORMAT));
                audioExpirationText.setVisible(true);
            } else {
                FXProperties.setEvenIfBound(dateFormattedProperty, null);
                audioExpirationText.setVisible(false);
            }

        }, eventProperty, pathItemCodeProperty);

        eventLabel.setWrapText(true);
        eventLabel.setMinHeight(Region.USE_PREF_SIZE);

        eventDescriptionHTMLText.managedProperty().bind(eventDescriptionHTMLText.textProperty().isNotEmpty());

        I18nControls.bindI18nProperties(audioExpirationText, AudioLibraryI18nKeys.AvailableUntil1, dateFormattedProperty);
        audioExpirationText.managedProperty().bind(dateFormattedProperty.isNotEmpty());
        Layouts.bindManagedToVisibleProperty(audioExpirationText);

        VBox titleVBox = new VBox(
            eventLabel,
            eventDescriptionHTMLText,
            audioExpirationText);
        VBox.setMargin(eventDescriptionHTMLText, new Insets(12, 0, 0, 0));
        VBox.setMargin(audioExpirationText, new Insets(30, 0, 0, 0));

        new ResponsiveDesign(responsiveHeader)
            // 1. Horizontal layout (for desktops) - as far as TitleVBox is not higher than the image
            .addResponsiveLayout(/* applicability test: */ width -> {
                    double spacing = width * 0.05;
                    HBox.setMargin(titleVBox, new Insets(0, 0, 0, spacing));
                    double titleVBoxWidth = width - imageMonoPane.getWidth() - spacing;
                    //Here we resize the font according to the size of the window
                    double fontSizeFactor = Double.max(0.75, Double.min(1, titleVBoxWidth * 0.0042));
                    //In JavaFX, the CSS has priority on Font, that's why we do a setStyle after. In web, the Font has priority on CSS
                    eventLabel.setFont(Font.font(fontSizeFactor * 30));
                    eventLabel.setStyle("-fx-font-size: " + fontSizeFactor * 30);
                    eventDescriptionHTMLText.setFont(Font.font(fontSizeFactor * 18));
                    return width > 400 // to prevent initial alternation on mobiles before the image is loaded
                           && fontSizeFactor > 0.75;
                }, /* apply method: */ () -> {
                    HBox.setHgrow(titleVBox, Priority.ALWAYS); // Necessary on the web version, otherwise width is limited to eventLabel
                    responsiveHeader.setContent(new HBox(imageMonoPane, titleVBox));
                }
                , /* test dependencies: */ imageMonoPane.widthProperty())
            // 2. Vertical layout (for mobiles) - when TitleVBox is too high (always applicable if 1. is not)
            .addResponsiveLayout(/* apply method: */ () -> {
                VBox vBox = new VBox(10, imageMonoPane, titleVBox);
                vBox.setAlignment(Pos.CENTER);
                VBox.setMargin(titleVBox, new Insets(15, 10, 5, 0)); // Same as cell margin => vertically aligned with cell content
                responsiveHeader.setContent(vBox);
            }).start();
    }

    MonoPane getView() {
        return responsiveHeader;
    }

    public Event getEvent() {
        return eventProperty.get();
    }

    public ObjectProperty<Event> eventProperty() {
        return eventProperty;
    }

    public StringProperty pathItemCodeProperty() {
        return pathItemCodeProperty;
    }

    private String extractLang(String itemCode) {
        //the itemCode is in the form audio-fr
        if (itemCode == null || !itemCode.contains("-")) {
            return null;
        }
        int dashIndex = itemCode.indexOf("-");
        return itemCode.substring(dashIndex + 1);
    }


}
