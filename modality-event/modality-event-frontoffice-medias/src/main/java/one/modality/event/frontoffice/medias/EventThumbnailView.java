package one.modality.event.frontoffice.medias;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.entity.Entities;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import one.modality.base.client.cloudinary.ModalityCloudinary;
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Event;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author Bruno Salmon
 */
public final class EventThumbnailView {

    private static final double WIDTH = 263;

    private final Event event;
    private String imageItemCode;
    private final VBox container = new VBox();
    private final CloudImageService cloudImageService = new ClientImageService();
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
        container.setPrefWidth(WIDTH);
        String isoCode = extractISOCode(imageItemCode);

        Label eventLabel = Bootstrap.h3(I18nControls.newLabel(new I18nSubKey("expression: i18n(this, '" + isoCode + "')", event)));
        eventLabel.setWrapText(true);
        VBox.setMargin(eventLabel, new Insets(10, 0, 0, 0));
        String shortDescription = "";
        if (event.getShortDescription() != null)
            shortDescription = event.getShortDescription();
        String shortDescriptionText = new String(shortDescription);
        //For now if the text if too long, we just do a substring.
        //In the future we can replace by a CollapsePan with a read more link
        int maxStringLength = 230;
        if (Strings.length(shortDescription) > maxStringLength) {
            shortDescriptionText = (shortDescription.substring(0, maxStringLength - 5) + " [ . . . ]");
        }
        HtmlText shortHTMLDescription = new HtmlText(shortDescriptionText);
        shortHTMLDescription.getStyleClass().add("short-description");


        StackPane thumbailStackPane = new StackPane();
        thumbailStackPane.setAlignment(Pos.BOTTOM_CENTER);
        thumbailStackPane.setPrefHeight(WIDTH);
        ImageView imageView = new ImageView();
        imageView.setFitHeight(WIDTH);
        imageView.setFitWidth(WIDTH);
        imageView.setPreserveRatio(true);
        thumbailStackPane.getChildren().add(imageView);

        Label availabilityLabel = new Label();

        if (isPublished) {
            if (itemType == ItemType.ITEM_TYPE_VIDEO) {
                //If the vodExpirationDate is set to null, it means the event is livestream Only
                if (event.getVodExpirationDate() != null && LocalDateTime.now().isAfter(event.getVodExpirationDate()))
                    availabilityType = AvailabilityType.EXPIRED;
                    //Case of the livestream only, we expired it when the event is finished
                else if (event.getVodExpirationDate() == null && LocalDateTime.now().isAfter(LocalDateTime.of(event.getEndDate(), LocalTime.of(23, 59, 59))))
                    availabilityType = AvailabilityType.EXPIRED;
                else if (event.getVodExpirationDate() == null && event.getLivestreamUrl() == null) {
                    //If the vodExpirationDate is set to null, it means the event is livestream Only, we check if we have a livestream url defined
                    availabilityType = AvailabilityType.UNPUBLISHED;
                } else
                    availabilityType = AvailabilityType.AVAILABLE;
            }
            if (itemType == ItemType.ITEM_TYPE_AUDIO)
                if (event.getAudioExpirationDate() != null && LocalDateTime.now().isAfter(event.getAudioExpirationDate()))
                    availabilityType = AvailabilityType.EXPIRED;
                    //                else if(true)
                    //                    availabilityLabel = Bootstrap.textDanger(I18nControls.newLabel("NotPublished"));
                else
                    availabilityType = AvailabilityType.AVAILABLE;
        } else
            availabilityType = AvailabilityType.UNPUBLISHED;

        I18nControls.bindI18nProperties(availabilityLabel, availabilityType.getKey());


        availabilityLabel.setPadding(new Insets(5, 15, 5, 15));
        availabilityLabel.setBackground(new Background(
            new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(7, 0, 7, 0, false) // Top-left and bottom-right corners rounded
                , new Insets(0))
        ));
        thumbailStackPane.getChildren().add(availabilityLabel);
        thumbailStackPane.setAlignment(Pos.TOP_LEFT);

        imageView.setImage(null);

        event.getId();
        //Here we're looking inb cloudinary if the picture for the cover exist
        //The item code list is as following:
        // Video : video | note: just one for all languages
        Object imageTag;
        imageTag = ModalityCloudinary.getEventCoverImageTag(Entities.getPrimaryKey(event), isoCode);
        if(event.getRepeatedEvent()!=null)
            imageTag = ModalityCloudinary.getEventCoverImageTag(Entities.getPrimaryKey(event.getRepeatedEvent()),isoCode);

        String pictureId = String.valueOf(imageTag);

        cloudImageService.exists(pictureId)
            .onFailure(Console::log)
            .onSuccess(exists -> Platform.runLater(() -> {
                Console.log("exists: " + exists);
                if (exists) {
                    thumbailStackPane.setBackground(null);
                    //First, we need to get the zoom factor of the screen
                    double zoomFactor = Screen.getPrimary().getOutputScaleX();
                    String url = cloudImageService.url(pictureId, (int) (WIDTH * zoomFactor), -1);
                    imageView.setFitWidth(WIDTH);
                    imageView.setPreserveRatio(true);
                    Image imageToDisplay = new Image(url, true);
                    imageView.setImage(imageToDisplay);
                } else {
                    //TODO: change in case it's a video instead of an audio
                    SVGPath audioCoverPath = SvgIcons.createAudioCoverPath();
                    thumbailStackPane.setBackground(new Background(
                        new BackgroundFill(Color.LIGHTGRAY, null, null)
                    ));
                    MonoPane audioCoverPictureMonoPane = new MonoPane(audioCoverPath);
                    thumbailStackPane.getChildren().add(audioCoverPictureMonoPane);
                    StackPane.setAlignment(audioCoverPictureMonoPane, Pos.CENTER);
                }
            }));

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
            thumbailStackPane,
            eventLabel,
            shortHTMLDescription,
            actionButton
        );
    }

    private void truncateToFiveLines(Text text, TextFlow textFlow) {
        //TODO implement if needed
    }

    public Button getActionButton() {
        return actionButton;
    }

    public static String extractISOCode(String itemCode) {
        if (itemCode == null) return null;
        String[] parts = itemCode.split("-");
        return parts.length > 1 ? parts[1] : null; // Return the second part (ISO 639-1 code)
    }
}
