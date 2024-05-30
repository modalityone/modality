package one.modality.crm.shared.services.authn.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.crm.shared.services.authn.ModalityUserPrincipal;

/**
 * @author Bruno Salmon
 */
public final class ModalityUserPrincipalSerialCodec extends SerialCodecBase<ModalityUserPrincipal> {

    private static final String CODEC_ID = "ModalityUserPrincipal";
    private static final String USER_PERSON_ID_KEY = "userPersonId";
    private static final String USER_ACCOUNT_ID_KEY = "userAccountId";

    public ModalityUserPrincipalSerialCodec() {
        super(ModalityUserPrincipal.class, CODEC_ID);
    }

    @Override
    public void encode(ModalityUserPrincipal arg, AstObject serial) {
        encodeObject(serial, USER_PERSON_ID_KEY,  arg.getUserPersonId());
        encodeObject(serial, USER_ACCOUNT_ID_KEY, arg.getUserAccountId());
    }

    @Override
    public ModalityUserPrincipal decode(ReadOnlyAstObject serial) {
        return new ModalityUserPrincipal(
                decodeObject(serial, USER_PERSON_ID_KEY),
                decodeObject(serial, USER_ACCOUNT_ID_KEY)
        );
    }
}
