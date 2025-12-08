package one.modality.ecommerce.payment.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.payment.SandboxCard;

/**
 * @author Bruno Salmon
 */
public final class SandboxCardSerialCodec extends SerialCodecBase<SandboxCard> {

    private static final String CODEC_ID = "SandboxCard";

    private static final String NAME_KEY = "name";
    private static final String NUMBERS_KEY = "numbers";
    private static final String EXPIRATION_DATE_KEY = "expirationDate";
    private static final String CVV_KEY = "cvv";
    private static final String ZIP_KEY = "zip";

    public SandboxCardSerialCodec() {
        super(SandboxCard.class, CODEC_ID);
    }

    @Override
    public void encode(SandboxCard arg, AstObject serial) {
        encodeString(serial, NAME_KEY,            arg.name());
        encodeString( serial, NUMBERS_KEY,        arg.numbers());
        encodeString(serial, EXPIRATION_DATE_KEY, arg.expirationDate());
        encodeString(serial, CVV_KEY,             arg.cvv());
        encodeString(serial, ZIP_KEY,             arg.zip());
    }

    @Override
    public SandboxCard decode(ReadOnlyAstObject serial) {
        return new SandboxCard(
                decodeString( serial, NAME_KEY),
                decodeString( serial, NUMBERS_KEY),
                decodeString( serial, EXPIRATION_DATE_KEY),
                decodeString( serial, CVV_KEY),
                decodeString( serial, ZIP_KEY)
        );
    }
}
