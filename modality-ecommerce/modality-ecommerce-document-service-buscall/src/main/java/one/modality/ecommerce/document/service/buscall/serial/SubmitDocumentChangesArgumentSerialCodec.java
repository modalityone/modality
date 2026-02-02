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
public final class SubmitDocumentChangesArgumentSerialCodec extends SerialCodecBase<SubmitDocumentChangesArgument> {

    private static final String CODEC_ID = "SubmitDocumentArgument";

    private static final String HISTORY_COMMENT_KEY = "historyComment";
    private static final String DOCUMENT_EVENTS_KEY = "documentEvents";
    private static final String QUEUE_CAPABLE_KEY = "queueCapable";

    public SubmitDocumentChangesArgumentSerialCodec() {
        super(SubmitDocumentChangesArgument.class, CODEC_ID);
    }

    @Override
    public void encode(SubmitDocumentChangesArgument arg, AstObject serial) {
        encodeString( serial, HISTORY_COMMENT_KEY, arg.historyComment());
        encodeArray(  serial, DOCUMENT_EVENTS_KEY, arg.documentEvents());
        encodeBoolean(serial, QUEUE_CAPABLE_KEY,   arg.queueCapable());
    }

    @Override
    public SubmitDocumentChangesArgument decode(ReadOnlyAstObject serial) {
        return new SubmitDocumentChangesArgument(
            decodeString( serial, HISTORY_COMMENT_KEY),
            decodeArray(  serial, DOCUMENT_EVENTS_KEY, AbstractDocumentEvent.class),
            decodeBoolean(serial, QUEUE_CAPABLE_KEY)
        );
    }

    static {
        RArray.register(AbstractDocumentEvent.class, AbstractDocumentEvent[]::new);
    }

}
