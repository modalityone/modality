package one.modality.base.client.conf;

import dev.webfx.platform.conf.Config;
import dev.webfx.platform.conf.ConfigLoader;

/**
 * @author Bruno Salmon
 */
public final class ModalityClientConfig {

    private final static String CONFIG_PATH = "modality.base.client.conf";

    private final static Config CONFIG = ConfigLoader.getRootConfig().childConfigAt(CONFIG_PATH);

    private ModalityClientConfig() {}

    public static boolean isBackOffice() {
        return CONFIG.getBoolean("backoffice", false);
    }

    public static boolean isFrontOffice() {
        return CONFIG.getBoolean("frontoffice", false);
    }

}
