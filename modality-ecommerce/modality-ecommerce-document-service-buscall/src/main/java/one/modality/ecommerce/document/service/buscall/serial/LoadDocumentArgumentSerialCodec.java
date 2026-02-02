package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.document.service.LoadDocumentArgument;

/**
 * @author Bruno Salmon
 */
public final class LoadDocumentArgumentSerialCodec extends SerialCodecBase<LoadDocumentArgument> {

    private static final String CODEC_ID = "LoadDocumentArgument";
    private static final String DOCUMENT_PRIMARY_KEY_KEY = "document";
    private static final String EVENT_PRIMARY_KEY_KEY = "event";
    private static final String PERSON_PRIMARY_KEY_KEY = "person";
    private static final String ACCOUNT_PRIMARY_KEY_KEY = "account";
    private static final String HISTORY_PRIMARY_KEY_KEY = "history";

    public LoadDocumentArgumentSerialCodec() {
        super(LoadDocumentArgument.class, CODEC_ID);
    }

    @Override
    public void encode(LoadDocumentArgument lda, AstObject serial) {
        encodeObject(serial, DOCUMENT_PRIMARY_KEY_KEY, lda.documentPrimaryKey());
        encodeObject(serial, EVENT_PRIMARY_KEY_KEY,    lda.eventPrimaryKey());
        encodeObject(serial, PERSON_PRIMARY_KEY_KEY,   lda.personPrimaryKey());
        encodeObject(serial, ACCOUNT_PRIMARY_KEY_KEY,  lda.accountPrimaryKey());
        encodeObject(serial, HISTORY_PRIMARY_KEY_KEY,  lda.historyPrimaryKey());
    }

    @Override
    public LoadDocumentArgument decode(ReadOnlyAstObject serial) {
        return new LoadDocumentArgument(
                decodeObject(serial, DOCUMENT_PRIMARY_KEY_KEY),
            decodeObject(serial, EVENT_PRIMARY_KEY_KEY),
            decodeObject(serial, PERSON_PRIMARY_KEY_KEY),
            decodeObject(serial, ACCOUNT_PRIMARY_KEY_KEY),
            decodeObject(serial, HISTORY_PRIMARY_KEY_KEY)
        );
    }

}
