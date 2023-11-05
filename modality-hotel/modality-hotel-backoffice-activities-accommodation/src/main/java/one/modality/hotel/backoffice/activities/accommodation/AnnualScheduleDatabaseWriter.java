package one.modality.hotel.backoffice.activities.accommodation;

import dev.webfx.platform.console.Console;
import dev.webfx.stack.orm.entity.UpdateStore;
import one.modality.base.shared.entities.*;
import one.modality.hotel.backoffice.accommodation.ResourceConfigurationLoader;

import java.time.LocalDate;
import java.util.*;

public class AnnualScheduleDatabaseWriter {

    private static final int MAX_INSERTS_PER_TRANSACTION = 5;

    private final Site site;
    private final List<ScheduledItem> latestScheduledItems;

    private List<ResourceConfigurationDates> resourceConfigurationDatesToInsert;
    private Map<Item, List<LocalDate>> itemDatesToInsert;
    private Map<ItemDate, ScheduledItem> scheduledItemForDate = new HashMap<>();
    private UpdateStore updateStore;

    public AnnualScheduleDatabaseWriter(Site site, LocalDate fromDate, LocalDate toDate, List<Item> selectedItems, List<ScheduledItem> latestScheduledItems, ResourceConfigurationLoader resourceConfigurationLoader) {
        this.site = site;
        this.latestScheduledItems = latestScheduledItems;
        determineRecordsToInsert(fromDate, toDate, selectedItems, resourceConfigurationLoader);
    }

    private void determineRecordsToInsert(LocalDate fromDate, LocalDate toDate, List<Item> selectedItems, ResourceConfigurationLoader resourceConfigurationLoader) {
        itemDatesToInsert = new HashMap<>();
        resourceConfigurationDatesToInsert = new ArrayList<>();
        for (Item item : selectedItems) {
            itemDatesToInsert.put(item, new ArrayList<>());

            ResourceConfigurationDates resourceConfigurationDates = new ResourceConfigurationDates(item);
            ScheduledItem latestScheduledItem = latestScheduledItem(item, scheduledItemsWithLatestDates);
            for (LocalDate date = fromDate; !date.isAfter(toDate); date = date.plusDays(1)) {
                if (latestScheduledItem != null && !date.isAfter(latestScheduledItem.getDate())) {
                    // Check no record exists for this date
                    continue;
                }

                itemDatesToInsert.get(item).add(date);

                for (ResourceConfiguration rc : resourceConfigurationLoader.getResourceConfigurations()) {
                    if (item.equals(rc.getItem())) {
                        resourceConfigurationDates.addDate(rc, date);
                    }
                }
                resourceConfigurationDatesToInsert.add(resourceConfigurationDates);
            }
        }
    }

    private ScheduledItem latestScheduledItem(Item item, List<ScheduledItem> scheduledItemsWithLatestDates) {
        return scheduledItemsWithLatestDates.stream()
                .filter(scheduledItem -> scheduledItem.getItem().equals(item))
                .sorted((scheduledItem1, scheduledItem2) -> scheduledItem2.getDate().compareTo(scheduledItem1.getDate()))
                .findFirst()
                .orElseGet(() -> null);
    }

    public void saveToUpdateStore(UpdateStore updateStore) {
        this.updateStore = updateStore;
        continueSaving();
    }

    private void continueSaving() {
        int numInsertions = 0;

        while (!itemDatesToInsert.isEmpty()) {
            Map.Entry<Item, List<LocalDate>> entry = itemDatesToInsert.entrySet().iterator().next();
            Item item = entry.getKey();
            while (!entry.getValue().isEmpty()) {
                LocalDate date = entry.getValue().iterator().next();

                ScheduledItem latestScheduledItem = latestScheduledItem(item);
                Boolean available = latestScheduledItem != null ? latestScheduledItem.getBooleanFieldValue("available") : true;
                Boolean online = latestScheduledItem != null ? latestScheduledItem.getBooleanFieldValue("online") : true;
                Boolean resource = latestScheduledItem != null ? latestScheduledItem.getBooleanFieldValue("resource") : true;

                ScheduledItem scheduledItem = updateStore.insertEntity(ScheduledItem.class);
                scheduledItem.setItem(item);
                scheduledItem.setDate(date);
                scheduledItem.setSite(site);
                scheduledItem.setFieldValue("available", available);
                scheduledItem.setFieldValue("online", online);
                scheduledItem.setFieldValue("resource", resource);

                scheduledItemForDate.put(new ItemDate(item, date), scheduledItem);

                entry.getValue().remove(date);
                numInsertions++;
                if (numInsertions >= MAX_INSERTS_PER_TRANSACTION) {
                    updateStore.submitChanges()
                            .onFailure(Console::log)
                            .onSuccess(result -> continueSaving());
                }
            }
            itemDatesToInsert.remove(entry.getKey());
        }

        while (!resourceConfigurationDatesToInsert.isEmpty()) {
            ResourceConfigurationDates resourceConfigurationDates = resourceConfigurationDatesToInsert.iterator().next();
            while (!resourceConfigurationDates.dates.isEmpty()) {
                Map.Entry<ResourceConfiguration, List<LocalDate>> entry = resourceConfigurationDates.dates.entrySet().iterator().next();
                ResourceConfiguration rc = entry.getKey();
                while (!entry.getValue().isEmpty()) {
                    Item item = resourceConfigurationDates.item;
                    LocalDate date = entry.getValue().iterator().next();

                    ScheduledItem latestScheduledItem = latestScheduledItem(item);
                    Boolean available = latestScheduledItem != null ? latestScheduledItem.getBooleanFieldValue("available") : true;
                    Boolean online = latestScheduledItem != null ? latestScheduledItem.getBooleanFieldValue("online") : true;

                    ScheduledItem scheduledItem = scheduledItemForDate.get(new ItemDate(item, date));

                    ScheduledResource scheduledResource = updateStore.insertEntity(ScheduledResource.class);
                    scheduledResource.setAvailable(available);
                    //scheduledResource.setFieldValue("clean", true);
                    scheduledResource.setResourceConfiguration(rc);
                    scheduledResource.setDate(date);
                    scheduledResource.setMax(rc.getMax());
                    scheduledResource.setOnline(online);
                    //scheduledResource.setForeignField("scheduled_item_id", scheduledItem);

                    entry.getValue().remove(date);

                    numInsertions++;
                    if (numInsertions >= MAX_INSERTS_PER_TRANSACTION) {
                        updateStore.submitChanges()
                                .onFailure(Console::log)
                                .onSuccess(result -> continueSaving());
                    }
                }
                resourceConfigurationDates.dates.remove(entry.getKey());
            }
            resourceConfigurationDatesToInsert.remove(resourceConfigurationDates);
        }
    }

    private ScheduledItem latestScheduledItem(Item item) {
        return latestScheduledItems.stream()
                .filter(scheduledItem -> scheduledItem.getItem().equals(item))
                .sorted((scheduledItem1, scheduledItem2) -> scheduledItem2.getDate().compareTo(scheduledItem1.getDate()))
                .findFirst()
                .orElseGet(() -> null);
    }

    private static class ResourceConfigurationDates {

        private final Item item;
        private final Map<ResourceConfiguration, List<LocalDate>> dates = new HashMap<>();

        public ResourceConfigurationDates(Item item) {
            this.item = item;
        }

        public void addDate(ResourceConfiguration resourceConfiguration, LocalDate date) {
            if (!dates.containsKey(resourceConfiguration)) {
                dates.put(resourceConfiguration, new ArrayList<>());
            }
            dates.get(resourceConfiguration).add(date);
        }
    }

    private static class ItemDate {

        private final Item item;
        private final LocalDate date;

        public ItemDate(Item item, LocalDate date) {
            this.item = item;
            this.date = date;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemDate itemDate = (ItemDate) o;
            return Objects.equals(item, itemDate.item) && Objects.equals(date, itemDate.date);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item, date);
        }
    }
}
