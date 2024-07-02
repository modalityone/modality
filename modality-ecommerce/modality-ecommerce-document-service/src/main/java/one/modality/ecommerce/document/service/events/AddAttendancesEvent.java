package one.modality.ecommerce.document.service.events;

import one.modality.base.shared.entities.Attendance;

/**
 * @author Bruno Salmon
 */
public final class AddAttendancesEvent extends AbstractAttendancesEvent {

    public AddAttendancesEvent(Attendance[] attendances) {
        super(attendances);
    }

    public AddAttendancesEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, Object[] attendancesPrimaryKeys, Object[] scheduledItemsPrimaryKeys) {
        super(documentPrimaryKey, documentLinePrimaryKey, attendancesPrimaryKeys, scheduledItemsPrimaryKeys);
    }

}
