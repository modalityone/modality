package one.modality.base.client.operations.i18n;

import dev.webfx.stack.i18n.HasI18nKey;
import dev.webfx.stack.ui.operation.HasOperationCode;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequest;
import dev.webfx.stack.i18n.operations.ChangeLanguageRequestEmitterImpl;
import one.modality.base.client.i18n.ModalityI18nKeys;

/**
 * @author Bruno Salmon
 */
public final class ChangeLanguageToFrenchRequest extends ChangeLanguageRequest implements HasOperationCode, HasI18nKey {

    private static final String OPERATION_CODE = "ChangeLanguageToFrench";

    public ChangeLanguageToFrenchRequest() {
        super("fr");
    }

    @Override
    public Object getOperationCode() {
        return OPERATION_CODE;
    }

    @Override
    public Object getI18nKey() {
        return ModalityI18nKeys.Français;
    }

    public static final class ProvidedEmitter extends ChangeLanguageRequestEmitterImpl {
        public ProvidedEmitter() {
            super(ChangeLanguageToFrenchRequest::new);
        }
    }
}
