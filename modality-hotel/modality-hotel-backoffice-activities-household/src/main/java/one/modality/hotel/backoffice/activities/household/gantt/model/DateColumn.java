package one.modality.hotel.backoffice.activities.household.gantt.model;

import java.time.LocalDate;

/**
 * Represents a date column in the calendar with its display properties
 */
public class DateColumn {
    private final LocalDate date;
    private final boolean isToday;
    private final boolean isWider;

    public DateColumn(LocalDate date, boolean isToday, boolean isWider) {
        this.date = date;
        this.isToday = isToday;
        this.isWider = isWider;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isToday() {
        return isToday;
    }

    public boolean isWider() {
        return isWider;
    }
}
