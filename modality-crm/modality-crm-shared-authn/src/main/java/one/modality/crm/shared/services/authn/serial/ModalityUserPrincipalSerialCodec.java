package one.modality.crm.shared.services.authn.serial;

import dev.webfx.platform.ast.json.JsonObject;
import dev.webfx.platform.ast.json.ReadOnlyJsonObject;
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
    public void encodeToJson(ModalityUserPrincipal arg, JsonObject json) {
        json.set(USER_PERSON_ID_KEY, arg.getUserPersonId());
        json.set(USER_ACCOUNT_ID_KEY, arg.getUserAccountId());
    }

    @Override
    public ModalityUserPrincipal decodeFromJson(ReadOnlyJsonObject json) {
        return new ModalityUserPrincipal(
                json.get(USER_PERSON_ID_KEY),
                json.get(USER_ACCOUNT_ID_KEY)
        );
    }
}
