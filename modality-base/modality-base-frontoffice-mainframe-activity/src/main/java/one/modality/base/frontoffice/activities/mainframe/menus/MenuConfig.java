package one.modality.base.frontoffice.activities.mainframe.menus;

import dev.webfx.extras.aria.AriaToggleGroup;
import dev.webfx.kit.util.aria.AriaRole;
import dev.webfx.platform.conf.Config;
import dev.webfx.platform.conf.SourcesConfig;

/**
 * @author Bruno Salmon
 */
public final class MenuConfig {

    static final double WEB_MAIN_MENU_HEIGHT = 100;
    static final double WEB_USER_MENU_HEIGHT = 52;

    public static final double LANG_MENU_HEIGHT = 52;
    public static final double LANG_BAR_MENU_HEIGHT = 29;

    private static final Config FRONT_OFFICE_CONFIG = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.base.frontoffice.application");
    public static final String[] USER_MENU_OPERATION_CODES = FRONT_OFFICE_CONFIG.getString("userMenuOperationCodes").split(",");
    public static final String[] MAIN_MENU_OPERATION_CODES = FRONT_OFFICE_CONFIG.getString("mainMenuOperationCodes").split(",");
    public static final String[] LANGUAGES = FRONT_OFFICE_CONFIG.getString("languages").split(",");

    public static final AriaToggleGroup<Integer> mainAndUserMenuItemGroup = new AriaToggleGroup<>(AriaRole.MENUITEM);
    public static final AriaToggleGroup<Integer> mobileMenuItemGroup = new AriaToggleGroup<>(AriaRole.MENUITEM);
}
