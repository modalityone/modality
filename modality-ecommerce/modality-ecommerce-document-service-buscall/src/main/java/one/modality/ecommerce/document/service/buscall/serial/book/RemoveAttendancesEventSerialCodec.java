package one.modality.ecommerce.document.service.buscall.serial.book;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractAttendancesEventSerialCodec;
import one.modality.ecommerce.document.service.events.book.RemoveAttendancesEvent;

/**
 * @author Bruno Salmon
 */
public final class RemoveAttendancesEventSerialCodec extends AbstractAttendancesEventSerialCodec<RemoveAttendancesEvent> {

    private static final String CODEC_ID = "RemoveAttendancesEvent";

    public RemoveAttendancesEventSerialCodec() {
        super(RemoveAttendancesEvent.class, CODEC_ID);
    }

    @Override
    public RemoveAttendancesEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new RemoveAttendancesEvent(
                decodeDocumentPrimaryKey(serial),
                decodeDocumentLinePrimaryKey(serial),
                decodeAttendancesPrimaryKeys(serial)
        ), serial);
    }
}
