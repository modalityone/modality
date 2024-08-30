package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractDocumentLineEventSerialCodec<T extends AbstractDocumentLineEvent> extends AbstractDocumentEventSerialCodec<T> {

    private static final String DOCUMENT_LINE_PRIMARY_KEY = "documentLine";

    public AbstractDocumentLineEventSerialCodec(Class<T> javaClass, String codecId) {
        super(javaClass, codecId);
    }

    @Override
    public void encode(T o, AstObject serial) {
        super.encode(o, serial, true); // null primary key allowed for document, but not documentLine
        encodeObject(serial, DOCUMENT_LINE_PRIMARY_KEY, o.getDocumentLinePrimaryKey(), NullEncoding.NULL_VALUE_NOT_ALLOWED);
    }

    protected Object decodeDocumentLinePrimaryKey(ReadOnlyAstObject serial) {
        return decodeObject(serial, DOCUMENT_LINE_PRIMARY_KEY);
    }
}
