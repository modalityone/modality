package one.modality.ecommerce.document.service.buscall.serial.registration;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.ConfirmDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class ConfirmDocumentEventSerialCodec extends AbstractDocumentEventSerialCodec<ConfirmDocumentEvent> {

    private static final String CODEC_ID = "ConfirmDocumentEvent";

    private static final String CONFIRMED_KEY = "confirmed";
    private static final String READ_KEY = "read";

    public ConfirmDocumentEventSerialCodec() {
        super(ConfirmDocumentEvent.class, CODEC_ID);
    }

    @Override
    public void encode(ConfirmDocumentEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeBoolean(serial, CONFIRMED_KEY, o.isConfirmed());
        encodeBoolean(serial, READ_KEY, o.isRead());
    }

    @Override
    public ConfirmDocumentEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new ConfirmDocumentEvent(
            decodeDocumentPrimaryKey(serial),
            decodeBoolean(serial, CONFIRMED_KEY),
            decodeBoolean(serial, READ_KEY)
        ), serial);
    }
}
