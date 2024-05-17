package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.reflect.RArray;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class SubmitDocumentArgumentSerialCodec extends SerialCodecBase<SubmitDocumentChangesArgument> {

    private static final String CODEC_ID = "SubmitDocumentArgument";

    private static final String DOCUMENT_EVENTS_KEY = "documentEvents";

    public SubmitDocumentArgumentSerialCodec() {
        super(SubmitDocumentChangesArgument.class, CODEC_ID);
    }

    @Override
    public void encode(SubmitDocumentChangesArgument arg, AstObject serial) {
        encodeArray(serial, DOCUMENT_EVENTS_KEY, arg.getDocumentEvents());
    }

    @Override
    public SubmitDocumentChangesArgument decode(ReadOnlyAstObject serial) {
        return new SubmitDocumentChangesArgument(
                decodeArray(serial, DOCUMENT_EVENTS_KEY, AbstractDocumentEvent.class)
        );
    }

    static {
        RArray.register(AbstractDocumentEvent.class, AbstractDocumentEvent[]::new);
    }

}
