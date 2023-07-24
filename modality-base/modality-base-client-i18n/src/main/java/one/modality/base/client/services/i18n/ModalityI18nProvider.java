package one.modality.base.client.services.i18n;

import dev.webfx.stack.i18n.Dictionary;
import dev.webfx.stack.i18n.TokenKey;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.i18n.spi.impl.json.JsonI18nProvider;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.HasEntity;

/**
 * @author Bruno Salmon
 */
public final class ModalityI18nProvider extends JsonI18nProvider {

  public ModalityI18nProvider() {
    super("one/modality/base/client/services/i18n/dictionaries/{lang}.json");
  }

  @Override
  protected <TK extends Enum<?> & TokenKey> Object getDictionaryTokenValueImpl(
      Object i18nKey,
      TK tokenKey,
      Dictionary dictionary,
      boolean skipDefaultDictionary,
      boolean skipMessageKeyInterpretation,
      boolean skipMessageLoading) {
    Object messageKey = i18nKeyToDictionaryMessageKey(i18nKey);
    if (messageKey instanceof String) {
      String s = (String) messageKey;
      if (s.startsWith("expression:")) {
        Entity entity = findEntity(i18nKey);
        if (entity != null) {
          Object tokenValue = entity.evaluate(s.substring(11));
          if (tokenValue instanceof String) i18nKey = new I18nSubKey(tokenValue, i18nKey);
          else return tokenValue;
        }
      }
    }
    return super.getDictionaryTokenValueImpl(
        i18nKey,
        tokenKey,
        dictionary,
        skipDefaultDictionary,
        skipMessageKeyInterpretation,
        skipMessageLoading);
  }

  private Entity findEntity(Object i18nKey) {
    if (i18nKey instanceof HasEntity) return ((HasEntity) i18nKey).getEntity();
    if (i18nKey instanceof I18nSubKey) return findEntity(((I18nSubKey) i18nKey).getParentI18nKey());
    return null;
  }
}
