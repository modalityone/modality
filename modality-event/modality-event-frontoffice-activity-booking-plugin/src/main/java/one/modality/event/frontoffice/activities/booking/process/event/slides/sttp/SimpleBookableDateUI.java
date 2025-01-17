package one.modality.event.frontoffice.activities.booking.process.event.slides.sttp;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.event.client.recurringevents.BookableDatesUi;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class SimpleBookableDateUI implements BookableDatesUi {
    private final ObservableList<ScheduledItem> scheduledItemsList = FXCollections.observableArrayList();
    protected ObservableList<LocalDate> selectedDates = FXCollections.observableArrayList(); // indicates the dates to add to the current booking

    public ObservableList<LocalDate> getSelectedDates() {
        return selectedDates;
    }

    public void addSelectedDates(List<LocalDate> dates) {
        selectedDates.addAll(dates);
    }

    public ObservableList<ScheduledItem> getSelectedScheduledItem() {
        return scheduledItemsList;
    }

    public void setScheduledItems(List<ScheduledItem> scheduledItems, boolean reapplyClickedDates) {
        scheduledItems.sort(Comparator.comparing(ScheduledItem::getDate));
        scheduledItemsList.setAll(scheduledItems);
        selectedDates.clear();
    }

    public void removeClickedDate(LocalDate date) {
        selectedDates.remove(date);
    }

    public void clearClickedDates() {
        selectedDates.clear();
    }

}
