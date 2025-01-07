package one.modality.base.shared.entities;

/**
 * @author Bruno Salmon
 */
public enum KnownItemFamily {
    // Note: primary keys are temporarily hardcoded (should be configurable)
    TEACHING("teach", 3),
    TRANSLATION("transl", 8),
    VIDEO("video", 34),
    ACCOMMODATION("acco", 1),
    MEALS("meals", 2),
    DIET("diet", 14),
    PARKING("park", 15),
    TRANSPORT("transp", 12),
    TAX("tax", 26),
    AUDIO_RECORDING("record", 20),
    UNKNOWN(null, -1);

    private final String code;
    private final int primaryKey;

    KnownItemFamily(String code, int primaryKey) {
        this.code = code;
        this.primaryKey = primaryKey;
    }

    public String getCode() {
        return code;
    }

    public int getPrimaryKey() {
        return primaryKey;
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
