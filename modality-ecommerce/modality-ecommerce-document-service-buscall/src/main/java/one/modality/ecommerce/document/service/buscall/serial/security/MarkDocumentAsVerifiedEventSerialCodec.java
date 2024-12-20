package one.modality.ecommerce.document.service.buscall.serial.security;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.security.MarkDocumentAsVerifiedEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentAsVerifiedEventSerialCodec extends AbstractDocumentEventSerialCodec<MarkDocumentAsVerifiedEvent> {

    private static final String CODEC_ID = "MarkDocumentAsVerifiedEvent";

    public MarkDocumentAsVerifiedEventSerialCodec() {
        super(MarkDocumentAsVerifiedEvent.class, CODEC_ID);
    }

    @Override
    public MarkDocumentAsVerifiedEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new MarkDocumentAsVerifiedEvent(
            decodeDocumentPrimaryKey(serial)
        ), serial);
    }
}
