package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.document.service.events.AbstractSourceEvent;

/**
 * @author Bruno Salmon
 */
public abstract class AbstractSourceEventSerialCodec<T extends AbstractSourceEvent> extends SerialCodecBase<T> {

    protected static final String DATE_TIME_KEY = "dateTime";
    protected static final String COMMENT_KEY = "comment";

    public AbstractSourceEventSerialCodec(Class<T> javaClass, String codecId) {
        super(javaClass, codecId);
    }

    @Override
    public void encode(T o, AstObject serial) {
        encodeLocalDateTime(serial, DATE_TIME_KEY, o.getDateTime());
        encodeString(       serial, COMMENT_KEY,   o.getComment());
    }

    protected T postDecode(T o, ReadOnlyAstObject serial) {
        o.setDateTime(decodeLocalDateTime(serial, DATE_TIME_KEY));
        o.setComment(decodeString(serial, COMMENT_KEY));
        return o;
    }
}
