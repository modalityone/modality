package one.modality.event.backoffice.activities.pricing;

import dev.webfx.extras.styles.bootstrap.Bootstrap;
import dev.webfx.extras.time.TimeUtil;
import dev.webfx.kit.util.properties.FXProperties;
import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.Numbers;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.i18n.controls.I18nControls;
import dev.webfx.stack.orm.entity.UpdateStore;
import dev.webfx.stack.orm.entity.binding.EntityBindings;
import dev.webfx.stack.ui.operation.OperationUtil;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
    private final UpdateStore updateStore;
    private final SiteItem siteItem; // The one used to display prices
    private final List<LocalDate> itemDates;
    private final List<Rate> rates;
    private final List<TextField> rateFields;
    private final Button saveButton = Bootstrap.successButton(I18nControls.newButton(ModalityI18nKeys.Save));
    private final Button cancelButton = Bootstrap.secondaryButton(I18nControls.newButton(ModalityI18nKeys.Cancel));

    public AbstractItemFamilyPricing(KnownItemFamily knownItemFamily, Object itemFamilyI18nKey, PolicyAggregate eventPolicy) {
        //this.knownItemFamily = knownItemFamily;
        this.itemFamilyI18nKey = itemFamilyI18nKey;
        this.eventPolicy = eventPolicy;
        this.updateStore = UpdateStore.createAbove(eventPolicy.getEntityStore());
        EntityBindings.disableNodesWhenUpdateStoreHasNoChanges(updateStore, saveButton, cancelButton);
        List<ScheduledItem> scheduledItems = eventPolicy.getFamilyScheduledItems(knownItemFamily);
        List<SiteItem> siteItems = scheduledItems.stream().map(SiteItem::new).distinct().collect(Collectors.toList());
        siteItem = Collections.first(siteItems);

        Event event = eventPolicy.getEvent();
        itemDates = TimeUtil.generateLocalDates(event.getStartDate(), event.getEndDate());
        int datesCount = itemDates.size();
        rates = new ArrayList<>(datesCount);
        rateFields = new ArrayList<>(datesCount);
        // Following line commented as order by is now already done on the server side (see PolicyAggregate)
        //Entities.orderBy(eventPolicy.getRates(), Rate.site, Rate.item, Rate.perDay + " desc", Rate.startDate, Rate.endDate, Rate.price);
        resetInitialRates();
    }

    private void resetInitialRates() {
        rates.clear();
        List<Rate> policyRates = eventPolicy.getRates();
        itemDates.forEach(date -> {
            Rate dateRate = Collections.findFirst(policyRates, rate -> rateMatchesItemFamilyPerDayAndDate(rate, date));
            rates.add(dateRate); // a null dateRate indicates that no rate has been defined so far for that date
        });
    }

    private boolean rateMatchesItemFamilyPerDayAndDate(Rate rate, LocalDate date) {
        return rateMatchesSiteItemPerDay(rate) && Times.isBetween(date, rate.getStartDate(), rate.getEndDate());
    }

    private boolean rateMatchesSiteItemPerDay(Rate rate) {
        return siteItem != null && Objects.equals(rate.getSite(), siteItem.getSite()) && Objects.equals(rate.getItem(), siteItem.getItem()) && rate.isPerDay();
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
        GridPane gridPane = new GridPane(10, 10);
        for (int i = 0; i < itemDates.size(); i++) {
            TextField rateField = new TextField();
            rateField.setPrefWidth(80);
            rateField.setAlignment(Pos.CENTER_RIGHT);
            rateFields.add(rateField);
            LocalDate date = itemDates.get(i);
            Text dateText = new Text(date.toString());
            GridPane.setHalignment(dateText, HPos.RIGHT);
            gridPane.add(dateText, 0, i);
            gridPane.add(rateField, 1, i);
        }
        syncRatesUiFromModel();
        rateFields.forEach(rateField ->
            FXProperties.runOnPropertyChange(this::syncRatesModelFromUi, rateField.textProperty())
        );

        saveButton.setOnAction(e -> OperationUtil.turnOnButtonsWaitModeDuringExecution(
            updateStore.submitChanges()
                .onFailure(Console::log)
            , saveButton));

        cancelButton.setOnAction(e -> {
            updateStore.cancelChanges();
            resetInitialRates();
            syncRatesUiFromModel();
        });

        HBox buttonBar = new HBox(10, saveButton, cancelButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        return new VBox(
            gridPane,
            buttonBar
        );
    }

    private void syncRatesUiFromModel() {
        for (int i = 0; i < rateFields.size(); i++) {
            TextField rateField = rateFields.get(i);
            Rate rate = rates.get(i);
            if (rate != null)
                rateField.setText(EventPriceFormatter.formatWithoutCurrency(rate.getPrice()));
        }
    }

    private void syncRatesModelFromUi() {
        updateStore.cancelChanges(); // Should be rates only
        List<Rate> policyRates = eventPolicy.getRates(); // trying to modify the existing ones before creating new ones
        int lastUsedPolicyRateIndex = -1;
        Rate lastUpdatedRate = null;
        for (int i = 0; i < rateFields.size(); i++) {
            LocalDate date = itemDates.get(i);
            TextField rateField = rateFields.get(i);
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
                if (siteItem != null) { // Assuming it's always true...
                    rate.setSite(siteItem.getSite());
                    rate.setItem(siteItem.getItem());
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
        // Deleting remaining daily teaching rates (i.e. those not reused)
        for (int i = lastUsedPolicyRateIndex + 1; i < policyRates.size(); i ++) {
            Rate rate = policyRates.get(i);
            if (rateMatchesSiteItemPerDay(rate))
                updateStore.deleteEntity(rate);
        }
    }
}
