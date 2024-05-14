package one.modality.ecommerce.document.service.buscall.serial;

import dev.webfx.platform.ast.AstObject;
import dev.webfx.platform.ast.ReadOnlyAstObject;
import dev.webfx.stack.com.serial.SerialCodecManager;
import dev.webfx.stack.com.serial.spi.impl.SerialCodecBase;
import dev.webfx.stack.db.query.QueryResult;
import one.modality.ecommerce.document.service.PolicyAggregate;

/**
 * @author Bruno Salmon
 */
public class PolicyAggregateSerialCodec extends SerialCodecBase<PolicyAggregate> {

    private static final String CODEC_ID = "PolicyAggregate";
    private static final String SCHEDULED_ITEMS_QUERY_BASE_KEY = "siqb";
    private static final String SCHEDULED_ITEMS_QUERY_RESULT_KEY = "siqr";
    private static final String RATES_QUERY_BASE_KEY = "rqb";
    private static final String RATES_QUERY_RESULT_KEY = "rqr";


    public PolicyAggregateSerialCodec() {
        super(PolicyAggregate.class, CODEC_ID);
    }

    @Override
    public PolicyAggregate decodeFromJson(ReadOnlyAstObject json) {
        QueryResult scheduledItemsQueryResult = SerialCodecManager.decodeFromJson(json.getObject(SCHEDULED_ITEMS_QUERY_RESULT_KEY));
        QueryResult ratesQueryResult = SerialCodecManager.decodeFromJson(json.getObject(RATES_QUERY_RESULT_KEY));
        return new PolicyAggregate(
                json.getString(SCHEDULED_ITEMS_QUERY_BASE_KEY),
                scheduledItemsQueryResult,
                json.getString(RATES_QUERY_BASE_KEY),
                ratesQueryResult);
    }

    @Override
    public void encodeToJson(PolicyAggregate pa, AstObject json) {
        encodeKey(SCHEDULED_ITEMS_QUERY_BASE_KEY, pa.getScheduledItemsQueryBase(), json);
        encodeKey(SCHEDULED_ITEMS_QUERY_RESULT_KEY, pa.getScheduledItemsQueryResult(), json);
        encodeKey(RATES_QUERY_BASE_KEY, pa.getRatesQueryBase(), json);
        encodeKey(RATES_QUERY_RESULT_KEY, pa.getRatesQueryResult(), json);
    }
}
