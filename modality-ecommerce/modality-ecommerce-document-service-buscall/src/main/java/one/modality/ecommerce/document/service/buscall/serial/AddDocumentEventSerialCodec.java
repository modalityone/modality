package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.AddDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class AddDocumentEventSerialCodec extends AbstractDocumentEventSerialCodec<AddDocumentEvent> {

    private static final String CODEC_ID = "AddDocumentEvent";

    private static final String EVENT_PRIMARY_KEY = "event";
    private static final String PERSON_PRIMARY_KEY = "person";

    public AddDocumentEventSerialCodec() {
        super(AddDocumentEvent.class, CODEC_ID);
    }

    @Override
    public void encode(AddDocumentEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeObject(serial, EVENT_PRIMARY_KEY,  o.getEventPrimaryKey());
        encodeObject(serial, PERSON_PRIMARY_KEY, o.getPersonPrimaryKey());
    }

    @Override
    public AddDocumentEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new AddDocumentEvent(
                decodeDocumentPrimaryKey(serial),
                decodeObject(serial, EVENT_PRIMARY_KEY),
                decodeObject(serial, PERSON_PRIMARY_KEY)
        ), serial);
    }
}
