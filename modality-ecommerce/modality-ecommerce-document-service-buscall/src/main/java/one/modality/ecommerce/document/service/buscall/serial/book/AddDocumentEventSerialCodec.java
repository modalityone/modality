package one.modality.ecommerce.document.service.buscall.serial.book;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.book.AddDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class AddDocumentEventSerialCodec extends AbstractDocumentEventSerialCodec<AddDocumentEvent> {

    private static final String CODEC_ID = "AddDocumentEvent";

    private static final String EVENT_PRIMARY_KEY = "event";
    private static final String PERSON_PRIMARY_KEY = "person";
    private static final String FIRST_NAME_KEY = "firstName";
    private static final String LAST_NAME_KEY = "lastName";
    private static final String EMAIL_KEY = "email";
    private static final String REF_KEY = "ref";

    public AddDocumentEventSerialCodec() {
        super(AddDocumentEvent.class, CODEC_ID);
    }

    @Override
    public void encode(AddDocumentEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeObject(serial,  EVENT_PRIMARY_KEY,  o.getEventPrimaryKey());
        encodeObject(serial,  PERSON_PRIMARY_KEY, o.getPersonPrimaryKey());
        encodeString(serial,  FIRST_NAME_KEY,     o.getFirstName());
        encodeString(serial,  LAST_NAME_KEY,      o.getLastName());
        encodeString(serial,  EMAIL_KEY,          o.getEmail());
        encodeInteger(serial, REF_KEY,            o.getRef());
    }

    @Override
    public AddDocumentEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new AddDocumentEvent(
            decodeDocumentPrimaryKey(serial),
            decodeObject(serial, EVENT_PRIMARY_KEY),
            decodeObject(serial, PERSON_PRIMARY_KEY),
            decodeString(serial, FIRST_NAME_KEY),
            decodeString(serial, LAST_NAME_KEY),
            decodeString(serial, EMAIL_KEY),
            decodeInteger(serial, REF_KEY)
        ), serial);
    }
}
