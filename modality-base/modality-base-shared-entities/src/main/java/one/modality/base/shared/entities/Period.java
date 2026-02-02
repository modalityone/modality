package one.modality.base.shared.entities;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author Bruno Salmon
 */
public interface Period {

    LocalDate getStartDate();

    LocalTime getStartTime();

    LocalDate getEndDate();

    LocalTime getEndTime();

}
