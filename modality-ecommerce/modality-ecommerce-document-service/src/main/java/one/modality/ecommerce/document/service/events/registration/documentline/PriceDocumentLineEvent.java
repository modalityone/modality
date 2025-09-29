package one.modality.ecommerce.document.service.events.registration.documentline;

import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.events.AbstractDocumentLineEvent;

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

    public static PriceDocumentLineEvent createDocumentLineDiscountEvent(DocumentLine documentLine, Integer price_discount) {
        return new PriceDocumentLineEvent(documentLine, null, null, null, price_discount);
    }
}
