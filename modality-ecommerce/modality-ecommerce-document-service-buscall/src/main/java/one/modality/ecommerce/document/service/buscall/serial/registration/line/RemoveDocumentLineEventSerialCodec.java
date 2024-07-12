package one.modality.ecommerce.document.service.buscall.serial.registration.line;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentLineEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.line.RemoveDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class RemoveDocumentLineEventSerialCodec extends AbstractDocumentLineEventSerialCodec<RemoveDocumentLineEvent> {

    private static final String CODEC_ID = "RemoveDocumentLineEvent";

    public RemoveDocumentLineEventSerialCodec() {
        super(RemoveDocumentLineEvent.class, CODEC_ID);
    }

    @Override
    public RemoveDocumentLineEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new RemoveDocumentLineEvent(
                decodeDocumentPrimaryKey(serial),
                decodeDocumentLinePrimaryKey(serial)
        ), serial);
    }
}
