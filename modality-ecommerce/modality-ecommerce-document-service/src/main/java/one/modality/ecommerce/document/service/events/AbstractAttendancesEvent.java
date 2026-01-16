package one.modality.ecommerce.document.service.events;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Attendance;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.ScheduledItem;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractAttendancesEvent extends AbstractDocumentLineEvent {

    protected Attendance[] attendances;                // Working entities on the client-side only (not serialized)
    private final Object[] attendancesPrimaryKeys;     // Their primary keys (serialized)
    private final Object[] scheduledItemsPrimaryKeys;  // Case 1: Their associated scheduledItems primary keys (serialized) - when the associated item is scheduled
    private final LocalDate[] dates;                   // Case 2: Alternatively, their dates - when the associated item is not scheduled

    protected boolean playedOnAttendances;

    public AbstractAttendancesEvent(Attendance[] attendances) {
        super(attendances[0].getDocumentLine());
        this.attendances = attendances;
        this.attendancesPrimaryKeys = Arrays.stream(attendances).map(Entities::getPrimaryKey).toArray();
        if (attendances[0].getScheduledItem() != null) {
            this.scheduledItemsPrimaryKeys = Arrays.stream(attendances).map(a -> Entities.getPrimaryKey(a.getScheduledItem())).toArray();
            this.dates = null;
        } else {
            this.scheduledItemsPrimaryKeys = null;
            this.dates = Arrays.stream(attendances).map(Attendance::getDate).toArray(LocalDate[]::new);
        }
    }

    public AbstractAttendancesEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, Object[] attendancesPrimaryKeys, Object[] scheduledItemsPrimaryKeys, LocalDate[] dates) {
        super(documentPrimaryKey, documentLinePrimaryKey);
        this.attendancesPrimaryKeys = attendancesPrimaryKeys;
        this.scheduledItemsPrimaryKeys = scheduledItemsPrimaryKeys;
        this.dates = dates;
    }

    public Attendance[] getAttendances() {
        if (attendances == null && entityStore != null) {
            attendances = new Attendance[attendancesPrimaryKeys.length];
        }
        if (attendances != null && !playedOnAttendances)
            replayEventOnAttendances();
        return attendances;
    }

    public Object[] getAttendancesPrimaryKeys() { // Also used to refactor new primary keys once inserted on server
        return attendancesPrimaryKeys;
    }

    public Object[] getScheduledItemsPrimaryKeys() {
        return scheduledItemsPrimaryKeys;
    }

    public LocalDate[] getDates() {
        return dates;
    }

    @Override
    public void replayEvent() {
        setPlayed(false);
        getAttendances();
    }

    @Override
    public void setPlayed(boolean played) {
        super.setPlayed(played);
        playedOnAttendances = played;
    }

    protected void replayEventOnAttendances() {
        DocumentLine documentLine = getDocumentLine();
        for (int i = 0; i < attendancesPrimaryKeys.length; i++) {
            Attendance attendance = createAttendance(attendancesPrimaryKeys[i]);
            attendance.setDocumentLine(documentLine);
            if (scheduledItemsPrimaryKeys != null) { // RemoveAttendancesEvent doesn't memorize scheduledItemsPrimaryKeys
                attendance.setScheduledItem(isForSubmit() ? scheduledItemsPrimaryKeys[i] : entityStore.getOrCreateEntity(ScheduledItem.class, scheduledItemsPrimaryKeys[i]));
            }
            if (dates != null)
                attendance.setDate(dates[i]);
            attendances[i] = attendance;
        }
        playedOnAttendances = true;
    }

    protected Attendance createAttendance(Object attendancePrimaryKey) {
        if (isForSubmit()) {
            return updateStore.updateEntity(Attendance.class, attendancePrimaryKey);
        } else {
            return entityStore.getOrCreateEntity(Attendance.class, attendancePrimaryKey);
        }
    }

}
