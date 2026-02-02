package one.modality.base.shared.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @author Bruno Salmon
 */
public class SimplePeriod implements Period {

    private final LocalDate startDate;
    private final LocalTime startTime;
    private final LocalDate endDate;
    private final LocalTime endTime;

    public SimplePeriod(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this(startDateTime.toLocalDate(), startDateTime.toLocalTime(), endDateTime.toLocalDate(), endDateTime.toLocalTime());
    }

    public SimplePeriod(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime) {
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
    }

    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public LocalTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public LocalTime getEndTime() {
        return endTime;
    }

}
