package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.events.AbstractSetDocumentFieldsEvent;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractSetDocumentFieldsEventSerialCodec<T extends AbstractSetDocumentFieldsEvent> extends AbstractDocumentEventSerialCodec<T> {

    private static final String VALUES_PRIMARY_KEY = "values";

    public AbstractSetDocumentFieldsEventSerialCodec(Class<T> javaClass, String codecId) {
        super(javaClass, codecId);
    }

    @Override
    public void encode(T o, AstObject serial) {
        super.encode(o, serial);
        encodeArray(serial, VALUES_PRIMARY_KEY, o.getFieldValues());
    }

    protected Object[] decodeValues(ReadOnlyAstObject serial) {
        return decodeArray(serial, VALUES_PRIMARY_KEY);
    }

}
