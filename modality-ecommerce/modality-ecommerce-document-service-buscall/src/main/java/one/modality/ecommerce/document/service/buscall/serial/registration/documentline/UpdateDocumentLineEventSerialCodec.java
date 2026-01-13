package one.modality.ecommerce.document.service.buscall.serial.registration.documentline;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentLineEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.documentline.UpdateDocumentLineEvent;

/**
 * Serial codec for UpdateDocumentLineEvent.
 *
 * @author Claude Code
 */
public final class UpdateDocumentLineEventSerialCodec extends AbstractDocumentLineEventSerialCodec<UpdateDocumentLineEvent> {

    private static final String CODEC_ID = "UpdateDocumentLineEvent";

    private static final String ITEM_KEY = "item";
    private static final String RESOURCE_CONFIGURATION_KEY = "resourceConfiguration";

    public UpdateDocumentLineEventSerialCodec() {
        super(UpdateDocumentLineEvent.class, CODEC_ID);
    }

    @Override
    public void encode(UpdateDocumentLineEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeObject(serial, ITEM_KEY, o.getItemPrimaryKey(), NullEncoding.NULL_VALUE_ALLOWED);
        encodeObject(serial, RESOURCE_CONFIGURATION_KEY, o.getResourceConfigurationPrimaryKey(), NullEncoding.NULL_VALUE_ALLOWED);
    }

    @Override
    public UpdateDocumentLineEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new UpdateDocumentLineEvent(
            decodeDocumentPrimaryKey(serial),
            decodeDocumentLinePrimaryKey(serial),
            decodeObject(serial, ITEM_KEY),
            decodeObject(serial, RESOURCE_CONFIGURATION_KEY)
        ), serial);
    }
}
