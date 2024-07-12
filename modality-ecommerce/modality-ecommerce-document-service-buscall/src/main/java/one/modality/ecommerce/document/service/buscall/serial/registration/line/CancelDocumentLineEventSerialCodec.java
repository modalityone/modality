package one.modality.ecommerce.document.service.buscall.serial.registration.line;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractSetDocumentLineFieldsEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.line.CancelDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class CancelDocumentLineEventSerialCodec extends AbstractSetDocumentLineFieldsEventSerialCodec<CancelDocumentLineEvent> {

    private static final String CODEC_ID = "CancelDocumentLineEvent";

    public CancelDocumentLineEventSerialCodec() {
        super(CancelDocumentLineEvent.class, CODEC_ID);
    }

    @Override
    public CancelDocumentLineEvent decode(ReadOnlyAstObject serial) {
        Object[] values = decodeValues(serial);
        return postDecode(new CancelDocumentLineEvent(
                decodeDocumentPrimaryKey(serial),
                decodeDocumentLinePrimaryKey(serial),
                CancelDocumentLineEvent.isCancelled(values),
                CancelDocumentLineEvent.isRead(values)
        ), serial);
    }
}
