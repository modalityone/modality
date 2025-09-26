package one.modality.event.client.scheduleditemsselector;

import javafx.collections.ObservableList;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;
import java.util.List;

/**
 * Generic interface for any UI component in a booking form allowing the user to select the scheduled items to book.
 *
 * @author Bruno Salmon
 */
public interface ScheduledItemsSelector {

    void setSelectableScheduledItems(List<ScheduledItem> selectableScheduledItems, boolean reapplyClickedDates);

    ObservableList<ScheduledItem> getSelectedScheduledItems();

    ObservableList<LocalDate> getSelectedDates();

    void addSelectedDates(List<LocalDate> dates);

    void removeClickedDate(LocalDate date);

    void clearClickedDates();

}
