package one.modality.base.shared.entities;

/**
 * @author Bruno Salmon
 */
public enum KnownItemFamily {
    TEACHING("teach"),
    TRANSLATION("transl"),
    VIDEO("video"),
    ACCOMMODATION("acco"),
    MEALS("meals"),
    DIET("diet"),
    PARKING("park"),
    TRANSPORT("transp"),
    TAX("tax"),
    AUDIO_RECORDING("record"),
    UNKNOWN(null);

    private final String code;

    KnownItemFamily(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static KnownItemFamily fromCode(String code) {
        if (code != null) {
            switch (code) {
                case "teach" : return TEACHING;
                case "transl" : return TRANSLATION;
                case "acco" : return ACCOMMODATION;
                case "meals" : return MEALS;
                case "diet" : return DIET;
                case "park" : return PARKING;
                case "transp" : return TRANSPORT;
                case "tax" : return TAX;
                case "record" : return AUDIO_RECORDING;
            }
        }
        return UNKNOWN;
    }
}
