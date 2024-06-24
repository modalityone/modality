package one.modality.ecommerce.payment.server.gateway.impl.square;

import dev.webfx.platform.console.Console;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
final class SquareRestApiOneTimeHtmlResponsesCache {

    private final static Map<String, String> ONE_TIME_HTML_RESPONSES = new HashMap<>();

    static void registerOneTimeHtmlResponse(String key, String value) {
        Console.log("Caching one time html response for key=" + key + ", value=" + value.substring(0, 10) + "..., Map instance=HashMap@" + System.identityHashCode(ONE_TIME_HTML_RESPONSES));
        ONE_TIME_HTML_RESPONSES.put(key, value);
    }

    static String getOneTimeHtmlResponse(String key) {
        String value = ONE_TIME_HTML_RESPONSES.get(key); // Not remove() because AWS is asking several times for some reason
        Console.log("Retrieving one time html response from key=" + key + ", Map instance=HashMap@" + System.identityHashCode(ONE_TIME_HTML_RESPONSES) +", value=" + value);
        return value;
    }
}
