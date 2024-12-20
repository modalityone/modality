package one.modality.ecommerce.document.service.buscall.serial.registration;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.MarkDocumentAsArrivedEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentAsArrivedEventSerialCodec extends AbstractDocumentEventSerialCodec<MarkDocumentAsArrivedEvent> {

    private static final String CODEC_ID = "MarkDocumentAsArrivedEvent";

    private static final String ARRIVED_KEY = "arrived";
    private static final String READ_KEY = "read";

    public MarkDocumentAsArrivedEventSerialCodec() {
        super(MarkDocumentAsArrivedEvent.class, CODEC_ID);
    }

    @Override
    public void encode(MarkDocumentAsArrivedEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeBoolean(serial, ARRIVED_KEY, o.isArrived());
        encodeBoolean(serial, READ_KEY, o.isRead());
    }

    @Override
    public MarkDocumentAsArrivedEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new MarkDocumentAsArrivedEvent(
            decodeDocumentPrimaryKey(serial),
            decodeBoolean(serial, ARRIVED_KEY),
            decodeBoolean(serial, READ_KEY)
        ), serial);
    }
}
