package one.modality.base.shared.entities;

/**
 * @author Bruno Salmon
 */
public enum MediaType {
    AUDIO,
    VOD,
    LIVESTREAM;

    public static MediaType of(String type) {
        return type == null ? null : valueOf(type);
    }
}
