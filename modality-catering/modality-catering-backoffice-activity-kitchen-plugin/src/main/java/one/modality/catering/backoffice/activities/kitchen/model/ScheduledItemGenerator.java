package one.modality.catering.backoffice.activities.kitchen.model;

import dev.webfx.platform.async.Future;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.db.submit.SubmitArgument;
import dev.webfx.stack.db.submit.SubmitArgumentBuilder;
import dev.webfx.stack.db.submit.SubmitService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.entity.*;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Site;
import one.modality.base.shared.entities.Timeline;
import one.modality.base.shared.knownitems.KnownItemFamily;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Business logic for generating scheduled items.
 * Encapsulates two generation strategies:
 * 1. Entity-based generation (using UpdateStore)
 * 2. SQL-based generation (using SQL INSERT with timelines)
 *
 * @author Claude Code (Extracted from KitchenActivity)
 */
public final class ScheduledItemGenerator {

    private final DataSourceModel dataSourceModel;

    public ScheduledItemGenerator(DataSourceModel dataSourceModel) {
        this.dataSourceModel = dataSourceModel;
    }

    /**
     * Generates missing scheduledItems for all meal items in the selected month.
     * This method:
     * 1. Queries for existing scheduledItems in the selected month
     * 2. Queries for all meal items (family.code = 'meals')
     * 3. Queries for the main site for the organization
     * 4. Creates scheduledItems for any missing (item, date) combinations
     * 5. Submits changes to the database
     *
     * @return Future<Integer> number of scheduled items created
     */
    public Future<Integer> generateMissingScheduledItems(EntityId organizationId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        EntityStore entityStore = EntityStore.create(dataSourceModel);

        return entityStore.executeQueryBatch(
                // Query 0: Existing scheduled items for the month
                new EntityStoreQuery(
                        "select id, item, date from ScheduledItem si where site.organization=? and si.date between ? and ? and item.family.code=?",
                        organizationId.getPrimaryKey(), startDate, endDate, KnownItemFamily.MEALS.getCode()),

                // Query 1: All meal items for the organization
                new EntityStoreQuery("select id, name from Item where organization=? and family.code=? order by name",
                        organizationId.getPrimaryKey(), KnownItemFamily.MEALS.getCode()),

                // Query 2: Main site for the organization
                new EntityStoreQuery("select id, name from Site where organization=? and main=true limit 1",
                        organizationId.getPrimaryKey()))
                .flatMap(results -> {
                    EntityList<ScheduledItem> existingScheduledItems = results[0];
                    EntityList<Item> mealItems = results[1];
                    EntityList<Site> sites = results[2];

                    if (sites.isEmpty()) {
                        Console.log("No main site found for organization");
                        return Future.failedFuture(new IllegalStateException("No main site found for organization"));
                    }

                    if (mealItems.isEmpty()) {
                        Console.log("No meal items found for organization");
                        return Future.succeededFuture(0);
                    }

                    Site mainSite = sites.get(0);

                    // Build a set of existing (item, date) combinations
                    Set<String> existingCombinations = new HashSet<>();
                    for (ScheduledItem si : existingScheduledItems) {
                        String key = si.getItem().getPrimaryKey() + "|" + si.getDate();
                        existingCombinations.add(key);
                    }

                    // Create update store for insertions
                    UpdateStore updateStore = UpdateStore.create(dataSourceModel);
                    int insertCount = 0;

                    // For each meal item and date in the month, create if missing
                    for (Item mealItem : mealItems) {
                        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                            String key = mealItem.getPrimaryKey() + "|" + date;
                            if (!existingCombinations.contains(key)) {
                                ScheduledItem newScheduledItem = updateStore.insertEntity(ScheduledItem.class);
                                newScheduledItem.setItem(mealItem);
                                newScheduledItem.setDate(date);
                                newScheduledItem.setSite(mainSite);
                                insertCount++;
                            }
                        }
                    }

                    final int finalInsertCount = insertCount;
                    if (insertCount > 0) {
                        Console.log("Creating " + insertCount + " scheduled items...");
                        return updateStore.submitChanges()
                                .map(result -> {
                                    Console.log("Successfully created " + finalInsertCount + " scheduled items");
                                    return finalInsertCount;
                                });
                    } else {
                        Console.log("All scheduled items already exist for the selected month");
                        return Future.succeededFuture(0);
                    }
                });
    }

    /**
     * Generates scheduledItems for the selected month using SQL INSERT statements
     * based on timelines from the organization's global site.
     *
     * @return Future<Void> completes when all scheduled items are generated
     */
    public Future<Void> generateScheduledItemsFromTimelines(EntityId organizationId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        EntityStore entityStore = EntityStore.create(dataSourceModel);

        Console.log("Querying timelines for organization " + organizationId.getPrimaryKey());

        return entityStore.executeQuery(
                new EntityStoreQuery(
                        "select id, site, item, startTime, endTime, itemFamily from Timeline where site=(select globalSite from Organization where id=?)",
                        organizationId))
                .flatMap(timelines -> {
                    if (timelines.isEmpty()) {
                        Console.log("No timelines found for organization's global site");
                        return Future.failedFuture(new IllegalStateException("No timelines found for organization's global site"));
                    }

                    Console.log("Found " + timelines.size() + " timelines");

                    // Filter timelines to only meal timelines (those with specific items)
                    List<Timeline> mealTimelines = new ArrayList<>();
                    for (Entity timeline : timelines) {
                        Timeline currentTimeline = (Timeline) timeline;
                        if (currentTimeline.getItem() != null) {
                            mealTimelines.add(currentTimeline);
                            Console.log("  Meal timeline: item_id=" + currentTimeline.getItem().getPrimaryKey() +
                                    ", start=" + currentTimeline.getStartTime() + ", end=" + currentTimeline.getEndTime());
                        } else if (currentTimeline.getItemFamily() != null) {
                            Console.log("  Accommodation timeline: family_id=" + currentTimeline.getItemFamily().getPrimaryKey() +
                                    ", start=" + currentTimeline.getStartTime() + ", end=" + currentTimeline.getEndTime());
                        }
                    }

                    if (mealTimelines.isEmpty()) {
                        Console.log("No meal timelines found");
                        return Future.failedFuture(new IllegalStateException("No meal timelines found"));
                    }

                    return generateScheduledItemsForMeals(mealTimelines, startDate, endDate, 0);
                });
    }

    /**
     * Recursively generates scheduled items for each meal timeline.
     */
    private Future<Void> generateScheduledItemsForMeals(
            List<Timeline> mealTimelines,
            LocalDate startDate,
            LocalDate endDate,
            int index) {

        if (index >= mealTimelines.size()) {
            Console.log("Completed generating scheduled items for all timelines");
            return Future.succeededFuture(null);
        }

        Timeline timeline = mealTimelines.get(index);

        SubmitArgument submitArgument = new SubmitArgumentBuilder()
                .setDataSourceId(dataSourceModel.getDataSourceId())
                .setStatement(
                        "insert into scheduled_item (date, timeline_id, event_id, site_id, item_id, start_time, end_time, resource) "
                                +
                                "select day::date, id, null, site_id, item_id, start_time, end_time, false " +
                                "from timeline, generate_series($1::date, $2::date, interval '1 day') as day " +
                                "where id = $3")
                .setParameters(startDate, endDate, timeline.getPrimaryKey())
                .build();

        return SubmitService.executeSubmit(submitArgument)
                .compose(result -> {
                    Console.log("Successfully created scheduled items for timeline " + timeline.getPrimaryKey());
                    return generateScheduledItemsForMeals(mealTimelines, startDate, endDate, index + 1);
                }, error -> {
                    Console.log("Error creating scheduled items for timeline " + timeline.getPrimaryKey() + ": "
                            + error.getMessage());
                    // Continue with next timeline even if this one fails
                    return generateScheduledItemsForMeals(mealTimelines, startDate, endDate, index + 1);
                });
    }
}
