package mongoose.ecommerce.backoffice.activities.moneyflows;

import dev.webfx.framework.client.orm.reactive.mapping.entities_to_objects.IndividualEntityToObjectMapper;
import dev.webfx.kit.util.properties.Properties;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import mongoose.base.shared.entities.MoneyAccount;
import mongoose.base.shared.entities.MoneyFlow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author Dan Newman
 */
public class MoneyTransferEntityGraph extends Region {

	private final ObservableList<MoneyAccountPane> moneyAccountPanes = FXCollections.observableArrayList();
	public ObservableList<MoneyAccountPane> moneyAccountPanes() { return moneyAccountPanes; }
	private final ObservableList<MoneyFlowArrowView> moneyFlowArrowViews = FXCollections.observableArrayList();
	public ObservableList<MoneyFlowArrowView> moneyFlowArrowViews() { return moneyFlowArrowViews; }
	private final ObjectProperty<MoneyAccount> selectedMoneyAccount = new SimpleObjectProperty<>();
	public ObjectProperty<MoneyAccount> selectedMoneyAccount() { return selectedMoneyAccount; }
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
