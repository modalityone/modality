package one.modality.base.client.cloudinary;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.blob.Blob;
import dev.webfx.stack.cloud.image.CloudImageService;
import dev.webfx.stack.cloud.image.impl.client.ClientImageService;


public final class ModalityCloudinary {

    public ModalityCloudinary(ModalityCloudinary.CloudinaryPrefix prefix) {
        this.prefix = prefix;
    }

    private final CloudinaryPrefix prefix;
    private final CloudImageService cloudImageService = new ClientImageService();
    private String language;

    public enum CloudinaryPrefix {
        RECURRING_EVENT,
        AUDIO_COVER,
        LIVESTREAM_COVER
    }

    public Future<Void> deleteCloudPicture(int key) {
        String pictureId = computeCloudinaryId(key);
        return cloudImageService.delete(pictureId, true);
    }

    public Future<Void> uploadCloudPicture(int key, Blob fileToUpload) {
            String pictureId = computeCloudinaryId(key);
            return cloudImageService.upload(fileToUpload, pictureId, true);
    }

    public javafx.scene.image.Image getImage(int eventId, int width, int height) {
        String url =  cloudImageService.url(computeCloudinaryId(eventId), width, height);
        return new javafx.scene.image.Image(url, true);
    }

    public Future<Boolean> doesCloudPictureExist(int key) {
        String pictureId = computeCloudinaryId(key);
        return cloudImageService.exists(pictureId);
    }

    private String computeCloudinaryId(Object id) {
        return switch (prefix) {
            case AUDIO_COVER, LIVESTREAM_COVER -> getEventCoverImageTag(id, language);
            case RECURRING_EVENT -> getEventImageTag(id);
        };
    }

    public static String getPersonImageTag(Object personId) {
        return "persons/person-" + personId;
    }

    public static String getEventImageTag(Object eventId) {
        return "events/event-" + eventId;
    }

    public static String getEventCoverImageTag(Object eventId, Object I18nLanguage) {
        if (I18nLanguage == null || "en".equals(I18nLanguage.toString())) {
            return "events-cover/event-" + eventId + "-cover";
        } else return "events-cover/event-" + eventId + "-cover-" + I18nLanguage;
    }
    public void setLanguage(String code) {
        language = code;
    }
}
