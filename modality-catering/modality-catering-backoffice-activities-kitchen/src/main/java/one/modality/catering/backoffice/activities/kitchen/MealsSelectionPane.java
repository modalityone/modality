package one.modality.catering.backoffice.activities.kitchen;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Organization;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MealsSelectionPane extends VBox {

    private final ObjectProperty<List<Item>> allOrganizationItemsProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<List<Item>> selectedItemsProperty = new SimpleObjectProperty<>(Collections.emptyList());
    public ObjectProperty<List<Item>> selectedItemsProperty() { return selectedItemsProperty; }

    private Map<Item, CheckBox> itemCheckBoxMap = Collections.emptyMap();

    public MealsSelectionPane() {
        allOrganizationItemsProperty.addListener(new ChangeListener<List<Item>>() {
            @Override
            public void changed(ObservableValue<? extends List<Item>> observableValue, List<Item> items, List<Item> t1) {
                populate();
            }
        });
    }

    private void populate() {
        itemCheckBoxMap = new HashMap<>();
        Platform.runLater(() -> getChildren().clear());
        if (allOrganizationItemsProperty.isNull().get()) {
            return;
        }

        AbbreviationGenerator abbreviationGenerator = buildAbbreviationGenerator();
        for (Item item : allOrganizationItemsProperty.get()) {
            CheckBox itemCheckBox = new CheckBox(item.getName() + " (" + abbreviationGenerator.getAbbreviation(item.getName()) + ")");
            itemCheckBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> updateSelectedItems());
            itemCheckBox.setSelected(true);
            itemCheckBoxMap.put(item, itemCheckBox);
            Platform.runLater(() -> getChildren().add(itemCheckBox));
        }
        updateSelectedItems();
    }

    private AbbreviationGenerator buildAbbreviationGenerator() {
        List<String> mealNames = allOrganizationItemsProperty.get().stream()
                .map(Item::getName)
                .collect(Collectors.toList());
        return new AbbreviationGenerator(mealNames);
    }

    private void updateSelectedItems() {
        selectedItemsProperty.set(getSelectedItems());
    }

    private List<Item> getSelectedItems() {
        return itemCheckBoxMap.entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public void setOrganization(Organization organization) {
        getChildren().clear();
        if (organization == null || organization.getId() == null) {
            return;
        }

        Object organizationIdPk = organization.getId().getPrimaryKey();
        organization.getStore()
                .executeQuery("select id,name,code,ord from Item i where i.family.code = 'meals' and organization = " + organizationIdPk + " order by ord")
                .onSuccess(entities -> {
                    List<Item> items = entities.stream()
                            .map(Item.class::cast)
                            .collect(Collectors.toList());
                    allOrganizationItemsProperty.set(items);
                });
    }

    public AbbreviationGenerator getAbbreviationGenerator() {
        return buildAbbreviationGenerator();
    }

}
