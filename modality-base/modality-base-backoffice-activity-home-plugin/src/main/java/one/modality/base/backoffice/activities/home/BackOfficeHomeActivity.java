package one.modality.base.backoffice.activities.home;

import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.Unregisterable;
import dev.webfx.platform.conf.Config;
import dev.webfx.platform.conf.SourcesConfig;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.action.ActionBuilder;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;
import one.modality.base.client.application.RoutingActions;
import one.modality.base.client.tile.Tile;
import one.modality.crm.backoffice.organization.fx.FXOrganization;

import java.util.Collection;

/**
 * @author Bruno Salmon
 */
final class BackOfficeHomeActivity extends ViewDomainActivityBase
    implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
    ModalityButtonFactoryMixin,
    OperationActionFactoryMixin {

    private final static Config HOME_CONFIG = SourcesConfig.getSourcesRootConfig().childConfigAt("modality.base.backoffice.home");

    private Unregisterable homeTilesBinding;

    @Override
    public Node buildUi() {
        HomePane homePane = new HomePane();
        LuminanceTheme.createPrimaryPanelFacet(homePane).style();
        FXProperties.runNowAndOnPropertyChange(organization -> {
            if (organization != null) {
                String organizationTypeCode = organization.evaluate("type.code");
                String homeOperations = organizationTypeCode == null ? null : HOME_CONFIG.getString(organizationTypeCode.toLowerCase() + "HomeOperations");
                if (homeOperations == null)
                    homeOperations = HOME_CONFIG.getString("defaultHomeOperations");
                Collection<Action> homeActions = RoutingActions.filterRoutingActions(this::operationCodeToAction, homeOperations.split(","));
                if (homeTilesBinding != null)
                    homeTilesBinding.unregister();
                homeTilesBinding = ActionBinder.bindChildrenToVisibleActions(homePane, homeActions, this::createHomeTile);
            }
        }, FXOrganization.organizationProperty());
        return homePane;
    }

    private Tile createHomeTile(Action action) {
        return new Tile(action)
            .setAdaptativeFontSize(true)
            .setShadowed(true);
    }

    private Action operationCodeToAction(String operationCode) {
        RouteRequestEmitter routeRequestEmitter = RoutingActions.findRouteRequestEmitterWithOperationCode(operationCode, this);
        if (routeRequestEmitter == null) { // Building a gray tile for operation not yet implemented in Modality
            return new ActionBuilder()
                    .setI18nKey(Strings.removePrefix(operationCode, "RouteTo"))
                    .setDisabledProperty(new SimpleBooleanProperty(true))
                    .setHiddenWhenDisabled(false)
                    .build();
        }
        return RoutingActions.getRouteEmitterAction(routeRequestEmitter, this, this);
    }

    static class HomePane extends Pane {

        @Override
        protected void layoutChildren() {
            ObservableList<Node> children = getChildren();
            int n = getChildren().size();
            if (n == 0)
                return;
            double width = getWidth(), height = getHeight();
            double hMargins = width * 0.08, vMargins = height * 0.155;
            width -= 2 * hMargins;
            height -= 2 * vMargins;
            int q = (int) Math.sqrt(n);
            int p = n / q;
            if (p * q < n) {
                if (width > height)
                    p++;
                else
                    q++;
            }
            double gap = 0.01 * width;
            Insets margin = new Insets(0, gap, gap, 0);
            double wp = (width) / p;
            double hp = (height) / q;
            for (int i = 0; i < n; i++) {
                Node child = children.get(i);
                int col = i % p, row = i / p;
                layoutInArea(child, hMargins + col * wp, vMargins + row * hp, wp, hp, 0, margin, HPos.LEFT, VPos.TOP);
            }
        }
    }

}
