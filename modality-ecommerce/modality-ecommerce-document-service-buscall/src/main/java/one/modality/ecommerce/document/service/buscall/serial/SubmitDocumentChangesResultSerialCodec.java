package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.document.service.SubmitDocumentChangesResult;

/**
 * @author Bruno Salmon
 */
public final class SubmitDocumentChangesResultSerialCodec extends SerialCodecBase<SubmitDocumentChangesResult> {

    private static final String CODEC_ID = "SubmitDocumentChangesResult";

    private static final String DOCUMENT_PRIMARY_KEY_KEY = "documentPk";
    private static final String DOCUMENT_REF_KEY = "documentRef";
    private static final String CART_PRIMARY_KEY_KEY = "cart";
    private static final String CART_UUID_KEY = "cartUuid";

    public SubmitDocumentChangesResultSerialCodec() {
        super(SubmitDocumentChangesResult.class, CODEC_ID);
    }

    @Override
    public void encode(SubmitDocumentChangesResult arg, AstObject serial) {
        encodeObject(serial, DOCUMENT_PRIMARY_KEY_KEY, arg.getDocumentPrimaryKey());
        encodeObject(serial, DOCUMENT_REF_KEY,         arg.getDocumentRef());
        encodeObject(serial, CART_PRIMARY_KEY_KEY,     arg.getCartPrimaryKey());
        encodeString(serial, CART_UUID_KEY,            arg.getCartUuid());
    }

    @Override
    public SubmitDocumentChangesResult decode(ReadOnlyAstObject serial) {
        return new SubmitDocumentChangesResult(
                decodeObject(serial, DOCUMENT_PRIMARY_KEY_KEY),
                decodeObject(serial, DOCUMENT_REF_KEY),
                decodeObject(serial, CART_PRIMARY_KEY_KEY),
                decodeString(serial, CART_UUID_KEY)
        );
    }

}
