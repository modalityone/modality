package one.modality.base.client.entities.functions;

import dev.webfx.extras.type.PrimType;
import dev.webfx.extras.type.Type;
import dev.webfx.platform.util.time.Times;
import dev.webfx.stack.orm.expression.lci.DomainReader;
import dev.webfx.stack.orm.expression.terms.function.Function;
import one.modality.base.client.time.ModalityDates;

import java.time.LocalDate;

/**
 * @author Bruno Salmon
 */
public final class DateIntervalFormat extends Function {

    public DateIntervalFormat() {
        super("dateIntervalFormat", new String[] {"date1", "date2"}, new Type[] {null, null}, PrimType.STRING, true);
    }

    @Override
    public Object evaluate(Object argument, DomainReader domainReader) {
        try {
            Object[] arguments = (Object[]) argument;
            LocalDate date1 = Times.toLocalDate(arguments[0]);
            LocalDate date2 = Times.toLocalDate(arguments[1]);
            return ModalityDates.formatDateInterval(date1, date2);
        } catch (Exception e) {
            return argument;
        }
    }
}
