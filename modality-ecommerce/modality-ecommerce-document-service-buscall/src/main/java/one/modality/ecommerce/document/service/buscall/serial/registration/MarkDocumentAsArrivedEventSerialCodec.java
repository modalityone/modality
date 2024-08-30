package one.modality.ecommerce.document.service.buscall.serial.registration;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractSetDocumentFieldsEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.MarkDocumentAsArrivedEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentAsArrivedEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<MarkDocumentAsArrivedEvent> {

    private static final String CODEC_ID = "MarkDocumentAsArrivedEvent";

    public MarkDocumentAsArrivedEventSerialCodec() {
        super(MarkDocumentAsArrivedEvent.class, CODEC_ID);
    }

    @Override
    public MarkDocumentAsArrivedEvent decode(ReadOnlyAstObject serial) {
        Object[] values = decodeValues(serial);
        return postDecode(new MarkDocumentAsArrivedEvent(
                decodeDocumentPrimaryKey(serial),
                MarkDocumentAsArrivedEvent.isArrived(values),
                MarkDocumentAsArrivedEvent.isRead(values)
        ), serial);
    }
}
