package one.modality.catering.backoffice.activities.kitchen.service;

import dev.webfx.platform.async.Future;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.EntityStoreQuery;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.knownitems.KnownItemFamily;
import one.modality.catering.backoffice.activities.kitchen.model.KitchenData;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for loading kitchen data using DSQL entity queries.
 * Fetches scheduled items, meals, dietary options, and attendance counts,
 * then processes them in Java using streams and filters.
 *
 * @author Bruno Salmon
 */
public class KitchenDataService {

    private static final String TOTAL_CODE = "Total";
    private static final String UNKNOWN_CODE = "?";

    /**
     * Load all kitchen data for a given organization and date range.
     * Uses multiple DSQL queries and processes data in Java.
     */
    public static Future<KitchenData> loadKitchenData(
            EntityStore entityStore,
            EntityId organizationId,
            LocalDate startDate,
            LocalDate endDate) {

        // Execute all queries in parallel for performance
        return entityStore.executeQueryBatch(
                // Query 0: Scheduled items for meals in the date range
                new EntityStoreQuery(
                        "select date, item.(id,name,ord), site " +
                                "from ScheduledItem " +
                                "where site.organization=? " +
                                "and item.family.code=? " +
                                "and date>=? and date<=? " +
                                "order by date, item.ord",
                        organizationId, KnownItemFamily.MEALS.getCode(), startDate, endDate),

                // Query 1: All meal items for the organization
                new EntityStoreQuery(
                        "select id, name, code, ord " +
                                "from Item " +
                                "where organization=? and family.code=? " +
                                "order by ord",
                        organizationId, KnownItemFamily.MEALS.getCode()),

                // Query 2: All dietary items for the organization
                new EntityStoreQuery(
                        "select id, name, code, ord, deprecated " +
                                "from Item " +
                                "where organization=? and family.code=? " +
                                "order by ord",
                        organizationId, KnownItemFamily.DIET.getCode()),

                // Query 3: All attendances for the date range with related data
                new EntityStoreQuery(
                        "select id, scheduledItem.(date,item), documentLine.(id,document,cancelled) " +
                                "from Attendance " +
                                "where scheduledItem.site.organization=? " +
                                "and scheduledItem.date>=? and scheduledItem.date<=?",
                        organizationId, startDate, endDate),

                // Query 4: All document lines for the documents involved in the attendances
                new EntityStoreQuery(
                        "select id, document, item, cancelled " +
                                "from DocumentLine " +
                                "where document in (select documentLine.document from Attendance where scheduledItem.site.organization=? and scheduledItem.date>=? and scheduledItem.date<=?)",
                        organizationId, startDate, endDate))
                .map(results -> processKitchenData(
                        entityStore,
                        results[0], // scheduled items
                        results[1], // meal items
                        results[2], // dietary items
                        results[3], // attendances
                        results[4] // document lines
                ));
    }

    /**
     * Process the raw entity lists into a clean KitchenData model.
     * All filtering, grouping, and counting is done in Java using streams.
     */
    private static KitchenData processKitchenData(
            EntityStore entityStore,
            EntityList<ScheduledItem> scheduledItems,
            EntityList<Item> mealItems,
            EntityList<Item> dietaryItems,
            EntityList<Attendance> attendances,
            EntityList<DocumentLine> allDocumentLines) {

        // Filter out deprecated dietary items and ALLINT
        List<Item> activeDietaryItems = dietaryItems.stream()
                .filter(item -> !Boolean.TRUE.equals(item.isDeprecated()))
                .filter(item -> !"ALLINT".equals(item.getCode()))
                .collect(Collectors.toList());

        KitchenData.Builder builder = KitchenData.builder()
                .scheduledItems(new ArrayList<>(scheduledItems))
                .mealItems()
                .dietaryItems(activeDietaryItems);

        // Index document lines by document ID for fast lookup
        Map<EntityId, List<DocumentLine>> documentLinesByDocumentId = allDocumentLines.stream()
                .filter(dl -> dl.getDocument() != null)
                .collect(Collectors.groupingBy(dl -> dl.getDocument().getId()));

        // Add SVG for Total icon
        builder.dietaryOptionSvg(TOTAL_CODE,
                "{fill: '#828788', svgPath: 'm 0.971924,10.7805 c 0,-2.85307 1.133386,-5.5893 3.150816,-7.60673 2.01743,-2.01744 4.75366,-3.1508208 7.60676,-3.1508208 2.8531,0 5.5893,1.1333808 7.6067,3.1508208 2.0175,2.01743 3.1508,4.75366 3.1508,7.60673 0,2.8531 -1.1333,5.5893 -3.1508,7.6068 -2.0174,2.0174 -4.7536,3.1508 -7.6067,3.1508 -2.8531,0 -5.58933,-1.1334 -7.60676,-3.1508 C 2.10531,16.3698 0.971924,13.6336 0.971924,10.7805 Z M 11.7295,1.36764 C 9.95688,1.36774 8.22032,1.86836 6.71969,2.81188 5.21906,3.75541 4.01535,5.10349 3.2471,6.70096 2.47885,8.29844 2.17729,10.0804 2.37713,11.8417 c 0.19984,1.7613 0.89295,3.4304 1.99956,4.8151 0.95473,-1.5383 3.05649,-3.1869 7.35281,-3.1869 4.2963,0 6.3967,1.6472 7.3528,3.1869 1.1066,-1.3847 1.7997,-3.0538 1.9995,-4.8151 C 21.2817,10.0804 20.9801,8.29844 20.2119,6.70096 19.4436,5.10349 18.2399,3.75541 16.7393,2.81188 15.2386,1.86836 13.5021,1.36774 11.7295,1.36764 Z m 4.034,6.72357 c 0,1.06991 -0.425,2.09599 -1.1816,2.85249 -0.7565,0.7566 -1.7826,1.1816 -2.8525,1.1816 -1.0699,0 -2.09599,-0.425 -2.85253,-1.1816 C 8.12033,10.1872 7.69531,9.16112 7.69531,8.09121 c 0,-1.0699 0.42502,-2.09599 1.18156,-2.85253 0.75654,-0.75653 1.78263-1.18155 2.85253-1.18155 1.0699,0 2.096,0.42502 2.8525,1.18155 0.7566,0.75654 1.1816,1.78263 1.1816,2.85253 z'}");

        // Add SVG graphics for dietary items
        for (Item dietaryItem : dietaryItems) {
            String graphic = (String) dietaryItem.getFieldValue("graphic");
            if (graphic != null) {
                builder.dietaryOptionSvg(dietaryItem.getCode(), graphic);
            }
        }

        // Create virtual items once
        Item totalVirtualItem = createVirtualItem(entityStore, TOTAL_CODE, TOTAL_CODE, 10001);
        Item unknownVirtualItem = createVirtualItem(entityStore, UNKNOWN_CODE, UNKNOWN_CODE, 10000);

        builder.totalVirtualItem(totalVirtualItem);
        builder.unknownVirtualItem(unknownVirtualItem);

        // Pre-compute dietary item IDs set ONCE for O(1) lookups
        Set<Object> dietaryItemIds = dietaryItems.stream()
                .map(Item::getId)
                .collect(Collectors.toSet());

        // Create a map from dietary item ID to Item for reverse lookup
        Map<Object, Item> dietaryItemById = dietaryItems.stream()
                .collect(Collectors.toMap(Item::getId, item -> item));

        // SINGLE-PASS: Process all attendances once and compute ALL counts
        // (total, unknown, and per-dietary-option) in one iteration
        AllAttendanceCounts allCounts = calculateAllAttendanceCounts(
                attendances, dietaryItemIds, dietaryItemById, documentLinesByDocumentId);

        // Build the attendance counts from the single-pass results
        for (ScheduledItem si : scheduledItems) {
            LocalDate date = si.getDate();
            Item meal = si.getItem();

            if (date == null || meal == null)
                continue;

            // Get total count from single-pass result
            int totalCount = allCounts.totalCounts.getOrDefault(si, 0);
            builder.addAttendanceCount(date, meal, totalVirtualItem, totalCount);

            // Get unknown count from single-pass result
            int unknownCount = allCounts.unknownCounts.getOrDefault(si, 0);
            builder.addAttendanceCount(date, meal, unknownVirtualItem, unknownCount);

            // Get dietary option counts from single-pass result
            for (Item dietaryItem : dietaryItems) {
                AttendanceKey key = new AttendanceKey(si, dietaryItem);
                int count = allCounts.dietaryCounts.getOrDefault(key, 0);
                builder.addAttendanceCount(date, meal, dietaryItem, count);
            }
        }

        return builder.build();
    }

    /**
     * Result container for single-pass attendance counting.
     * Holds total counts, unknown (no diet) counts, and per-dietary-option counts.
     */
    private static class AllAttendanceCounts {
        final Map<ScheduledItem, Integer> totalCounts = new HashMap<>();
        final Map<ScheduledItem, Integer> unknownCounts = new HashMap<>();
        final Map<AttendanceKey, Integer> dietaryCounts = new HashMap<>();
    }

    /**
     * SINGLE-PASS algorithm to calculate ALL attendance counts in one iteration.
     * Computes total, unknown, and per-dietary-option counts simultaneously.
     * This replaces the previous triple iteration approach.
     */
    private static AllAttendanceCounts calculateAllAttendanceCounts(
            EntityList<Attendance> attendances,
            Set<Object> dietaryItemIds,
            Map<Object, Item> dietaryItemById,
            Map<EntityId, List<DocumentLine>> documentLinesByDocumentId) {

        AllAttendanceCounts result = new AllAttendanceCounts();

        for (Attendance attendance : attendances) {
            DocumentLine dl = attendance.getDocumentLine();
            if (dl == null || dl.isCancelled() || dl.getDocument() == null)
                continue;

            ScheduledItem si = attendance.getScheduledItem();
            if (si == null)
                continue;

            // Count this attendance toward total
            result.totalCounts.merge(si, 1, Integer::sum);

            // Find dietary items in this booking's document
            List<DocumentLine> documentLines = documentLinesByDocumentId.get(dl.getDocument().getId());

            // Check if this attendance has any dietary option
            boolean hasDiet = false;
            if (documentLines != null) {
                for (DocumentLine docLine : documentLines) {
                    if (docLine.isCancelled())
                        continue;
                    Item item = docLine.getItem();
                    if (item != null && dietaryItemIds.contains(item.getId())) {
                        hasDiet = true;
                        // Count toward this dietary option
                        Item dietaryItem = dietaryItemById.get(item.getId());
                        if (dietaryItem != null) {
                            AttendanceKey key = new AttendanceKey(si, dietaryItem);
                            result.dietaryCounts.merge(key, 1, Integer::sum);
                        }
                    }
                }
            }

            // If no dietary option found, count toward unknown
            if (!hasDiet) {
                result.unknownCounts.merge(si, 1, Integer::sum);
            }
        }

        return result;
    }

    /**
     * Create a virtual item for Total/Unknown counts
     */
    private static Item createVirtualItem(EntityStore entityStore, String code, String name, int ord) {
        // Create a transient Item entity (not persisted to database)
        // This is used purely for display purposes in the UI
        Item virtualItem = entityStore.createEntity(Item.class);
        virtualItem.setFieldValue("code", code);
        virtualItem.setFieldValue("name", name);
        virtualItem.setFieldValue("ord", ord);
        return virtualItem;
    }

    /**
     * Key for grouping attendances by scheduled item and dietary option
     */
    private static class AttendanceKey {
        private final ScheduledItem scheduledItem;
        private final Item dietaryItem;

        public AttendanceKey(ScheduledItem scheduledItem, Item dietaryItem) {
            this.scheduledItem = scheduledItem;
            this.dietaryItem = dietaryItem;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof AttendanceKey that))
                return false;
            return Objects.equals(scheduledItem, that.scheduledItem) &&
                    Objects.equals(dietaryItem, that.dietaryItem);
        }

        @Override
        public int hashCode() {
            return Objects.hash(scheduledItem, dietaryItem);
        }
    }
}
