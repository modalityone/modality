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
    private static final String PRIMARY_KEY_KEY = "primaryKey";
    private static final String DATE_TIME_KEY = "dateTime";

    public LoadDocumentArgumentSerialCodec() {
        super(LoadDocumentArgument.class, CODEC_ID);
    }

    @Override
    public void encode(LoadDocumentArgument lda, AstObject serial) {
        encodeObject(       serial, PRIMARY_KEY_KEY, lda.getPrimaryKey(), NullEncoding.NULL_VALUE_NOT_ALLOWED);
        encodeLocalDateTime(serial, DATE_TIME_KEY,   lda.getDateTime());
    }

    @Override
    public LoadDocumentArgument decode(ReadOnlyAstObject serial) {
        return new LoadDocumentArgument(
                decodeObject(       serial, PRIMARY_KEY_KEY, NullEncoding.NULL_VALUE_NOT_ALLOWED),
                decodeLocalDateTime(serial, DATE_TIME_KEY)
        );
    }

}
