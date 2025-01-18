package one.modality.event.backoffice.activities.pricing;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.TimeUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import one.modality.base.client.i18n.ModalityI18nKeys;
import one.modality.base.shared.domainmodel.formatters.PriceFormatter;
import one.modality.base.shared.entities.*;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.ecommerce.document.service.PolicyAggregate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
abstract class AbstractItemFamilyPricing implements ItemFamilyPricing {

    //private final KnownItemFamily knownItemFamily;
    private final Object itemFamilyI18nKey;
    private final PolicyAggregate eventPolicy;
    private final boolean partialAllowed; // if yes, checkboxes will appear in front of each date (false for teaching, true for Audio Recording)
    private final UpdateStore updateStore;
    private final List<DateUI> dateUIs;
    // List containing all options known at the organization level that could possibly be offered for booking in that
    // item family (ex: for audio recording -> English, German, French, etc...). Each option is actually represented by
    // a SiteItem (the site being probably the venue).
    protected final ObservableList<SiteItem> availableSiteItems = FXCollections.observableArrayList();
    private final ObservableList<SiteItemUI> availableSiteItemUIs = FXCollections.observableArrayList(); {
        ObservableLists.bindConverted(availableSiteItemUIs, availableSiteItems, SiteItemUI::new);
    }
    // List containing all options already recorded in the database for this event (meaning that their ScheduledItem
    // have already been created).
    private final List<SiteItem> initialSelectedSiteItems;
    // If the managers add or remove some options, this list will contain their most recent selection.
    private final ObservableList<SiteItem> selectedSiteItems = FXCollections.observableArrayList();
    // All the selected options share the same dates and rates (in reality the ScheduledItem and Rate will be created
    // in the database for each SiteItem, but with the same information regarding dates and rates). So in the UI, we
    // show this information (dates and rates) only once. referenceSiteItem is the sample that we will use for this.
    private final SiteItem referenceSiteItem;
    private final List<Rate> initialRates;
    private final List<Rate> rates;
    private final List<ScheduledItem> initialScheduledItems;
    private final List<ScheduledItem> scheduledItems;
    private final Button saveButton = Bootstrap.successButton(I18nControls.newButton(ModalityI18nKeys.Save));
    private final Button cancelButton = Bootstrap.secondaryButton(I18nControls.newButton(ModalityI18nKeys.Cancel));

    public AbstractItemFamilyPricing(KnownItemFamily knownItemFamily, Object itemFamilyI18nKey, PolicyAggregate eventPolicy, boolean partialAllowed) {
        //this.knownItemFamily = knownItemFamily;
        this.itemFamilyI18nKey = itemFamilyI18nKey;
        this.eventPolicy = eventPolicy;
        this.partialAllowed = partialAllowed;
        this.updateStore = UpdateStore.createAbove(eventPolicy.getEntityStore());
        EntityBindings.disableNodesWhenUpdateStoreHasNoChanges(updateStore, saveButton, cancelButton);
        List<ScheduledItem> loadedFamilyScheduledItems = eventPolicy.getFamilyScheduledItems(knownItemFamily);
        initialSelectedSiteItems = loadedFamilyScheduledItems.stream().map(SiteItem::new).distinct().collect(Collectors.toList());
        referenceSiteItem = Collections.first(initialSelectedSiteItems);

        Event event = eventPolicy.getEvent();
        List<LocalDate> itemDates = TimeUtil.generateLocalDates(event.getStartDate(), event.getEndDate());
        dateUIs = Collections.map(itemDates, DateUI::new);
        int datesCount = itemDates.size();
        initialRates = new ArrayList<>(datesCount);
        rates = new ArrayList<>(datesCount);

        initialScheduledItems = new ArrayList<>(datesCount);
        scheduledItems = new ArrayList<>(datesCount);
        // Following line commented as order by is now already done on the server side (see PolicyAggregate)
        //Entities.orderBy(eventPolicy.getRates(), Rate.site, Rate.item, Rate.perDay + " desc", Rate.startDate, Rate.endDate, Rate.price);

        List<Rate> policyRates = eventPolicy.getRates();
        itemDates.forEach(date -> {
            Rate dateRate = Collections.findFirst(policyRates, rate -> rateMatchesItemFamilyPerDayAndDate(rate, date));
            initialRates.add(dateRate); // a null dateRate indicates that no rate has been defined so far for that date
            ScheduledItem dateScheduledItem = Collections.findFirst(loadedFamilyScheduledItems, si -> Objects.equals(si.getSite(), referenceSiteItem.getSite()) && Objects.equals(si.getItem(), referenceSiteItem.getItem()) && Objects.equals(si.getDate(), date));
            initialScheduledItems.add(dateScheduledItem);
        });

        resetToInitialValues();
    }

    private void resetToInitialValues() {
        Collections.setAll(selectedSiteItems, initialSelectedSiteItems);
        Collections.setAll(rates, initialRates);
        Collections.setAll(scheduledItems, initialScheduledItems);
    }

    private boolean rateMatchesItemFamilyPerDayAndDate(Rate rate, LocalDate date) {
        return rateMatchesSiteItemPerDay(rate) && Times.isBetween(date, rate.getStartDate(), rate.getEndDate());
    }

    private boolean rateMatchesSiteItemPerDay(Rate rate) {
        return referenceSiteItem != null && Objects.equals(rate.getSite(), referenceSiteItem.getSite()) && Objects.equals(rate.getItem(), referenceSiteItem.getItem()) && rate.isPerDay();
    }

    @Override
    public boolean hasChanges() {
        return updateStore.hasChanges();
    }

    @Override
    public Node getHeaderNode() {
        return I18nControls.newLabel(itemFamilyI18nKey);
    }

    @Override
    public Node getContentNode() {
        FlowPane flowPane = new FlowPane(10, 5);
        ObservableLists.bindConverted(flowPane.getChildren(), availableSiteItemUIs, siteItemUI -> siteItemUI.container);
        VBox.setMargin(flowPane, new Insets(0, 0, 20, 0));

        GridPane gridPane = new GridPane(10, 10);
        for (int i = 0; i < dateUIs.size(); i++) {
            DateUI dateUI = dateUIs.get(i);
            GridPane.setHalignment(dateUI.dateText, HPos.RIGHT);
            gridPane.add(dateUI.dateCheckBox, 0, i);
            gridPane.add(dateUI.dateText, 1, i);
            gridPane.add(dateUI.rateField, 2, i);
        }
        syncUiFromModel();

        saveButton.setOnAction(e -> OperationUtil.turnOnButtonsWaitModeDuringExecution(
            updateStore.submitChanges()
                .onFailure(Console::log)
            , saveButton)
        );

        cancelButton.setOnAction(e -> {
            updateStore.cancelChanges();
            resetToInitialValues();
            syncUiFromModel();
        });

        HBox buttonBar = new HBox(10, saveButton, cancelButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        return new VBox(
            flowPane,
            gridPane,
            buttonBar
        );
    }

    private void syncUiFromModel() {
        for (SiteItemUI siteItemUI : availableSiteItemUIs) {
            siteItemUI.checkBox.setSelected(selectedSiteItems.contains(siteItemUI.siteItem));
        }
        for (int i = 0; i < dateUIs.size(); i++) {
            DateUI dateUI = dateUIs.get(i);
            TextField rateField = dateUI.rateField;
            Rate rate = rates.get(i);
            if (rate != null)
                rateField.setText(EventPriceFormatter.formatWithoutCurrency(rate.getPrice()));
            CheckBox dateCheckBox = dateUI.dateCheckBox;
            ScheduledItem scheduledItem = scheduledItems.get(i);
            dateCheckBox.setSelected(scheduledItem != null);
        }
    }

    private void syncModelFromUi() {
        updateStore.cancelChanges(); // Should be rates only
        List<Rate> policyRates = eventPolicy.getRates(); // trying to modify the existing ones before creating new ones
        int lastUsedPolicyRateIndex = -1;
        Rate lastUpdatedRate = null;
        for (int i = 0; i < dateUIs.size(); i++) {
            DateUI dateUI = dateUIs.get(i);
            LocalDate date = dateUI.date;
            TextField rateField = dateUI.rateField;
            CheckBox dateCheckBox = dateUI.dateCheckBox;
            ScheduledItem scheduledItem = scheduledItems.get(i);
            if (!dateCheckBox.isSelected()) {
                if (scheduledItem != null)
                    updateStore.deleteEntity(scheduledItem);
            } else {
                if (scheduledItem == null) {
                    scheduledItem = updateStore.insertEntity(ScheduledItem.class);
                    scheduledItem.setDate(date);
                    if (referenceSiteItem != null) { // Assuming it's always true...
                        scheduledItem.setSite(referenceSiteItem.getSite());
                        scheduledItem.setItem(referenceSiteItem.getItem());
                    }
                }
                PriceFormatter priceFormatter = EventPriceFormatter.INSTANCE;
                Object price = priceFormatter.parseValue(rateField.getText());
                Rate rate = null;
                if (lastUpdatedRate != null && Numbers.identicalObjectsOrNumberValues(price, lastUpdatedRate.getPrice())) {
                    rate = lastUpdatedRate; // continuing using same rate (extending its end date)
                } else if (lastUsedPolicyRateIndex < policyRates.size() - 1) {
                    rate = Collections.findFirst(policyRates, lastUsedPolicyRateIndex + 1, r -> rateMatchesItemFamilyPerDayAndDate(r, date));
                    if (rate != null) {
                        Collections.swap(policyRates, ++lastUsedPolicyRateIndex, policyRates.indexOf(rate));
                        rate = updateStore.updateEntity(rate);
                    }
                }
                if (rate == null) {
                    rate = updateStore.insertEntity(Rate.class);
                    // TODO: cutoff dates
                    if (referenceSiteItem != null) { // Assuming it's always true...
                        rate.setSite(referenceSiteItem.getSite());
                        rate.setItem(referenceSiteItem.getItem());
                    }
                }
                rate.setPrice(price);
                rate.setStartDate(date);
                rate.setEndDate(date);
                rate.setPerDay(true);
                rate.setPerPerson(true);
                rate.setAge1Max(7);
                rate.setAge1Price(0);
                rate.setAge2Max(15);
                rate.setAge2Discount(50);
                rate.setResidentPrice(0);
                rate.setResident2Discount(50);
                lastUpdatedRate = rate;
            }
        }
        // Deleting remaining daily teaching rates (i.e. those not reused)
        for (int i = lastUsedPolicyRateIndex + 1; i < policyRates.size(); i ++) {
            Rate rate = policyRates.get(i);
            if (rateMatchesSiteItemPerDay(rate))
                updateStore.deleteEntity(rate);
        }
    }

    private class DateUI {
        private final LocalDate date;
        private final Text dateText;
        private final CheckBox dateCheckBox = new CheckBox();
        private final TextField rateField = new TextField();

        DateUI(LocalDate date) {
            this.date = date;
            dateText = new Text(date.toString());
            rateField.setPrefWidth(80);
            rateField.setAlignment(Pos.CENTER_RIGHT);
            dateCheckBox.setVisible(partialAllowed);
            FXProperties.runOnPropertyChange(AbstractItemFamilyPricing.this::syncModelFromUi, rateField.textProperty());
            if (partialAllowed) {
                rateField.visibleProperty().bind(dateCheckBox.selectedProperty());
                FXProperties.runOnPropertyChange(AbstractItemFamilyPricing.this::syncModelFromUi, dateCheckBox.selectedProperty());
            }
        }
    }

    private class SiteItemUI {
        private final SiteItem siteItem;
        private final CheckBox checkBox = new CheckBox();
        private final Label label;
        private final HBox container;

        public SiteItemUI(SiteItem siteItem) {
            this.siteItem = siteItem;
            label = new Label(siteItem.getItem().getName());
            container = new HBox(5, checkBox, label);
        }
    }

    private class SiteItemModel {
        private SiteItem siteItem;
        private ScheduledItem scheduledItem;
        private List<Rate> initialRates;
        private List<Rate> rates;

        void syncFromUI() {

        }
    }

}
