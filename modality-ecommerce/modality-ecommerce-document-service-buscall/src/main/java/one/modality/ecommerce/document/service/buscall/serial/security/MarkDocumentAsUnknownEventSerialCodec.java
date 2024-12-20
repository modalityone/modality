package one.modality.ecommerce.document.service.buscall.serial.security;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.security.MarkDocumentAsUnknownEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentAsUnknownEventSerialCodec extends AbstractDocumentEventSerialCodec<MarkDocumentAsUnknownEvent> {

    private static final String CODEC_ID = "MarkDocumentAsUnknownEvent";

    public MarkDocumentAsUnknownEventSerialCodec() {
        super(MarkDocumentAsUnknownEvent.class, CODEC_ID);
    }

    @Override
    public MarkDocumentAsUnknownEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new MarkDocumentAsUnknownEvent(
            decodeDocumentPrimaryKey(serial)
        ), serial);
    }
}
