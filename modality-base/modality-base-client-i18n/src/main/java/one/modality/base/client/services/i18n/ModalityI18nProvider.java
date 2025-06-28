package one.modality.base.client.services.i18n;

import dev.webfx.platform.ast.AST;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.platform.conf.ConfigLoader;
import dev.webfx.platform.console.Console;
import dev.webfx.stack.i18n.Dictionary;
import dev.webfx.stack.i18n.TokenKey;
import dev.webfx.stack.i18n.spi.impl.I18nSubKey;
import dev.webfx.stack.i18n.spi.impl.ast.AstI18nProvider;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.HasEntity;
import javafx.beans.value.ObservableValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Modality i18n implementation which is an extension of AstI18nProvider with 2 additional features:
 *
 * 1) possibility to evaluate expressions on entities
 *    Ex: new I18nSubKey("expression: 'Event name: ' + name", myEvent)
 *
 * 2) possibility to forward default i18n keys defined in Modality to other i18n keys defined in final application
 *    through configuration. Ex:
 *    ModalityI18nKeyForwards:
 *      RecurringEvents:
 *        entity: FXOrganization
 *        expression: "id=1 ? '[STTP]' : '[GPClasses]'"
 *
 * @author Bruno Salmon
 */
public final class ModalityI18nProvider extends AstI18nProvider {

    // Loading possible i18n keys forwards from configuration (empty in Modality but might be overridden in final app).
    private static final ReadOnlyAstObject I18N_KEY_FORWARDS = AST.lookupObject(ConfigLoader.getRootConfig(),
        "modality.base.client.i18n.ModalityI18nKeyForwards");

    //
    private static final Map<String/* ex: "FXOrganization" */, Supplier<Entity> /* ex: FXOrganization.getOrganization() */>
        FORWARD_ENTITY_HOLDERS = new HashMap<>();

    // This static method should be called by any entity holder that can be referenced for i18n key forwards
    // See FXOrganization as an example.

    public static void registerEntityHolder(String name, Supplier<Entity> entityGetter) {
        FORWARD_ENTITY_HOLDERS.put(name, entityGetter);
    }

    @Override
    protected <TK extends Enum<?> & TokenKey> Object getDictionaryTokenValueImpl(Object i18nKey, TK tokenKey, Dictionary dictionary, boolean skipDefaultDictionary, Dictionary originalDictionary, boolean skipMessageKeyInterpretation, boolean skipMessageLoading) {
        Object messageKey = i18nKeyToDictionaryMessageKey(i18nKey);
        if (messageKey instanceof String) {
            String stringMessageKey = (String) messageKey;
            Entity entity = null;
            String expression = null;

            if (stringMessageKey.startsWith("expression:")) {
                expression = stringMessageKey.substring(11);
            } else if (I18N_KEY_FORWARDS != null) {
                // Forward management (ex: RecurringEvents forwarded to STTP or GPClasses in dependence of the selected organization)

                // This test is to avoid an infinite loop in case we are already in a forward, which refers again to the
                // original i18nKey! Ex: although RecurringEvents is forwarded to STTP or GPClasses, we want these new keys
                // to keep the original graphic (STTP.graphic = [RecurringEvents]).
                if (!(i18nKey instanceof I18nSubKey) || !Objects.equals(stringMessageKey, ((I18nSubKey) i18nKey).getDictionaryMessageKey())) {
                    // Checking if there is a forward for the requested message key
                    ReadOnlyAstObject forward = I18N_KEY_FORWARDS.getObject(stringMessageKey);
                    if (forward != null) { // Yes, there is!
                        // The forward uses an expression to evaluate on an entity that will give the final i18n key to use.
                        expression = forward.getString("expression");
                        // We try to get the entity
                        String entityHolderName = forward.getString("entity"); // actually refers to the entity holder (ex: FXOrganization)
                        Supplier<Entity> entityGetter = FORWARD_ENTITY_HOLDERS.get(entityHolderName);
                        if (entityGetter == null) {
                            Console.log("[ModalityI18nProvider] ⚠️ Unknown '" + entityHolderName + "' entity holder for '" + stringMessageKey + "' forward");
                        } else {
                            entity = entityGetter.get();
                        }
                    }
                }
            }
            if (expression != null) {
                if (entity == null) // could be already set in case of a forward
                    entity = findEntity(i18nKey);
                // If no entities are found (can happen before data is loaded), we display nothing (displaying the
                // expression would look ugly for the final user). This can eventually be commented when not in production.
                if (entity == null)
                    return null;

                Object tokenValue = entity.evaluate(expression);
                if (!(tokenValue instanceof String))
                    return tokenValue;

                i18nKey = new I18nSubKey(tokenValue, i18nKey);
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
