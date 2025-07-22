package one.modality.ecommerce.document.service.buscall.serial.book;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.book.AddRequestEvent;

/**
 * @author Bruno Salmon
 */
public final class AddRequestEventSerialCodec extends AbstractDocumentEventSerialCodec<AddRequestEvent> {

    private static final String CODEC_ID = "AddRequestEvent";
    private static final String REQUEST_KEY = "request";

    public AddRequestEventSerialCodec() {
        super(AddRequestEvent.class, CODEC_ID);
    }

    @Override
    public void encode(AddRequestEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeString(serial, REQUEST_KEY, o.getRequest());
    }

    @Override
    public AddRequestEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new AddRequestEvent(
            decodeDocumentPrimaryKey(serial),
            decodeString(serial, REQUEST_KEY)
        ), serial);
    }
}
