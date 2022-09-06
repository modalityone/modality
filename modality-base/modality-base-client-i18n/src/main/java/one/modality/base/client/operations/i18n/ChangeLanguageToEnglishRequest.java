package one.modality.base.client.operations.i18n;

import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequest;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitterImpl;

/**
 * @author Bruno Salmon
 */
public final class ChangeLanguageToEnglishRequest extends ChangeLanguageRequest implements HasOperationCode {

    private static final String OPERATION_CODE = "ChangeLanguageToEnglish";

    public ChangeLanguageToEnglishRequest() {
        super("en");
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    public static final class ProvidedEmitter extends ChangeLanguageRequestEmitterImpl {
        public ProvidedEmitter() {
            super(ChangeLanguageToEnglishRequest::new);
        }
    }
}
