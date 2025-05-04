package one.modality.base.client.operations.i18n;

import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequest;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitterImpl;
import dev.webfx.stack.ui.operation.HasOperationCode;
import one.modality.base.client.i18n.BaseI18nKeys;

/**
 * @author Bruno Salmon
 */
public final class ChangeLanguageToEnglishRequest extends ChangeLanguageRequest implements HasOperationCode, HasI18nKey {

    private static final String OPERATION_CODE = "ChangeLanguageToEnglish";

    public ChangeLanguageToEnglishRequest() {
        super("en");
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return BaseI18nKeys.English;
    }

    public static final class ProvidedEmitter extends ChangeLanguageRequestEmitterImpl {
        public ProvidedEmitter() {
            super(ChangeLanguageToEnglishRequest::new);
        }
    }
}
