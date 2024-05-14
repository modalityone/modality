package one.modality.ecommerce.document.service.events;

import dev.webfx.stack.orm.entity.Entity;
import one.modality.base.shared.entities.Attendance;

import java.util.Arrays;

/**
 * @author Bruno Salmon
 */
public abstract class AttendancesEvent extends DocumentLineEvent {

    private final Attendance[] attendances;
    private final Object[] attendancePrimaryKeys;

    public AttendancesEvent(Attendance[] attendances) {
        super(attendances[0].getDocumentLine());
        this.attendances = attendances;
        this.attendancePrimaryKeys = Arrays.stream(attendances).map(Entity::getPrimaryKey).toArray();
    }

    public Attendance[] getAttendances() {
        return attendances;
    }

    public Object[] getAttendancePrimaryKeys() {
        return attendancePrimaryKeys;
    }
}
