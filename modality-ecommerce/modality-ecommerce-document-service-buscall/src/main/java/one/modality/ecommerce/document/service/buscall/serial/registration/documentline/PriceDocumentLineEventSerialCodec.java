package one.modality.ecommerce.document.service.buscall.serial.registration.documentline;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import one.modality.ecommerce.document.service.buscall.serial.AbstractDocumentLineEventSerialCodec;
import one.modality.ecommerce.document.service.events.registration.documentline.PriceDocumentLineEvent;

/**
 * @author Bruno Salmon
 */
public final class PriceDocumentLineEventSerialCodec extends AbstractDocumentLineEventSerialCodec<PriceDocumentLineEvent> {

    private static final String CODEC_ID = "PriceDocumentLineEvent";

    private static final String PRICE_NET_KEY = "net";
    private static final String PRICE_MIN_DEPOSIT_KEY = "minDeposit";
    private static final String PRICE_CUSTOM_KEY = "custom";
    private static final String PRICE_DISCOUNT_KEY = "discount";

    public PriceDocumentLineEventSerialCodec() {
        super(PriceDocumentLineEvent.class, CODEC_ID);
    }

    @Override
    public void encode(PriceDocumentLineEvent o, AstObject serial) {
        super.encode(o, serial);
        encodeInteger(serial, PRICE_NET_KEY,         o.getPrice_net());
        encodeInteger(serial, PRICE_MIN_DEPOSIT_KEY, o.getPrice_minDeposit());
        encodeInteger(serial, PRICE_CUSTOM_KEY,      o.getPrice_custom());
        encodeInteger(serial, PRICE_DISCOUNT_KEY,    o.getPrice_discount());
    }

    @Override
    public PriceDocumentLineEvent decode(ReadOnlyAstObject serial) {
        return postDecode(new PriceDocumentLineEvent(
            decodeDocumentPrimaryKey(serial),
            decodeDocumentLinePrimaryKey(serial),
            decodeInteger(serial, PRICE_NET_KEY),
            decodeInteger(serial, PRICE_MIN_DEPOSIT_KEY),
            decodeInteger(serial, PRICE_CUSTOM_KEY),
            decodeInteger(serial, PRICE_DISCOUNT_KEY)
        ), serial);
    }
}
