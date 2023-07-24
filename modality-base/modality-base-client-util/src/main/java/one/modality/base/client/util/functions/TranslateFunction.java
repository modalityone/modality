package one.modality.base.client.util.functions;

import dev.webfx.extras.type.PrimType;
import dev.webfx.stack.i18n.I18n;
import dev.webfx.stack.orm.expression.lci.DomainReader;
import dev.webfx.stack.orm.expression.terms.function.Function;

import one.modality.base.client.entities.util.Labels;

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
        if (argument instanceof String) return I18n.getI18nText(argument);
        return translate(domainReader.getDomainObjectFromId(argument, null));
    }

    protected String translate(T t) {
        return bestTranslationOrName(t);
    }

    protected String bestTranslationOrName(Object o) {
        return Labels.instantTranslateLabel(Labels.bestLabelOrName(o));
    }
}
