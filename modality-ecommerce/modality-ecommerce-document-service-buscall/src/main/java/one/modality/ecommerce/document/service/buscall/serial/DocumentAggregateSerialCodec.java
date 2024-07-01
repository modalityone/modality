package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.util.Arrays;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class DocumentAggregateSerialCodec extends SerialCodecBase<DocumentAggregate> {

    private static final String CODEC_ID = "DocumentAggregate";
    private static final String PREVIOUS_VERSION_KEY = "previousVersion";
    private static final String NEW_DOCUMENT_EVENTS_KEY = "newDocumentEvents";

    public DocumentAggregateSerialCodec() {
        super(DocumentAggregate.class, CODEC_ID);
    }

    @Override
    public void encode(DocumentAggregate o, AstObject serial) {
        encodeObject(serial, PREVIOUS_VERSION_KEY,     o.getPreviousVersion());
        encodeArray( serial, NEW_DOCUMENT_EVENTS_KEY,  o.getNewDocumentEvents().toArray());
    }

    @Override
    public DocumentAggregate decode(ReadOnlyAstObject serial) {
        return new DocumentAggregate(
                decodeObject(serial, PREVIOUS_VERSION_KEY),
                Arrays.asList(decodeArray(serial, NEW_DOCUMENT_EVENTS_KEY, AbstractDocumentEvent.class))
        );
    }

}
