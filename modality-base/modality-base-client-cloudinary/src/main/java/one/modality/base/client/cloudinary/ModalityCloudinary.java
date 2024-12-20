package one.modality.base.client.cloudinary;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.file.File;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;


public final class ModalityCloudinary {

    public ModalityCloudinary(ModalityCloudinary.CloudinaryPrefix prefix) {
        this.prefix = prefix;
    }

    private CloudinaryPrefix prefix;
    private final CloudImageService cloudImageService = new ClientImageService();
    private String language;

    private final int cloudinaryKey = 0;

    public enum CloudinaryPrefix {
        RECURRING_EVENT,
        AUDIO_COVER,
        LIVESTREAM_COVER
    }

    public Future<Void> deleteCloudPicture(int key) {
        String pictureId = computeCloudinaryId(key);
        return cloudImageService.delete(pictureId, true);
    }

    public Future<Void> uploadCloudPicture(int key, File fileToUpload) {
            String pictureId = computeCloudinaryId(key);
            return cloudImageService.upload(fileToUpload, pictureId, true);
    }

    public javafx.scene.image.Image getImage(int eventId, int size, int i) {
        String url =  cloudImageService.url(computeCloudinaryId(eventId), size, -1) + ".jpg";
        return new javafx.scene.image.Image(url, true);
    }

    public Future<Boolean> doesCloudPictureExist(int key) {
        String pictureId = computeCloudinaryId(key);
        return cloudImageService.exists(pictureId);
    }

    private String computeCloudinaryId(int key) {
        String toReturn = "";
        switch (prefix) {
            case AUDIO_COVER:
                if(language.equals("en"))
                    return  key + "-cover";
                else
                    return key + "-cover-" + language;
            case RECURRING_EVENT:
                return String.valueOf(key);
            case LIVESTREAM_COVER:
                return key + "-cover";
        }
        return toReturn;
    }

    public void setLanguage(String code) {
        language = code;
    }


}
