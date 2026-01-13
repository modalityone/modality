package one.modality.ecommerce.policy.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import one.modality.ecommerce.policy.service.PolicyAggregate;

/**
 * @author Bruno Salmon
 */
public final class PolicyAggregateSerialCodec extends SerialCodecBase<PolicyAggregate> {

    private static final String CODEC_ID = "PolicyAggregate";
    private static final String SCHEDULED_ITEMS_QUERY_BASE_KEY = "siqb";
    private static final String SCHEDULED_ITEMS_QUERY_RESULT_KEY = "siqr";
    private static final String SCHEDULED_BOUNDARIES_QUERY_BASE_KEY = "sbqb";
    private static final String SCHEDULED_BOUNDARIES_QUERY_RESULT_KEY = "sbqr";
    private static final String EVENT_PARTS_QUERY_BASE_KEY = "epaqb";
    private static final String EVENT_PARTS_QUERY_RESULT_KEY = "epaqr";
    private static final String EVENT_SELECTIONS_QUERY_BASE_KEY = "esqb";
    private static final String EVENT_SELECTIONS_QUERY_RESULT_KEY = "esqr";
    private static final String EVENT_PHASES_QUERY_BASE_KEY = "ephqb";
    private static final String EVENT_PHASES_QUERY_RESULT_KEY = "ephqr";
    private static final String PHASE_COVERAGES_QUERY_BASE_KEY = "pcqb";
    private static final String PHASE_COVERAGES_QUERY_RESULT_KEY = "pcqr";
    private static final String RATES_QUERY_BASE_KEY = "rqb";
    private static final String RATES_QUERY_RESULT_KEY = "rqr";
    private static final String ITEM_FAMILY_POLICIES_QUERY_BASE_KEY = "ifpqb";
    private static final String ITEM_FAMILY_POLICIES_QUERY_RESULT_KEY = "ifpqr";
    private static final String ITEM_POLICIES_QUERY_BASE_KEY = "ipqb";
    private static final String ITEM_POLICIES_QUERY_RESULT_KEY = "ipqr";
    private static final String BOOKABLE_PERIODS_QUERY_BASE_KEY = "bpqb";
    private static final String BOOKABLE_PERIODS_QUERY_RESULT_KEY = "bpqr";


    public PolicyAggregateSerialCodec() {
        super(PolicyAggregate.class, CODEC_ID);
    }

    @Override
    public void encode(PolicyAggregate pa, AstObject serial) {
        encodeString(serial, SCHEDULED_ITEMS_QUERY_BASE_KEY,         pa.getScheduledItemsQueryBase());
        encodeObject(serial, SCHEDULED_ITEMS_QUERY_RESULT_KEY,       pa.getScheduledItemsQueryResult());
        encodeString(serial, SCHEDULED_BOUNDARIES_QUERY_BASE_KEY,    pa.getScheduledBoundariesQueryBase());
        encodeObject(serial, SCHEDULED_BOUNDARIES_QUERY_RESULT_KEY,  pa.getScheduledBoundariesQueryResult());
        encodeString(serial, EVENT_PARTS_QUERY_BASE_KEY,             pa.getEventPartsQueryBase());
        encodeObject(serial, EVENT_PARTS_QUERY_RESULT_KEY,           pa.getEventPartsQueryResult());
        encodeString(serial, EVENT_SELECTIONS_QUERY_BASE_KEY,        pa.getEventSelectionsQueryBase());
        encodeObject(serial, EVENT_SELECTIONS_QUERY_RESULT_KEY,      pa.getEventSelectionsQueryResult());
        encodeString(serial, EVENT_PHASES_QUERY_BASE_KEY,            pa.getEventPhasesQueryBase());
        encodeObject(serial, EVENT_PHASES_QUERY_RESULT_KEY,          pa.getEventPhasesQueryResult());
        encodeString(serial, PHASE_COVERAGES_QUERY_BASE_KEY,         pa.getPhaseCoveragesQueryBase());
        encodeObject(serial, PHASE_COVERAGES_QUERY_RESULT_KEY,       pa.getPhaseCoveragesQueryResult());
        encodeString(serial, ITEM_FAMILY_POLICIES_QUERY_BASE_KEY,    pa.getItemFamilyPoliciesQueryBase());
        encodeObject(serial, ITEM_FAMILY_POLICIES_QUERY_RESULT_KEY,  pa.getItemFamilyPoliciesQueryResult());
        encodeString(serial, ITEM_POLICIES_QUERY_BASE_KEY,           pa.getItemPoliciesQueryBase());
        encodeObject(serial, ITEM_POLICIES_QUERY_RESULT_KEY,         pa.getItemPoliciesQueryResult());
        encodeString(serial, RATES_QUERY_BASE_KEY,                   pa.getRatesQueryBase());
        encodeObject(serial, RATES_QUERY_RESULT_KEY,                 pa.getRatesQueryResult());
        encodeString(serial, BOOKABLE_PERIODS_QUERY_BASE_KEY,        pa.getBookablePeriodsQueryBase());
        encodeObject(serial, BOOKABLE_PERIODS_QUERY_RESULT_KEY,      pa.getBookablePeriodsQueryResult());
    }

    @Override
    public PolicyAggregate decode(ReadOnlyAstObject serial) {
        return new PolicyAggregate(
            decodeString(serial, SCHEDULED_ITEMS_QUERY_BASE_KEY),
            decodeObject(serial, SCHEDULED_ITEMS_QUERY_RESULT_KEY),
            decodeString(serial, SCHEDULED_BOUNDARIES_QUERY_BASE_KEY),
            decodeObject(serial, SCHEDULED_BOUNDARIES_QUERY_RESULT_KEY),
            decodeString(serial, EVENT_PARTS_QUERY_BASE_KEY),
            decodeObject(serial, EVENT_PARTS_QUERY_RESULT_KEY),
            decodeString(serial, EVENT_SELECTIONS_QUERY_BASE_KEY),
            decodeObject(serial, EVENT_SELECTIONS_QUERY_RESULT_KEY),
            decodeString(serial, EVENT_PHASES_QUERY_BASE_KEY),
            decodeObject(serial, EVENT_PHASES_QUERY_RESULT_KEY),
            decodeString(serial, PHASE_COVERAGES_QUERY_BASE_KEY),
            decodeObject(serial, PHASE_COVERAGES_QUERY_RESULT_KEY),
            decodeString(serial, ITEM_FAMILY_POLICIES_QUERY_BASE_KEY),
            decodeObject(serial, ITEM_FAMILY_POLICIES_QUERY_RESULT_KEY),
            decodeString(serial, ITEM_POLICIES_QUERY_BASE_KEY),
            decodeObject(serial, ITEM_POLICIES_QUERY_RESULT_KEY),
            decodeString(serial, RATES_QUERY_BASE_KEY),
            decodeObject(serial, RATES_QUERY_RESULT_KEY),
            decodeString(serial, BOOKABLE_PERIODS_QUERY_BASE_KEY),
            decodeObject(serial, BOOKABLE_PERIODS_QUERY_RESULT_KEY)
        );
    }
}
