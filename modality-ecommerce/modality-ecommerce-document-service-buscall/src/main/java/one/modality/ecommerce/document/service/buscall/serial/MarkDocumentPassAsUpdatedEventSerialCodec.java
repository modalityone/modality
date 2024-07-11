package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.MarkDocumentPassAsReadyEvent;
import one.modality.ecommerce.document.service.events.MarkDocumentPassAsUpdatedEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentPassAsUpdatedEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<MarkDocumentPassAsUpdatedEvent> {

    private static final String CODEC_ID = "MarkDocumentPassAsUpdatedEvent";

    public MarkDocumentPassAsUpdatedEventSerialCodec() {
        super(MarkDocumentPassAsUpdatedEvent.class, CODEC_ID);
    }

    @Override
    public MarkDocumentPassAsUpdatedEvent decode(ReadOnlyAstObject serial) {
        Object[] values = decodeValues(serial);
        return postDecode(new MarkDocumentPassAsUpdatedEvent(
                decodeDocumentPrimaryKey(serial),
                MarkDocumentPassAsReadyEvent.isRead(values)
        ), serial);
    }
}
