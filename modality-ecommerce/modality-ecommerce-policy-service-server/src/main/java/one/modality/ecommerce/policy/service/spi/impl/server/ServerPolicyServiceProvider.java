package one.modality.ecommerce.policy.service.spi.impl.server;

import dev.webfx.platform.async.Batch;
import dev.webfx.platform.async.Future;
import dev.webfx.stack.db.query.QueryArgument;
import dev.webfx.stack.db.query.QueryService;
import dev.webfx.stack.orm.entity.DqlQueries;
import one.modality.ecommerce.policy.service.LoadPolicyArgument;
import one.modality.ecommerce.policy.service.PolicyAggregate;
import one.modality.ecommerce.policy.service.spi.PolicyServiceProvider;

/**
 * @author Bruno Salmon
 */
public class ServerPolicyServiceProvider implements PolicyServiceProvider {

    private final static String POLICY_SCHEDULED_ITEMS_QUERY_BASE = "select site.name,item.(name,label,code,family.(code,name,label),ord),date,startTime,timeline.startTime,cancelled from ScheduledItem";
    private final static String POLICY_RATES_QUERY_BASE = "select site,item,price,perDay,perPerson,facilityFee_price,startDate,endDate,onDate,offDate,minDeposit,cutoffDate,minDeposit2,age1_max,age1_price,age1_discount,age2_max,age2_price,age2_discount,resident_price,resident_discount,resident2_price,resident2_discount from Rate";
    private final static String POLICY_BOOKABLE_PERIODS_QUERY_BASE = "select startScheduledItem,endScheduledItem,name,label from BookablePeriod";

    @Override
    public Future<PolicyAggregate> loadPolicy(LoadPolicyArgument argument) {
        // Managing the case of recurring event only for now
        Object eventPk = argument.getEventPk();
        return QueryService.executeQueryBatch(
                new Batch<>(new QueryArgument[]{
                    // Loading scheduled items (of this event or of the repeated event if set)
                    DqlQueries.newQueryArgumentForDefaultDataSource(
                        POLICY_SCHEDULED_ITEMS_QUERY_BASE + " where event = (select coalesce(repeatedEvent, id) from Event where id=$1) and bookableScheduledItem=id " +
                        "order by site,item,date", eventPk),
                    // Loading rates (of this event or of the repeated event if set)
                    DqlQueries.newQueryArgumentForDefaultDataSource(
                        POLICY_RATES_QUERY_BASE + " where site.event = (select coalesce(repeatedEvent, id) from Event where id=$1) or site = (select coalesce(repeatedEvent.venue, venue) from Event where id=$1) " +
                        // Note: TeachingsPricing relies on the following order to work properly
                        "order by site,item,perDay desc,startDate,endDate,price", eventPk),
                    // Loading bookable periods (of this event or of the repeated event if set)
                    DqlQueries.newQueryArgumentForDefaultDataSource(
                        POLICY_BOOKABLE_PERIODS_QUERY_BASE + " where event = (select coalesce(repeatedEvent, id) from Event where id=$1)" +
                        "order by startScheduledItem.date,endScheduledItem.date", eventPk)
                }))
            .map(batch -> new PolicyAggregate(
                POLICY_SCHEDULED_ITEMS_QUERY_BASE, batch.get(0),
                POLICY_RATES_QUERY_BASE, batch.get(1),
                POLICY_BOOKABLE_PERIODS_QUERY_BASE, batch.get(2)));
    }
}
