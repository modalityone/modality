package one.modality.crm.server.authn.gateway.shared;

import dev.webfx.platform.util.Strings;

import java.util.Objects;

/**
 * @author Bruno Salmon
 */
public class ActivityHashUtil {

    private static final String HASH_PATH = "/#";

    public static String withHashPrefix(String path) {
        return path.startsWith(HASH_PATH) ? path : HASH_PATH + path;
    }

    public static String withoutHashPrefix(String path) {
        String p = Strings.removePrefix(path, HASH_PATH);
        return Objects.equals(p, path) ? p : withoutHashPrefix(p);
    }

    public static String withoutHashSuffix(String path) {
        String p = Strings.removeSuffix(path, HASH_PATH);
        return Objects.equals(p, path) ? p : withoutHashSuffix(p);
    }

}
