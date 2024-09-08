package one.modality.ecommerce.document.service.buscall.serial.registration.documentline;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentLineEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.documentline.CancelDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class CancelDocumentLineEventSerialCodec extends AbstractDocumentLineEventSerialCodec<CancelDocumentLineEvent> {

    private static final String CODEC_ID = "CancelDocumentLineEvent";

    private static final String CANCELLED_KEY = "cancelled";
    private static final String READ_KEY = "read";


    public CancelDocumentLineEventSerialCodec() {
        super(CancelDocumentLineEvent.class, CODEC_ID);
    }

    @Override
    public void encode(CancelDocumentLineEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeBoolean(serial, CANCELLED_KEY, o.isCancelled());
        encodeBoolean(serial, READ_KEY,      o.isRead());
    }

    @Override
    public CancelDocumentLineEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new CancelDocumentLineEvent(
            decodeDocumentPrimaryKey(serial),
            decodeDocumentLinePrimaryKey(serial),
            decodeBoolean(serial, CANCELLED_KEY),
            decodeBoolean(serial, READ_KEY)
        ), serial);
    }
}
