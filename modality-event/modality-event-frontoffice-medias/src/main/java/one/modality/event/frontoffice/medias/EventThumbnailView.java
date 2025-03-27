package one.modality.event.frontoffice.medias;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import javafx.beans.binding.BooleanBinding;
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
 * @author Bruno Salmon
 */
public final class EventThumbnailView {

    private static final double CONTAINER_WIDTH = 263;
    private static final double CONTAINER_HEIGHT = 263;

    private final Event event;
    private String imageItemCode;
    private final VBox container = new VBox();
    private Button actionButton;

    public enum ItemType {
        ITEM_TYPE_AUDIO,
        ITEM_TYPE_VIDEO
    }

    private enum AvailabilityType {
        AVAILABLE(MediasI18nKeys.Available),
        EXPIRED(MediasI18nKeys.Expired),
        UNPUBLISHED(MediasI18nKeys.Unpublished);

        private final String key;

        AvailabilityType(String initialText) {
            this.key = initialText;
        }

        public String getKey() {
            return this.key;
        }
    }

    private final ItemType itemType;
    private AvailabilityType availabilityType;
    private boolean isPublished = false;

    public EventThumbnailView(Event event, ItemType itemType) {
        this.event = event;
        this.itemType = itemType;
        buildUi();
    }

    public EventThumbnailView(Event event, String itemCode, ItemType itemType, boolean isPublished) {
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
        String language = extractLanguageISOCode(imageItemCode);

        Label eventLabel = Bootstrap.h3(I18nControls.newLabel(new I18nSubKey("expression: i18n(this, '" + language + "')", event)));
        eventLabel.setWrapText(true);
        VBox.setMargin(eventLabel, new Insets(10, 0, 0, 0));
        String shortDescription = Strings.toSafeString(event.getShortDescription());
        String shortDescriptionText = shortDescription;
        //For now if the text if too long, we just do a substring.
        //In the future we can replace by a CollapsePan with a read more link
        int maxStringLength = 230;
        if (Strings.length(shortDescription) > maxStringLength) {
            shortDescriptionText = shortDescription.substring(0, maxStringLength - 5) + " [ . . . ]";
        }
        HtmlText shortHTMLDescription = new HtmlText(shortDescriptionText);
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

        I18nControls.bindI18nProperties(availabilityLabel, availabilityType.getKey());

        availabilityLabel.setPadding(new Insets(5, 15, 5, 15));
        availabilityLabel.setBackground(new Background(
            new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(7, 0, 7, 0, false) // Top-left and bottom-right corners rounded
                , new Insets(0))
        ));

        MonoPane imageContainer = new MonoPane();
        String imagePath = ModalityCloudinary.eventCoverImagePath(event, language);
        ModalityCloudinary.loadImage(imagePath, imageContainer, CONTAINER_WIDTH, CONTAINER_HEIGHT, SvgIcons::createAudioCoverPath);

        StackPane thumbnailStackPane = new StackPane(imageContainer, availabilityLabel);
        StackPane.setAlignment(availabilityLabel, Pos.TOP_LEFT);
        thumbnailStackPane.setPrefSize(CONTAINER_WIDTH, CONTAINER_HEIGHT);
        imageContainer.setMaxSize(CONTAINER_WIDTH, CONTAINER_HEIGHT); // required so that it fills the stack pane

        actionButton = Bootstrap.primaryButton(I18nControls.newButton(MediasI18nKeys.View));
        actionButton.setPrefWidth(150);
        //We display the view button only if the content is available
        actionButton.visibleProperty().bind(new BooleanBinding() {
            @Override
            protected boolean computeValue() {
                return availabilityType == AvailabilityType.AVAILABLE;
            }
        });
        VBox.setMargin(actionButton, new Insets(30, 0, 0, 0));
        container.getChildren().addAll(
            thumbnailStackPane,
            eventLabel,
            shortHTMLDescription,
            actionButton
        );
    }

    public Button getActionButton() {
        return actionButton;
    }

    public static String extractLanguageISOCode(String itemCode) {
        if (itemCode == null) return null;
        String[] parts = itemCode.split("-");
        return parts.length > 1 ? parts[1] : null; // Return the second part (ISO 639-1 code)
    }
}
