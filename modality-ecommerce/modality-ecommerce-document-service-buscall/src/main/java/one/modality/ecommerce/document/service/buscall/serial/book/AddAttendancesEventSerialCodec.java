package one.modality.ecommerce.document.service.buscall.serial.book;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractAttendancesEventSerialCodec;
import one.modality.ecommerce.document.service.events.book.AddAttendancesEvent;

/**
 * @author Bruno Salmon
 */
public final class AddAttendancesEventSerialCodec extends AbstractAttendancesEventSerialCodec<AddAttendancesEvent> {

    private static final String CODEC_ID = "AddAttendancesEvent";

    public AddAttendancesEventSerialCodec() {
        super(AddAttendancesEvent.class, CODEC_ID);
    }

    @Override
    public AddAttendancesEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new AddAttendancesEvent(
                decodeDocumentPrimaryKey(serial),
                decodeDocumentLinePrimaryKey(serial),
                decodeAttendancesPrimaryKeys(serial),
                decodeScheduledItemsPrimaryKeys(serial)
        ), serial);
    }
}
