package one.modality.ecommerce.document.service.buscall.serial.book;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.book.CancelDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class CancelDocumentEventSerialCodec extends AbstractDocumentEventSerialCodec<CancelDocumentEvent> {

    private static final String CODEC_ID = "CancelDocumentEvent";
    private static final String CANCELLED_KEY = "cancelled";
    private static final String READ_KEY = "read";

    public CancelDocumentEventSerialCodec() {
        super(CancelDocumentEvent.class, CODEC_ID);
    }

    @Override
    public void encode(CancelDocumentEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeBoolean(serial, CANCELLED_KEY, o.isCancelled());
        encodeBoolean(serial, READ_KEY, o.isCancelled());
    }

    @Override
    public CancelDocumentEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new CancelDocumentEvent(
            decodeDocumentPrimaryKey(serial),
            decodeBoolean(serial, CANCELLED_KEY),
            decodeBoolean(serial, READ_KEY)
        ), serial);
    }
}
