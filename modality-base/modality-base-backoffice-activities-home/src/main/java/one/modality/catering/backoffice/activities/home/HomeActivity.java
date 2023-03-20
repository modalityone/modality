package one.modality.catering.backoffice.activities.home;

import dev.webfx.extras.scalepane.ScalePane;
import dev.webfx.extras.theme.Facet;
import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.luminance.LuminanceTheme;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.webtext.HtmlText;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityBase;
import dev.webfx.stack.orm.domainmodel.activity.viewdomain.impl.ViewDomainActivityContextFinal;
import dev.webfx.stack.routing.router.auth.authz.RouteRequest;
import dev.webfx.stack.routing.uirouter.activity.uiroute.UiRouteActivityContextMixin;
import dev.webfx.stack.routing.uirouter.operations.RoutePushRequest;
import dev.webfx.stack.routing.uirouter.operations.RouteRequestEmitter;
import dev.webfx.stack.ui.action.Action;
import dev.webfx.stack.ui.action.ActionBinder;
import dev.webfx.stack.ui.action.ActionBuilder;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import dev.webfx.extras.util.layout.LayoutUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public class HomeActivity extends ViewDomainActivityBase
        implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
        ModalityButtonFactoryMixin,
        OperationActionFactoryMixin {

    private final static String[] sortedPossibleHomeRoutingOperations = {
            // New tiles (from wireframes)
            "RouteToKitchen", // routed
            "RouteToUserAccount", // temporarily redirected to RouteToUsers
            "RouteToReception", // temporarily redirected to RouteToFilters
            "RouteToTranslationAndRecordings", // temporarily redirected to RouteToLetters
            "RouteToCafe",
            "RouteToBookingsAndSearch", // temporarily redirected to RouteToBookings
            "RouteToAdmin", // temporarily redirected to RouteToAuthorizations
            "RouteToFinancesAndStats", // temporarily redirected to RouteToMoneyFlows
            "RouteToVolunteering",
            "RouteToShop",
            "RouteToMarketing",
            "RouteToRecurringClasses",
            "RouteToEventsPlanner", // temporarily redirected to RouteToEvents
            "RouteToHousehold",
            "RouteToQR",
            "RouteToTransportation",
            "RouteToOrganizations", // temporarily routed
            "RouteToHumanResources", // temporarily redirected to RouteToStatistics
            "RouteToAccommodation", // temporarily redirected to RouteToRoomsGraphic
            "RouteToExtras", // temporarily redirected to RouteToRoomsGraphic
            "---",
            // Old tiles (from prototype)
            "RouteToEvents", // Shown in RouteToEventsPlanner
            "RouteToBookings", // Shown in RouteToBookingsAndSearch
            "RouteToStatistics", // Shown in RouteToHumanResources
            "RouteToPayments",
            "RouteToStatements",
            "RouteToIncome",
            "RouteToLetters", // Shown in RouteToTranslationAndRecordings
            "RouteToRoomsGraphic", // Shown in RouteToAccommodation
            "RouteToDiningAreas",
            "RouteToMonitor",
            "RouteToTester",
            "RouteToUsers", // Shown in RouteToUserAccount
            "RouteToOperations", // Shown in RouteToExtras
            "RouteToAuthorizations", // Shown in RouteToAdmin
            "RouteToMoneyFlows", // Shown in RouteToFinancesAndStats
            "RouteToFilters", // Shown in RouteToReception
    };

    // Temporary redirects from new wireframes area to old prototype activities
    private final Map<String, String> redirects = new HashMap<>();
    {
        redirects.put("RouteToUserAccount", "RouteToUsers");
        redirects.put("RouteToEventsPlanner", "RouteToEvents");
        redirects.put("RouteToBookingsAndSearch", "RouteToBookings");
        redirects.put("RouteToAdmin", "RouteToAuthorizations");
        redirects.put("RouteToAccommodation", "RouteToRoomsGraphic");
        redirects.put("RouteToTranslationAndRecordings", "RouteToLetters");
        redirects.put("RouteToFinancesAndStats", "RouteToMoneyFlows");
        redirects.put("RouteToHumanResources", "RouteToStatistics");
        redirects.put("RouteToReception", "RouteToFilters");
        redirects.put("RouteToExtras", "RouteToOperations");
    }

    @Override
    public Node buildUi() {
        HomePane homePane = new HomePane();
        LuminanceTheme.createPrimaryPanelFacet(homePane).style();
        return ActionBinder.bindChildrenToVisibleActions(homePane, homeRoutingActions(), Tile::new);
    }

    protected Collection<Action> homeRoutingActions() {
        return Arrays.stream(sortedPossibleHomeRoutingOperations)
                .map(this::operationCodeToAction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Action operationCodeToAction(String operationCode) {
        RouteRequestEmitter routeRequestEmitter = findRouteRequestEmitter(operationCode);
        // Temporary code for main areas that don't have a route yet
        List<String> sortedPossibleHomeRoutingOperationsList = Arrays.asList(sortedPossibleHomeRoutingOperations);
        int operationIndex = sortedPossibleHomeRoutingOperationsList.indexOf(operationCode);
        int areaLastIndex = sortedPossibleHomeRoutingOperationsList.indexOf("---");
        if (operationIndex < areaLastIndex) {
            if (routeRequestEmitter == null) {
                String redirect = redirects.get(operationCode);
                Action redirectAction = redirect == null ? null : getRouteEmitterAction(findRouteRequestEmitter(redirect));
                return new ActionBuilder()
                        .setI18nKey(operationCode.substring(7))
                        .setActionHandler(redirectAction)
                        .build();
            } else
                return getRouteEmitterAction(routeRequestEmitter);
        } else
            return null;
    }

    private Action getRouteEmitterAction(RouteRequestEmitter routeRequestEmitter) {
        return newOperationAction(() -> {
            RouteRequest routeRequest = routeRequestEmitter.instantiateRouteRequest(this);
            if (routeRequest instanceof RoutePushRequest)
                ((RoutePushRequest) routeRequest).setReplace(true);
            return routeRequest;
        });
    }

    private RouteRequestEmitter findRouteRequestEmitter(String operationCode) {
        return RouteRequestEmitter.getProvidedEmitters().stream()
                .filter(instantiator -> hasRequestOperationCode(instantiator.instantiateRouteRequest(this), operationCode))
                .findFirst().orElse(null);
    }

    private static boolean hasRequestOperationCode(Object request, Object operationCode) {
        return request instanceof HasOperationCode && operationCode.equals(((HasOperationCode) request).getOperationCode());
    }

    static class HomePane extends Pane {

        protected void layoutChildren() {
            double width = getWidth(), height = getHeight();
            double hMargins = width * 0.08, vMargins = height * 0.155;
            width -= 2 * hMargins;
            height -= 2 * vMargins;
            int n = getChildren().size();
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
            double wp = (width ) / p;
            double hp = (height ) / q;
            ObservableList<Node> children = getChildren();
            for (int i = 0; i < n; i++) {
                Node child = children.get(i);
                int col = i % p, row = i / p;
                layoutInArea(child, hMargins + col * wp, vMargins + row * hp, wp, hp, 0, margin, HPos.LEFT, VPos.TOP);
            }
        }
    }

    static class Tile extends Pane {
        private final Action action;
        private final HtmlText htmlText = new HtmlText();
        private double fontSize;
        private Node graphic;
        private ScalePane scaledGraphic;
        private final Facet luminanceFacet, textFacet;

        private final Pane clippedTextGraphicPane = new Pane() {
            private final Rectangle clip = new Rectangle();
            {
                setClip(clip);
                setCursor(Cursor.HAND);
            }
            @Override
            protected void layoutChildren() {
                double width = getWidth(), height = getHeight(), h2 = height / 2, h4 = h2 / 2, h8 = h4 / 2;
                if (scaledGraphic != null)
                    layoutInArea(scaledGraphic, 0, h4, width, h4, 0, HPos.CENTER, VPos.CENTER);
                setFontSize(Math.min(width * 0.125, h4 * 0.55));
                layoutInArea(htmlText, 0, h2 + h8, width, h4, 0, HPos.CENTER, VPos.CENTER);
                clip.setWidth(width);
                clip.setHeight(height);
            }
        };

        public Tile(Action action) {
            this.action = action;
            luminanceFacet = LuminanceTheme.createSecondaryPanelFacet(this)
                    .setShadowed(true)
                    .style();
            textFacet = TextTheme.createPrimaryTextFacet(htmlText)
                    .setFillProperty(htmlText.fillProperty())
                    .setFontProperty(htmlText.fontProperty())
                    .style();
            LayoutUtil.setMaxWidthToInfinite(this);
            LayoutUtil.setMaxHeightToInfinite(this);
            setOnMouseClicked(e -> action.handle(new ActionEvent()));
            setOnMouseEntered(e -> onHover(true));
            setOnMouseExited(e -> onHover(false));
            FXProperties.runNowAndOnPropertiesChange(this::onActionPropertiesChanged, action.textProperty(), action.graphicProperty());
        }

        private void setFontSize(double fontSize) {
            if (this.fontSize != fontSize) {
                this.fontSize = fontSize;
                textFacet.requestedFont(FontDef.font(fontSize));
            }
        }

        private void onHover(boolean hover) {
            luminanceFacet.setInverted(hover);
            textFacet.setInverted(hover);
        }

        private void onActionPropertiesChanged() {
            htmlText.setText("<center style='line-height: 1em'>" + action.getText() + "</center>");
            Node newGraphic = action.getGraphic();
            if (graphic != newGraphic) {
                graphic = action.getGraphic();
                scaledGraphic = graphic == null ? null : new ScalePane(graphic);
            }
            if (scaledGraphic == null)
                clippedTextGraphicPane.getChildren().setAll(htmlText);
            else
                clippedTextGraphicPane.getChildren().setAll(htmlText, scaledGraphic);
            getChildren().setAll(clippedTextGraphicPane);
            textFacet.setGraphicNode(graphic);
        }

        @Override
        public void layoutChildren() {
            double width = getWidth(), height = getHeight();
            layoutInArea(clippedTextGraphicPane, 0, 0, width, height, 0, HPos.LEFT, VPos.TOP);
        }
    }

}
