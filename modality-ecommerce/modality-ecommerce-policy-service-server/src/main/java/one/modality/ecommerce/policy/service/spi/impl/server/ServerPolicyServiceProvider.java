package one.modality.ecommerce.policy.service.spi.impl.server;

import dev.webfx.platform.async.Batch;
import dev.webfx.platform.async.Future;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.orm.entity.DqlQueries;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.ecommerce.policy.service.LoadPolicyArgument;
import one.modality.ecommerce.policy.service.PolicyAggregate;
import one.modality.ecommerce.policy.service.spi.PolicyServiceProvider;

/**
 * @author Bruno Salmon
 */
public class ServerPolicyServiceProvider implements PolicyServiceProvider {

    private static final int GENERAL_GUESTS_EVENT_POOL_ID = 4; // temporarily hardcoded

    private final static String SCHEDULED_ITEMS_QUERY_BASE =
        "select site.name,item.(name,label,code,family.(code,name,label),ord),date,startTime,timeline.startTime,cancelled" +
        // We also compute the remaining available space for guests
        ",(select sum(coalesce(max,configuration.max) - (select count(1) from Attendance where scheduledResource=sr and present and !documentLine.cancelled))" +
            " from ScheduledResource sr" +
            // We consider only the resources allocated to the general guest pool for this event
            " where scheduledItem=si and exists(select PoolAllocation where resource=sr.configuration.resource and pool= " + GENERAL_GUESTS_EVENT_POOL_ID + " and event=$1)" +
            " group by scheduledItem)" +
        " as " + ScheduledItem.guestsAvailability +
        " from ScheduledItem si";
    private final static String RATES_QUERY_BASE =
        "select site,item,price,perDay,perPerson,facilityFee_price,startDate,endDate,onDate,offDate,minDeposit,cutoffDate,minDeposit2,age1_max,age1_price,age1_discount,age2_max,age2_price,age2_discount,resident_price,resident_discount,resident2_price,resident2_discount" +
        " from Rate";
    private final static String BOOKABLE_PERIODS_QUERY_BASE =
        "select startScheduledItem,endScheduledItem,name,label" +
        " from BookablePeriod";
    private final static String ITEM_POLICIES_QUERY_BASE =
        "select scope.(organization,site,eventType,event),item,minDay" +
        " from ItemPolicy";

    @Override
    public Future<PolicyAggregate> loadPolicy(LoadPolicyArgument argument) {
        // Managing the case of recurring event only for now
        Object eventPk = argument.getEventPk();
        return QueryService.executeQueryBatch(
                new Batch<>(new QueryArgument[]{
                    // Loading scheduled items (of this event or of the repeated event if set)
                    DqlQueries.newQueryArgumentForDefaultDataSource(
                        SCHEDULED_ITEMS_QUERY_BASE + " where (event = (select coalesce(repeatedEvent, id) from Event where id=$1) or event=null and timeline..site..organization = (select organization from Event where id=$1)) and bookableScheduledItem=id " +
                        "order by site,item,date", eventPk)
                    // Loading rates (of this event or of the repeated event if set)
                    , DqlQueries.newQueryArgumentForDefaultDataSource(
                    RATES_QUERY_BASE + " where site.event = (select coalesce(repeatedEvent, id) from Event where id=$1) or site = (select coalesce(repeatedEvent.venue, venue) from Event where id=$1) " +
                    // Note: TeachingsPricing relies on the following order to work properly
                    "order by site,item,perDay desc,startDate,endDate,price", eventPk)
                    // Loading bookable periods (of this event or of the repeated event if set)
                    , DqlQueries.newQueryArgumentForDefaultDataSource(
                    BOOKABLE_PERIODS_QUERY_BASE + " where event = (select coalesce(repeatedEvent, id) from Event where id=$1)" +
                    "order by startScheduledItem.date,endScheduledItem.date", eventPk)
                    // Loading item policies (of this event or of the repeated event if set)
                    , DqlQueries.newQueryArgumentForDefaultDataSource(
                    ITEM_POLICIES_QUERY_BASE + " where scope.(" +
                    " organization = (select organization from Event where id=$1)" +
                    " and (site = null or site..event = null or site..event = (select coalesce(repeatedEvent, id) from Event where id=$1))" +
                    " and (eventType = null or eventType = (select coalesce(repeatedEvent.type, type) from Event where id=$1))" +
                    " and (event = null or event = (select coalesce(repeatedEvent, id) from Event where id=$1))" +
                    ") order by id", eventPk)
                }))
            .map(batch -> new PolicyAggregate(
                SCHEDULED_ITEMS_QUERY_BASE, batch.get(0),
                RATES_QUERY_BASE, batch.get(1),
                BOOKABLE_PERIODS_QUERY_BASE, batch.get(2),
                ITEM_POLICIES_QUERY_BASE, batch.get(3)
            ));
    }
}
