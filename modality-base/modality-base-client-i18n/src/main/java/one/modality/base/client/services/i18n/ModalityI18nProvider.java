package one.modality.base.client.services.i18n;


import dev.webfx.platform.json.Json;
import dev.webfx.platform.json.WritableJsonArray;
import dev.webfx.platform.json.WritableJsonObject;
import dev.webfx.stack.i18n.Dictionary;
import dev.webfx.stack.i18n.TokenKey;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.i18n.spi.impl.json.JsonI18nProvider;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.HasEntity;
import dev.webfx.stack.ui.fxraiser.FXValueRaiser;
import dev.webfx.stack.ui.fxraiser.impl.ValueConverterRegistry;

import static dev.webfx.platform.util.Objects.isAssignableFrom;

/**
 * @author Bruno Salmon
 */
public final class ModalityI18nProvider extends JsonI18nProvider {

    public ModalityI18nProvider() {
        super("one/modality/base/client/services/i18n/dictionaries/{lang}.json");
        ValueConverterRegistry.registerValueConverter(new FXValueRaiser() {
            @Override
            public <T> T raiseValue(Object value, Class<T> raisedClass, Object... args) {
                if (value instanceof String) {
                    String s = (String) value;
                    if (s.startsWith("{") && s.endsWith("}") && isAssignableFrom(raisedClass, WritableJsonObject.class))
                        return (T) Json.parseObjectSilently(s);
                    if (s.startsWith("[") && s.endsWith("]") && isAssignableFrom(raisedClass, WritableJsonArray.class))
                        return (T) Json.parseArraySilently(s);
                }
                return null;
            }
        });
    }

    @Override
    protected <TK extends Enum<?> & TokenKey> Object getDictionaryTokenValueImpl(Object i18nKey, TK tokenKey, Dictionary dictionary, boolean skipDefaultDictionary, boolean skipMessageKeyInterpretation, boolean skipMessageLoading) {
        Object messageKey = i18nKeyToDictionaryMessageKey(i18nKey);
        if (messageKey instanceof String) {
            String s = (String) messageKey;
            if (s.startsWith("expression:")) {
                Entity entity = findEntity(i18nKey);
                if (entity != null) {
                    Object tokenValue = entity.evaluate(s.substring(11));
                    if (tokenValue instanceof String)
                        i18nKey = new I18nSubKey(tokenValue, i18nKey);
                    else
                        return tokenValue;
                }
            }
        }
        return super.getDictionaryTokenValueImpl(i18nKey, tokenKey, dictionary, skipDefaultDictionary, skipMessageKeyInterpretation, skipMessageLoading);
    }

    private Entity findEntity(Object i18nKey) {
        if (i18nKey instanceof HasEntity)
            return ((HasEntity) i18nKey).getEntity();
        if (i18nKey instanceof I18nSubKey)
            return findEntity(((I18nSubKey) i18nKey).getParentI18nKey());
        return null;
    }
}
