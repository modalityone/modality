package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.MarkDocumentAsWillPayEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentAsWillPayEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<MarkDocumentAsWillPayEvent> {

    private static final String CODEC_ID = "MarkDocumentAsWillPayEvent";

    public MarkDocumentAsWillPayEventSerialCodec() {
        super(MarkDocumentAsWillPayEvent.class, CODEC_ID);
    }

    @Override
    public MarkDocumentAsWillPayEvent decode(ReadOnlyAstObject serial) {
        Object[] values = decodeValues(serial);
        return postDecode(new MarkDocumentAsWillPayEvent(
                decodeDocumentPrimaryKey(serial),
                MarkDocumentAsWillPayEvent.isWillPay(values)
        ), serial);
    }
}
