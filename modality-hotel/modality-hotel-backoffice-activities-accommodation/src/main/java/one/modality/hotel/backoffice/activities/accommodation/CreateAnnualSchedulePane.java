package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.ui.controls.dialog.DialogBuilderUtil;
import dev.webfx.stack.ui.controls.dialog.DialogContent;
import dev.webfx.stack.ui.dialog.DialogCallback;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CreateAnnualSchedulePane extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-uu");

    private final Pane parent;
    private final TextField toDateTextField;
    private final TextField fromDateTextField;
    private final VBox selectedItemsPane = new VBox(new Label("Loading items. Please wait."));

    private Map<CheckBox, Item> comboBoxItems;

    public CreateAnnualSchedulePane(Pane parent) {
        this.parent = parent;
        setPadding(new Insets(16));
        Label fromLabel = new Label("from");
        fromDateTextField = new TextField();
        fromDateTextField.setPromptText("e.g. 16-01-22");
        fromDateTextField.textProperty().addListener(change -> setToDateUsingFromDate());
        HBox fromDateRow = new HBox(fromLabel, fromDateTextField);
        Label toLabel = new Label("to");
        toDateTextField = new TextField();
        toDateTextField.setPromptText("e.g. 15-01-23");
        HBox toDateRow = new HBox(toLabel, toDateTextField);
        getChildren().setAll(fromDateRow, toDateRow, selectedItemsPane);
        displayItemsWithOpenConfiguration();
    }

    private void setToDateUsingFromDate() {
        try {
            LocalDate fromDate = LocalDate.parse(fromDateTextField.getText(), DATE_FORMATTER);
            LocalDate toDate = fromDate.plusYears(1).minusDays(1);
            String toDateText = DATE_FORMATTER.format(toDate);
            toDateTextField.setText(toDateText);
        } catch (DateTimeParseException | NullPointerException e) {
            // If the user has not entered a valid date then do nothing
        }
    }

    private void displayItemsWithOpenConfiguration() {
        EntityId organizationId = FXOrganizationId.getOrganizationId();
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        EntityStore.create(dataSourceModel).<ResourceConfiguration>executeQuery("select distinct rc.item.name from ResourceConfiguration rc where rc.item.family.code='acco' and rc.resource.site.organization.id=? and rc.resource.site.event is null and rc.endDate is null", organizationId)
                .onFailure(error -> {
                    Console.log("Error while reading scheduled items.", error);
                })
                .onSuccess(resourceConfigurations -> {
                    comboBoxItems = new LinkedHashMap<>();
                    List<Item> sortedItems = resourceConfigurations.stream()
                            .map(ResourceConfiguration::getItem)
                            .distinct()
                            .sorted(Comparator.comparing(EntityHasName::getName))
                            .collect(Collectors.toList());
                    for (Item item : sortedItems) {
                        CheckBox itemCheckBox = new CheckBox(item.getName());
                        itemCheckBox.setSelected(true);
                        comboBoxItems.put(itemCheckBox, item);
                    }
                    Platform.runLater(() -> selectedItemsPane.getChildren().setAll(comboBoxItems.keySet()));
                });
    }

    public void createAnnualSchedule() {
        LocalDate date = parseDate();
        if (date == null) {
            return;
        }

        List<Item> selectedItems = getSelectedItems();
        if (selectedItems.isEmpty()) {
            showMsg("No items selected.\n\nAnnual schedule not created.");
            return;
        }

        // TODO create ScheduledItem for each Item for each date for which one does not exist

        // TODO create ScheduleResource for each Item for each date for which one does not exist
    }

    private LocalDate parseDate() {
        String text = toDateTextField.getText();
        if (text.isBlank()) {
            showMsg("No date entered.\n\nAnnual schedule not created.");
            return null;
        }

        try {
            return LocalDate.parse(text, DATE_FORMATTER);
        } catch (DateTimeParseException | NullPointerException e) {
            String msg = "Unable to parse date: '" + text + "'.\n\nAnnual schedule not created.";
            showMsg(msg);
            return null;
        }
    }

    private List<Item> getSelectedItems() {
        return comboBoxItems.entrySet().stream()
                .filter(entry -> entry.getKey().isSelected())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private void showMsg(String msg) {
        DialogContent dialogContent = new DialogContent().setContent(new Label(msg));
        dialogContent.getOkButton().setVisible(false);
        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parent);
        DialogBuilderUtil.armDialogContentButtons(dialogContent, DialogCallback::closeDialog);
    }

}
