package one.modality.base.backoffice.activities.globalsetup.home;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.platform.conf.Config;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import javafx.scene.Node;
import one.modality.base.backoffice.homemenu.HomeMenu;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;

/**
 * @author Bruno Salmon
 */
final class GlobalSetupHomeActivity extends ViewDomainActivityBase
    implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
    ModalityButtonFactoryMixin,
    OperationActionFactoryMixin {

    private final static Config HOME_CONFIG = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.base.backoffice.globalsetup.home");
    private final static String HOME_OPERATIONS = HOME_CONFIG.getString("homeOperations");

    @Override
    public Node buildUi() {
        return new HomeMenu(HOME_OPERATIONS, this, this).getHomePane();
    }

}
