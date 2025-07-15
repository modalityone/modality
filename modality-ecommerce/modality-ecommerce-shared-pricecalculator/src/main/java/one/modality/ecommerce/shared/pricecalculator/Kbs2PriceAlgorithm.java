package one.modality.ecommerce.shared.pricecalculator;

import dev.webfx.platform.util.Booleans;
import dev.webfx.platform.util.Objects;
import dev.webfx.platform.util.collection.Collections;
import one.modality.base.shared.entities.*;
import one.modality.ecommerce.document.service.DocumentAggregate;
import one.modality.ecommerce.document.service.PolicyAggregate;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bruno Salmon
 */
final class Kbs2PriceAlgorithm {

    private record PriceMemo(Rate rate, int dailyPrice, int price, int consumableDays) {}

    public static int computeBookingPrice(DocumentAggregate documentAggregate, boolean ignoreLongStayDiscount, boolean update) {
        return computeBookingBill(documentAggregate, ignoreLongStayDiscount, update).getInvoiced();
    }

    public static int computeBookingMinDeposit(DocumentAggregate documentAggregate, boolean ignoreLongStayDiscount, boolean update) {
        return computeBookingBill(documentAggregate, ignoreLongStayDiscount, update).getMinDeposit();
    }

    private static Bill computeBookingBill(DocumentAggregate documentAggregate, boolean ignoreLongStayDiscount, boolean update) {
        return computeBookingBill(documentAggregate, documentAggregate.getDocumentLinesStream(), ignoreLongStayDiscount, update);
    }

    public static Bill computeBookingBill(DocumentAggregate documentAggregate, Stream<DocumentLine> documentLineStream, boolean ignoreLongStayDiscount, boolean update) {
        Map<SiteItem, Block> hash = new HashMap<>();
        documentLineStream.forEach(line -> {
            Site site = line.getSite();
            Item item = line.getItem();
            if (item.getRateAliasItem() != null)
                item = item.getRateAliasItem();
            SiteItem siteItem = new SiteItem(site, item);
            Block block = hash.computeIfAbsent(siteItem, Block::new);
            List<Attendance> lineAttendances = documentAggregate.getLineAttendances(line);
            lineAttendances.forEach(attendance -> {
                //LocalDate date = attendance.getDate(); // is null
                LocalDate date = attendance.getScheduledItem().getDate(); // running this on a KBS2 event will produce a NPE (should we do something about it?)
                BlockAttendance blockAttendance = new BlockAttendance(line, date);
                block.addBlockAttendance(blockAttendance);
            });
        });
        Collection<Block> blocks = hash.values();
        return new Bill(documentAggregate, blocks, ignoreLongStayDiscount, update);
    }

    public static final class Bill {

        //private final WorkingBooking workingBooking;
        private final DocumentAggregate documentAggregate;
        private final Collection<Block> blocks;
        private final boolean ignoreLongStayDiscount;
        private final boolean update;

        private int price = -1;
        private int minDeposit = -1;

        Bill(DocumentAggregate workingBooking, Collection<Block> blocks, boolean ignoreLongStayDiscount, boolean update) {
            this.documentAggregate = workingBooking;
            this.blocks = blocks;
            this.ignoreLongStayDiscount = ignoreLongStayDiscount;
            this.update = update;
            blocks.forEach(Block::sortBlockAttendances);
        }

        DocumentAggregate getDocumentAggregate() {
            return documentAggregate;
        }

        public int getInvoiced() {
            if (price == -1)
                price = computeBillPrice(false);
            return price;
        }

        public int getMinDeposit() {
            if (minDeposit == -1)
                minDeposit = computeBillPrice(true);
            return minDeposit;
        }

        private int computeBillPrice(boolean minDeposit) {
            int price = 0;
            for (Block block : blocks)
                price += block.computeBlockPrice(this, minDeposit);
    /* from KBS2
          // adding price of cancelled document lines if any
            if (bill.document.cancelledDocumentLines)
                for (i = 0; i < bill.document.cancelledDocumentLines.length; i++)
                    price += bill.document.cancelledDocumentLines[i].price_net;
    */
            if (update && !minDeposit) {
                documentAggregate.getDocument().setPriceNet(price);
            }
            return price;
        }
    }

    private static final class BlockAttendance {

        private final DocumentLine documentLine;
        private final LocalDate date;

        int price;

        BlockAttendance(DocumentLine documentLine, LocalDate date) {
            this.documentLine = documentLine;
            this.date = date;
        }

        LocalDate getDate() {
            return date;
        }
    }

    private static final class Block {

        private final SiteItem siteItem;
        private final List<BlockAttendance> blockAttendances = new ArrayList<>();

        Block(SiteItem siteItem) {
            this.siteItem = siteItem;
        }

        void addBlockAttendance(BlockAttendance blockAttendance) {
            blockAttendances.add(blockAttendance);
        }

        void sortBlockAttendances() {
            blockAttendances.sort((ba1, ba2) -> {
                int result = ba1.getDate().compareTo(ba2.getDate());
                /* Commented for now in KBS3
                if (result == 0) {
                    var tr1 = ba1.documentLine.timeRange;
                    var tr2 = ba2.documentLine.timeRange;
                    if (tr1 && tr2)
                        result = tr1.getInterval()[0] - tr2.getInterval()[0];
                    else
                        result = tr1 ? -1 : tr2 ? 1 : 0;
                }*/
                return result;
            });
        }

        int computeBlockPrice(Bill bill, boolean minDeposit) {
            int price = 0;
            List<BlockAttendance> bas = blockAttendances;
            int blockLength = bas.size();
            int remainingDays = blockLength;
            if (remainingDays == 0)
                return 0;
            int consumedDays = 0;
    /* Commented for now in KBS3
            if (bill.update) {
                for (var i = 0; i < blockLength; i++) {
                    var dl = bas[i].documentLine;
                    dl.price = 0;
                    dl.rounded = false;
                }
            }
    */
            LocalDate firstDay = Collections.first(bas).getDate();
            LocalDate lastDay = Collections.last(bas).getDate();
            DocumentAggregate documentAggregate = bill.getDocumentAggregate();
            PolicyAggregate policyAggregate = documentAggregate.getPolicyAggregate();
            List<Rate> rates = policyAggregate.getSiteItemRatesStream(siteItem.getSite(), siteItem.getItem())
                .filter(r -> r.getStartDate() == null || r.getStartDate().isBefore(lastDay) || r.getStartDate().isEqual(lastDay))
                .filter(r -> r.getEndDate() == null || r.getEndDate().isAfter(firstDay) || r.getEndDate().isEqual(firstDay))
                //.filter(r -> r.getRateMatchesDocument(bill.getDocument()))
                .collect(Collectors.toList());
            if (!rates.isEmpty()) {
                //rates.sort(function (r1, r2) { return (r1.perDay ? 1 : r1.maxDay ) - (r2.perDay ? 1 : r2.maxDay);});
                while (remainingDays > 0) {
                    // selecting the cheapest rate for the next attendances
                    PriceMemo cheapest = null;
                    PriceMemo second = null;
                    for (Rate rate : rates) {
                        // Ignoring rates for long stay discounts (if requested)
                        if (bill.ignoreLongStayDiscount && !rate.isPerDay()) // assuming that a rate not per day is a long stay discount TODO: check this more carefully
                            continue;
                        // Ignoring rates that are not in the range of dates
                        LocalDate startDate = rate.getStartDate();
                        LocalDate endDate = rate.getEndDate();
                        if (startDate != null || endDate != null) {
                            LocalDate date = bas.get(consumedDays).getDate();
                            if (startDate != null && date.isBefore(startDate) || endDate != null && date.isAfter(endDate) /* || rate.arrivingOrLeaving && date !== firstDay && date !== lastDay*/)
                                continue;
                        }
                        var quantity = 1; //rate.perPerson && dl.share_owner && dl.capacity ? dl.capacity.capacity : 1;
                        int ratePrice = getRatePrice(rate, documentAggregate) * quantity;
                        int minDay = Objects.coalesce(rate.getMinDay(), 1);
                        int maxDay = rate.isPerDay() ? 1 : Objects.coalesce(rate.getMaxDay(), 10000);
                        // Ignoring rates whose minDay is not honored
                        if (blockLength < minDay) {
                            /* Commented for now in KBS3
                            if (!rate.minDayCeiling)
                                continue;*/
                            // When a rate defines a new lower daily price that applies after a minimum of days (ex: 30% discount when >= 14 days),
                            // we need to ensure that people approaching that number of days (ex: 12 or 13 days)
                            // don't pay more with the previous rate than people staying that minimum of days (ex: 14 days)
                            // In other words, we need to put an upper limit for such people, equals to the price that is applied at that minimum of days
                            if (maxDay == 1) { // So if the block is less than the rate min day,
                                ratePrice = ratePrice * minDay; // we transform the daily rate into a fixed rate with the upper limit
                                maxDay = minDay; // that applies over that period
                            }
                        }
                        int consumableDays = Math.min(remainingDays, maxDay);
                        int dailyPrice = ratePrice / consumableDays;
                        // Ugly workaround for Online January retreat 2021 because this price algorithm is not always correct.
                        // Ex: 1 week (actually 8 days): £70, 2 weeks (actually 15 days): £120, 3 weeks (actually 22 days): £180
                        // => For the 3-weeks case, this price algorithm computes £190 instead of £180 because it considers
                        // the £120 rate is the cheaper (because £120 / 15 < £180 / 22) and then add £70 for the remaining days.
                        /* Commented in KBS3
                        if (rate.id === 27510 && remainingDays === maxDay) // £120 rate with 22 remaining days
                            dailyPrice = ratePrice / (maxDay + 1);*/ // Changing the daily price comparison to £180 / 23 to make it the cheapest
                        PriceMemo memo = new PriceMemo(rate, dailyPrice, ratePrice, consumableDays);
                        if (cheapest == null)
                            cheapest = memo;
                        else if (dailyPrice < cheapest.dailyPrice) {
                            second = cheapest;
                            cheapest = memo;
                        } else if (second == null || dailyPrice < second.dailyPrice)
                            second = memo;
                    }
                    if (cheapest == null) // Happens when no rate is finally applicable
                        break;
                    // applying the found cheapest rate on the next consumable days (applicable for this rate)
                    var remainingPrice = cheapest.price;
                    if (second == null)
                        second = cheapest;
                    var fullAttendancePrice = second.price / second.consumableDays;
                    for (int i = 0; i < cheapest.consumableDays; i++) {
                        BlockAttendance ba = bas.get(consumedDays + i);
                        ba.price = i == cheapest.consumableDays - 1 ? remainingPrice : Math.min(fullAttendancePrice, remainingPrice);
                        /*if (update)
                            ba.documentLine.price += ba.price;*/
                        remainingPrice -= ba.price;
                    }
                    // updating the block price
                    int deltaPrice = cheapest.price;
                    if (minDeposit) {
                        int minDepositPercent = Objects.coalesce(cheapest.rate.getMinDeposit(), 25);
                        deltaPrice = deltaPrice * minDepositPercent / 100;
                    }
                    price += deltaPrice;
                    // marking progress
                    consumedDays += cheapest.consumableDays;
                    remainingDays -= cheapest.consumableDays;
                }
            }
            /* Commented in KBS3 for now
            var roundingFactor = bill.document.event.optionRoundingFactor;
            if (update && roundingFactor) { // rounding document lines prices if a rounding factor is specified for the event
                price = 0; // also recomputing the total block price
                for (i = 0; i < blockLength; i++) {
                    dl = bas[i].documentLine;
                    if (!dl.rounded) {
                        var item = dl.getItemOption().item;
                        var fcode = item.family.code;
                        if (fcode !== 'acco' && fcode !== 'tax')
                            dl.price = Math.round(dl.price / roundingFactor) * roundingFactor;
                        price += dl.price;
                        dl.rounded = true;
                    }
                }
            }*/
            return price;
        }

        private int getRatePrice(Rate rate, DocumentAggregate documentAggregate) {
            int price = rate.getPrice();
            Document document = documentAggregate.getDocument();
            Person person = document.getPerson();
            Integer age =  document.getAge();
            if (age == null && person != null)
                age = person.getAge();
            boolean unemployed = Booleans.isTrue(document.isUnemployed());
            if (person != null && Booleans.isTrue(person.isUnemployed()))
                unemployed = true;
            boolean facilityFee = Booleans.isTrue(document.isPersonFacilityFee());
            if (person != null && Booleans.isTrue(person.isFacilityFee()))
                facilityFee = true;
            /*var workingVisit = document.person_workingVisit || document.person && document.person.workingVisit;
            var guest = document.person_guest || document.person && document.person.guest;
            var resident = document.person_resident || document.person && document.person.resident;
            var resident2 = document.person_resident2 || document.person && document.person.resident2;
            var discoveryReduced = document.person_discoveryReduced || document.person && document.person.discoveryReduced;
            var discovery = document.person_discovery || document.person && document.person.discovery;*/
            if (age != null) {
                if (rate.getAge1Max() != null && age <= rate.getAge1Max())
                    price = rate.getAge1Price() != null ? rate.getAge1Price() : price * (100 - rate.getAge1Discount()) / 100;
                else if (rate.getAge2Max() != null && age <= rate.getAge2Max())
                    price = rate.getAge2Price() != null ? rate.getAge2Price() : price * (100 - rate.getAge2Discount()) / 100;
                else if (rate.getAge3Max() != null && age <= rate.getAge3Max())
                    price = rate.getAge3Price() !=null ? rate.getAge3Price() : price * (100 - rate.getAge3Discount()) / 100;
            } /*else if (workingVisit && (rate.workingVisit_price || rate.workingVisit_discount))
                price = (rate.workingVisit_price || rate.workingVisit_price === 0) ? rate.workingVisit_price : price * (100 - rate.workingVisit_discount) / 100;
            else if (guest && (rate.guest_price || rate.guest_discount))
                price = (rate.guest_price || rate.guest_price === 0) ? rate.guest_price : price * (100 - rate.guest_discount) / 100;
            else if (resident && (rate.resident_price || rate.resident_discount))
                price = (rate.resident_price || rate.resident_price === 0) ? rate.resident_price : price * (100 - rate.resident_discount) / 100;
            else if (resident2 && (rate.resident2_price || rate.resident2_discount))
                price = (rate.resident2_price || rate.resident2_price === 0) ? rate.resident2_price : price * (100 - rate.resident2_discount) / 100;
            else if (discoveryReduced && (rate.discoveryReduced_price || rate.discoveryReduced_discount))
                price = (rate.discoveryReduced_price || rate.discoveryReduced_price === 0) ? rate.discoveryReduced_price : price * (100 - rate.discoveryReduced_discount) / 100;
            else if (discovery && (rate.discovery_price || rate.discovery_discount))
                price = (rate.discovery_price || rate.discovery_price === 0) ? rate.discovery_price : price * (100 - rate.discovery_discount) / 100;*/
            else if (unemployed && (rate.getUnemployedPrice() != null || rate.getUnemployedDiscount() != null))
                price = rate.getUnemployedPrice() != null ? rate.getUnemployedPrice() : price * (100 - rate.getUnemployedDiscount()) / 100;
            else if (facilityFee && (rate.getFacilityFeePrice() != null || rate.getFacilityFeeDiscount() != null))
                price = rate.getFacilityFeePrice() != null ? rate.getFacilityFeePrice() : price * (100 - rate.getFacilityFeeDiscount()) / 100;
            return price;
        }

    }

}
