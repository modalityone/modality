package one.modality.ecommerce.document.service.buscall.serial.book;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.book.ApplyFacilityFeeDocumentEvent;

/**
 * @author Bruno Salmon
 */
public final class ApplyFacilityFeeDocumentEventSerialCodec extends AbstractDocumentEventSerialCodec<ApplyFacilityFeeDocumentEvent> {

    private static final String CODEC_ID = "CancelDocumentEvent";
    private static final String APPLY_KEY = "apply";

    public ApplyFacilityFeeDocumentEventSerialCodec() {
        super(ApplyFacilityFeeDocumentEvent.class, CODEC_ID);
    }

    @Override
    public void encode(ApplyFacilityFeeDocumentEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeBoolean(serial, APPLY_KEY, o.isApply());
    }

    @Override
    public ApplyFacilityFeeDocumentEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new ApplyFacilityFeeDocumentEvent(
            decodeDocumentPrimaryKey(serial),
            decodeBoolean(serial, APPLY_KEY)
        ), serial);
    }
}
