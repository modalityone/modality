package one.modality.base.client.entities.functions;

import dev.webfx.extras.i18n.I18n;
import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.type.Type;
import dev.webfx.stack.orm.entity.Entity;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.expression.lci.DomainReader;
import dev.webfx.stack.orm.expression.terms.function.Function;
import one.modality.base.shared.entities.markers.EntityHasI18nFields;
import one.modality.base.shared.entities.markers.HasLabel;
import one.modality.base.shared.entities.markers.HasName;

/**
 * i18n() function that can be used in expressions. Special cases are handled depending on the passed argument:
 * - if an entity (or EntityId) is passed, it will try to find the translation in the associated label, or i18nKey, or name otherwise
 * - if a label is passed, it will return the translation in the current i18n language.
 * - in other cases, it will call I18n.getI18nText() with the passed argument
 *
 * @author Bruno Salmon
 */
public final class I18nFunction extends Function {

    public I18nFunction() {
        super("i18n", new String[] {"label"}, new Type[] {null}, PrimType.STRING, true);
    }

    @Override
    public Object evaluate(Object argument, DomainReader domainReader) {
        if (argument == null)
            return null;
        Object language = null;
        if (argument instanceof Object[]) {
            Object[] arguments = (Object[]) argument;
            argument = arguments[0];
            language = arguments[1];
        }
        return evaluate(argument, language, domainReader);
    }

    public static Object evaluate(Object argument, Object language, DomainReader domainReader) {
        if (argument == null)
            return null;
        Object result = null;
        if (argument instanceof EntityId)
            argument = domainReader.getDomainObjectFromId(argument, null);
        Object i18nEntity = null;
        if (argument instanceof EntityHasI18nFields) // Ex: Label or Letter
            i18nEntity = argument;
        else if (argument instanceof HasLabel)
            i18nEntity = ((HasLabel) argument).getLabel();
        if (i18nEntity != null) {
            if (language != null)
                result = domainReader.getDomainFieldValue(i18nEntity, language);
            if (result == null)
                result = domainReader.getDomainFieldValue(i18nEntity, I18n.getLanguage());
            if (result == null)
                result = domainReader.getDomainFieldValue(i18nEntity, I18n.getDefaultLanguage());
        } else if (argument instanceof Entity) {
            Object i18nKey = domainReader.getDomainFieldValue(argument, "i18nKey");
            if (i18nKey != null)
               result = I18n.getI18nText(i18nKey);
        }
        if (result == null && argument instanceof HasName)
            result = ((HasName) argument).getName();
        return result;
    }
}
