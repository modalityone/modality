package one.modality.ecommerce.document.service.buscall.serial.registration;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.FlagDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class FlagDocumentEventSerialCodec extends AbstractDocumentEventSerialCodec<FlagDocumentEvent> {

    private static final String CODEC_ID = "FlagDocumentEvent";

    private static final String FLAGGED_KEY = "flagged";

    public FlagDocumentEventSerialCodec() {
        super(FlagDocumentEvent.class, CODEC_ID);
    }

    @Override
    public void encode(FlagDocumentEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeBoolean(serial, FLAGGED_KEY, o.isFlagged());
    }

    @Override
    public FlagDocumentEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new FlagDocumentEvent(
            decodeDocumentPrimaryKey(serial),
            decodeBoolean(serial, FLAGGED_KEY)
        ), serial);
    }
}
