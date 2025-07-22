package one.modality.ecommerce.document.service.buscall.serial.book;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentEventSerialCodec;
import one.modality.ecommerce.document.service.events.book.ApplyFacilityFeeEvent;

/**
 * @author Bruno Salmon
 */
public final class ApplyFacilityFeeEventSerialCodec extends AbstractDocumentEventSerialCodec<ApplyFacilityFeeEvent> {

    private static final String CODEC_ID = "ApplyFacilityFeeEvent";
    private static final String APPLY_KEY = "apply";

    public ApplyFacilityFeeEventSerialCodec() {
        super(ApplyFacilityFeeEvent.class, CODEC_ID);
    }

    @Override
    public void encode(ApplyFacilityFeeEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeBoolean(serial, APPLY_KEY, o.isApply());
    }

    @Override
    public ApplyFacilityFeeEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new ApplyFacilityFeeEvent(
            decodeDocumentPrimaryKey(serial),
            decodeBoolean(serial, APPLY_KEY)
        ), serial);
    }
}
