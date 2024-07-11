package one.modality.ecommerce.document.service.buscall.serial.security;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractSetDocumentFieldsEventSerialCodec;
import one.modality.ecommerce.document.service.events.security.MarkDocumentAsKnownEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentAsKnownEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<MarkDocumentAsKnownEvent> {

    private static final String CODEC_ID = "MarkDocumentAsKnownEvent";

    public MarkDocumentAsKnownEventSerialCodec() {
        super(MarkDocumentAsKnownEvent.class, CODEC_ID);
    }

    @Override
    public MarkDocumentAsKnownEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new MarkDocumentAsKnownEvent(
                decodeDocumentPrimaryKey(serial)
        ), serial);
    }
}
