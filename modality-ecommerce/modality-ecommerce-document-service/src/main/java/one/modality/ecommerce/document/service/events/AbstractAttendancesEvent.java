package one.modality.ecommerce.document.service.events;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Attendance;

import java.util.Arrays;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractAttendancesEvent extends AbstractDocumentLineEvent {

    private final Attendance[] attendances;            // Working entities on client-side only (not serialised)
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
        attendances = null;
        this.attendancesPrimaryKeys = attendancesPrimaryKeys;
        this.scheduledItemsPrimaryKeys = scheduledItemsPrimaryKeys;
    }

    public Attendance[] getAttendances() {
        return attendances;
    }

    public Object[] getAttendancesPrimaryKeys() { // Also used to refactor new primary keys once inserted on server
        return attendancesPrimaryKeys;
    }

    public Object[] getScheduledItemsPrimaryKeys() {
        return scheduledItemsPrimaryKeys;
    }

}
