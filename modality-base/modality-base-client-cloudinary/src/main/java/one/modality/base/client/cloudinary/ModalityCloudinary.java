package one.modality.base.client.cloudinary;

import dev.webfx.extras.canvas.blob.CanvasBlob;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import dev.webfx.platform.blob.Blob;
import dev.webfx.platform.uischeduler.UiScheduler;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;
import dev.webfx.stack.orm.entity.Entities;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import one.modality.base.shared.entities.Event;

import java.util.function.Supplier;

public final class ModalityCloudinary {

    private static final CloudImageService CLOUD_IMAGE_SERVICE = new ClientImageService();

    // Low-level API

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

    public static Image getHdpiImage(String imagePath, double width, double height) {
        double zoomFactor = Screen.getPrimary().getOutputScaleX();
        return getImage(imagePath, width < 0 ? (int) width : (int) (width * zoomFactor), height < 0 ? (int) height : (int) (height * zoomFactor));
    }

    public static Future<Image> getImageOnReady(String imagePath, int width, int height) {
        return CLOUD_IMAGE_SERVICE.readyFuture().map(aVoid -> getImage(imagePath, width, height));
    }

    public static Future<Image> getHdpiImageOnReady(String imagePath, double width, double height) {
        return CLOUD_IMAGE_SERVICE.readyFuture().map(aVoid -> getHdpiImage(imagePath, width, height));
    }

    public static Future<Boolean> imageExists(String imagePath) {
        return CLOUD_IMAGE_SERVICE.exists(imagePath);
    }

    public static Future<Image> loadHdpiImage(String imagePath, double width, double height, MonoPane imageContainer, Supplier<Node> noImageNodeGetter) {
        return Images.onImageLoaded(getHdpiImageOnReady(imagePath, width, height), imageContainer, width, height, noImageNodeGetter);
    }

    public static Future<Image> loadHdpiEventCoverImage(Event event, Object language, double width, double height, MonoPane imageContainer, Supplier<Node> noImageNodeGetter) {
        String eventCoverImagePath = eventCoverImagePath(event, language);
        String defaultLanguageEventCoverImagePath = ModalityCloudinary.eventCoverImagePath(event, null);
        return loadHdpiImage(eventCoverImagePath, width, height, imageContainer, null)
            .recover(error -> loadHdpiImage(defaultLanguageEventCoverImagePath, width, height, imageContainer, noImageNodeGetter));
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

    // Image uploading API

    public static Future<Blob> prepareImageForUpload(Image originalImage, boolean makeSquare, double zoomFactor, double deltaX, double deltaY, double uploadImageMaxWidth, double uploadImageMaxHeight) {
        Image imageToUpload = originalImage;
        double imageWidth = originalImage.getWidth();
        double imageHeight = originalImage.getHeight();
        if (makeSquare && imageWidth != imageHeight) {
            //First, in case the image is not squared, we make a square one by adding transparent bg in the missing part
            double newWidth = Math.max(imageWidth, imageHeight);
            double newHeight = Math.max(imageWidth, imageHeight);
            WritableImage squareImage = new WritableImage((int) newWidth, (int) newHeight);
            imageToUpload = squareImage;
            // Draw the original image onto the new image with transparency
            Canvas canvas = new Canvas(newWidth, newHeight);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // Fill the background with transparent color
            gc.setFill(Color.TRANSPARENT);
            gc.fillRect(0, 0, newWidth, newHeight);

            // Draw the original image centered in the new image
            double x = (newWidth - originalImage.getWidth()) / 2;
            double y = (newHeight - originalImage.getHeight()) / 2; // Center vertically
            gc.drawImage(originalImage, x, y);
            // Snapshot the canvas into the WritableImage
            canvas.snapshot(null, squareImage);
        }

        imageWidth = imageToUpload.getWidth();
        imageHeight = imageToUpload.getHeight();
        double scalingPercentage = Math.max(imageToUpload.getWidth() / uploadImageMaxWidth, imageToUpload.getHeight() / uploadImageMaxHeight);

        double canvasWidth = uploadImageMaxWidth * 2;
        double canvasHeight = uploadImageMaxHeight * 2;
        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Calculate the scaled width and height of the image
        double scaledWidth = imageWidth / zoomFactor;
        double scaledHeight = imageHeight / zoomFactor;
        // scalingPercentage = 1;
        // Calculate offsets to center the image on the canvas
        double xOffset = (imageWidth - scaledWidth) / 2 - deltaX * scalingPercentage / zoomFactor;
        double yOffset = (imageHeight - scaledHeight) / 2 - deltaY * scalingPercentage / zoomFactor;
        // Clear the canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        // Draw the image scaled and centered
        gc.drawImage(imageToUpload, xOffset, yOffset, scaledWidth, scaledHeight, 0, 0, canvasWidth, canvasHeight);
        return CanvasBlob.createCanvasBlob(canvas);
    }

    public static Future<Void> replaceImage(String cloudImagePath, Blob fileToUpload) {
        Promise<Void> promise = Promise.promise();
        deleteImage(cloudImagePath)
            .onComplete(ar -> {
                //We wait for 2 seconds (if we don't wait, the picture doesn't change below, probably because
                //cloudinary server didn't have enough time to delete the old/proceed the old and new picture
                UiScheduler.scheduleDelay(ar.failed() ? 0 : 2000, () ->
                    uploadImage(cloudImagePath, fileToUpload)
                        .onComplete(promise));
            });
        return promise.future();
    }

}
