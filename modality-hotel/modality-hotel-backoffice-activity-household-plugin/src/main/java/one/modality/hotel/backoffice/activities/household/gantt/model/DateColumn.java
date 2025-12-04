package one.modality.hotel.backoffice.activities.household.gantt.model;

import java.time.LocalDate;

/**
 * Represents a date column in the calendar with its display properties
 */
public record DateColumn(LocalDate date, boolean isToday, boolean isWider) {
}
