package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.document.service.DocumentAggregate;

/**
 * @author Bruno Salmon
 */
public final class DocumentAggregateSerialCodec extends SerialCodecBase<DocumentAggregate> {

    private static final String CODEC_ID = "DocumentAggregate";

    public DocumentAggregateSerialCodec() {
        super(DocumentAggregate.class, CODEC_ID);
    }

    @Override
    public DocumentAggregate decode(ReadOnlyAstObject serial) {
        return new DocumentAggregate(null, null, null); // TODO
    }

    @Override
    public void encode(DocumentAggregate javaObject, AstObject serial) {
        // TODO
    }
}
