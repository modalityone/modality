package one.modality.ecommerce.document.service.buscall.serial.security;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractSetDocumentFieldsEventSerialCodec;
import one.modality.ecommerce.document.service.events.security.MarkDocumentAsUncheckedEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentAsUncheckedEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<MarkDocumentAsUncheckedEvent> {

    private static final String CODEC_ID = "MarkDocumentAsUncheckedEvent";

    public MarkDocumentAsUncheckedEventSerialCodec() {
        super(MarkDocumentAsUncheckedEvent.class, CODEC_ID);
    }

    @Override
    public MarkDocumentAsUncheckedEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new MarkDocumentAsUncheckedEvent(
                decodeDocumentPrimaryKey(serial)
        ), serial);
    }
}
