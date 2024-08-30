package one.modality.ecommerce.document.service.buscall.serial.book;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentLineEventSerialCodec;
import one.modality.ecommerce.document.service.events.book.AddDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class AddDocumentLineEventSerialCodec extends AbstractDocumentLineEventSerialCodec<AddDocumentLineEvent> {

    private static final String CODEC_ID = "AddDocumentLineEvent";

    private static final String SITE_PRIMARY_KEY = "site";
    private static final String ITEM_PRIMARY_KEY = "item";

    public AddDocumentLineEventSerialCodec() {
        super(AddDocumentLineEvent.class, CODEC_ID);
    }

    @Override
    public void encode(AddDocumentLineEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeObject(serial, SITE_PRIMARY_KEY, o.getSitePrimaryKey());
        encodeObject(serial, ITEM_PRIMARY_KEY, o.getItemPrimaryKey());
    }

    @Override
    public AddDocumentLineEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new AddDocumentLineEvent(
                decodeDocumentPrimaryKey(serial),
                decodeDocumentLinePrimaryKey(serial),
                decodeObject(serial, SITE_PRIMARY_KEY),
                decodeObject(serial, ITEM_PRIMARY_KEY)
        ), serial);
    }
}
