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
        ImageView imageView = new ImageView();
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        if (width < 0 || height < 0)
            imageView.setPreserveRatio(true);
        Promise<Image> promise = Promise.promise();
        onImageLoaded(imageFuture, image -> {
            imageView.setImage(image);
            imageContainer.setContent(imageView);
            promise.complete(image);
        }, error -> {
            if (noImageNodeGetter != null) {
                Node noImageNode = noImageNodeGetter.get();
                imageContainer.setBackground(Background.fill(Color.LIGHTGRAY));
                imageContainer.setContent(noImageNode);
            }
            promise.fail(error);
        });
        return promise.future().inUiThread();
    }
}
