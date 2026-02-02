package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.document.service.DocumentChangesRejectedReason;
import one.modality.ecommerce.document.service.DocumentChangesStatus;
import one.modality.ecommerce.document.service.SubmitDocumentChangesResult;

/**
 * @author Bruno Salmon
 */
public final class SubmitDocumentChangesResultSerialCodec extends SerialCodecBase<SubmitDocumentChangesResult> {

    private static final String CODEC_ID = "SubmitDocumentChangesResult";

    private static final String STATUS_KEY = "status";
    private static final String REASON_KEY = "reason";
    private static final String DOCUMENT_PRIMARY_KEY_KEY = "documentPk";
    private static final String DOCUMENT_REF_KEY = "documentRef";
    private static final String CART_PRIMARY_KEY_KEY = "cart";
    private static final String CART_UUID_KEY = "cartUuid";
    private static final String SOLD_OUT_SITE_PRIMARY_KEY_KEY = "soldOutSitePk";
    private static final String SOLD_OUT_ITEM_PRIMARY_KEY_KEY = "soldOutItemPk";
    private static final String QUEUE_TOKEN_KEY = "queueToken";
    private static final String ERROR_KEY = "error";

    public SubmitDocumentChangesResultSerialCodec() {
        super(SubmitDocumentChangesResult.class, CODEC_ID);
    }

    @Override
    public void encode(SubmitDocumentChangesResult arg, AstObject serial) {
        encodeString(serial, STATUS_KEY,                    arg.status().name());
        if (arg.rejectedReason() != null)
            encodeString(serial, REASON_KEY,                arg.rejectedReason().name());
        encodeObject(serial, DOCUMENT_PRIMARY_KEY_KEY,      arg.documentPrimaryKey());
        encodeObject(serial, DOCUMENT_REF_KEY,              arg.documentRef());
        encodeObject(serial, CART_PRIMARY_KEY_KEY,          arg.cartPrimaryKey());
        encodeString(serial, CART_UUID_KEY,                 arg.cartUuid());
        encodeObject(serial, SOLD_OUT_SITE_PRIMARY_KEY_KEY, arg.soldOutSitePrimaryKey());
        encodeObject(serial, SOLD_OUT_ITEM_PRIMARY_KEY_KEY, arg.soldOutItemPrimaryKey());
        encodeObject(serial, QUEUE_TOKEN_KEY,               arg.queueToken());
        encodeObject(serial, ERROR_KEY,                     arg.errorMessage());
    }

    @Override
    public SubmitDocumentChangesResult decode(ReadOnlyAstObject serial) {
        String reason = decodeString(serial, REASON_KEY);
        return new SubmitDocumentChangesResult(
            DocumentChangesStatus.valueOf(decodeString(serial, STATUS_KEY)),
            reason == null ? null : DocumentChangesRejectedReason.valueOf(reason),
            decodeObject(serial, DOCUMENT_PRIMARY_KEY_KEY),
            decodeObject(serial, DOCUMENT_REF_KEY),
            decodeObject(serial, CART_PRIMARY_KEY_KEY),
            decodeString(serial, CART_UUID_KEY),
            decodeObject(serial, SOLD_OUT_SITE_PRIMARY_KEY_KEY),
            decodeObject(serial, SOLD_OUT_ITEM_PRIMARY_KEY_KEY),
            decodeObject(serial, QUEUE_TOKEN_KEY),
            decodeString(serial, ERROR_KEY)
            );
    }

}
