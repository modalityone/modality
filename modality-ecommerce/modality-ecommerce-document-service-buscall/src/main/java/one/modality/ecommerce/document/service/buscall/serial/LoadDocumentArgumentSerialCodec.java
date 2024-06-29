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
    private static final String PERSON_PRIMARY_KEY_KEY = "person";
    private static final String EVENT_PRIMARY_KEY_KEY = "event";

    public LoadDocumentArgumentSerialCodec() {
        super(LoadDocumentArgument.class, CODEC_ID);
    }

    @Override
    public void encode(LoadDocumentArgument lda, AstObject serial) {
        encodeObject(serial, DOCUMENT_PRIMARY_KEY_KEY, lda.getDocumentPrimaryKey());
        encodeObject(serial, PERSON_PRIMARY_KEY_KEY,   lda.getPersonPrimaryKey());
        encodeObject(serial, EVENT_PRIMARY_KEY_KEY,    lda.getEventPrimaryKey());
    }

    @Override
    public LoadDocumentArgument decode(ReadOnlyAstObject serial) {
        return new LoadDocumentArgument(
                decodeObject(serial, DOCUMENT_PRIMARY_KEY_KEY),
                decodeObject(serial, PERSON_PRIMARY_KEY_KEY),
                decodeObject(serial, EVENT_PRIMARY_KEY_KEY)
        );
    }

}
