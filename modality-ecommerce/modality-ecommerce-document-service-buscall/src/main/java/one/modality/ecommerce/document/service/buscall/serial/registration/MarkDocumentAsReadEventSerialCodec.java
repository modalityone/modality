package one.modality.ecommerce.document.service.buscall.serial.registration;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.MarkDocumentAsReadEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentAsReadEventSerialCodec extends AbstractDocumentEventSerialCodec<MarkDocumentAsReadEvent> {

    private static final String CODEC_ID = "MarkDocumentAsReadEvent";

    private static final String READ_KEY = "read";

    public MarkDocumentAsReadEventSerialCodec() {
        super(MarkDocumentAsReadEvent.class, CODEC_ID);
    }

    @Override
    public void encode(MarkDocumentAsReadEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeBoolean(serial, READ_KEY, o.isRead());
    }

    @Override
    public MarkDocumentAsReadEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new MarkDocumentAsReadEvent(
                decodeDocumentPrimaryKey(serial),
                decodeBoolean(serial, READ_KEY)
        ), serial);
    }
}
