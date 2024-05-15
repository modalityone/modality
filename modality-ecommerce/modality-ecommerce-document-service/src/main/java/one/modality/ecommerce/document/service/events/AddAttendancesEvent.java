package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.Attendance;

/**
 * @author Bruno Salmon
 */
public final class AddAttendancesEvent extends AttendancesEvent {

    public AddAttendancesEvent(Attendance[] attendances) {
        super(attendances);
    }
}
