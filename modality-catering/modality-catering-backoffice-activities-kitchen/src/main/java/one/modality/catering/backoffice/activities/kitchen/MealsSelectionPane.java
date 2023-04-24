package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.extras.theme.text.TextTheme;
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

    private final ObservableList<Item> allOrganizationItemsProperty = FXCollections.observableArrayList();

    private final ObservableList<Item> selectedItemsProperty = FXCollections.observableArrayList();

    public ObservableList<Item> selectedItemsProperty() { return selectedItemsProperty; }

    private Map<Item, CheckBox> itemCheckBoxMap = Collections.emptyMap();
    private final HBox itemCheckBoxPane = new HBox(10);

    public MealsSelectionPane() {
        allOrganizationItemsProperty.addListener((ListChangeListener<Item>) c -> populate());
        setAlignment(Pos.CENTER);
        setFillWidth(false);
        itemCheckBoxPane.setAlignment(Pos.CENTER);
    }

    private void populate() {
        itemCheckBoxMap = new HashMap<>();
        Platform.runLater(() -> {
            getChildren().clear();
            itemCheckBoxPane.getChildren().clear();
        });

        if (!allOrganizationItemsProperty.isEmpty()) {
            addTitle();
        }

        AbbreviationGenerator abbreviationGenerator = buildAbbreviationGenerator();
        for (Item item : allOrganizationItemsProperty) {
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
        List<String> mealNames = allOrganizationItemsProperty.stream()
                .map(Item::getName)
                .collect(Collectors.toList());
        return new AbbreviationGenerator(mealNames);
    }

    private void updateSelectedItems() {
        selectedItemsProperty.setAll(getSelectedItems());
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
                .<Item>executeQuery("select id,name,code,ord from Item i where i.family.code = 'meals' and organization = " + organizationIdPk + " order by ord")
                .onSuccess(allOrganizationItemsProperty::setAll);
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
