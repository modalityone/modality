package one.modality.base.client.cloudinary;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.blob.Blob;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.orm.entity.Entities;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import one.modality.base.shared.entities.Event;

import java.util.function.Supplier;

public final class ModalityCloudinary {

    private static final CloudImageService CLOUD_IMAGE_SERVICE = new ClientImageService();

    // Low level API

    public static Future<Void> deleteImage(String imagePath) {
        return CLOUD_IMAGE_SERVICE.delete(imagePath, true);
    }

    public static Future<Void> uploadImage(String imagePath, Blob fileToUpload) {
        return CLOUD_IMAGE_SERVICE.upload(fileToUpload, imagePath, true);
    }

    public static Image getImage(String imagePath, int width, int height) {
        String url = CLOUD_IMAGE_SERVICE.url(imagePath, width, height);
        return new Image(url, true);
    }

    public static Future<Boolean> imageExists(String imagePath) {
        return CLOUD_IMAGE_SERVICE.exists(imagePath);
    }

    // Image path API

    public static String personImagePath(Object personEntityOrId) {
        Object primaryKey = Entities.getPrimaryKey(personEntityOrId);
        return "persons/person-" + primaryKey;
    }

    public static String eventImagePath(Object eventEntityOrId) {
        Object primaryKey = Entities.getPrimaryKey(eventEntityOrId);
        return "events/event-" + primaryKey;
    }

    public static String eventCoverImagePath(Object eventEntityOrId, Object language) {
        Object primaryKey = Entities.getPrimaryKey(eventEntityOrId);
        String imagePath = "events-cover/event-" + primaryKey + "-cover";
        if (language != null && !"en".equals(language.toString())) {
            imagePath += "-" + language;
        }
        return imagePath;
    }

    public static String eventCoverImagePath(Event event, Object language) {
        if (event.getRepeatedEvent() != null)
            event = event.getRepeatedEvent();
        return eventCoverImagePath(Entities.getPrimaryKey(event), language);
    }

    // Image loading API

    public static Future<ImageView> loadImage(String imagePath, MonoPane imageContainer, double width, double height, Supplier<Node> noImageNodeGetter) {
        Promise<ImageView> promise = Promise.promise();
        imageExists(imagePath)
            .onFailure(promise::fail)
            .onSuccess(exists -> Platform.runLater(() -> {
                if (exists) {
                    imageContainer.setBackground(null);
                    //First, we need to get the zoom factor of the screen
                    double zoomFactor = Screen.getPrimary().getOutputScaleX();
                    Image image = getImage(imagePath, width < 0 ? (int) width : (int) (width * zoomFactor), height < 0 ? (int) height : (int) (height * zoomFactor));
                    ImageView imageView = new ImageView();
                    imageView.setFitWidth(width);
                    imageView.setFitHeight(height);
                    if (width < 0 || height < 0)
                        imageView.setPreserveRatio(true);
                    imageView.setImage(image);
                    imageContainer.setContent(imageView);
                    promise.complete(imageView);
                } else {
                    if (noImageNodeGetter != null) {
                        Node noImageNode = noImageNodeGetter.get();
                        imageContainer.setBackground(Background.fill(Color.LIGHTGRAY));
                        imageContainer.setContent(noImageNode);
                    }
                    String message = imagePath + " doesn't exist in cloudinary";
                    promise.fail(message);
                    Console.log("⚠️ " + message);
                }
            }));
        return promise.future();
    }

}
