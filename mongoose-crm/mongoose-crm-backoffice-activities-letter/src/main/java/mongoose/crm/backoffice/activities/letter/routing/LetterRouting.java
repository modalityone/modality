package mongoose.crm.backoffice.activities.letter.routing;

import mongoose.base.client.util.routing.MongooseRoutingUtil;

/**
 * @author Bruno Salmon
 */
public final class LetterRouting {

    private final static String PATH = "/letter/:letterId";

    public static String getPath() {
        return PATH;
    }

    public static String getEditLetterPath(Object letterId) {
        return MongooseRoutingUtil.interpolateLetterIdInPath(letterId, PATH);
    }

}
