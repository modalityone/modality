package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.AddAttendancesEvent;

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
                decodeAttendancePrimaryKeys(serial)
        ), serial);
    }
}
