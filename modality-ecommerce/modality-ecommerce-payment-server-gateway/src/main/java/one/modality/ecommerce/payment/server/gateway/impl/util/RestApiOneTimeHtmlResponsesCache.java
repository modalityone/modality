package one.modality.ecommerce.payment.server.gateway.impl.util;

import dev.webfx.platform.console.Console;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
public final class RestApiOneTimeHtmlResponsesCache {

    private final static Map<String, String> ONE_TIME_HTML_RESPONSES = new HashMap<>();

    public static void registerOneTimeHtmlResponse(String key, String value) {
        Console.log("Payment cache set with key: " + key);
        ONE_TIME_HTML_RESPONSES.put(key, value);
    }

    public static String getOneTimeHtmlResponse(String key) {
        String result = ONE_TIME_HTML_RESPONSES.remove(key);
        Console.log("Payment cache requested with key: " + key + " -> " + (result != null ? "found" : "not found"));
        return result; // Removed from the cache when requested (one-time cache)
    }
}
