package one.modality.event.frontoffice.medias;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.orm.entity.EntityId;
import javafx.application.Platform;
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
import one.modality.base.client.icons.SvgIcons;
import one.modality.base.shared.entities.Event;

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

    public EventThumbnailView(Event event) {
        this.event = event;
        buildUi();
    }

    public EventThumbnailView(Event event, String itemCode) {
        this.event = event;
        this.imageItemCode = itemCode;
        buildUi();
    }

    public VBox getView() {
        return container;
    }

    //TODO:
    // - see the label we display according to  the event state (expired or not)
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
        String shortDescription = event.getShortDescription();
        Text shortDescriptionText = new Text(shortDescription);
        //For now if the text if too long, we just do a substring.
        //In the future we can replace by a CollapsePan with a read more link
        int maxStringLength = 230;
        if (Strings.length(shortDescription) > maxStringLength) {
            shortDescriptionText.setText(shortDescription.substring(0, maxStringLength - 5) + " [ . . . ]");
        }
        TextFlow textFlow = new TextFlow(shortDescriptionText);
        textFlow.setStyle("-fx-line-spacing: 5px;");
        shortDescriptionText.setStyle("-fx-font-size: 12px;"); // Adjust font size as needed

        // Add listener to handle text truncation after layout
        textFlow.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            truncateToFiveLines(shortDescriptionText, textFlow);
        });

        VBox.setMargin(textFlow, new Insets(20, 0, 0, 0));

        StackPane thumbailStackPane = new StackPane();
        thumbailStackPane.setPrefHeight(WIDTH);
        ImageView imageView = new ImageView();
        thumbailStackPane.getChildren().add(imageView);
        Label availabilityLabel = Bootstrap.textSuccess(I18nControls.newLabel("Available"));
        availabilityLabel.setPadding(new Insets(5, 15, 5, 15));
        availabilityLabel.setBackground(new Background(
            new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, new Insets(0))
        ));

        thumbailStackPane.getChildren().add(availabilityLabel);
        thumbailStackPane.setAlignment(Pos.TOP_LEFT);

        imageView.setImage(null);

        EntityId id = event.getId();
        //Here we're looking inb cloudinary if the picture for the cover exist
        //The item code list is as following:
        // Video : video | note: just one for all languages
        //TODO change in the production databases
        // English audio recording: audio-en
        // French audio recording: audio-fr
        // Spanish audio recording: audio-es
        // German audio recording: audio-de
        // Portuguese audio recording: audio-pt
        // Cantonese audio recording: audio-zh-yue
        // Mandarin audio recording: audio-zh
        // Vietnamese audio recording: audio-vi
        // Italian audio recording: audio-it
        // Greek audio recording: audio-el
        Object imageTag;
        if (isoCode == null || isoCode.equals("en")) {
            //We do add .jpg even if the image is not jpg, because for some reason, if we don't put an extension file, cloudinary doesn't always find the image, but it works when adding .jpg.
            imageTag = event.getId().getPrimaryKey() + "-cover.jpg";
        } else {
            imageTag = event.getId().getPrimaryKey() + "-cover-" + isoCode + ".jpg";
        }
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

        actionButton = Bootstrap.primaryButton(I18nControls.newButton("View"));
        actionButton.setPrefWidth(150);
        VBox.setMargin(actionButton, new Insets(30, 0, 0, 0));
        container.getChildren().addAll(
            thumbailStackPane,
            eventLabel,
            textFlow,
            actionButton
        );
    }

    private void truncateToFiveLines(Text text, TextFlow textFlow) {
//        String content = text.getText();
//        if (content.isEmpty()) return;
//
//        // Get the height of the TextFlow
//        double textFlowHeight = textFlow.getBoundsInParent().getHeight();
//        double maxHeight = text.getFont().getSize() * 8; // Approximately 5 lines (with some margin)
//
//        // If height exceeds limit, start truncating
//        if (textFlowHeight > maxHeight) {
//            // Binary search to find the right amount of text
//            int start = 0;
//            int end = content.length();
//            String truncated = content;
//
//            while (start < end) {
//                int mid = (start + end) / 2;
//                String testText = content.substring(0, mid) + "...";
//                text.setText(testText);
//
//                // Wait for layout to update
//                textFlow.layout();
//
//                if (textFlow.getBoundsInParent().getHeight() <= maxHeight) {
//                    truncated = testText;
//                    start = mid + 1;
//                } else {
//                    end = mid - 1;
//                }
//            }
//
//            // Find the last complete line
//            int lastNewline = truncated.lastIndexOf('\n');
//            if (lastNewline != -1) {
//                // Remove incomplete last line and add ellipsis
//                truncated = truncated.substring(0, lastNewline) + "\n...";
//            }
//
//            text.setText(truncated);
//        }
    }

    public Button getActionButton() {
        return actionButton;
    }

    public void setActionButton(Button actionButton) {
        this.actionButton = actionButton;
    }

    public static String extractISOCode(String itemCode) {
        if (itemCode == null) return null;
        String[] parts = itemCode.split("-");
        return parts.length > 1 ? parts[1] : null; // Return the second part (ISO 639-1 code)
    }
}
