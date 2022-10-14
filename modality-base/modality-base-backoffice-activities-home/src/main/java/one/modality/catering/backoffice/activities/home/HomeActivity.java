package one.modality.catering.backoffice.activities.home;

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
import dev.webfx.stack.ui.util.layout.LayoutUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import one.modality.base.client.activity.ModalityButtonFactoryMixin;

import java.util.*;
import java.util.stream.Collectors;
//webfx import javafx.scene.control.Control

/**
 * @author Bruno Salmon
 */
public class HomeActivity extends ViewDomainActivityBase
        implements UiRouteActivityContextMixin<ViewDomainActivityContextFinal>,
        ModalityButtonFactoryMixin,
        OperationActionFactoryMixin {

    private final static String[] sortedPossibleHomeRoutingOperations = {
            // New tiles (from wireframes)
            "RouteToKitchen",
            "RouteToUserAccount",
            "RouteToReception",
            "RouteToTranslationAndRecordings",
            "RouteToCafe",
            "RouteToBookingsAndSearch",
            "RouteToAdmin",
            "RouteToFinancesAndStats",
            "RouteToVolunteering",
            "RouteToShop",
            "RouteToMarketing",
            "RouteToRecurringClasses",
            "RouteToEventsPlanner",
            "RouteToHousehold",
            "RouteToQR",
            "RouteToTransportation",
            "RouteToOrganizations", // routed
            "RouteToHumanResources",
            "RouteToAccommodation",
            "RouteToExtras",
            "---",
            // Old tiles (from prototype)
            "RouteToEvents",
            "RouteToBookings",
            "RouteToStatistics",
            "RouteToPayments",
            "RouteToStatements",
            "RouteToIncome",
            "RouteToLetters",
            "RouteToRoomsGraphic",
            "RouteToDiningAreas",
            "RouteToMonitor",
            "RouteToTester",
            "RouteToUsers",
            "RouteToOperations",
            "RouteToAuthorizations",
            "RouteToMoneyFlows",
            "RouteToFilters",
    };

    @Override
    public Node buildUi() {
        return ActionBinder.bindChildrenToVisibleActions(new HomePane(), homeRoutingActions(), Tile::new);
    }

    protected Collection<Action> homeRoutingActions() {
        return Arrays.stream(sortedPossibleHomeRoutingOperations)
                .map(this::operationCodeToAction)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private final Collection<RouteRequestEmitter> providedEmitters = RouteRequestEmitter.getProvidedEmitters();
    private Action operationCodeToAction(String operationCode) {
        Optional<RouteRequestEmitter> routeRequestEmitter = providedEmitters.stream()
                .filter(instantiator -> hasRequestOperationCode(instantiator.instantiateRouteRequest(this), operationCode))
                .findFirst();
        // Temporary code for main areas that don't have a route yet
        List<String> sortedPossibleHomeRoutingOperationsList = Arrays.asList(sortedPossibleHomeRoutingOperations);
        int operationIndex = sortedPossibleHomeRoutingOperationsList.indexOf(operationCode);
        int areaLastIndex = sortedPossibleHomeRoutingOperationsList.indexOf("---");
        if (operationIndex < areaLastIndex) {
            if (routeRequestEmitter.isEmpty())
                return new ActionBuilder().setI18nKey(operationCode.substring(7)).build();
        } else
            return null;
        return routeRequestEmitter.isEmpty() ? null : newOperationAction(() -> {
            RouteRequest routeRequest = routeRequestEmitter.get().instantiateRouteRequest(this);
            if (routeRequest instanceof RoutePushRequest)
                ((RoutePushRequest) routeRequest).setReplace(true);
            return routeRequest;
        });
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

    class Tile extends Pane {
        private final Action action;
        private final HtmlText htmlText = new HtmlText();
        private double fontSize;
        private Node graphic;
        private ScalePane scaledGraphic;
        private boolean hover;
        private Paint textFill = Color.web("#0096D6");
        private Paint graphicFill = textFill;
        private Paint graphicStroke = null;
        private final Rectangle shadowRectangle = new Rectangle(0, 0);
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
                setFontSize(Math.min(width * 0.125, h4 * 0.6));
                layoutInArea(htmlText, 0, h2 + h8, width, h4, 0, HPos.CENTER, VPos.CENTER);
                clip.setWidth(width);
                clip.setHeight(height);
            }
        };

        public Tile(Action action) {
            this.action = action;
            LayoutUtil.setMaxWidthToInfinite(this);
            LayoutUtil.setMaxHeightToInfinite(this);
            setOnMouseClicked(e -> action.handle(new ActionEvent()));
            onHover(false);
            setOnMouseEntered(e -> onHover(true));
            setOnMouseExited(e -> onHover(false));
            shadowRectangle.setEffect(new DropShadow(10, 5, 5, Color.LIGHTGRAY));
            FXProperties.runNowAndOnPropertiesChange(this::onActionPropertiesChanged, action.textProperty(), action.graphicProperty());
        }

        private void setFontSize(double fontSize) {
            if (this.fontSize != fontSize) {
                this.fontSize = fontSize;
                htmlText.setFont(Font.font("Helvetica", FontWeight.NORMAL, fontSize));
            }
        }

        private void onHover(boolean hover) {
            this.hover = hover;
            updateColors();
        }

        private void updateColors() {
            Color white = Color.WHITE;
            Paint textColor = hover ? white : textFill;
            Paint backgroundColor = hover ? textFill : white;
            htmlText.setFill(textColor);
            shadowRectangle.setFill(backgroundColor);
            if (graphic instanceof SVGPath) {
                SVGPath svgPath = (SVGPath) graphic;
                if (graphicFill != null)
                    svgPath.setFill(hover ? white : graphicFill);
                if (graphicStroke != null)
                    svgPath.setStroke(hover ? white : graphicStroke);
            }
        }

        private void onActionPropertiesChanged() {
            htmlText.setText("<center style='line-height: 1em'>" + action.getText() + "</center>");
            Node newGraphic = action.getGraphic();
            if (graphic != newGraphic) {
                graphic = action.getGraphic();
                if (graphic instanceof SVGPath) {
                    SVGPath svgPath = (SVGPath) graphic;
                    graphicFill = svgPath.getFill();
                    graphicStroke = svgPath.getStroke();
                    if (graphicFill != null && !Color.BLACK.equals(graphicFill))
                        textFill = graphicFill;
                    else if (graphicStroke != null && !Color.BLACK.equals(graphicStroke))
                        textFill = graphicStroke;
                    if (Color.BLACK.equals(graphicFill) || graphicFill == null && graphicStroke == null)
                        graphicFill = textFill;
                    if (Color.BLACK.equals(graphicStroke))
                        graphicStroke = textFill;
                }
                scaledGraphic = graphic == null ? null : new ScalePane(graphic);
            }
            if (scaledGraphic == null)
                clippedTextGraphicPane.getChildren().setAll(htmlText);
            else
                clippedTextGraphicPane.getChildren().setAll(htmlText, scaledGraphic);
            getChildren().setAll(shadowRectangle, clippedTextGraphicPane);
            updateColors();
        }

        @Override
        public void layoutChildren() {
            double width = getWidth(), height = getHeight();
            shadowRectangle.setWidth(width);
            shadowRectangle.setHeight(height);
            layoutInArea(shadowRectangle, 0, 0, width, height, 0, HPos.LEFT, VPos.TOP);
            layoutInArea(clippedTextGraphicPane, 0, 0, width, height, 0, HPos.LEFT, VPos.TOP);
        }
    }

}
