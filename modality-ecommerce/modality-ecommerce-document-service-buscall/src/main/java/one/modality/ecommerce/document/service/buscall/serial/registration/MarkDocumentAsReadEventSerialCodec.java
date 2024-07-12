package one.modality.ecommerce.document.service.buscall.serial.registration;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractSetDocumentFieldsEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.MarkDocumentAsReadEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentAsReadEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<MarkDocumentAsReadEvent> {

    private static final String CODEC_ID = "MarkDocumentAsReadEvent";

    public MarkDocumentAsReadEventSerialCodec() {
        super(MarkDocumentAsReadEvent.class, CODEC_ID);
    }

    @Override
    public MarkDocumentAsReadEvent decode(ReadOnlyAstObject serial) {
        Object[] values = decodeValues(serial);
        return postDecode(new MarkDocumentAsReadEvent(
                decodeDocumentPrimaryKey(serial),
                MarkDocumentAsReadEvent.isRead(values)
        ), serial);
    }
}
