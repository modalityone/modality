package org.modality_project.base.shared.entities.markers;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public interface HasDate {

    void setDate(LocalDate date);

    LocalDate getDate();

}
