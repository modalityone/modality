package one.modality.ecommerce.document.service;

import dev.webfx.stack.db.query.QueryResult;
import dev.webfx.stack.orm.datasourcemodel.service.DataSourceModelService;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.sqlcompiler.mapping.QueryRowToEntityMapping;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.query_result_to_entities.QueryResultToEntitiesMapper;
import one.modality.base.shared.entities.Item;
import one.modality.base.shared.entities.Rate;
import one.modality.base.shared.entities.ScheduledItem;
import one.modality.base.shared.entities.Site;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Bruno Salmon
 */
public final class PolicyAggregate {

    // Fields intended for serialisation
    private final String scheduledItemsQueryBase;
    private final QueryResult scheduledItemsQueryResult;
    private final String ratesQueryBase;
    private final QueryResult ratesQueryResult;

    // Fields intended for application code
    private EntityStore entityStore;
    private EntityList<ScheduledItem> scheduledItems;
    private EntityList<Rate> rates;

    public PolicyAggregate(String scheduledItemsQueryBase, QueryResult scheduledItemsQueryResult, String ratesQueryBase, QueryResult ratesQueryResult) {
        this.scheduledItemsQueryBase = scheduledItemsQueryBase;
        this.scheduledItemsQueryResult = scheduledItemsQueryResult;
        this.ratesQueryBase = ratesQueryBase;
        this.ratesQueryResult = ratesQueryResult;
    }

    private void ensureStoreCreated() {
        if (entityStore == null) {
            DataSourceModel dataSourceModel = DataSourceModelService.getDefaultDataSourceModel();
            entityStore = EntityStore.create(dataSourceModel);
            QueryRowToEntityMapping queryMapping = dataSourceModel.parseAndCompileSelect(scheduledItemsQueryBase).getQueryMapping();
            scheduledItems = QueryResultToEntitiesMapper.mapQueryResultToEntities(scheduledItemsQueryResult, queryMapping, entityStore, "scheduledItems");
            queryMapping = dataSourceModel.parseAndCompileSelect(ratesQueryBase).getQueryMapping();
            rates = QueryResultToEntitiesMapper.mapQueryResultToEntities(ratesQueryResult, queryMapping, entityStore, "rates");
        }
    }

    public List<Rate> getRates() {
        ensureStoreCreated();
        return rates;
    }

    public List<Rate> getSiteItemRates(Site site, Item item) {
        ensureStoreCreated();
        return rates.stream()
                .filter(r -> Entities.sameId(r.getSite(), site) && Entities.sameId(r.getItem(), item))
                .collect(Collectors.toList());
    }

    public List<ScheduledItem> getScheduledItems() {
        ensureStoreCreated();
        return scheduledItems;
    }

    public String getRatesQueryBase() {
        return ratesQueryBase;
    }

    public QueryResult getRatesQueryResult() {
        return ratesQueryResult;
    }

    public String getScheduledItemsQueryBase() {
        return scheduledItemsQueryBase;
    }

    public QueryResult getScheduledItemsQueryResult() {
        return scheduledItemsQueryResult;
    }

}
