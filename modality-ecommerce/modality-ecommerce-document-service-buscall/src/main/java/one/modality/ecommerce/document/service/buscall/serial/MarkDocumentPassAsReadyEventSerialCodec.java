package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.MarkDocumentPassAsReadyEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentPassAsReadyEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<MarkDocumentPassAsReadyEvent> {

    private static final String CODEC_ID = "MarkDocumentPassAsReadyEvent";

    public MarkDocumentPassAsReadyEventSerialCodec() {
        super(MarkDocumentPassAsReadyEvent.class, CODEC_ID);
    }

    @Override
    public MarkDocumentPassAsReadyEvent decode(ReadOnlyAstObject serial) {
        Object[] values = decodeValues(serial);
        return postDecode(new MarkDocumentPassAsReadyEvent(
                decodeDocumentPrimaryKey(serial),
                MarkDocumentPassAsReadyEvent.isPassReady(values),
                MarkDocumentPassAsReadyEvent.isRead(values)
        ), serial);
    }
}
