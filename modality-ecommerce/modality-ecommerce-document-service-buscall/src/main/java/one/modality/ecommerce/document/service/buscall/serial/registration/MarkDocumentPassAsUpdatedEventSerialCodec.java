package one.modality.ecommerce.document.service.buscall.serial.registration;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.MarkDocumentPassAsUpdatedEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentPassAsUpdatedEventSerialCodec extends AbstractDocumentEventSerialCodec<MarkDocumentPassAsUpdatedEvent> {

    private static final String CODEC_ID = "MarkDocumentPassAsUpdatedEvent";

    private static final String READ_KEY = "read";

    public MarkDocumentPassAsUpdatedEventSerialCodec() {
        super(MarkDocumentPassAsUpdatedEvent.class, CODEC_ID);
    }

    @Override
    public void encode(MarkDocumentPassAsUpdatedEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeBoolean(serial, READ_KEY, o.isRead());
    }

    @Override
    public MarkDocumentPassAsUpdatedEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new MarkDocumentPassAsUpdatedEvent(
                decodeDocumentPrimaryKey(serial),
                decodeBoolean(serial, READ_KEY)
        ), serial);
    }
}
