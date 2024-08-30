package one.modality.ecommerce.document.service.buscall.serial.registration;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractSetDocumentFieldsEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.ConfirmDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class ConfirmDocumentEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<ConfirmDocumentEvent> {

    private static final String CODEC_ID = "ConfirmDocumentEvent";

    public ConfirmDocumentEventSerialCodec() {
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
