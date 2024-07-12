package one.modality.ecommerce.document.service.events.book;

import one.modality.base.shared.entities.Attendance;
import one.modality.ecommerce.document.service.events.AbstractAttendancesEvent;

/**
 * @author Bruno Salmon
 */
public final class RemoveAttendancesEvent extends AbstractAttendancesEvent {

    public RemoveAttendancesEvent(Attendance[] attendances) {
        super(attendances);
    }

    public RemoveAttendancesEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, Object[] attendancePrimaryKeys) {
        super(documentPrimaryKey, documentLinePrimaryKey, attendancePrimaryKeys, null);
    }
}
