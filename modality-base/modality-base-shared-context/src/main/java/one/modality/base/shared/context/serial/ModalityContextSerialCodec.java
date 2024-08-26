package one.modality.base.shared.context.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.base.shared.context.ModalityContext;

/**
 * @author Bruno Salmon
 */
public class ModalityContextSerialCodec extends SerialCodecBase<ModalityContext> {

    private static final String CODEC_ID = "ModalityContext";
    private static final String ORGANIZATION_ID_KEY = "organizationId";
    private static final String EVENT_ID_KEY = "eventId";
    private static final String DOCUMENT_ID_KEY = "documentId";
    private static final String MAGIC_LINK_ID_KEY = "magicLinkId";

    public ModalityContextSerialCodec() {
        super(ModalityContext.class, CODEC_ID);
    }

    @Override
    public void encode(ModalityContext arg, AstObject serial) {
        encodeObject(serial, ORGANIZATION_ID_KEY, arg.getOrganizationId());
        encodeObject(serial, EVENT_ID_KEY, arg.getEventId());
        encodeObject(serial, DOCUMENT_ID_KEY, arg.getDocumentId());
        encodeObject(serial, MAGIC_LINK_ID_KEY, arg.getMagicLinkId());
    }

    @Override
    public ModalityContext decode(ReadOnlyAstObject serial) {
        return new ModalityContext(
            decodeObject(serial, ORGANIZATION_ID_KEY),
            decodeObject(serial, EVENT_ID_KEY),
            decodeObject(serial, DOCUMENT_ID_KEY),
            decodeObject(serial, MAGIC_LINK_ID_KEY)
        );
    }
}
