package one.modality.event.backoffice.activities.pricing;

import dev.webfx.extras.i18n.controls.I18nControls;
import dev.webfx.extras.async.AsyncSpinner;
import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.TimeUtil;
import dev.webfx.extras.validation.ValidationSupport;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.kit.util.properties.ObservableLists;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.util.collection.HashList;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import one.modality.base.client.i18n.BaseI18nKeys;
import one.modality.base.shared.domainmodel.formatters.PriceFormatter;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.SiteItem;
import one.modality.base.shared.entities.formatters.EventPriceFormatter;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.ecommerce.document.service.PolicyAggregate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
abstract class AbstractItemFamilyPricing implements ItemFamilyPricing {

    private final Object itemFamilyI18nKey;
    private final PolicyAggregate eventPolicy;
    private final boolean everyday; // if false, checkboxes will appear in front of each date (ex: false for Audio Recording)
    private final UpdateStore updateStore;
    private final List<DateUI> dateUIs;
    // List containing all options declared at the organization level that could eventually be offered for booking in
    // that item family (ex: for audio recording -> English, German, French, etc...). Each option is actually represented
    // by a SiteItem (the site being probably the venue).
    protected final ObservableList<SiteItem> availableSiteItems = FXCollections.observableArrayList();
    private final ObservableList<SiteItemUI> availableSiteItemUIs = FXCollections.observableArrayList(); {
        ObservableLists.bindConverted(availableSiteItemUIs, availableSiteItems, SiteItemUI::new);
    }
    // List containing all options already recorded in the database for this event (meaning that their ScheduledItem
    // have already been created).
    private final List<SiteItem> initialSelectedSiteItems;
    // If the managers add or remove some options, this list will contain their most recent selection.
    private final List<SiteItem> selectedSiteItems = new ArrayList<>();
    // Map associating each option to its model (containing the rates and scheduled items for that option). This map
    // may contain more options than selectedSiteItems, because it also contains the options that have been removed
    private final Map<SiteItem, SiteItemModel> siteItemModels = new HashMap<>();
    private final List<ScheduledItem> initialScheduledItems;

    private boolean syncingUI;
    private final Button saveButton = Bootstrap.successButton(I18nControls.newButton(BaseI18nKeys.Save));
    private final Button cancelButton = Bootstrap.secondaryButton(I18nControls.newButton(BaseI18nKeys.Cancel));

    public AbstractItemFamilyPricing(KnownItemFamily knownItemFamily, Object itemFamilyI18nKey, PolicyAggregate eventPolicy, boolean everyday) {
        this.itemFamilyI18nKey = itemFamilyI18nKey;
        this.eventPolicy = eventPolicy;
        this.everyday = everyday;
        this.updateStore = UpdateStore.createAbove(eventPolicy.getEntityStore());
        EntityBindings.disableNodesWhenUpdateStoreHasNoChanges(updateStore, saveButton, cancelButton);
        initialScheduledItems = eventPolicy.getFamilyScheduledItems(knownItemFamily);
        initialSelectedSiteItems = initialScheduledItems.stream().map(SiteItem::new).distinct().collect(Collectors.toList());

        Event event = getEvent();
        List<LocalDate> itemDates = TimeUtil.generateLocalDates(event.getStartDate(), event.getEndDate());
        dateUIs = Collections.map(itemDates, DateUI::new);

        for (SiteItem siteItem : initialSelectedSiteItems) {
            siteItemModels.put(siteItem, new SiteItemModel(siteItem));
        }

        resetToInitialValues();

        // In case the availableSiteItems have been loaded already, we need to reinitialize the checkboxes to tell which
        availableSiteItemUIs.forEach(SiteItemUI::initCheckBoxState); // ones are selected (from initialSelectedSiteItems)
    }

    protected Event getEvent() {
        return eventPolicy.getEvent();
    }

    private void resetToInitialValues() {
        Collections.setAll(selectedSiteItems, initialSelectedSiteItems);
        siteItemModels.values().forEach(SiteItemModel::resetToInitialValues);
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
        FlowPane flowPane = new FlowPane(20, 5);
        ObservableLists.bindConverted(flowPane.getChildren(), availableSiteItemUIs, siteItemUI -> siteItemUI.checkBox);
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

        saveButton.setOnAction(e -> {
                if (dateUIs.stream().allMatch(DateUI::isValid)) {
                    AsyncSpinner.displayButtonSpinnerDuringAsyncExecution(
                        updateStore.submitChanges()
                            .onFailure(Console::log)
                        , saveButton);
                }
            }
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
        syncingUI = true;
        for (SiteItemUI siteItemUI : availableSiteItemUIs) {
            siteItemUI.checkBox.setSelected(selectedSiteItems.contains(siteItemUI.siteItem));
        }

        // All the selected options share the same dates and rates (in reality the ScheduledItem and Rate will be created
        // in the database for each SiteItem, but with the same information regarding dates and rates). So in the UI, we
        // show this information (dates and rates) only once. referenceSiteItem is the sample that we will use for this.
        SiteItem referenceSiteItem = Collections.first(selectedSiteItems);
        SiteItemModel referenceSiteItemModel = siteItemModels.get(referenceSiteItem);
        for (int i = 0; i < dateUIs.size(); i++) {
            DateUI dateUI = dateUIs.get(i);
            TextField rateField = dateUI.rateField;
            Rate rate = referenceSiteItem == null ? null : referenceSiteItemModel.rates.get(i);
            rateField.setDisable(referenceSiteItemModel == null);
            if (rate != null)
                rateField.setText(EventPriceFormatter.formatWithoutCurrency(rate.getPrice()));
            CheckBox dateCheckBox = dateUI.dateCheckBox;
            ScheduledItem scheduledItem = referenceSiteItemModel == null ? null : referenceSiteItemModel.scheduledItems.get(i);
            dateCheckBox.setSelected(scheduledItem != null);
        }
        syncingUI = false;
    }

    private void syncModelFromUi() {
        if (syncingUI)
            return;
        updateStore.cancelChanges();
        // Synchronizing SiteItems that have been eventually modified, removed or newly created.
        // To achieve this, we will iterate over all SiteItems (initialSelectedSiteItems + selectedSiteItems)
        HashList<SiteItem> allSiteItems = new HashList<>(initialSelectedSiteItems);
        allSiteItems.addAll(selectedSiteItems); // there won't be any duplicate because we use a HashList
        for (SiteItem siteItem : allSiteItems) {
            SiteItemModel siteItemModel = siteItemModels.computeIfAbsent(siteItem, SiteItemModel::new);
            // If the siteItem has been removed, we will delete all its scheduled items and rates
            boolean removed = !selectedSiteItems.contains(siteItem);
            if (removed) {
                siteItemModel.initialScheduledItems.forEach(updateStore::deleteEntity);
                siteItemModel.initialRates.forEach(updateStore::deleteEntity);
                continue; // nothing more to do for this siteItem
            }
            // If we reach this points, it's because the siteItem has been added or eventually modified
            // We will try to reuse existing rates if possible (and eventually update them), otherwise we will create new ones
            List<Rate> initialRates = siteItemModel.initialRates; // from policy rates, kept only those related to this siteItem
            int lastUsedInitialRateIndex = -1;
            Rate lastUpdatedRate = null;
            for (int i = 0; i < dateUIs.size(); i++) {
                ScheduledItem scheduledItem = siteItemModel.scheduledItems.get(i);
                DateUI dateUI = dateUIs.get(i);
                CheckBox dateCheckBox = dateUI.dateCheckBox;
                if (!dateCheckBox.isSelected()) {
                    if (scheduledItem != null)
                        updateStore.deleteEntity(scheduledItem);
                    continue;
                }
                LocalDate date = dateUI.date;
                TextField rateField = dateUI.rateField;
                if (scheduledItem == null) {
                    scheduledItem = updateStore.insertEntity(ScheduledItem.class);
                    scheduledItem.setEvent(eventPolicy.getEvent());
                    scheduledItem.setSite(siteItem.getSite());
                    scheduledItem.setItem(siteItem.getItem());
                    scheduledItem.setDate(date);
                }
                PriceFormatter priceFormatter = EventPriceFormatter.INSTANCE;
                Object price = priceFormatter.parseValue(rateField.getText());
                Rate rate = null;
                if (lastUpdatedRate != null && Numbers.identicalObjectsOrNumberValues(price, lastUpdatedRate.getPrice())) {
                    rate = lastUpdatedRate; // continuing using same rate (extending its end date)
                } else if (lastUsedInitialRateIndex < initialRates.size() - 1) {
                    rate = Collections.findFirst(initialRates, lastUsedInitialRateIndex + 1, r -> siteItemModel.rateMatchesSiteItemPerDayAndDate(r, date));
                    if (rate != null) {
                        Collections.swap(initialRates, ++lastUsedInitialRateIndex, initialRates.indexOf(rate));
                        while (Collections.get(initialRates, lastUsedInitialRateIndex + 1) == rate)
                            lastUsedInitialRateIndex++;
                        rate = updateStore.updateEntity(rate);
                    }
                }
                if (rate == null) {
                    rate = updateStore.insertEntity(Rate.class);
                    rate.setSite(siteItem.getSite());
                    rate.setItem(siteItem.getItem());
                    rate.setStartDate(date);
                }
                // If we took an existing rate, we may have to adjust its start date
                if (rate.getStartDate() != null && rate.getStartDate().isAfter(date))
                    rate.setStartDate(date);
                rate.setPrice(price);
                rate.setEndDate(date);
                completeRate(rate);
                lastUpdatedRate = rate;
            }
            // Deleting remaining daily teaching rates (i.e. those not reused)
            for (int i = lastUsedInitialRateIndex + 1; i < initialRates.size(); i++) {
                Rate rate = initialRates.get(i);
                if (rate != null && !Entities.sameId(rate, lastUpdatedRate))
                    updateStore.deleteEntity(rate);
            }
        }
    }

    protected void completeRate(Rate rate) {
        rate.setPerDay(true);
        rate.setPerPerson(true);
        // Hardcoded 100% min deposit for online events (to change this when in-person and online events are merged)
        if (getEvent().isOnlineEvent())
            rate.setMinDeposit(100);
    }

    // Class containing the UI elements associated to a date (checkbox, text and rate text field)
    private class DateUI {
        private final LocalDate date;
        private final Text dateText;
        private final CheckBox dateCheckBox = new CheckBox();
        private final TextField rateField = new TextField();
        private final ValidationSupport validationSupport = new ValidationSupport();

        DateUI(LocalDate date) {
            this.date = date;
            dateText = new Text(date.toString());
            rateField.setPrefWidth(80);
            rateField.setAlignment(Pos.CENTER_RIGHT);
            dateCheckBox.setVisible(!everyday);
            FXProperties.runOnPropertyChange(AbstractItemFamilyPricing.this::syncModelFromUi, rateField.textProperty());
            if (!everyday) {
                rateField.visibleProperty().bind(dateCheckBox.selectedProperty());
                FXProperties.runOnPropertyChange(AbstractItemFamilyPricing.this::syncModelFromUi, dateCheckBox.selectedProperty());
            }
            validationSupport.addRequiredInput(rateField);
        }

        boolean isValid() {
            return !dateCheckBox.isSelected() || validationSupport.isValid();
        }
    }

    // Class containing the UI elements associated to a SiteIem (checkbox with text)
    private class SiteItemUI {
        private final SiteItem siteItem;
        private final CheckBox checkBox;

        SiteItemUI(SiteItem siteItem) { // Created when availableSiteItems is populated
            this.siteItem = siteItem;
            checkBox = new CheckBox(siteItem.getItem().getName());
            initCheckBoxState(); // selectedSiteItems may have been loaded at this time or not
            FXProperties.runOnPropertyChange(() -> {
                Collections.addIfNotContainsOrRemove(selectedSiteItems, checkBox.isSelected(), siteItem);
                syncModelFromUi();
                boolean empty = selectedSiteItems.isEmpty();
                for (DateUI dateUI : dateUIs) {
                    dateUI.rateField.setDisable(empty);
                }
            }, checkBox.selectedProperty());
        }

        void initCheckBoxState() { // called again when selectedSiteItems have been loaded
            checkBox.setSelected(selectedSiteItems.contains(siteItem));
        }
    }

    // Class containing the information (model) associated to a SiteItem.
    private class SiteItemModel {
        private final SiteItem siteItem;
        private final List<Rate> initialRates;
        private final List<Rate> rates; // Maybe always identical to initialRates => remove it?
        private final List<ScheduledItem> initialScheduledItems;
        private final List<ScheduledItem> scheduledItems; // Maybe always identical to initialScheduledItems => remove it?

        SiteItemModel(SiteItem siteItem) {
            this.siteItem = siteItem;
            int datesCount = dateUIs.size();
            initialRates = new ArrayList<>(datesCount);
            rates = new ArrayList<>(datesCount);

            initialScheduledItems = new ArrayList<>(datesCount);
            scheduledItems = new ArrayList<>(datesCount);

            List<Rate> policyRates = eventPolicy.getRates();
            dateUIs.forEach(dateUI -> {
                LocalDate date = dateUI.date;
                Rate dateRate = Collections.findFirst(policyRates, rate -> rateMatchesSiteItemPerDayAndDate(rate, date));
                initialRates.add(dateRate); // a null dateRate indicates that no rate has been defined so far for that date
                ScheduledItem dateScheduledItem = Collections.findFirst(AbstractItemFamilyPricing.this.initialScheduledItems, si -> scheduledItemMatchesSiteItemAndDate(si, date));
                initialScheduledItems.add(dateScheduledItem);
            });

            resetToInitialValues();
        }

        void resetToInitialValues() {
            Collections.setAll(rates, initialRates);
            Collections.setAll(scheduledItems, initialScheduledItems);
        }

        boolean rateMatchesSiteItemPerDayAndDate(Rate rate, LocalDate date) {
            return rateMatchesSiteItemPerDay(rate) && Times.isBetween(date, rate.getStartDate(), rate.getEndDate());
        }

        private boolean rateMatchesSiteItemPerDay(Rate rate) {
            return siteItem != null && rate != null && Objects.equals(rate.getSite(), siteItem.getSite()) && Objects.equals(rate.getItem(), siteItem.getItem()) && rate.isPerDay();
        }

        boolean scheduledItemMatchesSiteItemAndDate(ScheduledItem si, LocalDate date) {
            return Objects.equals(si.getSite(), siteItem.getSite()) && Objects.equals(si.getItem(), siteItem.getItem()) && Objects.equals(si.getDate(), date);
        }
    }

}
