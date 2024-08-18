package one.modality.crm.shared.services.authn.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.crm.shared.services.authn.ModalityGuestPrincipal;

/**
 * @author Bruno Salmon
 */
public final class ModalityGuestPrincipalSerialCodec extends SerialCodecBase<ModalityGuestPrincipal> {

    private static final String CODEC_ID = "ModalityGuestPrincipal";
    private static final String EMAIL_KEY = "email";

    public ModalityGuestPrincipalSerialCodec() {
        super(ModalityGuestPrincipal.class, CODEC_ID);
    }

    @Override
    public void encode(ModalityGuestPrincipal arg, AstObject serial) {
        encodeString(serial, EMAIL_KEY,  arg.getEmail());
    }

    @Override
    public ModalityGuestPrincipal decode(ReadOnlyAstObject serial) {
        return new ModalityGuestPrincipal(
                decodeString(serial, EMAIL_KEY)
        );
    }
}
