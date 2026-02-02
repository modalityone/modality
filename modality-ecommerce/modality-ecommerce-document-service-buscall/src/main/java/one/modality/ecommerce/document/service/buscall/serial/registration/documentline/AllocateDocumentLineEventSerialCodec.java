package one.modality.ecommerce.document.service.buscall.serial.registration.documentline;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.orm.entity.Entities;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentLineEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.documentline.AllocateDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class AllocateDocumentLineEventSerialCodec extends AbstractDocumentLineEventSerialCodec<AllocateDocumentLineEvent> {

    private static final String CODEC_ID = "AllocateDocumentLineEvent";

    private static final String RESOURCE_CONFIGURATION_KEY = "resourceConfiguration";

    public AllocateDocumentLineEventSerialCodec() {
        super(AllocateDocumentLineEvent.class, CODEC_ID);
    }

    @Override
    public void encode(AllocateDocumentLineEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeObject(serial, RESOURCE_CONFIGURATION_KEY, Entities.getPrimaryKey(o.getResourceConfiguration()));
    }

    @Override
    public AllocateDocumentLineEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new AllocateDocumentLineEvent(
            decodeDocumentPrimaryKey(serial),
            decodeDocumentLinePrimaryKey(serial),
            decodeObject(serial, RESOURCE_CONFIGURATION_KEY)
        ), serial);
    }
}
