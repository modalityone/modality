package one.modality.ecommerce.shared.pricecalculator;

import one.modality.base.shared.entities.*;
import one.modality.ecommerce.document.service.DocumentAggregate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Bruno Salmon
 */
final class Kbs2PriceAlgorithm {

    public static DocumentBill computeDocumentBill(DocumentAggregate documentAggregate, boolean ignoreLongStayDiscount, boolean update) {
        return computeDocumentBill(documentAggregate, documentAggregate.getDocumentLinesStream(), ignoreLongStayDiscount, update);
    }

    public static DocumentBill computeDocumentBill(DocumentAggregate documentAggregate, Stream<DocumentLine> documentLineStream, boolean ignoreLongStayDiscount, boolean update) {
        List<DocumentLine> alreadyPricedLines = new ArrayList<>();
        Map<SiteItem, SiteItemBill> siteItemBills = new HashMap<>();
        documentLineStream.forEach(line -> {
            if (line.getPriceNet() != null) {
                alreadyPricedLines.add(line);
                return;
            }
            Site site = line.getSite();
            Item item = line.getItem();
            if (item.getRateAliasItem() != null)
                item = item.getRateAliasItem();
            SiteItem siteItem = new SiteItem(site, item);
            SiteItemBill siteItemBill = siteItemBills.computeIfAbsent(siteItem, SiteItemBill::new);
            List<Attendance> lineAttendances = documentAggregate.getLineAttendances(line);
            lineAttendances.forEach(attendance -> {
                //LocalDate date = attendance.getDate(); // is null
                LocalDate date = attendance.getScheduledItem().getDate(); // running this on a KBS2 event will produce a NPE (should we do something about it?)
                AttendanceBill attendanceBill = new AttendanceBill(line, date);
                siteItemBill.addAttendanceBill(attendanceBill);
            });
        });
        return new DocumentBill(documentAggregate, alreadyPricedLines, siteItemBills.values(), ignoreLongStayDiscount, update);
    }

}
