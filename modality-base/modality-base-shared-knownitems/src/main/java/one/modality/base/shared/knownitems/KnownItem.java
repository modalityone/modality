package one.modality.base.shared.knownitems;

/**
 * @author Bruno Salmon
 */
public enum KnownItem {
    VIDEO("video"),
    AUDIO_RECORDING_ENGLISH("audio-en"),
    PROGRAM_SESSION("program-session"),
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
                case "audio-en" : return AUDIO_RECORDING_ENGLISH;
                case "program-session" : return PROGRAM_SESSION;
            }
        }
        return UNKNOWN;
    }
}
