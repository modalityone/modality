package one.modality.event.client.booking;

import javafx.collections.ObservableList;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;
import java.util.List;

/**
 * Generic interface for any UI component in an event booking form allowing the user to select the dates to book.
 *
 * @author Bruno Salmon
 */
public interface BookableDatesUi {

    ObservableList<LocalDate> getSelectedDates();

    void addSelectedDates(List<LocalDate> dates);

    ObservableList<ScheduledItem> getSelectedScheduledItem();

    void setScheduledItems(List<ScheduledItem> scheduledItems, boolean reapplyClickedDates);

    void removeClickedDate(LocalDate date);

    void clearClickedDates();

}
