package one.modality.ecommerce.document.service;

import dev.webfx.platform.console.Console;
import dev.webfx.platform.util.collection.Collections;
import dev.webfx.stack.db.query.QueryResult;
import dev.webfx.stack.orm.domainmodel.DataSourceModel;
import dev.webfx.stack.orm.dql.sqlcompiler.mapping.QueryRowToEntityMapping;
import dev.webfx.stack.orm.entity.Entities;
import dev.webfx.stack.orm.entity.EntityList;
import dev.webfx.stack.orm.entity.EntityStore;
import dev.webfx.stack.orm.entity.query_result_to_entities.QueryResultToEntitiesMapper;
import one.modality.base.shared.entities.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private Event event;
    private EntityStore entityStore;
    private EntityList<ScheduledItem> scheduledItems;
    private EntityList<Rate> rates;

    public PolicyAggregate(String scheduledItemsQueryBase, QueryResult scheduledItemsQueryResult, String ratesQueryBase, QueryResult ratesQueryResult) {
        this.scheduledItemsQueryBase = scheduledItemsQueryBase;
        this.scheduledItemsQueryResult = scheduledItemsQueryResult;
        this.ratesQueryBase = ratesQueryBase;
        this.ratesQueryResult = ratesQueryResult;
    }

    public void rebuildEntities(Event event) {
        this.event = event;
        this.entityStore = EntityStore.createAbove(event.getStore());
        DataSourceModel dataSourceModel = entityStore.getDataSourceModel();
        QueryRowToEntityMapping queryMapping = dataSourceModel.parseAndCompileSelect(scheduledItemsQueryBase).getQueryMapping();
        scheduledItems = QueryResultToEntitiesMapper.mapQueryResultToEntities(scheduledItemsQueryResult, queryMapping, entityStore, "scheduledItems");
        queryMapping = dataSourceModel.parseAndCompileSelect(ratesQueryBase).getQueryMapping();
        rates = QueryResultToEntitiesMapper.mapQueryResultToEntities(ratesQueryResult, queryMapping, entityStore, "rates");
    }

    public EntityStore getEntityStore() {
        return entityStore;
    }

    public Event getEvent() {
        return event;
    }

    public List<ScheduledItem> getScheduledItems() {
        return scheduledItems;
    }

    public Stream<ScheduledItem> getScheduledItemsStream() {
        return getScheduledItems().stream();
    }

    public Stream<ScheduledItem> getFamilyScheduledItemsStream(ItemFamily family) {
        return getFamilyScheduledItemsStream(family.getCode());
    }

    public Stream<ScheduledItem> getFamilyScheduledItemsStream(String familyCode) {
        return getScheduledItemsStream()
            .filter(scheduledItem -> Objects.equals(familyCode, scheduledItem.getItem().getFamily().getCode()));
    }

    public Stream<ScheduledItem> getTeachingScheduledItemsStream() {
        return getFamilyScheduledItemsStream(ItemFamilyType.ACCOMMODATION.getCode());
    }

    public List<ScheduledItem> getTeachingScheduledItems() {
        return getTeachingScheduledItemsStream().collect(Collectors.toList());
    }

    public List<Rate> getRates() {
        return rates;
    }

    public Stream<Rate> getRatesStream() {
        return getRates().stream();
    }

    public Stream<Rate> getDailyRatesStream() {
        return getRatesStream()
            .filter(Rate::isPerDay);
    }

    public List<Rate> getDailyRates() {
        return getDailyRatesStream().collect(Collectors.toList());
    }

    public Rate getDailyRate() {
        List<Rate> dailyRates = getDailyRates();
        int dailyRatesCount = dailyRates.size();
        if (dailyRatesCount > 1) {
            Console.log("⚠️ WARNING: PolicyAggregate.getDailyRate() is meant to be used with single daily rate policies, but this policy has " + dailyRatesCount + " rates.");
        }
        return Collections.first(dailyRates);
    }

    public int getDailyRatePrice() {
        Rate rate = getDailyRate();
        return rate != null ? rate.getPrice() : 0;
    }

    public Stream<Rate> getSiteItemRatesStream(Site site, Item item) {
        return getRatesStream()
            .filter(r -> Entities.sameId(r.getSite(), site) && Entities.sameId(r.getItem(), item));
    }

    public Stream<Rate> getSiteItemDailyRatesStream(Site site, Item item) {
        return getSiteItemRatesStream(site, item)
            .filter(Rate::isPerDay);
    }

    public Stream<Rate> getSiteItemFixedRatesStream(Site site, Item item) {
        return getSiteItemRatesStream(site, item)
            .filter(r -> !r.isPerDay());
    }

    public List<Rate> getSiteItemRates(Site site, Item item) {
        return getSiteItemRatesStream(site, item)
                .collect(Collectors.toList());
    }

    // The following methods are meant to be used for serialization, not for application code

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
