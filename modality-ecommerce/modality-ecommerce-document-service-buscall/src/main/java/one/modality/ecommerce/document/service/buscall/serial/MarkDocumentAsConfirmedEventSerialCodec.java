package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.ConfirmDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentAsConfirmedEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<ConfirmDocumentEvent> {

    private static final String CODEC_ID = "ConfirmDocumentEvent";

    public MarkDocumentAsConfirmedEventSerialCodec() {
        super(ConfirmDocumentEvent.class, CODEC_ID);
    }

    @Override
    public ConfirmDocumentEvent decode(ReadOnlyAstObject serial) {
        Object[] values = decodeValues(serial);
        return postDecode(new ConfirmDocumentEvent(
                decodeDocumentPrimaryKey(serial),
                ConfirmDocumentEvent.isConfirmed(values),
                ConfirmDocumentEvent.isRead(values)
        ), serial);
    }
}
