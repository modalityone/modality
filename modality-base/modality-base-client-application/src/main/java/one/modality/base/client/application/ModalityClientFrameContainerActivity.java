package one.modality.base.client.application;

import dev.webfx.extras.materialdesign.util.background.BackgroundUtil;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitter;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.action.ActionBuilder;
import dev.webfx.stack.ui.action.ActionGroup;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Bruno Salmon
 */
public class ModalityClientFrameContainerActivity extends ViewDomainActivityBase
        implements ModalityButtonFactoryMixin
        , OperationActionFactoryMixin {

    private final BorderPane borderPane = new BorderPane();

    @Override
    public Node buildUi() {
        FlowPane topPane = new FlowPane(ActionBinder.bindButtonToAction(newButton(), operationCodeToAction("RouteToHome")), ActionBinder.bindButtonToAction(newButton(), operationCodeToAction("RouteBackward")), ActionBinder.bindButtonToAction(newButton(), operationCodeToAction("RouteForward")));
        topPane.setBackground(BackgroundUtil.newBackground(Color.web("#D9D9D9")));
        borderPane.setTop(topPane);
        setUpContextMenu(topPane, this::contextMenuActionGroup);
        borderPane.centerProperty().bind(mountNodeProperty());
        return borderPane;
    }

    private final Collection<RouteRequestEmitter> providedEmitters = RouteRequestEmitter.getProvidedEmitters();
    private final Action invisibleVoidAction = new ActionBuilder().setVisibleProperty(new SimpleBooleanProperty(false)).build();

    private Action operationCodeToAction(String operationCode) {
        Optional<RouteRequestEmitter> routeRequestEmitter = providedEmitters.stream()
                .filter(instantiator -> hasRequestOperationCode(instantiator.instantiateRouteRequest(this), operationCode))
                .findFirst();
        return routeRequestEmitter.isEmpty() ? invisibleVoidAction : newOperationAction(() -> routeRequestEmitter.get().instantiateRouteRequest(this));
    }

    protected ActionGroup contextMenuActionGroup() {
        return newActionGroup(
                ChangeLanguageRequestEmitter.getProvidedEmitters().stream()
                        .map(instantiator -> newOperationAction(instantiator::emitLanguageRequest))
                        .toArray(Action[]::new)
        );
    }

    private static boolean hasRequestOperationCode(Object request, Object operationCode) {
        return request instanceof HasOperationCode && operationCode.equals(((HasOperationCode) request).getOperationCode());
    }
}
