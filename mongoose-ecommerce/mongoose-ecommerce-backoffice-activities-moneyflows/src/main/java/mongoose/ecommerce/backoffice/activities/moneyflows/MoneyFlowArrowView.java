package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.kit.util.properties.Properties;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import mongoose.base.shared.entities.MoneyFlow;

/**
 * @author Dan Newman
 */
public class MoneyFlowArrowView extends Pane {

    private static final int ARROW_HEAD_LENGTH = 30;

    private final ObjectProperty<MoneyFlow> moneyFlowProperty = new SimpleObjectProperty<>();
    public ObjectProperty<MoneyFlow> moneyFlowProperty() { return moneyFlowProperty; }
    private final ObjectProperty<Pane> sourceVertexProperty = new SimpleObjectProperty<>();
    public ObjectProperty<Pane> sourceVertexProperty() { return sourceVertexProperty; }
    private final ObjectProperty<Pane> destVertexProperty = new SimpleObjectProperty<>();
    public ObjectProperty<Pane> destVertexProperty() { return destVertexProperty; }

    public MoneyFlowArrowView(MoneyFlow moneyFlow, Pane sourceVertex, Pane destVertex) {
        moneyFlowProperty.addListener(e -> createLines());
        moneyFlowProperty.set(moneyFlow);
        sourceVertexProperty.set(sourceVertex);
        destVertexProperty.set(destVertex);
        createLines();
    }

    private void createLines() {
        getChildren().clear();
        Pane sourceVertex = sourceVertexProperty.get();
        Pane destVertex = destVertexProperty.get();
        if (sourceVertex == null || destVertex == null) {
            return;
        }

        ArrowLine arrowLine = new ArrowLine();
        ObservableValue<Double> lineLayoutXProperty = Properties.combine(sourceVertex.layoutXProperty(), sourceVertex.widthProperty(), (x, width) -> x.doubleValue() + width.doubleValue());
        ObservableValue<Double> lineLayoutYProperty = Properties.combine(sourceVertex.layoutYProperty(), sourceVertex.heightProperty(), (y, height) -> y.doubleValue() + height.doubleValue() / 2);
        ObservableValue<Double> lineEndXProperty = Properties.combine(destVertex.layoutXProperty(), arrowLine.layoutXProperty(), (destX, arrowX) -> destX.doubleValue() - arrowX.doubleValue());
        ObservableValue<Double> lineEndYProperty = Properties.combine(
                Properties.combine(destVertex.layoutYProperty(), arrowLine.layoutYProperty(), (destY, arrowY) -> destY.doubleValue() - arrowY.doubleValue()),
                destVertex.heightProperty(),
                (a, b) -> a + b.doubleValue() / 2);

        arrowLine.layoutXProperty().bind(lineLayoutXProperty);
        arrowLine.layoutYProperty().bind(lineLayoutYProperty);
        arrowLine.endXProperty().bind(lineEndXProperty);
        arrowLine.endYProperty().bind(lineEndYProperty);

        ObservableValue<Double> arrowHeadXProperty = Properties.combine(lineLayoutXProperty, lineEndXProperty, Double::sum);
        ObservableValue<Double> arrowHeadYProperty = Properties.combine(lineLayoutYProperty, lineEndYProperty, Double::sum);
        ArrowLine arrowHeadLeft = new ArrowLine();
        arrowHeadLeft.layoutXProperty().bind(arrowHeadXProperty);
        arrowHeadLeft.layoutYProperty().bind(arrowHeadYProperty);
        arrowHeadLeft.setEndX(-30);
        arrowHeadLeft.setEndY(-30);

        ArrowLine arrowHeadRight = new ArrowLine();
        arrowHeadRight.layoutXProperty().bind(arrowHeadXProperty);
        arrowHeadRight.layoutYProperty().bind(arrowHeadYProperty);
        arrowHeadRight.setEndX(-30);
        arrowHeadRight.setEndY(30);

        lineLayoutXProperty.addListener((a, b, c) -> updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight));
        lineLayoutYProperty.addListener((a, b, c) -> updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight));
        lineEndXProperty.addListener((a, b, c) -> updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight));
        lineEndYProperty.addListener((a, b, c) -> updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight));
        updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight);
        arrowHeadRight.setStroke(Color.RED);
        getChildren().addAll(arrowLine, arrowHeadLeft, arrowHeadRight);
    }

    private void updateArrowHead(Line arrow, Line arrowHeadLeft, Line arrowHeadRight) {


        double arrowLength = Math.sqrt((arrow.getEndX() * arrow.getEndX()) + (arrow.getEndY() * arrow.getEndY()));
        double arrowAngleDegrees = 90 - Math.toDegrees(Math.asin(arrow.getEndX() / arrowLength));
        System.out.println("arrowAngleDegrees = " + arrowAngleDegrees);
        arrowHeadLeft.setEndX(-ARROW_HEAD_LENGTH * Math.cos(Math.toRadians(arrowAngleDegrees - 45)));
        arrowHeadRight.setEndX(ARROW_HEAD_LENGTH * Math.sin(Math.toRadians(arrowAngleDegrees - 45)));
        arrowHeadLeft.setEndY(-ARROW_HEAD_LENGTH * Math.sin(Math.toRadians(arrowAngleDegrees - 45)));
        arrowHeadRight.setEndY(-ARROW_HEAD_LENGTH * Math.cos(Math.toRadians(arrowAngleDegrees - 45)));
    }

    private static class ArrowLine extends Line {

        public ArrowLine() {
            setStroke(Color.BLACK);
        }
    }

}
