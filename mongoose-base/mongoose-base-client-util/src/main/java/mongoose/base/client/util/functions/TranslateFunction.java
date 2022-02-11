package mongoose.base.client.util.functions;

import mongoose.base.client.entities.util.Labels;
import dev.webfx.framework.shared.orm.expression.lci.DomainReader;
import dev.webfx.framework.shared.orm.expression.terms.function.Function;
import dev.webfx.framework.client.services.i18n.I18n;
import dev.webfx.extras.type.PrimType;

/**
 * @author Bruno Salmon
 */
public class TranslateFunction<T> extends Function<T> {

    public TranslateFunction() {
        this("translate");
    }

    public TranslateFunction(String name) {
        super(name, null, null, PrimType.STRING, true);
    }

    @Override
    public Object evaluate(T argument, DomainReader<T> domainReader) {
        if (argument instanceof String)
            return I18n.getI18nText(argument);
        return translate(domainReader.getDomainObjectFromId(argument, null));
    }

    protected String translate(T t) {
        return bestTranslationOrName(t);
    }

    protected String bestTranslationOrName(Object o) {
        return Labels.instantTranslateLabel(Labels.bestLabelOrName(o));
    }
}
