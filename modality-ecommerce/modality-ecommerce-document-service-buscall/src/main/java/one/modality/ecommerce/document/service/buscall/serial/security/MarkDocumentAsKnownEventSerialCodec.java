package one.modality.ecommerce.document.service.buscall.serial.security;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.security.MarkDocumentAsKnownEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentAsKnownEventSerialCodec extends AbstractDocumentEventSerialCodec<MarkDocumentAsKnownEvent> {

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
