package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.SerialCodecManager;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.document.service.LoadDocumentArgument;

/**
 * @author Bruno Salmon
 */
public class LoadDocumentArgumentSerialCodec extends SerialCodecBase<LoadDocumentArgument> {

    private static final String CODEC_ID = "LoadDocumentArgument";
    private static final String PRIMARY_KEY_KEY = "primaryKey";
    private static final String DATE_TIME_KEY = "dateTime";

    public LoadDocumentArgumentSerialCodec() {
        super(LoadDocumentArgument.class, CODEC_ID);
    }

    @Override
    public LoadDocumentArgument decodeFromJson(ReadOnlyAstObject json) {
        return new LoadDocumentArgument(
                SerialCodecManager.decodeFromJson(json.get(PRIMARY_KEY_KEY)),
                SerialCodecManager.decodeLocalDateTime(json.get(DATE_TIME_KEY))
        );
    }

    @Override
    public void encodeToJson(LoadDocumentArgument lda, AstObject json) {
        encodeKey(PRIMARY_KEY_KEY, lda.getPrimaryKey(), json);
        json.set(DATE_TIME_KEY, SerialCodecManager.encodeLocalDateTime(lda.getDateTime()));
    }
}
