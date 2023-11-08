package one.modality.base.shared.entities.functions;

import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.type.Type;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.entity.EntityId;
import dev.webfx.stack.orm.expression.lci.DomainReader;
import dev.webfx.stack.orm.expression.terms.function.Function;
import one.modality.base.shared.entities.Label;
import one.modality.base.shared.entities.markers.HasLabel;
import one.modality.base.shared.entities.markers.HasName;

/**
 * @author Bruno Salmon
 */
public final class I18nFunction extends Function {

    public I18nFunction() {
        super("i18n", new String[] {"label"}, new Type[] {null}, PrimType.STRING, true);
    }

    @Override
    public Object evaluate(Object argument, DomainReader domainReader) {
        Object result = null;
        if (argument instanceof EntityId)
            argument = domainReader.getDomainObjectFromId(argument, null);
        Label label = null;
        if (argument instanceof Label)
            label = (Label) argument;
        else if (argument instanceof HasLabel)
            label = ((HasLabel) argument).getLabel();
        if (label != null) {
            result = domainReader.getDomainFieldValue(label, I18n.getLanguage());
            if (result == null)
                result = domainReader.getDomainFieldValue(label, I18n.getDefaultLanguage());
        }
        if (result == null && argument instanceof HasName)
            result = ((HasName) argument).getName();
        return result;
    }
}
