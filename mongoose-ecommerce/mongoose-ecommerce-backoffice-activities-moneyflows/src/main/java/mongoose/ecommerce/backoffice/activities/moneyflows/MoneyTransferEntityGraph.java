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

	private final ObservableList<MoneyAccount> moneyAccounts = FXCollections.observableArrayList();
	private final Map<MoneyAccount, Pane> nodeForMoneyAccount = new HashMap<>();
	private final ObservableList<MoneyFlowArrowView> moneyFlowArrowViews = FXCollections.observableArrayList();
	public ObservableList<MoneyFlowArrowView> moneyFlowArrowViews() { return moneyFlowArrowViews; }
	private MoneyAccount selectedEntity;
	private Pane newButton;

	public MoneyTransferEntityGraph() {
		moneyAccounts.addListener(new ListChangeListener<MoneyAccount>() {
			@Override
			public void onChanged(Change<? extends MoneyAccount> c) {
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
		ensureArrowsExistForAllMoneyFlows();
		removeObsoleteMoneyAccountPanes();
		Map<Integer, List<MoneyAccount>> nodesByDistanceFromRoot = orderByDistanceFromRoot();
		if (nodesByDistanceFromRoot.isEmpty()) {
			return;
		}
		System.out.println("========\n========\n=======\n=======\n=========");
		int highestDistance = nodesByDistanceFromRoot.keySet().stream().max(Integer::compare).get();
		int widthDenominator = highestDistance + 2;
		for (Entry<Integer, List<MoneyAccount>> entry : nodesByDistanceFromRoot.entrySet()) {
			int widthNumerator = Math.max(entry.getKey() + 1, 1);
			int heightDenominator = entry.getValue().size() + 2;
			int index = 1;
			List<MoneyAccount> alphabetizedMoneyAccounts = entry.getValue().stream()
					.sorted((account1, account2) -> account1.getName().compareToIgnoreCase(account2.getName()))
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

	private void removeObsoleteMoneyAccountPanes() {
		List<Node> obsoleteMoneyAccountPanes = getChildren().stream()
				.filter(node -> node instanceof MoneyAccountPane)
				.filter(child -> !moneyAccounts.contains(((MoneyAccountPane) child).moneyAccount))
				.collect(Collectors.toList());
		getChildren().removeAll(obsoleteMoneyAccountPanes);
	}

	private Map<Integer, List<MoneyAccount>> orderByDistanceFromRoot() {
		Map<Integer, List<MoneyAccount>> result = new HashMap<>();
		for (MoneyAccount moneyAccount : moneyAccounts) {
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
			MoneyFlow moneyFlow = moneyFlowArrowView.getMoneyFlow();
			if (moneyFlow.getToMoneyAccount().equals(moneyAccount)) {
				int maxDistance = getMaxDistanceFromRoot(moneyFlow.getFromMoneyAccount()) + 1;
				result = Math.max(result, maxDistance);
			}
		}
		return result;
	}

	private Pane getPaneForMoneyAccount(MoneyAccount moneyAccount) {
		if (!nodeForMoneyAccount.containsKey(moneyAccount)) {
			MoneyAccountPane moneyAccountPane = new MoneyAccountPane(moneyAccount);
			nodeForMoneyAccount.put(moneyAccount, moneyAccountPane);
			getChildren().add(moneyAccountPane);
		}
		return nodeForMoneyAccount.get(moneyAccount);
	}

	private class MoneyAccountPane extends Pane {

		private final MoneyAccount moneyAccount;

		public MoneyAccountPane(MoneyAccount moneyAccount) {
			this.moneyAccount = moneyAccount;
			//pane.setStyle(getMoneyAccountStyle(moneyAccount));
			setPadding(new Insets(8));
			setOnMouseClicked(e -> showVertexContextMenu(moneyAccount));
			Label label = new Label(moneyAccount.getName());
			getChildren().addAll(label);
			label.setAlignment(Pos.CENTER);
		}
	}

	private String getMoneyAccountStyle(MoneyAccount moneyAccount) {
		// TODO retrieve colour from the ReactiveVisualMapper
		String colorName = moneyAccount.equals(selectedEntity) ? "yellow" : "blue";
		return "-fx-border-color: " + colorName + ";" +
                "-fx-border-insets: 5;" +
                "-fx-border-width: 3;" +
                "-fx-border-style: dashed;";
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

	public void ensureArrowsExistForAllMoneyFlows() {
		removePreviousArrows();
		getChildren().addAll(moneyFlowArrowViews);
		System.out.println("moneyFlowArrowViews.size() = " + moneyFlowArrowViews.size());
		moneyFlowArrowViews.forEach(arrow -> {
			System.out.println(arrow.getMoneyFlow().getFromMoneyAccount().getName() + "(" + arrow.getMoneyFlow().getFromMoneyAccount().getId() + ", " + arrow.getMoneyFlow().getFromMoneyAccount().getStore() + ")"
					+ " - " +
					arrow.getMoneyFlow().getToMoneyAccount().getName() + "(" + arrow.getMoneyFlow().getToMoneyAccount().getId() + "," + arrow.getMoneyFlow().getToMoneyAccount().getStore() + ")");
		});

		List<EntityId> moneyAccoundIds = moneyAccounts.stream()
				.map(MoneyAccount::getId)
				.collect(Collectors.toList());
		List<MoneyFlowArrowView> validArrows = moneyFlowArrowViews.stream()
				.filter(arrow -> moneyAccoundIds.contains(arrow.getMoneyFlow().getFromMoneyAccount().getId()) &&
						moneyAccoundIds.contains(arrow.getMoneyFlow().getToMoneyAccount().getId()))
				.collect(Collectors.toList());
		System.out.println("moneyFlowArrowViews.size() = " + moneyFlowArrowViews.size());
	}

	private void removePreviousArrows() {
		List<Node> previousArrows = getChildren().stream()
				.filter(child -> child instanceof MoneyFlowArrowView)
				.collect(Collectors.toList());
		getChildren().removeAll(previousArrows);
	}

	private List<MoneyFlow> listMoneyFlows() {
		return moneyFlowArrowViews.stream()
				.map(MoneyFlowArrowView::getMoneyFlow)
				.collect(Collectors.toList());
	}

	public void setMoneyAccounts(List<MoneyAccount> moneyAccounts) {
		this.moneyAccounts.setAll(moneyAccounts != null ? moneyAccounts : Collections.emptyList());
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
			if (toAndFromAccountsPresent(moneyFlow)) {
				Pane sourceVertex = getPaneForMoneyAccount(moneyFlow.getFromMoneyAccount());
				Pane destVertex = getPaneForMoneyAccount(moneyFlow.getToMoneyAccount());
				moneyFlowArrowView = new MoneyFlowArrowView(moneyFlow, sourceVertex, destVertex);
			} else {
				moneyFlowArrowView = MoneyFlowArrowView.empty(moneyFlow);
			}
		}

		private boolean toAndFromAccountsPresent(MoneyFlow moneyFlow) {
			return nodeForMoneyAccount.containsKey(moneyFlow.getFromMoneyAccount()) &&
					nodeForMoneyAccount.containsKey(moneyFlow.getToMoneyAccount());
		}

		@Override
		public MoneyFlowArrowView getMappedObject() {
			return moneyFlowArrowView;
		}

		@Override
		public void onEntityChangedOrReplaced(MoneyFlow entity) {
		}

		@Override
		public void onEntityRemoved(MoneyFlow entity) {
		}
	}
}
