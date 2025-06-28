package one.modality.event.frontoffice.medias;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.panes.ScalePane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.extras.util.layout.Layouts;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Event;

import java.time.LocalDateTime;

/**
 * @author David Hello
 */
public final class EventThumbnail {

    public static final double CONTAINER_WIDTH = 533;
    private static final double CONTAINER_HEIGHT = 533;

    private final Event event;
    private final String imageItemCode;
    private final VBox container = new VBox();
    private Button viewButton;

    public enum ItemType {
        ITEM_TYPE_AUDIO,
        ITEM_TYPE_VIDEO
    }

    private enum AvailabilityType {
        AVAILABLE(MediasI18nKeys.Available),
        EXPIRED(MediasI18nKeys.Expired),
        UNPUBLISHED(MediasI18nKeys.Unpublished);

        private final Object i18nKey;

        AvailabilityType(Object i18nKey) {
            this.i18nKey = i18nKey;
        }

        public Object getI18nKey() {
            return this.i18nKey;
        }
    }

    private final ItemType itemType;
    private AvailabilityType availabilityType;
    private final boolean isPublished;

    public EventThumbnail(Event event, String itemCode, ItemType itemType, boolean isPublished) {
        this.event = event;
        this.imageItemCode = itemCode;
        this.itemType = itemType;
        this.isPublished = isPublished;
        buildUi();
    }

    public VBox getView() {
        return container;
    }

    //TODO:
    // - See cursor hand on button with bootstrap
    // - Change the language, and the cover also
    // - short description as a label
    // - mettre le maximum d'attributs dans le css
    // - tester avec plusieurs événements

    private void buildUi() {
        container.setPrefWidth(CONTAINER_WIDTH);
        String languageOfTheItem = extractLanguageISOCode(imageItemCode);
        //Here we manage 2 cases:
        //1. For Video, the item is the same for all languages, so we want to display the title and description according to the language of user
        //2. For Audio, since we can order audios in different languages, we want to display the title and description according to the language of the item
        I18nSubKey titleSubkey =  new I18nSubKey("expression: i18n(this, '" + languageOfTheItem + "')", event);

        Label eventLabel = Bootstrap.h3(I18nControls.newLabel(titleSubkey));
        Controls.setupTextWrapping(eventLabel, true, false);
        VBox.setMargin(eventLabel, new Insets(10, 0, 0, 0));

        HtmlText shortHTMLDescription = new HtmlText();
        I18n.bindI18nTextProperty(shortHTMLDescription.textProperty(), new I18nSubKey("expression: coalesce(i18n(shortDescriptionLabel,'"+languageOfTheItem+"'),shortDescription)", event), event);

        shortHTMLDescription.getStyleClass().add("short-description");

        Label availabilityLabel = new Label();
        if (isPublished) {
            LocalDateTime nowInEventTimezone = Event.nowInEventTimezone();
            if (itemType == ItemType.ITEM_TYPE_VIDEO) {
                //If the vodExpirationDate is set to null, it means the event is livestream Only
                LocalDateTime vodExpirationDate = event.getVodExpirationDate();
                if (vodExpirationDate != null && nowInEventTimezone.isAfter(vodExpirationDate))
                    availabilityType = AvailabilityType.EXPIRED;
                    //Case of the livestream only, we expired it when the event is finished
                else if (vodExpirationDate == null && nowInEventTimezone.isAfter(event.getEndDate().atTime(23, 59, 59)))
                    availabilityType = AvailabilityType.EXPIRED;
                else if (vodExpirationDate == null && event.getLivestreamUrl() == null) {
                    //If the vodExpirationDate is set to null, it means the event is livestream Only, we check if we have a livestream url defined
                    availabilityType = AvailabilityType.UNPUBLISHED;
                } else
                    availabilityType = AvailabilityType.AVAILABLE;
            }
            if (itemType == ItemType.ITEM_TYPE_AUDIO) {
                LocalDateTime audioExpirationDate = event.getAudioExpirationDate();
                boolean expired = audioExpirationDate != null && nowInEventTimezone.isAfter(audioExpirationDate);
                availabilityType = expired ? AvailabilityType.EXPIRED : AvailabilityType.AVAILABLE;
            }
        } else
            availabilityType = AvailabilityType.UNPUBLISHED;

        I18nControls.bindI18nProperties(availabilityLabel, availabilityType.getI18nKey());

        availabilityLabel.setPadding(new Insets(5, 15, 5, 15));
        availabilityLabel.setBackground(new Background(
            new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(7, 0, 7, 0, false) // Top-left and bottom-right corners rounded
                , new Insets(0))
        ));

        MonoPane imageContainer = new MonoPane();
        String imagePath = ModalityCloudinary.eventCoverImagePath(event, itemType == ItemType.ITEM_TYPE_AUDIO ? languageOfTheItem : I18n.getLanguage());
        ModalityCloudinary.loadImage(imagePath, imageContainer, CONTAINER_WIDTH, CONTAINER_HEIGHT, SvgIcons::createAudioCoverPath)
            .onFailure(error-> {
                //If we can't find the picture of the cover for the selected language, we display the default image
                ModalityCloudinary.loadImage(ModalityCloudinary.eventCoverImagePath(event, null), imageContainer, CONTAINER_WIDTH, CONTAINER_HEIGHT, SvgIcons::createAudioCoverPath);
            });

        StackPane thumbnailStackPane = new StackPane(imageContainer, availabilityLabel);
        thumbnailStackPane.setAlignment(Pos.TOP_LEFT);
        thumbnailStackPane.setPrefSize(CONTAINER_WIDTH, CONTAINER_HEIGHT);
        imageContainer.setMaxSize(CONTAINER_WIDTH, CONTAINER_HEIGHT); // required so that it fills the stack pane

        viewButton = Bootstrap.primaryButton(I18nControls.newButton(MediasI18nKeys.View));
        viewButton.setMinWidth(150);
        // We display the view button only if the content is available
        viewButton.setVisible(availabilityType == AvailabilityType.AVAILABLE);
        VBox.setMargin(viewButton, new Insets(30, 0, 0, 0));
        ScalePane thumbnailScalePane = new ScalePane(thumbnailStackPane);
        container.getChildren().addAll(
            thumbnailScalePane,
            eventLabel,
            shortHTMLDescription,
            Layouts.createVGrowable(), // Extra space so that all view buttons are aligned at the bottom in the same row
            viewButton
        );
    }

    public Button getViewButton() {
        return viewButton;
    }

    public static String extractLanguageISOCode(String itemCode) {
        if (itemCode == null) return null;
        String[] parts = itemCode.split("-");
        return parts.length > 1 ? parts[1] : null; // Return the second part (ISO 639-1 code)
    }
}
