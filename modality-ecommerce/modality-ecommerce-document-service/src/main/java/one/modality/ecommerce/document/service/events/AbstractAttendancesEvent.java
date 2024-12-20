package one.modality.ecommerce.document.service.events;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.ScheduledItem;

import java.util.Arrays;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractAttendancesEvent extends AbstractDocumentLineEvent {

    protected Attendance[] attendances;                // Working entities on client-side only (not serialised)
    private final Object[] attendancesPrimaryKeys;     // Their primary keys (serialised)
    private final Object[] scheduledItemsPrimaryKeys;  // Their associated scheduledItems primary keys (serialised)

    public AbstractAttendancesEvent(Attendance[] attendances) {
        super(attendances[0].getDocumentLine());
        this.attendances = attendances;
        this.attendancesPrimaryKeys = Arrays.stream(attendances).map(Entities::getPrimaryKey).toArray();
        this.scheduledItemsPrimaryKeys = Arrays.stream(attendances).map(a -> Entities.getPrimaryKey(a.getScheduledItem())).toArray();
    }

    public AbstractAttendancesEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, Object[] attendancesPrimaryKeys, Object[] scheduledItemsPrimaryKeys) {
        super(documentPrimaryKey, documentLinePrimaryKey);
        this.attendancesPrimaryKeys = attendancesPrimaryKeys;
        this.scheduledItemsPrimaryKeys = scheduledItemsPrimaryKeys;
    }

    public Attendance[] getAttendances() {
        if (attendances == null && entityStore != null) {
            attendances = new Attendance[attendancesPrimaryKeys.length];
            replayEventOnAttendances();
        }
        return attendances;
    }

    public Object[] getAttendancesPrimaryKeys() { // Also used to refactor new primary keys once inserted on server
        return attendancesPrimaryKeys;
    }

    public Object[] getScheduledItemsPrimaryKeys() {
        return scheduledItemsPrimaryKeys;
    }

    @Override
    public void replayEvent() {
        getAttendances();
    }

    protected void replayEventOnAttendances() {
        DocumentLine documentLine = getDocumentLine();
        for (int i = 0; i < attendancesPrimaryKeys.length; i++) {
            Attendance attendance = createAttendance(attendancesPrimaryKeys[i]);
            attendance.setDocumentLine(documentLine);
            if (scheduledItemsPrimaryKeys != null) { // RemoveAttendancesEvent doesn't memorise scheduledItemsPrimaryKeys
                attendance.setScheduledItem(isForSubmit() ? scheduledItemsPrimaryKeys[i] : entityStore.getOrCreateEntity(ScheduledItem.class, scheduledItemsPrimaryKeys[i]));
            }
            attendances[i] = attendance;
        }
    }

    protected Attendance createAttendance(Object attendancePrimaryKey) {
        if (isForSubmit()) {
            return updateStore.updateEntity(Attendance.class, attendancePrimaryKey);
        } else {
            return entityStore.getOrCreateEntity(Attendance.class, attendancePrimaryKey);
        }
    }

}
