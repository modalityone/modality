package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.document.service.SubmitDocumentChangesArgument;
import one.modality.ecommerce.document.service.events.DocumentEvent;

/**
 * @author Bruno Salmon
 */
public class SubmitDocumentArgumentSerialCodec extends SerialCodecBase<SubmitDocumentChangesArgument> {

    private static final String CODEC_ID = "SubmitDocumentArgument";

    public SubmitDocumentArgumentSerialCodec() {
        super(SubmitDocumentChangesArgument.class, CODEC_ID);
    }

    @Override
    public SubmitDocumentChangesArgument decodeFromJson(ReadOnlyAstObject json) {
        return new SubmitDocumentChangesArgument(new DocumentEvent[]{});
    }

    @Override
    public void encodeToJson(SubmitDocumentChangesArgument javaObject, AstObject json) {

    }

}
