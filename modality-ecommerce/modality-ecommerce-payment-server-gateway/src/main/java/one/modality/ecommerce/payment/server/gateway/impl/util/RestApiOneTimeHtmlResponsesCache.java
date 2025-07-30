package one.modality.ecommerce.payment.server.gateway.impl.util;

import dev.webfx.platform.console.Console;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Bruno Salmon
 */
public final class RestApiOneTimeHtmlResponsesCache {

    private static final Path PAYMENT_CACHE_DIRECTORY_PATH = Paths.get(System.getProperty("user.home"), ".modality", "payment-cache");
    static {
        PAYMENT_CACHE_DIRECTORY_PATH.toFile().mkdirs();
    }

    private final static Map<String, String> ONE_TIME_HTML_RESPONSES = new HashMap<>();

    public static void registerOneTimeHtmlResponse(String key, String value) {
        Console.log("[PAYMENT] cache set with key: " + key);
        ONE_TIME_HTML_RESPONSES.put(key, value);
        try {
            Path paymentCacheFile = Files.createFile(PAYMENT_CACHE_DIRECTORY_PATH.resolve(key));
            BufferedWriter writer = Files.newBufferedWriter(paymentCacheFile, StandardCharsets.UTF_8);
            writer.write(value);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            Console.log("[PAYMENT] Error while writing cache key: " + key + ": " + e.getMessage());
        }
    }

    public static String getOneTimeHtmlResponse(String key) {
        String result = ONE_TIME_HTML_RESPONSES.remove(key);
        try {
            Path paymentCacheFile = PAYMENT_CACHE_DIRECTORY_PATH.resolve(key);
            if (result == null)
                result = new String(Files.readAllBytes(paymentCacheFile));
            Files.delete(paymentCacheFile);
        } catch (Exception e) {
            Console.log("[PAYMENT] Error while reading cache key: " + key + ": " + e.getMessage());
        }
        Console.log("Payment cache requested with key: " + key + " -> " + (result != null ? "found" : "not found"));
        return result; // Removed from the cache when requested (one-time cache)
    }
}
