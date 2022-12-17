package one.modality.catering.backoffice.activities.kitchen;

import dev.webfx.stack.i18n.I18n;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Organization;

import java.util.*;
import java.util.stream.Collectors;

public class MealsSelectionPane extends VBox {

    private static final Color TITLE_TEXT_COLOR = Color.web("#0096d6");

    private final ObjectProperty<List<Item>> allOrganizationItemsProperty = new SimpleObjectProperty<>();

    private final ObjectProperty<List<Item>> selectedItemsProperty = new SimpleObjectProperty<>(Collections.emptyList());

    public ObjectProperty<List<Item>> selectedItemsProperty() { return selectedItemsProperty; }

    private Map<Item, CheckBox> itemCheckBoxMap = Collections.emptyMap();
    private final VBox itemCheckBoxPane = new VBox();

    public MealsSelectionPane() {
        allOrganizationItemsProperty.addListener((observableValue, oldValue, newValue) -> populate());
        setAlignment(Pos.CENTER);
        setFillWidth(false);
    }

    private void populate() {
        itemCheckBoxMap = new HashMap<>();
        Platform.runLater(() -> getChildren().clear());
        if (allOrganizationItemsProperty.get() == null) {
            return;
        }

        if (!allOrganizationItemsProperty.get().isEmpty()) {
            addTitle();
        }

        AbbreviationGenerator abbreviationGenerator = buildAbbreviationGenerator();
        for (Item item : allOrganizationItemsProperty.get()) {
            CheckBox itemCheckBox = new CheckBox(item.getName() + " (" + abbreviationGenerator.getAbbreviation(item.getName()) + ")");
            itemCheckBox.setSelected(true);
            itemCheckBox.selectedProperty().addListener((observableValue, oldValue, newValue) -> updateSelectedItems());
            itemCheckBox.setCursor(Cursor.HAND);
            itemCheckBoxMap.put(item, itemCheckBox);
        }

        Platform.runLater(() -> {
            getChildren().add(itemCheckBoxPane);
            updateSelectedItems();
        });
    }

    private void addTitle() {
        Label titleLabel = new Label();
        I18n.bindI18nTextProperty(titleLabel.textProperty(),"Meals");
        titleLabel.setTextFill(TITLE_TEXT_COLOR);
        Platform.runLater(() -> getChildren().add(titleLabel));
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
                .<Item>executeQuery("select id,name,code,ord from Item i where i.family.code = 'meals' and organization = " + organizationIdPk + " order by ord")
                .onSuccess(allOrganizationItemsProperty::set);
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
