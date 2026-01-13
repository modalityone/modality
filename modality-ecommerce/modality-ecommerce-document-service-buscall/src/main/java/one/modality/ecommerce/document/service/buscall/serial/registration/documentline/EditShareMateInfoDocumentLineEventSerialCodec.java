package one.modality.ecommerce.document.service.buscall.serial.registration.documentline;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentLineEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.documentline.EditShareMateInfoDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class EditShareMateInfoDocumentLineEventSerialCodec extends AbstractDocumentLineEventSerialCodec<EditShareMateInfoDocumentLineEvent> {

    private static final String CODEC_ID = "EditShareMateInfoDocumentLineEvent";

    private static final String OWNER_NAME_KEY = "ownerName";

    public EditShareMateInfoDocumentLineEventSerialCodec() {
        super(EditShareMateInfoDocumentLineEvent.class, CODEC_ID);
    }

    @Override
    public void encode(EditShareMateInfoDocumentLineEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeString(serial, OWNER_NAME_KEY, o.getOwnerName());
    }

    @Override
    public EditShareMateInfoDocumentLineEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new EditShareMateInfoDocumentLineEvent(
            decodeDocumentPrimaryKey(serial),
            decodeDocumentLinePrimaryKey(serial),
            decodeString(serial, OWNER_NAME_KEY)
        ), serial);
    }
}
