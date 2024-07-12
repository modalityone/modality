package one.modality.ecommerce.document.service.buscall.serial.registration;

import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractSetDocumentFieldsEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.FlagDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class FlagDocumentEventSerialCodec extends AbstractSetDocumentFieldsEventSerialCodec<FlagDocumentEvent> {

    private static final String CODEC_ID = "FlagDocumentEvent";

    public FlagDocumentEventSerialCodec() {
        super(FlagDocumentEvent.class, CODEC_ID);
    }

    @Override
    public FlagDocumentEvent decode(ReadOnlyAstObject serial) {
        Object[] values = decodeValues(serial);
        return postDecode(new FlagDocumentEvent(
                decodeDocumentPrimaryKey(serial),
                FlagDocumentEvent.isFlagged(values)
        ), serial);
    }
}
