package one.modality.ecommerce.document.service.buscall.serial.registration;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.MarkDocumentAsWillPayEvent;

/**
 * @author Bruno Salmon
 */
public final class MarkDocumentAsWillPayEventSerialCodec extends AbstractDocumentEventSerialCodec<MarkDocumentAsWillPayEvent> {

    private static final String CODEC_ID = "MarkDocumentAsWillPayEvent";

    private static final String WILL_PAY_KEY = "willPay";

    public MarkDocumentAsWillPayEventSerialCodec() {
        super(MarkDocumentAsWillPayEvent.class, CODEC_ID);
    }

    @Override
    public void encode(MarkDocumentAsWillPayEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeBoolean(serial, WILL_PAY_KEY, o.isWillPay());
    }

    @Override
    public MarkDocumentAsWillPayEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new MarkDocumentAsWillPayEvent(
                decodeDocumentPrimaryKey(serial),
                decodeBoolean(serial, WILL_PAY_KEY)
        ), serial);
    }
}
