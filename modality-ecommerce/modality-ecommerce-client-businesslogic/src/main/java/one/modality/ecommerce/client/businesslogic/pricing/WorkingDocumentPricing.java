package one.modality.ecommerce.client.businesslogic.pricing;

import one.modality.ecommerce.client.businessdata.workingdocument.WorkingDocument;
import one.modality.ecommerce.client.businessdata.workingdocument.WorkingDocumentLine;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Site;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.collection.HashList;

/**
 * @author Bruno Salmon
 */
public final class WorkingDocumentPricing {

    public static int computeDocumentPrice(WorkingDocument workingDocument) {
        HashList<SiteRateItemBlock> siteRateItemBlocks = new HashList<>();
        for (WorkingDocumentLine wdl : workingDocument.getWorkingDocumentLines()) {
            if (!wdl.isCancelled() && wdl.isConcrete()) {
                Site site = wdl.getSite();
                Item item = wdl.getItem();
                item = Objects.coalesce(item.getRateAliasItem(), item);
                SiteRateItemBlock block = new SiteRateItemBlock(workingDocument, site, item);
                SiteRateItemBlock existingBlock = siteRateItemBlocks.getExistingElement(block);
                if (existingBlock != null)
                    block = existingBlock;
                else
                    siteRateItemBlocks.add(block);
                block.addWorkingDocumentLineAttendances(wdl);
            }
        }
        int price = 0;
        for (SiteRateItemBlock block : siteRateItemBlocks) {
            int blockPrice = block.computePrice();
            //Logger.log(block + " price = " + blockPrice);
            price += blockPrice;
        }
/*
        if (bill.document.cancelledDocumentLines) // adding price of cancelled document lines if any
            for (i = 0; i < bill.document.cancelledDocumentLines.length; i++)
                price += bill.document.cancelledDocumentLines[i].price_net;
*/
        return price;
    }
}
