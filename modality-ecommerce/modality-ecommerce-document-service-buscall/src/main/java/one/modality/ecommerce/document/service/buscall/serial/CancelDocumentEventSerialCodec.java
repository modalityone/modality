package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.CancelDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class CancelDocumentEventSerialCodec extends AbstractDocumentEventSerialCodec<CancelDocumentEvent> {

    private static final String CODEC_ID = "CancelDocumentEvent";

    public CancelDocumentEventSerialCodec() {
        super(CancelDocumentEvent.class, CODEC_ID);
    }

    @Override
    public void encode(CancelDocumentEvent o, AstObject serial) {
        super.encode(o, serial);
    }

    @Override
    public CancelDocumentEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new CancelDocumentEvent(
                decodeDocumentPrimaryKey(serial)
        ), serial);
    }
}
