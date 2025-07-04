package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.extras.theme.FontDef;
import dev.webfx.extras.theme.text.TextTheme;
import dev.webfx.extras.time.format.LocalizedTime;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Strings;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.extras.util.dialog.builder.DialogBuilderUtil;
import dev.webfx.extras.util.dialog.builder.DialogContent;
import dev.webfx.extras.util.dialog.DialogCallback;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.FontWeight;
import one.modality.base.client.time.BackOfficeTimeFormats;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ResourceConfiguration;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Site;
import one.modality.base.shared.entities.markers.EntityHasName;
import one.modality.crm.backoffice.organization.fx.FXOrganizationId;
import one.modality.hotel.backoffice.accommodation.ResourceConfigurationLoader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateAnnualSchedulePane extends VBox {

    private final ResourceConfigurationLoader resourceConfigurationLoader;
    private final Pane parent;
    private final TextField toDateTextField;
    private final TextField fromDateTextField;
    private final VBox selectedItemsPane = new VBox(new Label("Loading items. Please wait."));
    private final DateTimeFormatter dateFormatter = LocalizedTime.dateFormatter(BackOfficeTimeFormats.ANNUAL_SCHEDULE_DATE_FORMAT);

    private Map<CheckBox, Item> comboBoxItems;
    private Site site;

    public CreateAnnualSchedulePane(Pane parent, ResourceConfigurationLoader resourceConfigurationLoader) {
        this.parent = parent;
        this.resourceConfigurationLoader = resourceConfigurationLoader;
        setPadding(new Insets(16));
        Label headingLabel = new Label("Create Annual Schedule");
        headingLabel.setFont(TextTheme.getFont(FontDef.font(FontWeight.BOLD, 14)));
        Label fromLabel = new Label("From");
        fromDateTextField = new TextField();
        fromDateTextField.setPromptText("e.g. 16-01-22");
        fromDateTextField.textProperty().addListener(change -> setToDateUsingFromDate());
        Label toLabel = new Label("To");
        toDateTextField = new TextField();
        toDateTextField.setPromptText("e.g. 15-01-23");
        GridPane dateGridPane = new GridPane();
        dateGridPane.add(fromLabel, 0, 0);
        dateGridPane.add(fromDateTextField, 1, 0);
        dateGridPane.add(toLabel, 0, 1);
        dateGridPane.add(toDateTextField, 1, 1);
        getChildren().setAll(headingLabel, dateGridPane, selectedItemsPane);
        displayItemsWithOpenConfiguration();
    }

    private void setToDateUsingFromDate() {
        try {
            LocalDate fromDate = LocalDate.parse(fromDateTextField.getText(), dateFormatter);
            LocalDate toDate = fromDate.plusYears(1).minusDays(1);
            String toDateText = dateFormatter.format(toDate);
            toDateTextField.setText(toDateText);
        } catch (DateTimeParseException | NullPointerException e) {
            // If the user has not entered a valid date then do nothing
        }
    }

    private void displayItemsWithOpenConfiguration() {
        EntityId organizationId = FXOrganizationId.getOrganizationId();
        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        EntityStore.create(dataSourceModel).<ResourceConfiguration>executeQuery("select distinct rc.item.name, rc.resource.site from ResourceConfiguration rc where rc.item.family.code='acco' and rc.resource.site.organization.id=? and rc.resource.site.event is null and rc.endDate is null", organizationId)
                .onFailure(error -> {
                    Console.log("Error while reading scheduled items.", error);
                })
                .onSuccess(resourceConfigurations -> {
                    site = resourceConfigurations.iterator().next().getResource().getSite();
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

    public void confirmCreateAnnualSchedule() {
        LocalDate fromDate = getFromDate();
        if (fromDate == null) {
            return;
        }
        LocalDate toDate = getToDate();
        if (toDate == null) {
            return;
        }

        if (!toDate.isAfter(fromDate)) {
            showMsg("\"To\" date " + toDateTextField.getText() + " is not after \"from\" date " + fromDateTextField.getText() + ".\n\nAnnual schedule not created.");
            return;
        }

        List<Item> selectedItems = getSelectedItems();
        if (selectedItems.isEmpty()) {
            showMsg("No items selected.\n\nAnnual schedule not created.");
            return;
        }

        String fromDateString = dateFormatter.format(fromDate);
        String toDateString = dateFormatter.format(toDate);
        String commaSeparatedItemNames = selectedItems.stream()
                .map(EntityHasName::getName)
                .sorted()
                .collect(Collectors.joining("\n"));
        String msg = "Please confirm:\n\nAnnual snapshot will be created\n\nfrom " + fromDateString + "\nto " + toDateString
                + "\n\nFor the following items\n\n" + commaSeparatedItemNames + "\n\nThis process cannot be undone.";
        DialogContent dialogContent = new DialogContent().setContentText(msg);
        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parent);
        DialogBuilderUtil.armDialogContentButtons(dialogContent, dialogCallback -> {
            createAnnualSchedule(fromDate, toDate, selectedItems);
            dialogCallback.closeDialog();
        });
    }

    private LocalDate getFromDate() {
        String text = fromDateTextField.getText();
        if (Strings.isBlank(text)) {
            showMsg("No \"from\" date entered.\n\nAnnual schedule not created.");
            return null;
        } else {
            return parseDate(text);
        }
    }

    private LocalDate getToDate() {
        String text = toDateTextField.getText();
        if (Strings.isBlank(text)) {
            showMsg("No \"to\" date entered.\n\nAnnual schedule not created.");
            return null;
        } else {
            return parseDate(text);
        }
    }

    private LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text, dateFormatter);
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
        dialogContent.getPrimaryButton().setVisible(false);
        DialogBuilderUtil.showModalNodeInGoldLayout(dialogContent, parent);
        DialogBuilderUtil.armDialogContentButtons(dialogContent, DialogCallback::closeDialog);
    }

    private void createAnnualSchedule(LocalDate fromDate, LocalDate toDate, List<Item> selectedItems) {
        // Retrieve the latest ScheduledItem for each selected Item to use as a template for the ScheduledItem created in the annual snapshot
        String commaSeparatedItemIds = selectedItems.stream()
                .map(si -> si.getId().getPrimaryKey().toString())
                .collect(Collectors.joining(","));

        DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
        EntityStore.create(dataSourceModel).<ScheduledItem>executeQuery("select item.id, max(date) as date, available, online, resource from ScheduledItem si where item.id in (" + commaSeparatedItemIds + ") group by item.id, available, online, resource")
                .onFailure(error -> {
                    Console.log("Error while reading scheduled items.", error);
                })
                .onSuccess(latestScheduledItems -> {
                    UpdateStore updateStore = UpdateStore.createAbove(selectedItems.iterator().next().getStore());
                    AnnualScheduleDatabaseWriter writer = new AnnualScheduleDatabaseWriter(site, fromDate, toDate, selectedItems, latestScheduledItems, resourceConfigurationLoader);
                    FXProperties.runOnPropertyChange(percent ->
                        System.out.println(percent + "% complete.")
                    , writer.percentageCompleteProperty());
                    writer.saveToUpdateStore(updateStore);
                });
    }

}
