package one.modality.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.stack.orm.reactive.entities.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.stack.ui.controls.ControlFactoryMixin;
import dev.webfx.stack.ui.operation.action.OperationActionFactoryMixin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import one.modality.base.shared.entities.MoneyAccount;
import one.modality.base.shared.entities.MoneyFlow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author Dan Newman
 */
public class MoneyTransferEntityGraph extends Pane implements ControlFactoryMixin, OperationActionFactoryMixin {

	private final ObservableList<MoneyAccountPane> moneyAccountPanes = FXCollections.observableArrayList();
	public ObservableList<MoneyAccountPane> moneyAccountPanes() { return moneyAccountPanes; }
	private final ObservableList<MoneyFlowArrowView> moneyFlowArrowViews = FXCollections.observableArrayList();
	public ObservableList<MoneyFlowArrowView> moneyFlowArrowViews() { return moneyFlowArrowViews; }
	private final ObjectProperty<MoneyAccount> selectedMoneyAccount = new SimpleObjectProperty<>();
	public ObjectProperty<MoneyAccount> selectedMoneyAccount() { return selectedMoneyAccount; }
	private final ObjectProperty<MoneyFlow> selectedMoneyFlow = new SimpleObjectProperty<>();
	public ObjectProperty<MoneyFlow> selectedMoneyFlow() { return selectedMoneyFlow; }

	public MoneyTransferEntityGraph() {
		moneyAccountPanes.addListener((ListChangeListener<MoneyAccountPane>) c -> updateLayout());
		moneyFlowArrowViews.addListener((ListChangeListener<MoneyFlowArrowView>) c -> updateLayout());
		FXProperties.runOnPropertyChange(moneyFlow -> moneyFlowArrowViews().forEach(arrow ->
			arrow.setHighlighted(moneyFlow != null && moneyFlow.equals(arrow.moneyFlowProperty().get()))
		), selectedMoneyFlow);
	}

	private void updateLayout() {
		removePreviousArrows();
		removePreviousMoneyAccountPanes();
		getChildren().addAll(moneyFlowArrowViews);
		getChildren().addAll(moneyAccountPanes);

		Map<Integer, List<MoneyAccount>> nodesByDistanceFromRoot = orderByDistanceFromRoot();
		if (nodesByDistanceFromRoot.isEmpty()) {
			return;
		}
		int highestDistance = nodesByDistanceFromRoot.keySet().stream().max(Integer::compare).get();
		int widthDenominator = Math.max(highestDistance + 1, 1);
		for (Entry<Integer, List<MoneyAccount>> entry : nodesByDistanceFromRoot.entrySet()) {
			int widthNumerator = entry.getKey();
			int heightDenominator = entry.getValue().size() + 2;
			int index = 1;
			List<MoneyAccount> alphabetizedMoneyAccounts = entry.getValue().stream()
					.sorted((account1, account2) -> ensureStringNotNull(account1.getName()).compareToIgnoreCase(ensureStringNotNull(account2.getName())))
					.collect(Collectors.toList());
			for (MoneyAccount moneyAccount : alphabetizedMoneyAccounts) {
				final int finalIndex = index;
				Pane pane = getPaneForMoneyAccount(moneyAccount);
				pane.layoutXProperty().bind(FXProperties.compute(widthProperty(), width -> 20 + width.doubleValue() * widthNumerator / widthDenominator));
				pane.layoutYProperty().bind(FXProperties.compute(heightProperty(), height -> height.doubleValue() * finalIndex / heightDenominator));
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
			MoneyAccount moneyAccount = moneyAccountPane.getMoneyAccount();
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
				.filter(pane -> pane.getMoneyAccount().equals(moneyAccount))
				.findAny()
				.orElse(null);
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

	public MoneyFlowToArrowMapper newMoneyFlowToArrowMapper(MoneyFlow moneyFlow) {
		return new MoneyFlowToArrowMapper(moneyFlow);
	}

	class MoneyFlowToArrowMapper implements IndividualEntityToObjectMapper<MoneyFlow, MoneyFlowArrowView> {

		final MoneyFlow moneyFlow;
		final MoneyFlowArrowView moneyFlowArrowView;

		MoneyFlowToArrowMapper(MoneyFlow moneyFlow) {
			this.moneyFlow = moneyFlow;
			Pane sourceVertex = getPaneForMoneyAccount(moneyFlow.getFromMoneyAccount());
			Pane destVertex = getPaneForMoneyAccount(moneyFlow.getToMoneyAccount());
			moneyFlowArrowView = new MoneyFlowArrowView(moneyFlow, sourceVertex, destVertex, MoneyTransferEntityGraph.this, selectedMoneyFlow);
			moneyAccountPanes.addListener((ListChangeListener<? super MoneyAccountPane>) e -> updateVertices());
		}

		private void updateVertices() {
			Pane sourceVertex = getPaneForMoneyAccount(moneyFlow.getFromMoneyAccount());
			Pane destVertex = getPaneForMoneyAccount(moneyFlow.getToMoneyAccount());
			moneyFlowArrowView.sourceVertexProperty().set(sourceVertex);
			moneyFlowArrowView.destVertexProperty().set(destVertex);
		}

		@Override
		public MoneyFlowArrowView getMappedObject() {
			return moneyFlowArrowView;
		}

		@Override
		public void onEntityChangedOrReplaced(MoneyFlow moneyFlow) {
			updateVertices();
			moneyFlowArrowView.moneyFlowProperty().set(moneyFlow);
		}

		@Override
		public void onEntityRemoved(MoneyFlow moneyFlow) {
		}
	}
}
