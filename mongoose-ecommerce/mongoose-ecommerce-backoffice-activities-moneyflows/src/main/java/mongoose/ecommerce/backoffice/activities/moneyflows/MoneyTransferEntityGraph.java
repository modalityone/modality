package mongoose.ecommerce.backoffice.activities.moneyflows;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import dev.webfx.framework.client.orm.reactive.mapping.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.framework.shared.orm.entity.Entity;
import dev.webfx.framework.shared.orm.entity.EntityId;
import dev.webfx.kit.util.properties.Properties;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import mongoose.base.shared.entities.MoneyAccount;
import mongoose.base.shared.entities.MoneyFlow;

/**
 * @author Dan Newman
 */
public class MoneyTransferEntityGraph extends Region {

	private final ObservableList<MoneyAccountPane> moneyAccountPanes = FXCollections.observableArrayList();
	public ObservableList<MoneyAccountPane> moneyAccountPanes() { return moneyAccountPanes; }
	private final ObservableList<MoneyFlowArrowView> moneyFlowArrowViews = FXCollections.observableArrayList();
	public ObservableList<MoneyFlowArrowView> moneyFlowArrowViews() { return moneyFlowArrowViews; }
	private MoneyAccount selectedEntity;
	private Pane newButton;

	public MoneyTransferEntityGraph() {
		moneyAccountPanes.addListener(new ListChangeListener<MoneyAccountPane>() {
			@Override
			public void onChanged(Change<? extends MoneyAccountPane> c) {
				updateLayout();
			}
		});
		moneyFlowArrowViews.addListener(new ListChangeListener<MoneyFlowArrowView>() {
			@Override
			public void onChanged(Change<? extends MoneyFlowArrowView> c) {
				updateLayout();
			}
		});
		addNewButton();
	}

	private void addNewButton() {
		Label label = new Label("+");
		label.setFont(new Font(128));
		label.setOnMouseClicked(e -> showNewPopup());
		newButton = new Pane();
		newButton.getChildren().add(label);
		newButton.layoutXProperty().bind(Properties.combine(widthProperty(), newButton.widthProperty(), (nodeWidth, buttonWidth) -> nodeWidth.doubleValue() - buttonWidth.doubleValue()));
		newButton.layoutYProperty().bind(Properties.combine(heightProperty(), newButton.heightProperty(), (nodeHeight, buttonHeight) -> nodeHeight.doubleValue() - buttonHeight.doubleValue()));
		getChildren().add(newButton);
	}

	private void showNewPopup() {
		/*MoneyTransferCreatorView moneyTransferCreatorView = new MoneyTransferCreatorView((ObservableList<MoneyAccount>) vertices);
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

	private void updateLayout() {
		removePreviousMoneyAccountPanes();
		removePreviousArrows();
		getChildren().addAll(moneyAccountPanes);
		getChildren().addAll(moneyFlowArrowViews);

		Map<Integer, List<MoneyAccount>> nodesByDistanceFromRoot = orderByDistanceFromRoot();
		if (nodesByDistanceFromRoot.isEmpty()) {
			return;
		}
		int highestDistance = nodesByDistanceFromRoot.keySet().stream().max(Integer::compare).get();
		int widthDenominator = highestDistance + 2;
		for (Entry<Integer, List<MoneyAccount>> entry : nodesByDistanceFromRoot.entrySet()) {
			int widthNumerator = Math.max(entry.getKey() + 1, 1);
			int heightDenominator = entry.getValue().size() + 2;
			int index = 1;
			List<MoneyAccount> alphabetizedMoneyAccounts = entry.getValue().stream()
					.sorted((account1, account2) -> ensureStringNotNull(account1.getName()).compareToIgnoreCase(ensureStringNotNull(account2.getName())))
					.collect(Collectors.toList());
			for (MoneyAccount moneyAccount : alphabetizedMoneyAccounts) {
				final int finalIndex = index;
				Pane pane = getPaneForMoneyAccount(moneyAccount);
				pane.layoutXProperty().bind(Properties.compute(widthProperty(), width -> width.doubleValue() * widthNumerator / widthDenominator));
				pane.layoutYProperty().bind(Properties.compute(heightProperty(), height -> height.doubleValue() * finalIndex / heightDenominator));
				index++;
			}
		}
	}

	private String ensureStringNotNull(String s) {
		// TODO investigate why MoneyAccount with ID 14 has no name or type
		return s != null ? s : "";
	}

	private Map<Integer, List<MoneyAccount>> orderByDistanceFromRoot() {
		Map<Integer, List<MoneyAccount>> result = new HashMap<>();
		for (MoneyAccountPane moneyAccountPane : moneyAccountPanes) {
			MoneyAccount moneyAccount = moneyAccountPane.moneyAccountProperty().get();
			int maxDistanceFromRoot = getMaxDistanceFromRoot(moneyAccount);
			if (!result.containsKey(maxDistanceFromRoot)) {
				result.put(maxDistanceFromRoot, new ArrayList<>());
			}
			result.get(maxDistanceFromRoot).add(moneyAccount);
		}
		return result;
	}

	private int getMaxDistanceFromRoot(MoneyAccount moneyAccount) {
		int result = 0;
		for (MoneyFlowArrowView moneyFlowArrowView : moneyFlowArrowViews) {
			MoneyFlow moneyFlow = moneyFlowArrowView.moneyFlowProperty().get();
			if (moneyFlow.getToMoneyAccount().equals(moneyAccount)) {
				int maxDistance = getMaxDistanceFromRoot(moneyFlow.getFromMoneyAccount()) + 1;
				result = Math.max(result, maxDistance);
			}
		}
		return result;
	}

	private Pane getPaneForMoneyAccount(MoneyAccount moneyAccount) {
		return moneyAccountPanes.stream()
				.filter(pane -> pane.moneyAccountProperty().get().equals(moneyAccount))
				.findAny()
				.orElse(null);
	}

	private void showVertexContextMenu(MoneyAccount entity) {
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

	private Point2D getVertexPos(MoneyAccount vertex) {
		Pane pane = getPaneForMoneyAccount(vertex);
		double x = pane.getLayoutX() + getScene().getWindow().getX();
		double y = pane.getLayoutY() + getScene().getWindow().getY();
		return new Point2D(x, y);
	}

	private void removePreviousMoneyAccountPanes() {
		List<Node> previousMoneyAccountPanes = getChildren().stream()
				.filter(child -> child instanceof MoneyAccountPane)
				.collect(Collectors.toList());
		getChildren().removeAll(previousMoneyAccountPanes);
	}

	private void removePreviousArrows() {
		List<Node> previousArrows = getChildren().stream()
				.filter(child -> child instanceof MoneyFlowArrowView)
				.collect(Collectors.toList());
		getChildren().removeAll(previousArrows);
	}

	public void setSelectedEntity(MoneyAccount selectedEntity) {
		this.selectedEntity = selectedEntity;
		updateLayout();
	}

	public MoneyFlowToArrowMapper newMoneyFlowToArrowMapper(MoneyFlow moneyFlow) {
		return new MoneyFlowToArrowMapper(moneyFlow);
	}

	class MoneyFlowToArrowMapper implements IndividualEntityToObjectMapper<MoneyFlow, MoneyFlowArrowView> {

		final MoneyFlowArrowView moneyFlowArrowView;

		MoneyFlowToArrowMapper(MoneyFlow moneyFlow) {
			Pane sourceVertex = getPaneForMoneyAccount(moneyFlow.getFromMoneyAccount());
			Pane destVertex = getPaneForMoneyAccount(moneyFlow.getToMoneyAccount());
			moneyFlowArrowView = new MoneyFlowArrowView(moneyFlow, sourceVertex, destVertex);
		}

		@Override
		public MoneyFlowArrowView getMappedObject() {
			return moneyFlowArrowView;
		}

		@Override
		public void onEntityChangedOrReplaced(MoneyFlow moneyFlow) {
			moneyFlowArrowView.moneyFlowProperty().set(moneyFlow);
		}

		@Override
		public void onEntityRemoved(MoneyFlow moneyFlow) {
		}
	}
}
