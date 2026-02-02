package one.modality.base.backoffice.activities.home;

import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.conf.Config;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import javafx.scene.Node;
import one.modality.base.backoffice.homemenu.HomeMenu;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

/**
 * @author Bruno Salmon
 */
final class BackOfficeHomeActivity extends ViewDomainActivityBase
    implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
    ModalityButtonFactoryMixin,
    OperationActionFactoryMixin {

    private final static Config HOME_CONFIG = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.base.backoffice.home");

    @Override
    public Node buildUi() {
        HomeMenu homeMenu = new HomeMenu(this, this);
        FXProperties.runNowAndOnPropertyChange(organization -> {
            if (organization != null) {
                String organizationTypeCode = organization.evaluate("type.code");
                String homeOperations = organizationTypeCode == null ? null : HOME_CONFIG.getString(organizationTypeCode.toLowerCase() + "HomeOperations");
                if (homeOperations == null)
                    homeOperations = HOME_CONFIG.getString("defaultHomeOperations");
                homeMenu.setHomeOperations(homeOperations);
            }
        }, FXOrganization.organizationProperty());
        return homeMenu.getHomePane();
    }

}
