package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.stack.cache.client.LocalStorageCache;
import dev.webfx.stack.i18n.I18n;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Organization;

import java.util.*;
import java.util.stream.Collectors;

public class MealsSelectionPane extends VBox {

    private final ObservableList<Item> organizationAllMealsItemsObservableList = FXCollections.observableArrayList();

    private final ObservableList<Item> selectedItemsObservableList = FXCollections.observableArrayList();

    public ObservableList<Item> selectedItemsObservableList() { return selectedItemsObservableList; }

    private final Map<Item, CheckBox> itemCheckBoxMap = new HashMap<>();
    private final HBox itemCheckBoxPane = new HBox(10);

    public MealsSelectionPane() {
        organizationAllMealsItemsObservableList.addListener((ListChangeListener<Item>) c -> populate());
        setAlignment(Pos.CENTER);
        setFillWidth(false);
        itemCheckBoxPane.setAlignment(Pos.CENTER);
    }

    private void populate() {
        itemCheckBoxMap.clear();
        Platform.runLater(() -> {
            getChildren().clear();
            itemCheckBoxPane.getChildren().clear();
        });

        if (!organizationAllMealsItemsObservableList.isEmpty()) {
            addTitle();
        }

        AbbreviationGenerator abbreviationGenerator = buildAbbreviationGenerator();
        for (Item item : organizationAllMealsItemsObservableList) {
            CheckBox itemCheckBox = new CheckBox(item.getName() + " (" + abbreviationGenerator.getAbbreviation(item.getName()) + ")");
            itemCheckBox.setSelected(true);
            itemCheckBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> updateSelectedItems());
            itemCheckBox.setCursor(Cursor.HAND);
            TextTheme.createDefaultTextFacet(itemCheckBox).style();
            itemCheckBoxMap.put(item, itemCheckBox);
        }

        Platform.runLater(() -> {
            getChildren().add(itemCheckBoxPane);
            updateSelectedItems();
        });
    }

    private void addTitle() {
        Label titleLabel = new Label();
        I18n.bindI18nTextProperty(titleLabel.textProperty(),"Meals:");
        TextTheme.createPrimaryTextFacet(titleLabel).style();
        Platform.runLater(() -> itemCheckBoxPane.getChildren().add(titleLabel));
    }

    private AbbreviationGenerator buildAbbreviationGenerator() {
        List<String> mealNames = organizationAllMealsItemsObservableList.stream()
                .map(Item::getName)
                .collect(Collectors.toList());
        return new AbbreviationGenerator(mealNames);
    }

    private void updateSelectedItems() {
        selectedItemsObservableList.setAll(getSelectedItems());
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

        organization.getStore().<Item>executeCachedQuery(
            LocalStorageCache.get().getCacheEntry("cache-kitchen-mealsItems"), organizationAllMealsItemsObservableList::setAll,
            "select id,name,code,ord from Item i where i.family.code='meals' and organization=? order by ord", organization
        ).onSuccess(organizationAllMealsItemsObservableList::setAll);
    }

    public AbbreviationGenerator getAbbreviationGenerator() {
        return buildAbbreviationGenerator();
    }

    public void setDisplayedMealNames(Set<String> displayedMealNames) {
        List<CheckBox> displayedCheckBoxes = itemCheckBoxMap.entrySet().stream()
                .filter(entry -> displayedMealNames.contains(entry.getKey().getName()))
                .sorted(Comparator.comparing(e -> e.getKey().getName()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        Platform.runLater(() -> itemCheckBoxPane.getChildren().setAll(displayedCheckBoxes));
    }
}
