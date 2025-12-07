package one.modality.base.backoffice.homemenu;

import dev.webfx.extras.action.Action;
import dev.webfx.extras.action.ActionBinder;
import dev.webfx.extras.action.ActionBuilder;
import dev.webfx.extras.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.panes.MonoPane;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.util.control.Controls;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.kit.util.properties.Unregisterable;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContext;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import one.modality.base.client.application.RoutingActions;
import one.modality.base.client.tile.Tile;

import java.util.Collection;

/**
 * @author Bruno Salmon
 */
public final class HomeMenu {

    private final UiRouteActivityContext<?> context;
    private final OperationActionFactoryMixin mixin;

    private final HomePane homePane = new HomePane();

    private Unregisterable homeTilesBinding;

    public HomeMenu(UiRouteActivityContext<?> context, OperationActionFactoryMixin mixin) {
        this(null, context, mixin);
    }

    public HomeMenu(String homeOperations, UiRouteActivityContext<?> context, OperationActionFactoryMixin mixin) {
        this.context = context;
        this.mixin = mixin;
        LuminanceTheme.createPrimaryPanelFacet(homePane).style();
        if (homeOperations != null)
            setHomeOperations(homeOperations);
    }

    public Region getHomePane() {
        if (!homePane.getChildrenUnmodifiable().isEmpty())
            return homePane;
        // Displaying a spinner while the home pane is empty
        MonoPane monoPane = new MonoPane(Controls.createSpinner(80));
        ObservableLists.runOnListChange(() -> monoPane.setContent(homePane), homePane.getChildrenUnmodifiable());
        return monoPane;
    }

    public void setHomeOperations(String homeOperations) {
        Collection<Action> homeActions = RoutingActions.filterRoutingActions(this::operationCodeToAction, homeOperations.split(","));
        if (homeTilesBinding != null)
            homeTilesBinding.unregister();
        homeTilesBinding = ActionBinder.bindChildrenToVisibleActions(homePane, homeActions, HomeMenu::createHomeTile);
    }

    private static Tile createHomeTile(Action action) {
        return new Tile(action)
            .setAdaptativeFontSize(true)
            .setShadowed(true);
    }

    private Action operationCodeToAction(String operationCode) {
        RouteRequestEmitter routeRequestEmitter = RoutingActions.findRouteRequestEmitterWithOperationCode(operationCode, context);
        if (routeRequestEmitter == null) { // Building a gray tile for operation not yet implemented in Modality
            return new ActionBuilder()
                .setI18nKey(Strings.removePrefix(operationCode, "RouteTo") + "Menu")
                .setDisabledProperty(new SimpleBooleanProperty(true))
                .setHiddenWhenDisabled(false)
                .build();
        }
        return RoutingActions.getRouteEmitterAction(routeRequestEmitter, context, mixin);
    }

    static final class HomePane extends Pane {

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
