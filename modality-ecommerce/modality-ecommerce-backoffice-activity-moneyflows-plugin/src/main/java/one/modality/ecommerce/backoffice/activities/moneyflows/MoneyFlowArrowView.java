package one.modality.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.ui.action.ActionGroup;
import dev.webfx.stack.ui.controls.ControlFactoryMixin;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import one.modality.base.shared.entities.MoneyFlow;
import one.modality.ecommerce.backoffice.operations.entities.moneyflow.DeleteMoneyFlowRequest;

/**
 * @author Dan Newman
 */
public class MoneyFlowArrowView extends Group {

    private static final int ARROW_HEAD_LENGTH = 30;
    private static final double ARROW_HEAD_ANGLE_IN_RADIANS = Math.PI / 6.0;

    private final ObjectProperty<MoneyFlow> moneyFlowProperty = new SimpleObjectProperty<>();
    public ObjectProperty<MoneyFlow> moneyFlowProperty() { return moneyFlowProperty; }
    private final ObjectProperty<Pane> sourceVertexProperty = new SimpleObjectProperty<>();
    public ObjectProperty<Pane> sourceVertexProperty() { return sourceVertexProperty; }
    private final ObjectProperty<Pane> destVertexProperty = new SimpleObjectProperty<>();
    public ObjectProperty<Pane> destVertexProperty() { return destVertexProperty; }
    private final Pane parentContainer;
    private final ObjectProperty<MoneyFlow> selectedMoneyFlow;

    private ArrowLine arrowLine;
    private ArrowLine arrowHeadLeft;
    private ArrowLine arrowHeadRight;

    public MoneyFlowArrowView(MoneyFlow moneyFlow, Pane sourceVertex, Pane destVertex, Pane parentContainer, ObjectProperty<MoneyFlow> selectedMoneyFlow) {
        this.parentContainer = parentContainer;
        this.selectedMoneyFlow = selectedMoneyFlow;
        sourceVertexProperty.set(sourceVertex);
        destVertexProperty.set(destVertex);
        sourceVertexProperty.addListener(e -> createLines());
        destVertexProperty.addListener(e -> createLines());
        moneyFlowProperty.addListener(e -> createLines());
        moneyFlowProperty.set(moneyFlow);
    }

    private void createLines() {
        getChildren().clear();
        Pane sourceVertex = sourceVertexProperty.get();
        Pane destVertex = destVertexProperty.get();
        if (sourceVertex == null || destVertex == null) {
            return;
        }

        arrowLine = new ArrowLine();
        ObservableValue<Double> lineLayoutXProperty = FXProperties.combine(sourceVertex.layoutXProperty(), sourceVertex.widthProperty(), (x, width) -> x.doubleValue() + width.doubleValue());
        ObservableValue<Double> lineLayoutYProperty = FXProperties.combine(sourceVertex.layoutYProperty(), sourceVertex.heightProperty(), (y, height) -> y.doubleValue() + height.doubleValue() / 2);
        ObservableValue<Double> lineEndXProperty = FXProperties.combine(destVertex.layoutXProperty(), arrowLine.layoutXProperty(), (destX, arrowX) -> destX.doubleValue() - arrowX.doubleValue());
        ObservableValue<Double> lineEndYProperty = FXProperties.combine(
                FXProperties.combine(destVertex.layoutYProperty(), arrowLine.layoutYProperty(), (destY, arrowY) -> destY.doubleValue() - arrowY.doubleValue()),
                destVertex.heightProperty(),
                (a, b) -> a + b.doubleValue() / 2);

        arrowLine.layoutXProperty().bind(lineLayoutXProperty);
        arrowLine.layoutYProperty().bind(lineLayoutYProperty);
        arrowLine.endXProperty().bind(lineEndXProperty);
        arrowLine.endYProperty().bind(lineEndYProperty);

        ObservableValue<Double> arrowHeadXProperty = FXProperties.combine(lineLayoutXProperty, lineEndXProperty, Double::sum);
        ObservableValue<Double> arrowHeadYProperty = FXProperties.combine(lineLayoutYProperty, lineEndYProperty, Double::sum);
        arrowHeadLeft = new ArrowLine();
        arrowHeadLeft.layoutXProperty().bind(arrowHeadXProperty);
        arrowHeadLeft.layoutYProperty().bind(arrowHeadYProperty);

        arrowHeadRight = new ArrowLine();
        arrowHeadRight.layoutXProperty().bind(arrowHeadXProperty);
        arrowHeadRight.layoutYProperty().bind(arrowHeadYProperty);

        lineLayoutXProperty.addListener((a, b, c) -> updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight));
        lineLayoutYProperty.addListener((a, b, c) -> updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight));
        lineEndXProperty.addListener((a, b, c) -> updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight));
        lineEndYProperty.addListener((a, b, c) -> updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight));
        updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight);

        getChildren().addAll(arrowLine, arrowHeadLeft, arrowHeadRight);
        setHighlighted(false);
    }

    private void updateArrowHead(Line arrow, Line arrowHeadLeft, Line arrowHeadRight) {
        double arrowSlope = Math.atan2(arrow.getEndY() - arrow.getStartY(), arrow.getEndX() - arrow.getStartX());
        double headSlopeLeft = arrowSlope - ARROW_HEAD_ANGLE_IN_RADIANS;
        double headSlopeRight = arrowSlope + ARROW_HEAD_ANGLE_IN_RADIANS;
        arrowHeadLeft.setEndX(-ARROW_HEAD_LENGTH * Math.cos(headSlopeLeft));
        arrowHeadLeft.setEndY(-ARROW_HEAD_LENGTH * Math.sin(headSlopeLeft));
        arrowHeadRight.setEndX(-ARROW_HEAD_LENGTH * Math.cos(headSlopeRight));
        arrowHeadRight.setEndY(-ARROW_HEAD_LENGTH * Math.sin(headSlopeRight));
    }

    public void setHighlighted(boolean highlighted) {
        Paint color = highlighted ? Color.ORANGE : Color.INDIGO;
        arrowLine.setStroke(color);
        arrowHeadLeft.setStroke(color);
        arrowHeadRight.setStroke(color);
    }

    private class ArrowLine extends Line implements ControlFactoryMixin, OperationActionFactoryMixin {

        public ArrowLine() {
            setStrokeWidth(5);
            setUpContextMenu(this, this::createContextMenuActionGroup);
            setOnMouseClicked(e -> selectedMoneyFlow.set(moneyFlowProperty.get()));
            setCursor(Cursor.HAND);
        }

        private ActionGroup createContextMenuActionGroup() {
            return newActionGroup(
                    newOperationAction(() -> new DeleteMoneyFlowRequest(moneyFlowProperty.get(), parentContainer))
            );
        }
    }

}
