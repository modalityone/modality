package one.modality.base.client.cloudinary;

import dev.webfx.extras.panes.MonoPane;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.async.Future;
import dev.webfx.platform.async.Promise;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Bruno Salmon
 */
final class Images {

    public static void onImageLoaded(Image image, Consumer<Image> onImageSuccess, Consumer<Throwable> onError) {
        FXProperties.runNowAndOnPropertiesChange(() -> {
            if (image.isError())
                onError.accept(image.getException());
            else if (image.getProgress() >= 1)
                onImageSuccess.accept(image);
        }, image.progressProperty(), image.errorProperty());
    }

    public static Future<Image> onImageLoaded(Future<Image> imageFuture, Consumer<Image> onImageSuccess, Consumer<Throwable> onError) {
        return imageFuture.inUiThread()
            .onSuccess(image -> onImageLoaded(image, onImageSuccess, onError))
            .onFailure(onError::accept);
    }

    public static Future<Image> onImageLoaded(Future<Image> imageFuture, MonoPane imageContainer, double width, double height, Supplier<Node> noImageNodeGetter) {
        Promise<Image> promise = Promise.promise();
        onImageLoaded(imageFuture, image -> {
            ImageView imageView;
            // Reusing the same image view if already present to prevent flashing transition between images
            if (imageContainer.getContent() instanceof ImageView iv)
                imageView = iv;
            else
                imageView = new ImageView();
            imageView.setImage(image);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            if (width < 0 || height < 0)
                imageView.setPreserveRatio(true);
            imageContainer.setContent(imageView);
            promise.tryComplete(image);
        }, error -> {
            if (noImageNodeGetter != null) {
                Node noImageNode = noImageNodeGetter.get();
                imageContainer.setBackground(Background.fill(Color.LIGHTGRAY));
                imageContainer.setContent(noImageNode);
            }
            promise.tryFail(error);
        });
        return promise.future().inUiThread();
    }
}
