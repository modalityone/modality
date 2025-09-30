package one.modality.ecommerce.document.service.events.registration.documentline;

import dev.webfx.platform.util.Objects;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.base.shared.entities.util.DocumentLines;
import one.modality.ecommerce.document.service.events.AbstractDocumentEvent;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

import java.util.List;

/**
 * @author Bruno Salmon
 */
public final class PriceDocumentLineEvent extends AbstractDocumentLineEvent {

    private final Integer price_net;
    private final Integer price_minDeposit;
    private final Integer price_custom;
    private final Integer price_discount;

    public PriceDocumentLineEvent(Object documentPrimaryKey, Object documentLinePrimaryKey, Integer price_net, Integer price_minDeposit, Integer price_custom, Integer price_discount) {
        super(documentPrimaryKey, documentLinePrimaryKey);
        this.price_net = price_net;
        this.price_minDeposit = price_minDeposit;
        this.price_custom = price_custom;
        this.price_discount = price_discount;
    }

    public PriceDocumentLineEvent(DocumentLine documentLine) {
        this(documentLine, documentLine.getPriceNet(), documentLine.getPriceMinDeposit(), documentLine.getPriceCustom(), documentLine.getPriceDiscount());
    }

    public PriceDocumentLineEvent(DocumentLine documentLine, Integer price_net, Integer price_minDeposit, Integer price_custom, Integer price_discount) {
        super(documentLine);
        this.price_net = price_net;
        this.price_minDeposit = price_minDeposit;
        this.price_custom = price_custom;
        this.price_discount = price_discount;
    }

    public Integer getPrice_net() {
        return price_net;
    }

    public Integer getPrice_minDeposit() {
        return price_minDeposit;
    }

    public Integer getPrice_custom() {
        return price_custom;
    }

    public Integer getPrice_discount() {
        return price_discount;
    }

    @Override
    public void replayEventOnDocumentLine() {
        super.replayEventOnDocumentLine();
        if (price_net != null)
            documentLine.setPriceNet(price_net);
        if (price_minDeposit != null)
            documentLine.setPriceMinDeposit(price_minDeposit);
        if (price_custom != null)
            documentLine.setPriceCustom(price_custom);
        if (price_discount != null)
            documentLine.setPriceDiscount(price_discount);
    }

    // static methods

    public static PriceDocumentLineEvent createDocumentLineDiscountEvent(DocumentLine documentLine, Integer price_discount) {
        return new PriceDocumentLineEvent(documentLine, null, null, null, price_discount);
    }

    public static PriceDocumentLineEvent mergeDocumentLinePriceEvents(PriceDocumentLineEvent oldEvent, PriceDocumentLineEvent newEvent) {
        if (oldEvent == null)
            return newEvent;

        if (newEvent == null)
            return oldEvent;

        if (!DocumentLines.sameDocumentLine(oldEvent.getDocumentLine(), newEvent.getDocumentLine()))
            throw new IllegalArgumentException("Cannot merge events for different document lines");

        // Computing the final field values of the merged event
        Integer price_net        = Objects.coalesce(newEvent.price_net,        oldEvent.price_net);
        Integer price_minDeposit = Objects.coalesce(newEvent.price_minDeposit, oldEvent.price_minDeposit);
        Integer price_custom     = Objects.coalesce(newEvent.price_custom,     oldEvent.price_custom);
        Integer price_discount   = Objects.coalesce(newEvent.price_discount,   oldEvent.price_discount);

        if (sameFieldValues(newEvent, price_net, price_minDeposit, price_custom, price_discount))
            return newEvent;

        if (sameFieldValues(oldEvent, price_net, price_minDeposit, price_custom, price_discount))
            return oldEvent;

        return new PriceDocumentLineEvent(newEvent.getDocumentLine(), price_net, price_minDeposit, price_custom, price_discount);
    }

    public static boolean sameFieldValues(PriceDocumentLineEvent event, Integer price_net, Integer price_minDeposit, Integer price_custom, Integer price_discount) {
        return java.util.Objects.equals(price_net, event.price_net)
               && java.util.Objects.equals(price_discount, event.price_discount)
               && java.util.Objects.equals(price_minDeposit, event.price_minDeposit)
               && java.util.Objects.equals(price_custom, event.price_custom);
    }

    public static PriceDocumentLineEvent mergeDocumentLinePriceEvents(List<AbstractDocumentEvent> events, DocumentLine documentLine, boolean removeEvents) {
        PriceDocumentLineEvent mergedEvent = null;
        for (int i = events.size() - 1; i >=0; i--) {
            if (events.get(i) instanceof PriceDocumentLineEvent event && DocumentLines.sameDocumentLine(event.getDocumentLine(), documentLine)) {
                mergedEvent = mergeDocumentLinePriceEvents(mergedEvent, event);
                if (removeEvents) {
                    events.remove(i);
                    if (events.isEmpty())
                        break;
                    i++;
                }
            }
        }
        return mergedEvent;
    }
}
