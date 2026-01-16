package one.modality.ecommerce.document.service.events.book;

import one.modality.base.shared.entities.Attendance;
import one.modality.ecommerce.document.service.events.AbstractAttendancesEvent;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public final class AddAttendancesEvent extends AbstractAttendancesEvent {

    public AddAttendancesEvent(Attendance[] attendances) {
        super(attendances);
    }

    public AddAttendancesEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, Object[] attendancesPrimaryKeys, Object[] scheduledItemsPrimaryKeys, LocalDate[] dates) {
        super(documentPrimaryKey, documentLinePrimaryKey, attendancesPrimaryKeys, scheduledItemsPrimaryKeys, dates);
    }

    @Override
    protected Attendance createAttendance(Object attendancePrimaryKey) {
        if (isForSubmit())
            return updateStore.insertEntity(Attendance.class, attendancePrimaryKey);
        return super.createAttendance(attendancePrimaryKey);
    }
}
