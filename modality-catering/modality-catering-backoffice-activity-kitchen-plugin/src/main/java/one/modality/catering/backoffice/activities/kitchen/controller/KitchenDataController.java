package one.modality.catering.backoffice.activities.kitchen.controller;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityStore;
import one.modality.catering.backoffice.activities.kitchen.model.KitchenData;
import one.modality.catering.backoffice.activities.kitchen.model.KitchenDisplayModel;
import one.modality.catering.backoffice.activities.kitchen.service.KitchenDataService;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Controls data loading and processing flow.
 * Bridges KitchenDataService and KitchenDisplayModel.
 *
 * @author Claude Code (Extracted from KitchenActivity)
 */
public final class KitchenDataController {

    private final DataSourceModel dataSourceModel;
    private KitchenDisplayModel currentDisplayModel;

    public KitchenDataController(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
        this.currentDisplayModel = KitchenDisplayModel.empty();
    }

    /**
     * Loads kitchen data for the specified organization and date range.
     * Transforms the data into a display model suitable for UI rendering.
     *
     * @return Future<KitchenDisplayModel> the display model when loading completes
     */
    public Future<KitchenDisplayModel> loadKitchenData(EntityId organizationId, LocalDate startDate,
            LocalDate endDate) {
        Console.log("loadKitchenData called: " + startDate + " to " + endDate + ", org=" + organizationId);

        if (startDate == null || endDate == null || organizationId == null) {
            Console.log("Cannot load kitchen data: dates or organization not provided");
            return Future.succeededFuture(KitchenDisplayModel.empty());
        }

        EntityStore entityStore = EntityStore.create(dataSourceModel);

        Console.log("Loading kitchen data using DSQL queries from " + startDate + " to " + endDate);
        return KitchenDataService.loadKitchenData(entityStore, organizationId, startDate, endDate)
                .map(kitchenData -> {
                    Console.log(
                            "KitchenDataService returned kitchenData with " + kitchenData.getDates().size() + " dates");
                    Console.log("KitchenData dietary items: " + kitchenData.getDietaryItems().size());
                    Console.log("KitchenData dates: " + kitchenData.getDates());

                    // Transform KitchenData into KitchenDisplayModel
                    currentDisplayModel = KitchenDisplayModel.from(kitchenData);
                    Console.log("KitchenDisplayModel created with "
                            + currentDisplayModel.getAttendanceCounts().getDates().size() + " dates");
                    return currentDisplayModel;
                })
                .recover(error -> {
                    Console.log("Error loading kitchen data: " + error);
                    if (error instanceof Throwable) {
                        ((Throwable) error).printStackTrace();
                    }
                    currentDisplayModel = KitchenDisplayModel.empty();
                    return Future.succeededFuture(currentDisplayModel);
                });
    }

    /**
     * Returns the current display model.
     */
    public KitchenDisplayModel getCurrentDisplayModel() {
        return currentDisplayModel;
    }
}
