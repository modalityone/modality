package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.AbstractAttendancesEvent;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractAttendancesEventSerialCodec<T extends AbstractAttendancesEvent> extends AbstractDocumentLineEventSerialCodec<T> {

    private static final String ATTENDANCES_PRIMARY_KEYS = "attendances";
    private static final String SCHEDULED_ITEMS_PRIMARY_KEYS = "scheduledItems";

    public AbstractAttendancesEventSerialCodec(Class<T> javaClass, String codecId) {
        super(javaClass, codecId);
    }

    @Override
    public void encode(T o, AstObject serial) {
        super.encode(o, serial);
        encodeObjectArray(serial, ATTENDANCES_PRIMARY_KEYS,     o.getAttendancesPrimaryKeys());
        encodeObjectArray(serial, SCHEDULED_ITEMS_PRIMARY_KEYS, o.getScheduledItemsPrimaryKeys());
    }

    protected Object[] decodeAttendancesPrimaryKeys(ReadOnlyAstObject serial) {
        return decodeObjectArray(serial, ATTENDANCES_PRIMARY_KEYS);
    }

    protected Object[] decodeScheduledItemsPrimaryKeys(ReadOnlyAstObject serial) {
        return decodeObjectArray(serial, SCHEDULED_ITEMS_PRIMARY_KEYS);
    }


}
