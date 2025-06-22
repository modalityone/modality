package one.modality.ecommerce.payment.server.gateway.impl.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
public final class RestApiOneTimeHtmlResponsesCache {

    private final static Map<String, String> ONE_TIME_HTML_RESPONSES = new HashMap<>();

    public static void registerOneTimeHtmlResponse(String key, String value) {
        ONE_TIME_HTML_RESPONSES.put(key, value);
    }

    public static String getOneTimeHtmlResponse(String key) {
        return ONE_TIME_HTML_RESPONSES.remove(key); // Removed from cache when requested (one-time cache)
    }
}
