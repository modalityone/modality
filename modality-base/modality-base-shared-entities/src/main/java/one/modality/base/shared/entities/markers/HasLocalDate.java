package one.modality.base.shared.entities.markers;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public interface HasLocalDate {

    void setDate(LocalDate date);

    LocalDate getDate();

}
