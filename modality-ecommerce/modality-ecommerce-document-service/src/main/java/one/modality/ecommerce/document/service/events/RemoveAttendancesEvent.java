package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.Attendance;

/**
 * @author Bruno Salmon
 */
public class RemoveAttendancesEvent extends AttendancesEvent {

    public RemoveAttendancesEvent(Attendance[] attendances) {
        super(attendances);
    }
}
