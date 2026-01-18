package one.modality.ecommerce.shared.pricecalculator;

import dev.webfx.platform.util.collection.Collections;
import one.modality.base.shared.entities.DocumentLine;
import one.modality.ecommerce.document.service.DocumentAggregate;

import java.util.Collection;

/**
 * @author Bruno Salmon
 */
public final class DocumentBill {

    private final DocumentAggregate documentAggregate;
    private final Collection<DocumentLine> alreadyPricedLines;
    private final Collection<SiteItemBill> siteItemBills;
    final boolean ignoreLongStayDiscount;
    private final boolean update;

    private int totalPrice = -1;
    private int minDeposit = -1;

    DocumentBill(DocumentAggregate documentAggregate, Collection<DocumentLine> alreadyPricedLines, Collection<SiteItemBill> siteItemBills, boolean ignoreLongStayDiscount, boolean update) {
        this.documentAggregate = documentAggregate;
        this.alreadyPricedLines = alreadyPricedLines;
        this.siteItemBills = siteItemBills;
        this.ignoreLongStayDiscount = ignoreLongStayDiscount;
        this.update = update;
        siteItemBills.forEach(SiteItemBill::sortAttendanceBillsByDate);
    }

    DocumentAggregate getDocumentAggregate() {
        return documentAggregate;
    }

    public int getTotalPrice() {
        if (totalPrice == -1)
            totalPrice = computePrice(false);
        return totalPrice;
    }

    public int getMinDeposit() {
        if (minDeposit == -1)
            minDeposit = computePrice(true);
        return minDeposit;
    }

    public Collection<SiteItemBill> getSiteItemBills() {
        return siteItemBills;
    }

    public boolean isChildRateApplied() {
        // Note: this doesn't consider already priced lines
        return Collections.anyMatch(siteItemBills, SiteItemBill::isChildRateApplied);
    }

    private int computePrice(boolean minDeposit) {
        int price = 0;
        for (DocumentLine pricedLine : alreadyPricedLines) {
            price += minDeposit ? pricedLine.getPriceMinDeposit() : pricedLine.getPriceNet();
        }
        for (SiteItemBill siteItemBill : siteItemBills)
            price += siteItemBill.computePrice(this, minDeposit);
/* from KBS2
      // adding price of cancelled document lines if any
        if (bill.document.cancelledDocumentLines)
            for (i = 0; i < bill.document.cancelledDocumentLines.length; i++)
                price += bill.document.cancelledDocumentLines[i].price_net;
*/
        if (minDeposit) {
            // Rounding the min deposit of the booking to the superior amount (without cents)
            int rounded = ((price + 99) / 100) * 100;
            if (rounded > price) { // If the rounded amount is indeed superior,
                // we still need to check that it doesn't exceed the total amount
                price = Math.min(rounded, getTotalPrice());
            }
        } else if (update) {
            documentAggregate.getDocument().setPriceNet(price);
        }
        return price;
    }
}
