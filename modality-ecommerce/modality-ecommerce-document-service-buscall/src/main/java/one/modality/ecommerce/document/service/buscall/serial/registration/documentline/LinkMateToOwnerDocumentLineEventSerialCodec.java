package one.modality.ecommerce.document.service.buscall.serial.registration.documentline;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.orm.entity.Entities;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentLineEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.documentline.LinkMateToOwnerDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class LinkMateToOwnerDocumentLineEventSerialCodec extends AbstractDocumentLineEventSerialCodec<LinkMateToOwnerDocumentLineEvent> {

    private static final String CODEC_ID = "LinkMateToOwnerDocumentLineEvent";

    private static final String OWNER_DOCUMENT_LINE_KEY = "ownerDocumentLine";
    private static final String OWNER_PERSON_KEY = "ownerPerson";

    public LinkMateToOwnerDocumentLineEventSerialCodec() {
        super(LinkMateToOwnerDocumentLineEvent.class, CODEC_ID);
    }

    @Override
    public void encode(LinkMateToOwnerDocumentLineEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeObject(serial, OWNER_DOCUMENT_LINE_KEY, Entities.getPrimaryKey(o.getOwnerDocumentLine()));
        encodeObject(serial, OWNER_PERSON_KEY,        Entities.getPrimaryKey(o.getOwnerPerson()));
    }

    @Override
    public LinkMateToOwnerDocumentLineEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new LinkMateToOwnerDocumentLineEvent(
            decodeDocumentPrimaryKey(serial),
            decodeDocumentLinePrimaryKey(serial),
            decodeObject(serial, OWNER_DOCUMENT_LINE_KEY),
            decodeObject(serial, OWNER_PERSON_KEY)
        ), serial);
    }
}
