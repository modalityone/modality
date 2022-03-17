package mongoose.ecommerce.backoffice.activities.moneyflows;

import java.util.*;
import java.util.Map.Entry;

import dev.webfx.kit.util.properties.Properties;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

public class MoneyTransferEntityGraph extends Region {

	private static final int ARROW_HEAD_LENGTH = 30;

	private Pane newButton;
	private ObservableList<MoneyTransferEntity> vertices;
	private Map<MoneyTransferEntity, Pane> nodeForVertex;
	//private MoneyTransferConnections connections;
	private Map<Connection, Line> connectionArrows;
	private MoneyTransferEntity selectedEntity;

	public MoneyTransferEntityGraph() {
		vertices = FXCollections.observableList(new ArrayList<MoneyTransferEntity>());
		vertices.addListener(new ListChangeListener<MoneyTransferEntity>() {
			@Override
			public void onChanged(Change<? extends MoneyTransferEntity> c) {
				updateLayout();
			}
		});
		nodeForVertex = new HashMap<>();
		/*connections = new MoneyTransferConnections();
		connections.addListener(new ListChangeListener<MoneyTransferEntity>() {
			@Override
			public void onChanged(Change<? extends MoneyTransferEntity> c) {
				updateLayout();
			}
		});*/
		connectionArrows = new HashMap<>();
		addNewButton();
	}

	private void addNewButton() {
		Label label = new Label("+");
		label.setFont(new Font(32));
		label.setOnMouseClicked(e -> showNewPopup());
		newButton = new Pane();
		newButton.getChildren().add(label);
		newButton.layoutXProperty().bind(Properties.combine(widthProperty(), newButton.widthProperty(), (nodeWidth, buttonWidth) -> nodeWidth.doubleValue() - buttonWidth.doubleValue()));
		newButton.layoutYProperty().bind(Properties.combine(heightProperty(), newButton.heightProperty(), (nodeHeight, buttonHeight) -> nodeHeight.doubleValue() - buttonHeight.doubleValue()));
		getChildren().add(newButton);
	}

	private void showNewPopup() {
		/*MoneyTransferCreatorView moneyTransferCreatorView = new MoneyTransferCreatorView((ObservableList<MoneyTransferEntity>) vertices);
		showPopup(moneyTransferCreatorView);*/
	}

	private void showPopup(Node node) {
		/*Popup popup = new Popup();
		popup.getContent().add(buildPopupNode(node));
		popup.setAutoHide(true);
		popup.show(getScene().getWindow());*/
	}

	private Node buildPopupNode(Node node) {
		VBox result = new VBox(node);
		result.setPadding(new Insets(8));
		result.setStyle("-fx-border-color: gray; -fx-border-width: 3;");
		return result;
	}

	public void addNode(MoneyTransferEntity node) {
		vertices.add(node);
	}

	private void updateLayout() {
		Map<Integer, List<MoneyTransferEntity>> nodesByDistanceFromRoot = orderByDistanceFromRoot();
		if (nodesByDistanceFromRoot.isEmpty()) {
			return;
		}
		int highestDistance = nodesByDistanceFromRoot.keySet().stream().max(Integer::compare).get();
		int widthDenominator = highestDistance + 2;
		int longestColumn = nodesByDistanceFromRoot.values().stream().map(List::size).max(Integer::compare).get();
		int heightDenominator = longestColumn + 2;
		for (Entry<Integer, List<MoneyTransferEntity>> entry : nodesByDistanceFromRoot.entrySet()) {
			int widthNumerator = Math.max(entry.getKey() + 1, 1);
			int index = 1;
			for (MoneyTransferEntity vertex : entry.getValue()) {
				final int finalIndex = index;
				Pane pane = getPaneForVertex(vertex);
				pane.layoutXProperty().bind(Properties.compute(widthProperty(), width -> width.doubleValue() * widthNumerator / widthDenominator));
				pane.layoutYProperty().bind(Properties.compute(heightProperty(), height -> height.doubleValue() * finalIndex / heightDenominator));
				index++;
			}
		}
		ensureArrowsExistForAllConnections();
	}

	private Map<Integer, List<MoneyTransferEntity>> orderByDistanceFromRoot() {
		Map<Integer, List<MoneyTransferEntity>> result = new HashMap<>();
		result.put(0, vertices);
		/*for (MoneyTransferEntity vertex : vertices) {
			int maxDistanceFromRoot = connections.getMaxDistanceFromRoot(vertex);
			if (!result.containsKey(maxDistanceFromRoot)) {
				result.put(maxDistanceFromRoot, new ArrayList<>());
			}
			result.get(maxDistanceFromRoot).add(vertex);
		}*/
		return result;
	}

	private Pane getPaneForVertex(MoneyTransferEntity vertex) {
		if (!nodeForVertex.containsKey(vertex)) {
			nodeForVertex.put(vertex, buildPane(vertex));
		}
		return nodeForVertex.get(vertex);
	}

	private Pane buildPane(MoneyTransferEntity entity) {
		Pane pane = new Pane();
		pane.setStyle(getEntityStyle(entity));
		pane.setPadding(new Insets(8));
		pane.setOnMouseClicked(e -> showVertexContextMenu(entity));
		Label label = new Label(entity.toString());
		pane.getChildren().addAll(label);
		label.setAlignment(Pos.CENTER);
		getChildren().add(pane);
		return pane;
	}

	private String getEntityStyle(MoneyTransferEntity entity) {
		// TODO retrieve colour from the ReactiveVisualMapper
		String colorName = entity.equals(selectedEntity) ? "yellow" : "blue";
		return "-fx-border-color: " + colorName + ";" +
                "-fx-border-insets: 5;" +
                "-fx-border-width: 3;" +
                "-fx-border-style: dashed;";
	}

	private void showVertexContextMenu(MoneyTransferEntity entity) {
		/*MenuItem addConnectionMenuItem = new MenuItem("Add connection");
		ContextMenu vertexContextMenu = new ContextMenu(addConnectionMenuItem);
		vertexContextMenu.setOnAction(e -> {
			MoneyTransferConnectionCreatorView moneyTransferConnectionCreatorView =
					new MoneyTransferConnectionCreatorView(entity, vertices, connections);
			showPopup(moneyTransferConnectionCreatorView);
		});
		Point2D vertexPos = getVertexPos(entity);
		vertexContextMenu.show(getScene().getWindow(), vertexPos.getX(), vertexPos.getY());*/
	}

	private Point2D getVertexPos(MoneyTransferEntity vertex) {
		Pane pane = getPaneForVertex(vertex);
		double x = pane.getLayoutX() + getScene().getWindow().getX();
		double y = pane.getLayoutY() + getScene().getWindow().getY();
		return new Point2D(x, y);
	}

	private void ensureArrowsExistForAllConnections() {
		/*for (Connection connection : connections.getConnections()) {
			if (!connectionArrows.containsKey(connection)) {
				Line arrow = buildArrow(connection);
				connectionArrows.put(connection, arrow);
			}
		}*/
	}

	/*private Line buildArrow(Connection connection) {
		Line arrowLine = new Line();
		Pane sourceVertex = getPaneForVertex(connection.getSource());
		Pane destVertex = getPaneForVertex(connection.getDest());
		ObservableValue<Double> lineLayoutXProperty = Properties.combine(sourceVertex.layoutXProperty(), sourceVertex.widthProperty(), (x, width) -> x.doubleValue() + width.doubleValue());
		ObservableValue<Double> lineLayoutYProperty = Properties.combine(sourceVertex.layoutYProperty(), sourceVertex.heightProperty(), (y, height) -> y.doubleValue() + height.doubleValue() / 2);
		ObservableValue<Double> lineEndXProperty = Properties.combine(destVertex.layoutXProperty(), arrowLine.layoutXProperty(), (destX, arrowX) -> destX.doubleValue() - arrowX.doubleValue());
		ObservableValue<Double> lineEndYProperty = Properties.combine(
				Properties.combine(destVertex.layoutYProperty(), arrowLine.layoutYProperty(), (destY, arrowY) -> destY.doubleValue() - arrowY.doubleValue()),
				destVertex.heightProperty(),
				(a, b) -> a.doubleValue() + b.doubleValue() / 2);

		arrowLine.layoutXProperty().bind(lineLayoutXProperty);
		arrowLine.layoutYProperty().bind(lineLayoutYProperty);
		arrowLine.endXProperty().bind(lineEndXProperty);
		arrowLine.endYProperty().bind(lineEndYProperty);

		ObservableValue<Double> arrowHeadXProperty = Properties.combine(lineLayoutXProperty, lineEndXProperty, (layoutX, endX) -> layoutX.doubleValue() + endX.doubleValue());
		ObservableValue<Double> arrowHeadYProperty = Properties.combine(lineLayoutYProperty, lineEndYProperty, (layoutY, endY) -> layoutY.doubleValue() + endY.doubleValue());
		Line arrowHeadLeft = new Line();
		arrowHeadLeft.layoutXProperty().bind(arrowHeadXProperty);
		arrowHeadLeft.layoutYProperty().bind(arrowHeadYProperty);
		arrowHeadLeft.setEndX(-30);
		arrowHeadLeft.setEndY(-30);

		Line arrowHeadRight = new Line();
		arrowHeadRight.layoutXProperty().bind(arrowHeadXProperty);
		arrowHeadRight.layoutYProperty().bind(arrowHeadYProperty);
		arrowHeadRight.setEndX(-30);
		arrowHeadRight.setEndY(30);

		lineLayoutXProperty.addListener((a, b, c) -> updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight));
		lineLayoutYProperty.addListener((a, b, c) -> updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight));
		lineEndXProperty.addListener((a, b, c) -> updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight));
		lineEndYProperty.addListener((a, b, c) -> updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight));
		updateArrowHead(arrowLine, arrowHeadLeft, arrowHeadRight);

		getChildren().addAll(arrowLine, arrowHeadLeft, arrowHeadRight);
		return arrowLine;
	}

	private void updateArrowHead(Line arrow, Line arrowHeadLeft, Line arrowHeadRight) {
		double arrowLength = Math.sqrt((arrow.getEndX() * arrow.getEndX()) + (arrow.getEndY() * arrow.getEndY()));
		double arrowAngleDegrees = Math.toDegrees(Math.asin(arrow.getEndX() / arrowLength));
		arrowHeadLeft.setEndX(-ARROW_HEAD_LENGTH * Math.sin(Math.toRadians(arrowAngleDegrees - 45)));
		arrowHeadRight.setEndX(-ARROW_HEAD_LENGTH * Math.sin(Math.toRadians(arrowAngleDegrees + 45)));
		arrowHeadLeft.setEndY(ARROW_HEAD_LENGTH * Math.cos(Math.toRadians(arrowAngleDegrees - 45)));
		arrowHeadRight.setEndY(ARROW_HEAD_LENGTH * Math.cos(Math.toRadians(arrowAngleDegrees + 45)));
	}*/

	public void setEntities(List<MoneyTransferEntity> entities) {
		vertices.setAll(entities);
	}

	public void setSelectedEntity(MoneyTransferEntity selectedEntity) {
		this.selectedEntity = selectedEntity;
		updateLayout();
	}

}
