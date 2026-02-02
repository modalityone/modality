package one.modality.ecommerce.document.service.buscall.serial.book;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.book.EditCarersInfoEvent;

/**
 * @author Bruno Salmon
 */
public final class EditCarersInfoEventSerialCodec extends AbstractDocumentEventSerialCodec<EditCarersInfoEvent> {

    private static final String CODEC_ID = "EditCarersInfoEventSerialCodec";

    private static final String CARER1_NAME_KEY = "carer1Name";
    private static final String CARER1_DOCUMENT_PRIMARY_KEY_KEY = "carer1DocumentPk";
    private static final String CARER2_NAME_KEY = "carer2Name";
    private static final String CARER2_DOCUMENT_PRIMARY_KEY_KEY = "carer2DocumentPk";

    public EditCarersInfoEventSerialCodec() {
        super(EditCarersInfoEvent.class, CODEC_ID);
    }

    @Override
    public void encode(EditCarersInfoEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeString(serial, CARER1_NAME_KEY, o.getCarer1Name());
        encodeObject(serial, CARER1_DOCUMENT_PRIMARY_KEY_KEY, o.getCarer1DocumentPrimaryKey());
        encodeString(serial, CARER2_NAME_KEY, o.getCarer2Name());
        encodeObject(serial, CARER2_DOCUMENT_PRIMARY_KEY_KEY, o.getCarer2DocumentPrimaryKey());
    }

    @Override
    public EditCarersInfoEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new EditCarersInfoEvent(
            decodeDocumentPrimaryKey(serial),
            decodeString(serial, CARER1_NAME_KEY),
            decodeObject(serial, CARER1_DOCUMENT_PRIMARY_KEY_KEY),
            decodeString(serial, CARER2_NAME_KEY),
            decodeObject(serial, CARER2_DOCUMENT_PRIMARY_KEY_KEY)
        ), serial);
    }
}
