package one.modality.base.shared.entities;

/**
 * @author Bruno Salmon
 */
public enum ItemFamilyType {
    TEACHING("teach"),
    TRANSLATION("transl"),
    LIVESTREAM("livestream"),
    VIDEO_ON_DEMAND("vod"),
    ACCOMMODATION("acco"),
    MEALS("meals"),
    DIET("diet"),
    PARKING("park"),
    TRANSPORT("transp"),
    TAX("tax"),
    UNKNOWN(null);

    private final String code;

    ItemFamilyType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ItemFamilyType fromCode(String code) {
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
            }
        }
        return UNKNOWN;
    }
}
