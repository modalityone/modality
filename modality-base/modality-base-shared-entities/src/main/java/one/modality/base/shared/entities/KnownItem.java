package one.modality.base.shared.entities;

/**
 * @author Bruno Salmon
 */
public enum KnownItem {
    VIDEO("video"),
    AUDIO_RECORDING_ENGLISH("audio-en"),
    UNKNOWN(null);

    private final String code;

    KnownItem(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static KnownItem fromCode(String code) {
        if (code != null) {
            switch (code) {
                case "video" : return VIDEO;
                case "audio-en" : return VIDEO;
            }
        }
        return UNKNOWN;
    }
}
