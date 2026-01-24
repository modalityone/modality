package one.modality.ecommerce.policy.service.spi.impl.server;

import dev.webfx.platform.async.Batch;
import dev.webfx.platform.async.Future;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryResult;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.orm.entity.DqlQueries;
import one.modality.base.shared.entities.Event;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.ecommerce.policy.service.LoadPolicyArgument;
import one.modality.ecommerce.policy.service.PolicyAggregate;
import one.modality.ecommerce.policy.service.spi.PolicyServiceProvider;

/**
 * @author Bruno Salmon
 */
public final class ServerPolicyServiceProvider implements PolicyServiceProvider {

    private static final int GENERAL_GUESTS_EVENT_POOL_ID = 4; // temporarily hardcoded

    private final static String EVENT_QUERY_BASE =
        "select venue.(name,label), state, shortDescriptionLabel, longDescriptionLabel, termsUrlEn, organization.privacyUrlLabel, openingDate, bookingProcessStart, timezone, noAccountBooking" +
        ", date_part('epoch', openingDate - now()) as " + Event.secondsToOpeningDateAtLoadingTime +
        ", date_part('epoch', coalesce(bookingProcessStart, openingDate) - now()) as " + Event.secondsToBookingProcessStartAtLoadingTime +
        " from Event"; // loading terms url for US Festival (way to load terms will change later)
    private final static String SCHEDULED_ITEMS_QUERY_BASE =
        "select name,label,comment,site.name,item.(name,label,code,family.(code,name,label,ord),capacity,share_mate,ord),date,startTime,timeline.(site,item,startTime,endTime),cancelled,resource" +
        // We also compute the remaining available space for guests
        ",(select [" +
            // male availability
            "sum(!sr.configuration.allowsMale ? 0 :" +
            " coalesce((select quantity from PoolAllocation where resource=sr.configuration.resource and pool= " + GENERAL_GUESTS_EVENT_POOL_ID + " and event=$1 limit 1), 0)" +
            " - coalesce((select sum(documentLine.quantity) from Attendance where scheduledResource=sr and present and documentLine.(!frontend_released and (pool = null or pool= " + GENERAL_GUESTS_EVENT_POOL_ID + "))), 0)" +
            ")," +
            // female availability
            "sum(!sr.configuration.allowsFemale ? 0 :" +
            " coalesce((select quantity from PoolAllocation where resource=sr.configuration.resource and pool= " + GENERAL_GUESTS_EVENT_POOL_ID + " and event=$1 limit 1), 0)" +
            " - coalesce((select sum(documentLine.quantity) from Attendance where scheduledResource=sr and present and documentLine.(!frontend_released and (pool = null or pool= " + GENERAL_GUESTS_EVENT_POOL_ID + "))), 0)" +
            ")] from ScheduledResource sr" +
        // We consider only the resources allocated to the general guest pool for this event
        " where scheduledItem=si and exists(select PoolAllocation where resource=sr.configuration.resource and pool= " + GENERAL_GUESTS_EVENT_POOL_ID + " and event=$1)" +
        " group by scheduledItem)" +
        " as " + ScheduledItem.maleFemaleAvailabilities +
        " from ScheduledItem si";
    private final static String SCHEDULED_BOUNDARIES_QUERY_BASE =
        "select event,scheduledItem,timeline.(startTime,endTime),atStartTime,date" +
        " from ScheduledBoundary sb";
    private final static String EVENT_PARTS_QUERY_BASE =
        "select event,name,label,startBoundary,endBoundary,accommodationChangeAllowed" +
        " from EventPart epa";
    private final static String EVENT_SELECTIONS_QUERY_BASE =
        "select event,name,label,inPerson,online,part1,part2,part3" +
        " from EventSelection es";
    private final static String EVENT_PHASES_QUERY_BASE =
        "select event,name,label,startBoundary,endBoundary" +
        " from EventPhase eph";
    private final static String EVENT_PHASE_COVERAGES_QUERY_BASE =
        "select event,name,label,phase1,phase2,phase3,phase4" +
        " from EventPhaseCoverage epc";
    private final static String ITEM_FAMILY_POLICIES_QUERY_BASE =
        "select scope.(organization,site,eventType,event)" +
        ",itemFamily.ord" +
        ",eventPhaseCoverage1,eventPhaseCoverage2,eventPhaseCoverage3,eventPhaseCoverage4" +
        " from ItemFamilyPolicy ifp";
    private final static String ITEM_POLICIES_QUERY_BASE =
        "select scope.(organization,site,eventType,event)" +
        ",item.(name,label,code,family.(code,name,label,ord),capacity,share_mate,ord)" +
        ",descriptionLabel,noticeLabel,minDay,default,genderInfoRequired,earlyAccommodationAllowed,lateAccommodationAllowed,minOccupancy,forceSoldOut" +
        " from ItemPolicy ip";
    private final static String RATES_QUERY_BASE =
        "select site,item,price,perDay,perPerson,facilityFee_price,startDate,endDate,onDate,offDate,minDeposit" +
        ",cutoffDate,minDeposit2" +
        ",age1_max,age1_price,age1_discount,age2_max,age2_price,age2_discount" +
        ",resident_price,resident_discount,resident2_price,resident2_discount" +
        " from Rate r";
    @Deprecated
    private final static String BOOKABLE_PERIODS_QUERY_BASE =
        "select startScheduledItem,endScheduledItem,name,label" +
        " from BookablePeriod bp";

    @Override
    public Future<PolicyAggregate> loadPolicy(LoadPolicyArgument argument) {
        // Managing the case of recurring event only for now
        Object eventPk = argument.getEventPk();
        return QueryService.executeQueryBatch(
                new Batch<>(new QueryArgument[]{
                    // 0 - Loading event
                    DqlQueries.newQueryArgumentForDefaultDataSource(
                        EVENT_QUERY_BASE + " where id=$1", eventPk),
                    // 1 - Loading scheduled items (of this event or of the repeated event if set)
                    DqlQueries.newQueryArgumentForDefaultDataSource(
                        SCHEDULED_ITEMS_QUERY_BASE + " where" +
                        // Only bookable items
                        " bookableScheduledItem=id" +
                        // specific to this event
                        " and (select si.event = coalesce(e.repeatedEvent, e) " +
                        // or global sites of the organization with scheduled items over the period of the event
                        "      or si.event=null and si.timeline..site..organization = e.organization and (si.date >= e.startDate and si.date <= e.endDate or exists(select EventPart ep where ep.event=e and si.date>=coalesce(ep.startBoundary.date, ep.startBoundary.scheduledItem.date) and si.date<=coalesce(ep.endBoundary.date, ep.endBoundary.scheduledItem.date))) from Event e where id=$1)" +
                        // excluding accommodation items with no resource allocated to the general guest pool for this event
                        " and (si.item.family.code!='acco' or exists(select ScheduledResource sr where scheduledItem=si and exists(select PoolAllocation where resource=sr.configuration.resource and pool= " + GENERAL_GUESTS_EVENT_POOL_ID + " and event=$1)))" +
                        " order by site..ord,item..ord,date", eventPk)
                    // 2 - Loading scheduled boundaries (of this event or of the repeated event if set)
                    , DqlQueries.newQueryArgumentForDefaultDataSource(
                    SCHEDULED_BOUNDARIES_QUERY_BASE + " where (select sb.event = coalesce(e.repeatedEvent, e) from Event e where id=$1)" +
                    " order by scheduledItem.date", eventPk)
                    // 3 - Loading event parts (of this event or of the repeated event if set)
                    , DqlQueries.newQueryArgumentForDefaultDataSource(
                    EVENT_PARTS_QUERY_BASE + " where (select epa.event = coalesce(e.repeatedEvent, e) from Event e where id=$1)" +
                    " order by startBoundary.id", eventPk)
                    // 4 - Loading event selections (of this event or of the repeated event if set)
                    , DqlQueries.newQueryArgumentForDefaultDataSource(
                    EVENT_SELECTIONS_QUERY_BASE + " where (select es.event = coalesce(e.repeatedEvent, e) from Event e where id=$1)" +
                    " order by id", eventPk) // Will introduce an ord later
                    // 5 - Loading event phases (of this event or of the repeated event if set)
                    , DqlQueries.newQueryArgumentForDefaultDataSource(
                    EVENT_PHASES_QUERY_BASE + " where (select eph.event = coalesce(e.repeatedEvent, e) from Event e where id=$1)" +
                    " order by id", eventPk)
                    // 6 - Loading phase coverages (of this event or of the repeated event if set)
                    , DqlQueries.newQueryArgumentForDefaultDataSource(
                    EVENT_PHASE_COVERAGES_QUERY_BASE + " where (select epc.event = coalesce(e.repeatedEvent, e) from Event e where id=$1)" +
                    " order by id", eventPk) // Will introduce an ord later
                    // 7 - Loading item policies (of this event or of the repeated event if set)
                    , DqlQueries.newQueryArgumentForDefaultDataSource(
                    ITEM_FAMILY_POLICIES_QUERY_BASE + " where (select ifp.scope.(" +
                    " organization = e.organization" +
                    " and (site = null or site..event = null or site..event = coalesce(e.repeatedEvent, e))" +
                    " and (eventType = null or eventType = coalesce(e.repeatedEvent.type, e.type))" +
                    " and (event = null or event = coalesce(e.repeatedEvent, e))" +
                    " ) from Event e where id=$1)" +
                    " order by itemFamily.ord,id", eventPk)
                    // 8 - Loading item policies (of this event or of the repeated event if set)
                    , DqlQueries.newQueryArgumentForDefaultDataSource(
                    ITEM_POLICIES_QUERY_BASE + " where (select ip.scope.(" +
                    " organization = e.organization" +
                    " and (site = null or site..event = null or site..event = coalesce(e.repeatedEvent, e))" +
                    " and (eventType = null or eventType = coalesce(e.repeatedEvent.type, e.type))" +
                    " and (event = null or event = coalesce(e.repeatedEvent, e))" +
                    " ) from Event e where id=$1)" +
                    " order by item.family.ord,item.ord,id", eventPk)
                    // 9 - Loading rates (of this event or of the repeated event if set)
                    , DqlQueries.newQueryArgumentForDefaultDataSource(
                    RATES_QUERY_BASE + " where (" +
                    // Sites dedicated to this event
                    "select r.site.event = coalesce(e.repeatedEvent, e)" +
                    // or global sites of the organization with scheduled items over the period of the event
                    " or r.site.(event = null and organization=e.organization and exists(select ScheduledItem si where si.site=r.site and si.item=r.item and si.date>=e.startDate and si.date<=e.endDate))" +
                    " from Event e where id=$1)" +
                    // Note: TeachingsPricing relies on the following order to work properly
                    " order by site,item,perDay desc,startDate,endDate,price", eventPk)
                    // 10 (deprecated) - Loading bookable periods (of this event or of the repeated event if set)
                    , DqlQueries.newQueryArgumentForDefaultDataSource(
                    BOOKABLE_PERIODS_QUERY_BASE + " where (select bp.event = coalesce(e.repeatedEvent, e) from Event e where id=$1)" +
                    " order by startScheduledItem.date,endScheduledItem.date", eventPk)
                }))
            .map(batch -> new PolicyAggregate(
                EVENT_QUERY_BASE, batch.get(0),
                SCHEDULED_ITEMS_QUERY_BASE, batch.get(1),
                SCHEDULED_BOUNDARIES_QUERY_BASE, batch.get(2),
                EVENT_PARTS_QUERY_BASE, batch.get(3),
                EVENT_SELECTIONS_QUERY_BASE, batch.get(4),
                EVENT_PHASES_QUERY_BASE, batch.get(5),
                EVENT_PHASE_COVERAGES_QUERY_BASE, batch.get(6),
                ITEM_FAMILY_POLICIES_QUERY_BASE, batch.get(7),
                ITEM_POLICIES_QUERY_BASE, batch.get(8),
                RATES_QUERY_BASE, batch.get(9),
                BOOKABLE_PERIODS_QUERY_BASE, batch.get(10)
            ));
    }

    @Override
    public Future<QueryResult> loadAvailabilities(LoadPolicyArgument argument) {
        return QueryService.executeQuery(
            DqlQueries.newQueryArgumentForDefaultDataSource(
                SCHEDULED_ITEMS_QUERY_BASE + " where bookableScheduledItem=id and (select si.event = coalesce(e.repeatedEvent, e) or si.event=null and si.timeline..site..organization = e.organization and (si.date >= e.startDate and si.date <= e.endDate or exists(select EventPart ep where ep.event=e and si.date>=coalesce(ep.startBoundary.date, ep.startBoundary.scheduledItem.date) and si.date<=coalesce(ep.endBoundary.date, ep.endBoundary.scheduledItem.date))) from Event e where id=$1)" +
                " order by site..ord,item..ord,date", argument.getEventPk())
        );
    }
}
