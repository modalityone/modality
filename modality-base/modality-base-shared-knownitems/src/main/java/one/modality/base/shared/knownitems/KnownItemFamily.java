package one.modality.base.shared.knownitems;

/**
 * @author Bruno Salmon
 */
public enum KnownItemFamily {
    // Note: primary keys are temporarily hardcoded (should be configurable)
    // TODO: complete the i18n keys
    TEACHING("teach", 3, KnownItemI18nKeys.Teachings),
    TRANSLATION("transl", 8, null),
    VIDEO("video", 29, null),
    ACCOMMODATION("acco", 1, null),
    MEALS("meals", 2, null),
    DIET("diet", 14, null),
    PARKING("park", 15, null),
    TRANSPORT("transp", 12, null),
    TAX("tax", 26, null),
    AUDIO_RECORDING("record", 20, KnownItemI18nKeys.AudioRecordings),
    CEREMONY("cerem", 16, null),
    UNKNOWN(null, -1, null);

    private final String code;
    private final int primaryKey;
    private final Object i18nKey;

    KnownItemFamily(String code, int primaryKey, Object i18nKey) {
        this.code = code;
        this.primaryKey = primaryKey;
        this.i18nKey = i18nKey;
    }

    public String getCode() {
        return code;
    }

    public int getPrimaryKey() {
        return primaryKey;
    }

    public Object getI18nKey() {
        return i18nKey;
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
