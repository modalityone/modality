package one.modality.base.shared.entities.util;

import dev.webfx.platform.util.collection.Collections;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.markers.EntityHasItem;
import one.modality.base.shared.knownitems.KnownItemFamily;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bruno Salmon
 */
public final class ScheduledItems {

    public static boolean isOfFamily(ScheduledItem scheduledItem, KnownItemFamily family) {
        return Items.isOfFamily(scheduledItem.getItem(), family);
    }

    public static Stream<ScheduledItem> filterFamily(Stream<ScheduledItem> scheduledItems, KnownItemFamily family) {
        return scheduledItems.filter(scheduledItem -> isOfFamily(scheduledItem, family));
    }

    public static List<ScheduledItem> filterFamily(List<ScheduledItem> scheduledItems, KnownItemFamily family) {
        return Collections.filter(scheduledItems, scheduledItem -> isOfFamily(scheduledItem, family));
    }

    public static Map<Item, List<ScheduledItem>> groupScheduledItemsByItems(Stream<ScheduledItem> scheduledItems) {
        return scheduledItems
            .collect(Collectors.groupingBy(EntityHasItem::getItem,
                () -> new TreeMap<>(Comparator.comparing(Item::getOrd)),
                Collectors.toList()));
    }

    public static Map<Item, List<ScheduledItem>> groupScheduledItemsByAudioRecordingItems(Stream<ScheduledItem> scheduledItems) {
        return groupScheduledItemsByItems(filterFamily(scheduledItems, KnownItemFamily.AUDIO_RECORDING));
    }

    public static List<ScheduledItem> fromAttendances(List<Attendance> attendances) {
        return Collections.map(attendances, Attendance::getScheduledItem);
    }

    public static List<LocalDate> toDates(List<ScheduledItem> scheduledItems) {
        return Collections.map(scheduledItems, ScheduledItem::getDate);
    }

}
