package one.modality.base.shared.entities.markers;

import java.time.LocalDateTime;

/**
 * @author Bruno Salmon
 */
public interface HasLocalDateTime {

    void setDate(LocalDateTime date);

    LocalDateTime getDate();

}
