package one.modality.base.client.services.i18n;

import dev.webfx.stack.i18n.Dictionary;
import dev.webfx.stack.i18n.TokenKey;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.i18n.spi.impl.ast.AstI18nProvider;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.HasEntity;
import javafx.beans.value.ObservableValue;

/**
 * @author Bruno Salmon
 */
public final class ModalityI18nProvider extends AstI18nProvider {

    @Override
    protected <TK extends Enum<?> & TokenKey> Object getDictionaryTokenValueImpl(Object i18nKey, TK tokenKey, Dictionary dictionary, boolean skipDefaultDictionary, Dictionary originalDictionary, boolean skipMessageKeyInterpretation, boolean skipMessageLoading) {
        Object messageKey = i18nKeyToDictionaryMessageKey(i18nKey);
        if (messageKey instanceof String) {
            String s = (String) messageKey;
            if (s.startsWith("expression:")) {
                Entity entity = findEntity(i18nKey);
                // If no entities is found (can happen before data is loaded), we display nothing (displaying the
                // expression would look ugly for the final user). This can eventually be commented when not in production.
                if (entity == null)
                    return null;
                String expression = s.substring(11);
                Object tokenValue = entity.evaluate(expression);
                if (tokenValue instanceof String)
                    i18nKey = new I18nSubKey(tokenValue, i18nKey);
                else
                    return tokenValue;
            }
        }
        return super.getDictionaryTokenValueImpl(i18nKey, tokenKey, dictionary, skipDefaultDictionary, originalDictionary, skipMessageKeyInterpretation, skipMessageLoading);
    }

    private Entity findEntity(Object i18nKey) {
        if (i18nKey instanceof ObservableValue)
            i18nKey = ((ObservableValue<?>) i18nKey).getValue();
        if (i18nKey instanceof Entity)
            return (Entity) i18nKey;
        if (i18nKey instanceof HasEntity)
            return ((HasEntity) i18nKey).getEntity();
        if (i18nKey instanceof I18nSubKey)
            return findEntity(((I18nSubKey) i18nKey).getParentI18nKey());
        return null;
    }
}
