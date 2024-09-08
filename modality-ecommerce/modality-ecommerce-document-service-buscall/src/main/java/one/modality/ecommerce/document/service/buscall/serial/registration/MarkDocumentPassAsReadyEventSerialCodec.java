package one.modality.ecommerce.document.service.buscall.serial.registration;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.MarkDocumentPassAsReadyEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentPassAsReadyEventSerialCodec extends AbstractDocumentEventSerialCodec<MarkDocumentPassAsReadyEvent> {

    private static final String CODEC_ID = "MarkDocumentPassAsReadyEvent";

    private static final String PASS_READY_KEY = "passReady";
    private static final String READ_KEY = "read";

    public MarkDocumentPassAsReadyEventSerialCodec() {
        super(MarkDocumentPassAsReadyEvent.class, CODEC_ID);
    }

    @Override
    public void encode(MarkDocumentPassAsReadyEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeBoolean(serial, PASS_READY_KEY, o.isPassReady());
        encodeBoolean(serial, READ_KEY, o.isRead());
    }

    @Override
    public MarkDocumentPassAsReadyEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new MarkDocumentPassAsReadyEvent(
            decodeDocumentPrimaryKey(serial),
            decodeBoolean(serial, PASS_READY_KEY),
            decodeBoolean(serial, READ_KEY)
        ), serial);
    }
}
