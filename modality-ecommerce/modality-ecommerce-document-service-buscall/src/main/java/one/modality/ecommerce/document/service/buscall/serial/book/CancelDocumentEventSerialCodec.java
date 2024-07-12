package one.modality.ecommerce.document.service.buscall.serial.book;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractSetDocumentFieldsEventSerialCodec;
import one.modality.ecommerce.document.service.events.book.CancelDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class CancelDocumentEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<CancelDocumentEvent> {

    private static final String CODEC_ID = "CancelDocumentEvent";

    public CancelDocumentEventSerialCodec() {
        super(CancelDocumentEvent.class, CODEC_ID);
    }

    @Override
    public CancelDocumentEvent decode(ReadOnlyAstObject serial) {
        Object[] values = decodeValues(serial);
        return postDecode(new CancelDocumentEvent(
                decodeDocumentPrimaryKey(serial),
                CancelDocumentEvent.isCancelled(values),
                CancelDocumentEvent.isRead(values)
        ), serial);
    }
}
