package one.modality.base.shared.entities;

import one.modality.base.shared.entities.util.ScheduledBoundaries;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author Bruno Salmon
 */
public interface BoundaryPeriod extends Period {

    ScheduledBoundary getStartBoundary();

    ScheduledBoundary getEndBoundary();

    @Override
    default LocalDate getStartDate() {
        return ScheduledBoundaries.getDate(getStartBoundary());
    }

    @Override
    default LocalTime getStartTime() {
        return ScheduledBoundaries.getTime(getStartBoundary());
    }

    default LocalDate getEndDate() {
        return ScheduledBoundaries.getDate(getEndBoundary());
    }

    @Override
    default LocalTime getEndTime() {
        return ScheduledBoundaries.getTime(getEndBoundary());
    }

}
