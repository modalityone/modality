package one.modality.ecommerce.document.service.events;

import dev.webfx.stack.orm.entity.Entities;
import one.modality.base.shared.entities.Attendance;

import java.util.Arrays;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractAttendancesEvent extends AbstractDocumentLineEvent {

    private final Attendance[] attendances;
    private final Object[] scheduledItemsPrimaryKeys;

    public AbstractAttendancesEvent(Attendance[] attendances) {
        super(attendances[0].getDocumentLine());
        this.attendances = attendances;
        this.scheduledItemsPrimaryKeys = Arrays.stream(attendances).map(a -> Entities.getPrimaryKey(a.getScheduledItem())).toArray();
    }

    public AbstractAttendancesEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, Object[] scheduledItemsPrimaryKeys) {
        super(documentPrimaryKey, documentLinePrimaryKey);
        this.scheduledItemsPrimaryKeys = scheduledItemsPrimaryKeys;
        attendances = null;
    }

    public Attendance[] getAttendances() {
        return attendances;
    }

    public Object[] getScheduledItemsPrimaryKeys() {
        return scheduledItemsPrimaryKeys;
    }
}
